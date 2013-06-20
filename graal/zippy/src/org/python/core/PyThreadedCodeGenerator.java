package org.python.core;

import static com.sun.max.platform.Platform.target;
import static com.sun.max.vm.compiler.CallEntryPoint.OPTIMIZED_ENTRY_POINT;
import static org.python.core.PyBytecode.*;
import java.util.Stack;
import static com.sun.cri.ci.CiCallingConvention.Type.JavaCall;
import com.oracle.max.asm.target.amd64.AMD64;
import com.oracle.max.asm.target.amd64.AMD64MacroAssembler;
import com.oracle.max.asm.target.amd64.AMD64Assembler.ConditionFlag;
import com.oracle.max.criutils.HexCodeFile;
import com.oracle.max.vm.ext.t1x.PatchInfo;
import com.sun.cri.ci.CiAddress;
import com.sun.cri.ci.CiKind;
import com.sun.cri.ci.CiRegisterConfig;
import com.sun.cri.ci.CiUtil;
import com.sun.cri.ci.CiValue;
import com.sun.max.platform.Platform;
import com.sun.max.unsafe.CodePointer;
import com.sun.max.vm.MaxineVM;
import com.sun.max.vm.actor.member.ClassMethodActor;
import com.sun.max.vm.compiler.target.HexCodeFileTool;
import com.sun.max.vm.compiler.target.TargetMethod;
import com.sun.max.vm.compiler.target.SubroutineThreadedCode;
import com.sun.max.vm.compiler.target.SubroutineThreadedCode.SubroutineCall;
import com.sun.max.vm.runtime.FatalError;

public class PyThreadedCodeGenerator {
    byte[] pyBytecode;
    String functionName;
    IOpsCodeTable iOpsCodeTable;

    AMD64MacroAssembler asm;
    PatchInfoAMD64 patchInfo;
    CiRegisterConfig registerConfig;

    int[] bciToPos;
    Stack<Block> blocks;
    CiValue[] locations;
    CiAddress firstStackSlot;
    int offsetOfReenteringImplementation;
    SubroutineThreadedCode subroutineThreadedCode;
    final int frameSize = 24;

    private static int totalThreadedCodeSize;

    public PyThreadedCodeGenerator(String functionName, byte[] pyBytecode) {
        this.iOpsCodeTable = IOpsCodeTable.instance();
        this.functionName = functionName;
        this.pyBytecode = pyBytecode;

        this.registerConfig = MaxineVM.vm().registerConfigs.standard;
        this.asm = new AMD64MacroAssembler(target(), registerConfig);
        this.patchInfo = new PatchInfoAMD64();

        this.blocks = new Stack<Block>();
        this.bciToPos = new int[pyBytecode.length];
        this.firstStackSlot = new CiAddress(CiKind.Object, AMD64.rsp.asValue(), 8);
    }

    public TargetMethod generateThreadedCode() {
        subroutineThreadedCode = new SubroutineThreadedCode(functionName,frameSize);
        genAdaptorPadding(asm);
        genPrologue();
        processReenteringImplementation();
        processPyBytecode();
        int endPos = asm.codeBuffer.position();
        fixup(asm, patchInfo, bciToPos);
        asm.codeBuffer.setPosition(endPos);
        byte[] code = asm.codeBuffer.close(true);
        subroutineThreadedCode.setCode(code);
        subroutineThreadedCode.linkDirectSubroutineCalls();
        // printThreadedCode(targetMethod.code(), targetMethod);
        totalThreadedCodeSize += subroutineThreadedCode.codeLength();
        return subroutineThreadedCode;
    }

    private void processReenteringImplementation() {
        TargetMethod reenteringImplementation = iOpsCodeTable.getReenteringImplementation();
        ClassMethodActor currentTemplateActor = reenteringImplementation.classMethodActor();
        CiKind[] parameters = CiUtil.signatureToKinds(currentTemplateActor.signature(), currentTemplateActor.holder().kind(true));
        locations = registerConfig.getCallingConvention(JavaCall, parameters, target(), false).locations;
        emitSubroutineCall(reenteringImplementation);
        restoreThisPointer();
        offsetOfReenteringImplementation = emitConditionalForwardJump(0);
    }

