package org.python.core;

import com.oracle.max.asm.target.amd64.AMD64MacroAssembler;
import com.oracle.max.asm.target.amd64.AMD64Assembler.ConditionFlag;
import com.oracle.max.vm.ext.t1x.PatchInfo;
import com.sun.max.vm.runtime.FatalError;

public class PyThreadedCodeGeneratorUtil {

    /*
     * PatchInfoAMD64 is taken from AMD64T1XCompilation 
     */
    static class PyPatchInfoAMD64 extends PatchInfo {

        /**
         * Denotes a conditional jump patch. Encoding: {@code cc, pos, targetBCI}.
         */
        static final int JCC = 0;

        /**
         * Denotes an unconditional jump patch. Encoding: {@code pos, targetBCI}.
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

    protected static void fixup(AMD64MacroAssembler asm, PatchInfo patchInfo, int[] bciToPos) {
        int i = 0;
        int[] data = patchInfo.data;
        
        while (i < patchInfo.size) {
            int tag = data[i++];
            if (tag == PyPatchInfoAMD64.JMP) {
                int pos = data[i++];
                int targetBCI = data[i++];
                int target = bciToPos[targetBCI];
                assert target != 0;
                asm.codeBuffer.setPosition(pos);
                asm.jmp(target, true);
            } else if (tag == PyPatchInfoAMD64.JCC) {
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
