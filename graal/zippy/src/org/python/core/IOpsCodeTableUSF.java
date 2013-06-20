package org.python.core;

import static com.sun.max.vm.stack.VMFrameLayout.STACK_SLOT_SIZE;

import java.lang.reflect.Method;
import com.oracle.max.criutils.HexCodeFile;
import com.sun.max.lang.Ints;
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
import static org.python.core.PyBytecode.*;

public class IOpsCodeTableUSF {
    private int totalPyBytecodeImplemantationCodeSize;
    private TargetMethod[] compiledImplementations;
    private TargetMethod reenteringImplementation;

    static IOpsCodeTableUSF IOpsCodeTable;

    /**
     * This is the max number of slots used by any i-op and is computed when the i-ops are created.
     */
    static int iOpSlots;

    /**
     * Gets the number of slots to be reserved in each Subroutine threaded code frame for i-op spill slots.
     */
    public static int iOpSlots() {
        return iOpSlots;
    }

    static {
        /*
         * Compile i-ops
         */
    	IOpsCodeTable = new IOpsCodeTableUSF();

        /*
         * statically initialize critical runtime classes to allow
         * C1X to inline more methods when compiling templates
         */
        new PyBytecode.PyStack();
        new PyCell(); new PyStackException(new PyException());
        new PyStack();
        new PyBlock(0, 0, 0);
        new PyThreadedCodeInterpreterUSF().new ActivationRecord();
    }

    private IOpsCodeTableUSF() {
    }

    public static IOpsCodeTableUSF instance() {
    	return IOpsCodeTable;
    }

    public void generateICT() {
        CompilationBroker cb = MaxineVM.vm().compilationBroker;
        RuntimeCompiler optimizingCompiler = cb.optimizingCompiler;
        compiledImplementations = new TargetMethod[Opcode.LAST + 1];

        try {
            Method method = PyThreadedCodeInterpreterUSF.ActivationRecord.class.getMethod("isReenteringImplementation");
            ClassMethodActor classMethodActor = ClassMethodActor.fromJava(method);
            classMethodActor.setAsTemplate();
            TargetMethod targetMethod = optimizingCompiler.compile(classMethodActor, false, true, null);
            targetMethod.setAsSubroutineWithUSF();
            updateIOpSlotsNumber(targetMethod);
            reenteringImplementation = targetMethod;
        } catch (Throwable e) {
            ProgramError.unexpected(e);
        }

        final Method[] bytecodeMethods = PyThreadedCodeInterpreterUSF.ActivationRecord.class.getDeclaredMethods();

        for (Method method : bytecodeMethods) {
        	PYBYTECODEIMPLEMENTATION anno = method.getAnnotation(PYBYTECODEIMPLEMENTATION.class);
            if (anno != null) {
                try {
                    int opcode = anno.value();
                    ClassMethodActor classMethodActor = ClassMethodActor.fromJava(method);
                    classMethodActor.setAsTemplate();
                    TargetMethod targetMethod = optimizingCompiler.compile(classMethodActor, false, true, null);
                    targetMethod.setAsSubroutineWithUSF();
                    updateIOpSlotsNumber(targetMethod);
                    compiledImplementations[opcode] = targetMethod;
                    totalPyBytecodeImplemantationCodeSize += targetMethod.codeLength();
                    //printAssembly(targetMethod, targetMethod.getEntryPoint(CallEntryPoint.BASELINE_ENTRY_POINT));
                } catch (Throwable e) {
                    ProgramError.unexpected(e);
                }
            }
        }
    }

    private void updateIOpSlotsNumber(TargetMethod targetMethod) {
        int frameSlots = Ints.roundUp(targetMethod.frameSize(), STACK_SLOT_SIZE) / STACK_SLOT_SIZE;
        if (frameSlots > iOpSlots) {
            iOpSlots = frameSlots;
        }
    }

    private void printAssembly(TargetMethod targetMethod, CodePointer startAddress) {
        System.out.println("IOP " + targetMethod.name());
        final Platform platform = Platform.platform();
        HexCodeFile hcf = new HexCodeFile(targetMethod.code(), startAddress.toLong(), platform.isa.name(),
        platform.wordWidth().numberOfBits);
        String s = HexCodeFileTool.toText(hcf);
        System.out.println(s);
    }

    public TargetMethod getImplementation(int opcode) {
    	return compiledImplementations[opcode];
    }

    public TargetMethod getReenteringImplementation() {
    	return reenteringImplementation;
    }

    public TargetMethod[] getIOps() {
        return compiledImplementations;
    }

    public void printMetrics() {
        System.out.println("Bytecode Implementations Code Size = " + totalPyBytecodeImplemantationCodeSize / 1024 + " kb");
    }
}