    private void processPyBytecode() {
        int next_instr = 0;
        int oparg = 0;

        while (next_instr < pyBytecode.length) {
            bciToPos[next_instr] = asm.codeBuffer.position();
            int opcode = getUnsigned(pyBytecode, next_instr);

            if (opcode < Opcode.HAVE_ARGUMENT) {
                next_instr += 1;
                processPyBytecodeWithoutArgument(opcode, next_instr);
                if (opcode == Opcode.RETURN_VALUE
                        || opcode == Opcode.YIELD_VALUE) {
                    genEpilogue();
                }
            } else {
                next_instr += 2;
                oparg = (getUnsigned(pyBytecode, next_instr) << 8)
                        + getUnsigned(pyBytecode, next_instr - 1);
                next_instr += 1;
                processPyBytecodeWithArgument(opcode, next_instr, oparg);
            }
            /**
             * LOAD_CONST, POP_TOP, DUP_TOP, LOAD_LOCALS instruction
             * implementation methods do not modify this pointer RETURN_VALUE
             * and YIELD_VALUE instructions are the method exit instructions
             * There is no method call happening after these two instructions.
             * For all the instruction implementation methods other than these,
             * this pointer is put to the first register for the next method
             * call
             */
            if (opcode != Opcode.RETURN_VALUE && opcode != Opcode.YIELD_VALUE
                    && opcode != Opcode.LOAD_CONST && opcode != Opcode.POP_TOP
                    && opcode != Opcode.DUP_TOP && opcode != Opcode.LOAD_LOCALS) {
                restoreThisPointer();
            }
        }
    }

    private void processPyBytecodeWithoutArgument(int opcode, int next_instr) {
        int targetBCI;

        if (opcode == Opcode.BREAK_LOOP) {
            TargetMethod callee = iOpsCodeTable
                    .getImplementation(opcode);
            emitSubroutineCall(callee);
            Block block = blocks.pop();
            /**
             * BREAK_LOOP implementation method modifies this pointer This
             * pointer is restored before the jump
             */
            restoreThisPointer();
            targetBCI = block.bytecodeIndexOfBlockExit;
            if (next_instr <= targetBCI) {
                emitUnconditionalForwardJump(targetBCI);
            } else {
                emitUnconditionalBackwardsJump(targetBCI);
            }
        } else if (opcode == Opcode.YIELD_VALUE) {
            targetBCI = next_instr;
            patchInfo.retrieveTargetBCI(offsetOfReenteringImplementation,
                    targetBCI);
            TargetMethod callee = iOpsCodeTable.getImplementation(opcode);
            emitSubroutineCall(callee);
        } else {
            TargetMethod callee = iOpsCodeTable.getImplementation(opcode);
            emitSubroutineCall(callee);
        }
    }

    private void processPyBytecodeWithArgument(int opcode, int next_instr,
            int oparg) {
        TargetMethod callee;
        switch (opcode) {
        case Opcode.JUMP_FORWARD: {
            restoreThisPointer();
            emitUnconditionalForwardJump(next_instr + oparg);
            break;
        }
        case Opcode.JUMP_ABSOLUTE: {
            restoreThisPointer();
            if (next_instr <= oparg) {
                emitUnconditionalForwardJump(oparg);
            } else {
                emitUnconditionalBackwardsJump(oparg);
            }
            break;
        }
        case Opcode.JUMP_IF_FALSE:
        case Opcode.JUMP_IF_TRUE: {
            putFirstArgumentIntoRegister(oparg);
            callee = iOpsCodeTable.getImplementation(opcode);
            emitSubroutineCall(callee);
            restoreThisPointer();
            if (next_instr <= next_instr + oparg) {
                emitConditionalForwardJump(next_instr + oparg);
            } else {
                emitConditionalBackwardsJump(next_instr + oparg);
            }
            break;
        }
        case Opcode.SETUP_LOOP: {
            putFirstArgumentIntoRegister(oparg);
            putSecondArgumentIntoRegister(oparg);
            callee = iOpsCodeTable.getImplementation(opcode);
            emitSubroutineCall(callee);
            Block b = new Block(oparg + next_instr);
            blocks.push(b);
            break;
        }
        case Opcode.FOR_ITER: {
            putFirstArgumentIntoRegister(oparg);
            callee = iOpsCodeTable.getImplementation(opcode);
            emitSubroutineCall(callee);
            restoreThisPointer();
            emitConditionalForwardJump(next_instr + oparg);
            break;
        }
        case Opcode.COMPARE_OP: {
            /*
             * pl: comment out this case to disable compare operation
             * specialization.
             */
            assert oparg >= 0 && oparg <= Opcode.PyCmp_EXC_MATCH;
            callee = iOpsCodeTable
                    .getImplementation(Opcode.COMPARE_LT + oparg);
            emitSubroutineCall(callee);
            break;
        }
        default: {
            putFirstArgumentIntoRegister(oparg);
            callee = iOpsCodeTable.getImplementation(opcode);
            emitSubroutineCall(callee);
        }
        }
    }

    private void genPrologue() {
        // allocate frame
        asm.subq(AMD64.rsp, frameSize);
        // put the this pointer into the first stack slot
        asm.movq(firstStackSlot, AMD64.rdi);
    }

    private void genEpilogue() {
        // deallocate frame
        asm.addq(AMD64.rsp, frameSize);
        asm.ret(0);
    }

