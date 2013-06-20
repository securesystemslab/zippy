package org.python.core;

import static com.sun.max.vm.stack.VMFrameLayout.*;
import java.lang.reflect.*;
import org.python.core.PyBytecode.*;
import com.oracle.max.criutils.*;
import com.sun.max.lang.*;
import com.sun.max.platform.*;
import com.sun.max.program.*;
import com.sun.max.unsafe.*;
import com.sun.max.vm.*;
import com.sun.max.vm.actor.member.*;
import com.sun.max.vm.compiler.*;
import com.sun.max.vm.compiler.target.*;
import com.sun.max.vm.stack.*;
import com.sun.max.vm.stack.amd64.*;


public class IOPsCompilation {
    private int totalPyBytecodeImplemantationCodeSize;
    private TargetMethod[] compiledIOPs;
    private TargetMethod reenteringImplementation;

    private IOPsCompilation() {
    }

    static int iOpSlots;

    /**
     * Gets the number of slots to be reserved in each Subroutine threaded code frame for i-op spill slots.
     */
    public static int iOpSlots() {
        return iOpSlots;
    }

    static IOPsCompilation iopsCompilation;

    static {
        iopsCompilation = new IOPsCompilation();
        new PyBytecode(false);
        new PyCell(); new PyStackException(new PyException());
        new PyStack();
        new PyBlock(0, 0, 0);
        new DirectThreadingTable();
        new DirectThreadingTableGenerator();
        new PyDirectThreadedInterpreter().new ActivationRecord();
        new PyDirectThreadedInterpreterOSM().new ActivationRecord();
    }

    public static IOPsCompilation instance() {
        return iopsCompilation;
    }

    public TargetMethod[] compiledIOPS() {
        return compiledIOPs;
    }

    public void compileIOPs(Class<?> activationRecordClass) {
        CompilationBroker cb = MaxineVM.vm().compilationBroker;
        RuntimeCompiler optimizingCompiler = cb.optimizingCompiler;
        compiledIOPs = new TargetMethod[Opcode.LAST + 1];

        try {
            Method method = activationRecordClass.getMethod("isReenteringTemplate", Word[].class, Integer.TYPE);
            ClassMethodActor classMethodActor = ClassMethodActor.fromJava(method);
            classMethodActor.setAsTemplate();
            TargetMethod targetMethod = optimizingCompiler.compile(classMethodActor, false, true, null);
            targetMethod.setAsDirectThreadingWithOSM();
            updateIOpSlotsNumber(targetMethod);
            reenteringImplementation = targetMethod;
        } catch (Throwable e) {
            ProgramError.unexpected(e);
        }

        final Method[] bytecodeMethods = activationRecordClass.getDeclaredMethods();

        for (Method method : bytecodeMethods) {
            PYBYTECODEIMPLEMENTATION anno = method.getAnnotation(PYBYTECODEIMPLEMENTATION.class);

            if (anno != null) {
                try {
                    int opcode = anno.value();
                    ClassMethodActor classMethodActor = ClassMethodActor.fromJava(method);
                    classMethodActor.setAsTemplate();
                    TargetMethod targetMethod = optimizingCompiler.compile(classMethodActor, false, true, null);
                    targetMethod.setAsDirectThreadingWithOSM();
                    updateIOpSlotsNumber(targetMethod);
                    compiledIOPs[opcode] = targetMethod;
                } catch (Throwable e) {
                    ProgramError.unexpected(e);
                }
            }
        }

        fixPrologueAndEpilogues();
    }

    private void fixPrologueAndEpilogues() {
        TargetMethod returnValue = compiledIOPs[Opcode.RETURN_VALUE];
        fixEpilogue(returnValue);
        TargetMethod yieldValue = compiledIOPs[Opcode.YIELD_VALUE];
        fixEpilogue(yieldValue);
    }

    void fixEpilogue(TargetMethod targetMethod) {
        byte[] code = targetMethod.code();
        int index = 0;

        for (int i = code.length - 1; i >= 0; i--) {
            // looking for leave
            if (code[i] == (byte) 0xC9) {
                index = i - 4;
                break;
            }
        }

        assert index != 0 : "did not find 0xC9";
        emitInt(code, framePointerAdjustment(), index);
    }

    static void emitInt(byte[] code, int b, int pos) {
        code[pos++] = (byte) (b & 0xFF);
        code[pos++] = (byte) ((b >> 8) & 0xFF);
        code[pos++] = (byte) ((b >> 16) & 0xFF);
        code[pos++] = (byte) ((b >> 24) & 0xFF);
    }

    /*
     * duplicated from AMD64T1XCompilation
     */
    private int framePointerAdjustment() {
        int maxLocals = 0; // PyCode locals are not mapped to native machine stack
        int maxStack = 0; // PyStack is not mapped to native machine stack
        int maxParams = 0; // for some reason man
        maxLocals += 0; // the reference of ActivationRecord (this pointer) and direct threading table reference are stored on the stack as a local variable
        JVMSFrameLayout frame = new AMD64JVMSFrameLayout(maxLocals, maxStack, maxParams, IOPsCompilation.iOpSlots());
        final int enterSize = frame.frameSize() - Word.size();
        return enterSize - frame.sizeOfNonParameterLocals();
    }

    private void updateIOpSlotsNumber(TargetMethod targetMethod) {
        int frameSlots = Ints.roundUp(targetMethod.frameSize(), STACK_SLOT_SIZE) / STACK_SLOT_SIZE;
        if (frameSlots > iOpSlots) {
            iOpSlots = frameSlots;
        }
    }

    private static void printAssembly(TargetMethod targetMethod, CodePointer startAddress) {
        System.out.println("TARGET METHOD " + targetMethod.name());
        final Platform platform = Platform.platform();
        HexCodeFile hcf = new HexCodeFile(targetMethod.code(), startAddress.toLong(), platform.isa.name(),
        platform.wordWidth().numberOfBits);
        String s = HexCodeFileTool.toText(hcf);
        System.out.println(s);
    }

    public TargetMethod getImplementation(int opcode) {
        return compiledIOPs[opcode];
    }

    public TargetMethod getReenteringImplementation() {
        return reenteringImplementation;
    }

    public void printMetrics() {
        System.out.println("Bytecode Implementations Code Size = " + totalPyBytecodeImplemantationCodeSize / 1024 + " kb");
    }
}