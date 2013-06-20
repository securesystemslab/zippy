package org.python.core;

import java.lang.reflect.Method;

import com.oracle.max.criutils.HexCodeFile;
import com.sun.max.platform.Platform;
import com.sun.max.program.ProgramError;
import com.sun.max.unsafe.CodePointer;
import com.sun.max.vm.MaxineVM;
import com.sun.max.vm.actor.member.ClassMethodActor;
import com.sun.max.vm.compiler.CallEntryPoint;
import com.sun.max.vm.compiler.CompilationBroker;
import com.sun.max.vm.compiler.RuntimeCompiler;
import com.sun.max.vm.compiler.target.HexCodeFileTool;
import com.sun.max.vm.compiler.target.TargetMethod;

public class IOpsCodeTable {
    private int totalPyBytecodeImplemantationCodeSize;
    private TargetMethod[] bytecodeImplementations;
    private TargetMethod reenteringImplementation;

    static IOpsCodeTable IOpsCodeTable;

    static {
        IOpsCodeTable = new IOpsCodeTable();

        /*
         * statically initialize critical runtime classes to allow
         * C1X to inline more methods when compiling templates
         */
        try {
            new PyCell();
            new PyBytecode(false);
            new PyBytecode.PyStack();
            new PyThreadedCodeInterpreter().new ActivationRecord();;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private IOpsCodeTable() {
    }

    public static IOpsCodeTable instance() {
        return IOpsCodeTable;
    }

    public void compilePyBytecodeImplementations() {
        CompilationBroker cb = MaxineVM.vm().compilationBroker;
        RuntimeCompiler optimizingCompiler = cb.optimizingCompiler;
        bytecodeImplementations = new TargetMethod[Opcode.LAST + 1];

        try {
            Method method = PyThreadedCodeInterpreter.ActivationRecord.class.getMethod("isReenteringTemplate", Integer.TYPE, Integer.TYPE);
            ClassMethodActor classMethodActor = ClassMethodActor.fromJava(method);
            TargetMethod targetMethod = optimizingCompiler.compile(classMethodActor, false, true, null);
            targetMethod.setAsSubroutine();
            reenteringImplementation = targetMethod;
        } catch (Throwable e) {
            ProgramError.unexpected(e);
        }

        final Method[] bytecodeMethods = PyThreadedCodeInterpreter.ActivationRecord.class.getDeclaredMethods();

        for (Method method : bytecodeMethods) {
            PYBYTECODEIMPLEMENTATION anno = method.getAnnotation(PYBYTECODEIMPLEMENTATION.class);
            if (anno != null) {
                try {
                    int opcode = anno.value();
                    ClassMethodActor classMethodActor = ClassMethodActor.fromJava(method);
                    TargetMethod targetMethod = optimizingCompiler.compile(classMethodActor, false, true, null);
                    targetMethod.setAsSubroutine();
                    bytecodeImplementations[opcode] = targetMethod;
                    totalPyBytecodeImplemantationCodeSize += targetMethod.codeLength();
                    //printAssembly(targetMethod, targetMethod.getEntryPoint(CallEntryPoint.OPTIMIZED_ENTRY_POINT));
                } catch (Throwable e) {
                    ProgramError.unexpected(e);
                }
            }
        }
    }

    public TargetMethod getImplementation(int opcode) {
        return bytecodeImplementations[opcode];
    }

    public TargetMethod getReenteringImplementation() {
        return reenteringImplementation;
    }

    public void printMetrics() {
        System.out.println("Implementations Code Size = " + totalPyBytecodeImplemantationCodeSize / 1024 + " kb");
    }

    private void printAssembly(TargetMethod targetMethod, CodePointer startAddress) {
        System.out.println("IOP " + targetMethod.name());
        final Platform platform = Platform.platform();
        HexCodeFile hcf = new HexCodeFile(targetMethod.code(), startAddress.toLong(), platform.isa.name(),
        platform.wordWidth().numberOfBits);
        String s = HexCodeFileTool.toText(hcf);
        System.out.println(s);
    }
}