    private void genAdaptorPadding(AMD64MacroAssembler asm) {
        // Emit 8 bytes of nop for stackwalker
        for (int i = 0; i < 8; i++) {
            asm.nop();
        }
    }

    private void emitSubroutineCall(TargetMethod callee) {
        CodePointer calleeEntryPoint = callee.getEntryPoint(OPTIMIZED_ENTRY_POINT);
        subroutineThreadedCode.addSubroutineCall(new SubroutineCall(asm.codeBuffer.position(), calleeEntryPoint));
        asm.call();
    }

    private void restoreThisPointer() {
        asm.movq(locations[0].asRegister(), firstStackSlot);
    }

    private void putFirstArgumentIntoRegister(int argument) {
        asm.movl(locations[1].asRegister(), argument);
    }

    private void putSecondArgumentIntoRegister(int argument) {
        asm.movl(locations[2].asRegister(), argument);
    }

    private int emitUnconditionalForwardJump(int targetBCI) {
        int position = patchInfo.addJMP(asm.codeBuffer.position(), targetBCI);
        asm.jmp(0, true);
        return position;
    }

    private void emitUnconditionalBackwardsJump(int targetBCI) {
        int target = bciToPos[targetBCI];
        asm.jmp(target, false);
    }

    private int emitConditionalForwardJump(int targetBCI) {
        asm.cmpq(AMD64.rax, 0);
        int position = patchInfo.addJCC(ConditionFlag.notEqual,
                asm.codeBuffer.position(), targetBCI);
        asm.jcc(ConditionFlag.notEqual, 0, true);
        return position;
    }

    private void emitConditionalBackwardsJump(int targetBCI) {
        int target = bciToPos[targetBCI];
        asm.cmpq(AMD64.rax, 0);
        asm.jcc(ConditionFlag.notEqual, target, false);
    }

    private void printThreadedCode() {
        final Platform platform = Platform.platform();
        long startAddress = this.subroutineThreadedCode.codeStart().toLong();
        HexCodeFile hcf = new HexCodeFile(this.subroutineThreadedCode.code(), startAddress, platform.isa.name(), platform.wordWidth().numberOfBits);
        String s = HexCodeFileTool.toText(hcf);
//        HexCodeFileDis dis = new HexCodeFileDis(false);
//        dis.process(hcf, null);
        System.out.println(s);
    }

    public void printMetrics() {
        System.out.println("Subroutine Threaded Code Size = "+ totalThreadedCodeSize / 1024 + " kb");
    }

    static class Block {
        int bytecodeIndexOfBlockExit;

        public Block(int bytecodeIndexOfBlockExit) {
            this.bytecodeIndexOfBlockExit = bytecodeIndexOfBlockExit;
        }
    }

    /*
     * PatchInfoAMD64 is taken from AMD64T1XCompilation
     */
    static class PatchInfoAMD64 extends PatchInfo {

        /**
         * Denotes a conditional jump patch. Encoding:
         * {@code cc, pos, targetBCI}.
         */
        static final int JCC = 0;

        /**
         * Denotes an unconditional jump patch. Encoding: {@code pos, targetBCI}
         * .
         */
        static final int JMP = 1;

        int addJCC(ConditionFlag cc, int pos, int targetBCI) {
            ensureCapacity(size + 4);
            data[size++] = JCC;
            data[size++] = cc.ordinal();
            data[size++] = pos;
            int position = size;
            data[size++] = targetBCI;
            return position;
        }

        int addJMP(int pos, int targetBCI) {
            ensureCapacity(size + 3);
            data[size++] = JMP;
            data[size++] = pos;
            int position = size;
            data[size++] = targetBCI;
            return position;
        }

        void retrieveTargetBCI(int pos, int targetBCI) {
            data[pos] = targetBCI;
        }
    }

    protected static void fixup(AMD64MacroAssembler asm, PatchInfo patchInfo,
            int[] bciToPos) {
        int i = 0;
        int[] data = patchInfo.data;

        while (i < patchInfo.size) {
            int tag = data[i++];
            if (tag == PatchInfoAMD64.JMP) {
                int pos = data[i++];
                int targetBCI = data[i++];
                int target = bciToPos[targetBCI];
                assert target != 0;
                asm.codeBuffer.setPosition(pos);
                asm.jmp(target, true);
            } else if (tag == PatchInfoAMD64.JCC) {
                ConditionFlag cc = ConditionFlag.values[data[i++]];
                int pos = data[i++];
                int targetBCI = data[i++];
                int target = bciToPos[targetBCI];
                assert target != 0;
                asm.codeBuffer.setPosition(pos);
                asm.jcc(cc, target, true);
            } else {
                throw FatalError.unexpected(String.valueOf(tag));
            }
        }
    }
}
