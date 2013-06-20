package org.python.core;

import static com.oracle.max.asm.target.amd64.AMD64.*;
import static com.sun.max.platform.Platform.target;
import com.oracle.max.asm.target.amd64.AMD64;
import com.oracle.max.asm.target.amd64.AMD64MacroAssembler;
import com.oracle.max.criutils.HexCodeFile;
import com.oracle.max.vm.ext.t1x.PrologueTargetMethod;
import com.sun.cri.ci.CiRegisterConfig;
import com.sun.max.platform.Platform;
import com.sun.max.unsafe.CodePointer;
import com.sun.max.unsafe.Word;
import com.sun.max.vm.MaxineVM;
import com.sun.max.vm.compiler.target.HexCodeFileTool;
import static com.sun.max.vm.compiler.target.SubroutineThreadedCode.SubroutineCall;
import com.sun.max.vm.compiler.target.TargetMethod;
import com.sun.max.vm.stack.JVMSFrameLayout;
import com.sun.max.vm.stack.amd64.AMD64JVMSFrameLayout;

public class PyDirectThreadedFrameInitializer {

    AMD64MacroAssembler asm;
    /**
     * Frame info for the method being compiled.
     */
    protected JVMSFrameLayout frame;

    public PyDirectThreadedFrameInitializer() {
        asm = new AMD64MacroAssembler(target(), MaxineVM.vm().registerConfigs.standard);
        int maxLocals = 0; // PyCode locals are not mapped to native machine stack
        int maxStack = 0; // PyStack is not mapped to native machine stack
        int maxParams = 0; // for some reason man
        maxLocals += 0; // the reference of ActivationRecord (this pointer) and direct threading table reference are stored on the stack as a local variable
        frame = new AMD64JVMSFrameLayout(maxLocals, maxStack, maxParams, IOPsCompilation.iOpSlots());
    }

    public TargetMethod generatePrologue() {
        PrologueTargetMethod prologue = new PrologueTargetMethod("prologue", frame);
        genPrologue();

        TargetMethod isReenteringTemplate;
        isReenteringTemplate = IOPsCompilation.instance().getReenteringImplementation();
        CodePointer calleeEntryPoint = isReenteringTemplate.codeStart();
        prologue.addSubroutineCall(new SubroutineCall(asm.codeBuffer.position(), calleeEntryPoint));
        asm.call();

        // Just add an instruction to get around assertion error(pos < codeLength) in targetMethod
        asm.nop();
        int endPos = asm.codeBuffer.position();
        asm.codeBuffer.setPosition(endPos);
        byte[] code = asm.codeBuffer.close(true);
        prologue.setCode(code);
        prologue.linkDirectSubroutineCalls();

        if (Options.printThreadedCode) {
            printThreadedCode(prologue, prologue.codeStart());
        }

        return prologue;
    }

    private void genPrologue() {
        int frameSize = frame.frameSize();
        /*
         *  padding is needed to make i-ops' stack frame properly aligned including
         *  the return address pushed by the threaded code.
         */
        int padding = Word.size();
        asm.enter(frameSize - Word.size() + padding, 0);
        asm.subq(rbp, framePointerAdjustment());
        asm.movl(AMD64.rdx, 0); // initialize direct table index = 0
    }

    /*
     * duplicated from AMD64T1XCompilation
     */
    private int framePointerAdjustment() {
        final int enterSize = frame.frameSize() - Word.size();
        return enterSize - frame.sizeOfNonParameterLocals();
    }

    private void printThreadedCode(TargetMethod targetMethod, CodePointer startAddress) {
        System.out.println("TARGET MEHTOD " + targetMethod);
        final Platform platform = Platform.platform();
        HexCodeFile hcf = new HexCodeFile(targetMethod.code(), startAddress.toLong(), platform.isa.name(),
                        platform.wordWidth().numberOfBits);
        String s = HexCodeFileTool.toText(hcf);
        System.out.println(s);
    }
}