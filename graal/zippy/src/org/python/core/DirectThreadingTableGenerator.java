package org.python.core;

import static java.lang.System.out;
import static org.python.core.PyBytecode.getUnsigned;
import java.util.Stack;
import org.python.core.PyThreadedCodeGeneratorUSF.Block;
import com.oracle.max.asm.target.amd64.AMD64Assembler.ConditionFlag;
import com.oracle.max.vm.ext.t1x.PatchInfo;
import com.sun.max.unsafe.Address;
import com.sun.max.vm.compiler.target.TargetMethod;
import com.sun.max.vm.runtime.FatalError;

public class DirectThreadingTableGenerator {
    // static info
    static final IOPsCompilation compiledIOps = IOPsCompilation.instance();

    final PyBytecode bytecode;
    final DirectThreadingTable directThreadingTable;
    public final int[] bciToPos;
    PatchInfoAMD64 patchInfo;

    // dummy
    public DirectThreadingTableGenerator() {
        this.bytecode = null;
        this.directThreadingTable = new DirectThreadingTable();
        this.bciToPos = new int[0];
        this.patchInfo = new PatchInfoAMD64();
    }

    public DirectThreadingTableGenerator(PyBytecode bytecode) {
        this.bytecode = bytecode;
        this.directThreadingTable = new DirectThreadingTable(bytecode);
        this.bciToPos = new int[bytecode.co_code.length];
        this.patchInfo = new PatchInfoAMD64();
    }

    public DirectThreadingTable generateTable() {
        processBytecodeStream();
        fixup(directThreadingTable, patchInfo, bciToPos);
        // debug
        //printDirectThreadedCode();
        directThreadingTable.reset();
        return directThreadingTable;
    }

    void processBytecodeStream() {
        byte[] code = bytecode.co_code;
        int bci = 0;
        assert directThreadingTable.position() == 0;

        while(bci <= code.length - 1) {
            bciToPos[bci] = directThreadingTable.position();

            int opcode = getUnsigned(code, bci);
            if (opcode < Opcode.HAVE_ARGUMENT) {
                bci += 1;
                processPyBytecodeWithoutArgument(opcode, bci);
            } else {
                bci += 2;
                int oparg = (getUnsigned(code, bci) << 8) + getUnsigned(code, bci - 1);
                bci += 1;
                processPyBytecodeWithArgument(opcode, bci, oparg);
            }
        }

        directThreadingTable.close(true);
    }

    void processPyBytecodeWithoutArgument(int opcode, int bci) {
        TargetMethod iOp = compiledIOps.getImplementation(opcode);
        Address address = iOp.codeStart().toAddress();
        directThreadingTable.putAddress(address);
    }

    void processPyBytecodeWithArgument(int opcode, int bci, int oparg) {
        TargetMethod iOp = compiledIOps.getImplementation(opcode);
        Address address = iOp.codeStart().toAddress();
        directThreadingTable.putAddress(address);

        switch (opcode) {
            case Opcode.FOR_ITER: {
                emitConditionalForwardJump(bci + oparg);
                break;
            }
            case Opcode.SETUP_LOOP: {
                emitUnconditionalForwardJump(bci + oparg);
                break;
            }
            case Opcode.JUMP_FORWARD:{
                emitUnconditionalForwardJump(bci + oparg);
                break;
            }
            case Opcode.JUMP_ABSOLUTE: {
                if (bci <= oparg) {
                    emitUnconditionalForwardJump(oparg);
                } else {
                    emitUnconditionalBackwardsJump(oparg);
                }
                break;
            }
            case Opcode.JUMP_IF_FALSE:
            case Opcode.JUMP_IF_TRUE: {
                if (bci <= bci + oparg) {
                    emitConditionalForwardJump(bci + oparg);
                } else {
                    emitConditionalBackwardsJump(bci + oparg);
                }
                break;
            }
            default:
                directThreadingTable.putArgument(oparg);
                break;
        }
    }

    private int emitUnconditionalForwardJump(int targetBCI) {
        int position = patchInfo.addJMP(directThreadingTable.position(), targetBCI);
        directThreadingTable.putArgument(0);
        return position;
    }

    private void emitUnconditionalBackwardsJump(int targetBCI) {
        int target = bciToPos[targetBCI];
        directThreadingTable.putArgument(target);
    }

    private int emitConditionalForwardJump(int targetBCI) {
        int position = patchInfo.addJCC(ConditionFlag.notEqual, directThreadingTable.position(), targetBCI);
        directThreadingTable.putArgument(0);
        return position;
    }

    private void emitConditionalBackwardsJump(int targetBCI) {
        int target = bciToPos[targetBCI];
        directThreadingTable.putArgument(target);
    }

    /*
     * PatchInfoAMD64 is taken from AMD64T1XCompilation
     */
    static class PatchInfoAMD64 extends PatchInfo {

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

    protected static void fixup(DirectThreadingTable directThreadingTable, PatchInfo patchInfo, int[] bciToPos) {
        int i = 0;
        int[] data = patchInfo.data;

        while (i < patchInfo.size) {
            int tag = data[i++];
            if (tag == PatchInfoAMD64.JMP) {
                int pos = data[i++];
                int targetBCI = data[i++];
                int target = bciToPos[targetBCI];
                directThreadingTable.setPosition(pos);
                directThreadingTable.setArgument(target);
            } else if (tag == PatchInfoAMD64.JCC) {
                ConditionFlag cc = ConditionFlag.values[data[i++]];
                int pos = data[i++];
                int targetBCI = data[i++];
                int target = bciToPos[targetBCI];
                assert target != 0;
                directThreadingTable.setPosition(pos);
                directThreadingTable.setArgument(target);
            } else {
                throw FatalError.unexpected(String.valueOf(tag));
            }
        }
    }

    void printDirectThreadedCode() {
        directThreadingTable.reset();
        int index = 0;
        out.println(">>> Direct Threaded Code of " +  bytecode.co_name);

        while (index < directThreadingTable.data.length) {
            Address address = directThreadingTable.getAddress(index);
            int opcode = getOpcodeFromAddress(address);

            if (opcode == -1) {
                out.println(index + " isReentering");
            } else if (opcode == -2) {
                out.println(index + " invalid address");
            } else if (opcode < Opcode.HAVE_ARGUMENT) {
                PyObject opcodeName = PyBytecode.get_opname().__getitem__(Py.newInteger(opcode));
                out.println(index + " " + opcodeName);
            } else {
                PyObject opcodeName = PyBytecode.get_opname().__getitem__(Py.newInteger(opcode));
                out.println(index + " " + opcodeName);
                int oparg = directThreadingTable.getArgument(++index);
                out.println(index + " " + oparg);
            }

            index++;
        }

        out.println();
        directThreadingTable.reset();
    }

    static int getOpcodeFromAddress(Address address) {
        TargetMethod[] iOps = IOPsCompilation.instance().compiledIOPS();
        TargetMethod tm;

        for (int i = 0; i < iOps.length; i++) {
            tm = iOps[i];
            if (tm == null) {
                continue;
            }

            Address candidate = tm.codeStart().toAddress();

            if (candidate.equals(address)) {
                return i;
            }
        }

        // do not find the opcode
        return -2;
    }
}
