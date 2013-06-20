package org.python.core;

import com.sun.c1x.*;
import com.sun.max.unsafe.*;
import com.sun.max.vm.Intrinsics;
import com.sun.max.vm.compiler.target.TargetMethod;
import com.sun.max.vm.object.ArrayAccess;
import static org.python.core.maxine.MaxineUtils.*;

public class PyDirectThreadedInterpreterOSM extends PyBytecode {

    static {
        C1XOptions.ForcedInlinees += "call_function,rot2,rot3,rot4,dup,popN,slice,top,unpack_iterable,deleteSlice,storeSlice,__iternext__,_mul";
        IOPsCompilation.instance().compileIOPs(PyDirectThreadedInterpreterOSM.ActivationRecord.class);
    }

    DirectThreadingTable directThreadingTable;
    static TargetMethod prologue;

    PyDirectThreadedInterpreterOSM() {
        super(false);
        directThreadingTable = null;
    }

    public PyDirectThreadedInterpreterOSM(PyBytecode pyBytecode) {
        super(pyBytecode, false);
    }

    @Override
    public PyObject interpret(PyFrame frame, ThreadState ts) {
        if (prologue == null) {
            long start = Options.startTimer(Options.timeThreadGen);
            PyDirectThreadedFrameInitializer frameInitializer = new PyDirectThreadedFrameInitializer();
            prologue = frameInitializer.generatePrologue();
            Options.stopTimer(Options.timeThreadGen, start, Options.TIMER.THREADGEN);
        }

        if (directThreadingTable == null) {
            long start = Options.startTimer(Options.timeThreadGen);
            directThreadingTable = new DirectThreadingTableGenerator(this).generateTable();
            Options.stopTimer(Options.timeThreadGen, start, Options.TIMER.THREADGEN);
        }

        return new ActivationRecord(frame, new PyStack(co_stacksize), directThreadingTable).execute();
    }

    public class ActivationRecord {
        final PyFrame f;
        PyObject returnValue = null;
        Word[] table;

        // dummy constructor
        ActivationRecord() {
            this.f = null;
            this.table = null;
        }

        ActivationRecord(PyFrame frame, PyStack stack, DirectThreadingTable directThreadingTable) {
            this.f = frame;
            this.table = directThreadingTable.data;
        }

        PyObject execute() {
            Intrinsics.indirectCallWithMultipleArgs(prologue.codeStart().toAddress(), this, table, null);
            return returnValue;
        }

        public void isReenteringTemplate(Word[] table, int position) {
            position = f.f_lasti;

            if (f.f_savedlocals != null) {
                for (int i = 0; i < f.f_savedlocals.length; i++) {
                    PyObject v = (PyObject) (f.f_savedlocals[i]);
                    push(v);
                }

                f.f_savedlocals = null;

                Object generatorInput = f.getGeneratorInput();
                if (generatorInput instanceof PyException) {
                    throw (PyException) generatorInput;
                }
                push((PyObject) generatorInput);
            }

            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.NOP)
        public void nopTemplate(Word[] table, int position) {
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.LOAD_FAST)
        public void loadFastTemplate(Word[] table, int position) {
            int oparg = ArrayAccess.getWord(table, position++).asOffset().toInt();
            //print_debug(position, stackTop(), stackSize(), Opcode.LOAD_FAST, oparg);
            push(f.getlocal(oparg));
            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.LOAD_CONST)
        public void loadConstTemplate(Word[] table, int position) {
            int oparg = ArrayAccess.getWord(table, position++).asOffset().toInt();
            //print_debug(position, stackTop(), stackSize(), Opcode.LOAD_CONST, oparg);
            push(co_consts[oparg]);
            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.STORE_FAST)
        public void storeFastTemplate(Word[] table, int position) {
            int oparg = ArrayAccess.getWord(table, position++).asOffset().toInt();
            //print_debug(position, stackTop(), stackSize(), Opcode.STORE_FAST, oparg);
            f.setlocal(oparg, pop());
            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.POP_TOP)
        public void popTopTemplate(Word[] table, int position) {
            //print_debug(position, stackTop(), stackSize(), Opcode.POP_TOP, 0);
            pop();
            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.ROT_TWO)
        public void rotTwoTemplate(Word[] table, int position) {
            //print_debug(position, stackTop(), stackSize(), Opcode.ROT_TWO, 0);
            rot2();
            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.ROT_THREE)
        public void rotThreeTemplate(Word[] table, int position) {
            //print_debug(position, stackTop(), stackSize(), Opcode.ROT_THREE, 0);
            rot3();
            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.ROT_FOUR)
        public void rotFourTemplate(Word[] table, int position) {
            //print_debug(position, stackTop(), stackSize(), Opcode.ROT_FOUR, 0);
            rot4();
            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.DUP_TOP)
        public void dupTopTemplate(Word[] table, int position) {
            //print_debug(position, stackTop(), stackSize(), Opcode.DUP_TOP, 0);
            dup();
            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        /**
         * this opcode is mostlikely not being used by
         * the language shootout game benchmarks suite.
         */
        @PYBYTECODEIMPLEMENTATION(Opcode.DUP_TOPX)
        public void dupTopXTemplate(Word[] table, int position) {
            int oparg = ArrayAccess.getWord(table, position++).asOffset().toInt();
            //print_debug(position, stackTop(), stackSize(), Opcode.DUP_TOPX, oparg);
            if (oparg == 2 || oparg == 3) {
                dup(oparg);
            } else {
                throw Py.RuntimeError("invalid argument to DUP_TOPX" + " (bytecode corruption?)");
            }

            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.UNARY_POSITIVE)
        public void unaryPositiveTemplate(Word[] table, int position) {
            //print_debug(position, stackTop(), stackSize(), Opcode.UNARY_POSITIVE, 0);
            push(pop().__pos__());
            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.UNARY_NEGATIVE)
        public void unaryNegativeTemplate(Word[] table, int position) {
            //print_debug(position, stackTop(), stackSize(), Opcode.UNARY_NEGATIVE, 0);
            push(pop().__neg__());
            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.UNARY_NOT)
        public void unaryNotTemplate(Word[] table, int position) {
            //print_debug(position, stackTop(), stackSize(), Opcode.UNARY_NOT, 0);
            push(pop().__not__());
            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.UNARY_CONVERT)
        public void unaryConvertTemplate(Word[] table, int position) {
            //print_debug(position, stackTop(), stackSize(), Opcode.UNARY_CONVERT, 0);
            push(pop().__repr__());
            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.UNARY_INVERT)
        public void unaryInvertTemplate(Word[] table, int position) {
            //print_debug(position, stackTop(), stackSize(), Opcode.UNARY_INVERT, 0);
            push(pop().__invert__());
            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.BINARY_POWER)
        public void binaryPowerTemplate(Word[] table, int position) {
            //print_debug(position, stackTop(), stackSize(), Opcode.BINARY_POWER, 0);
            PyObject b = pop();
            PyObject a = pop();
            push(a._pow(b));
            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.BINARY_MULTIPLY)
        public void binaryMultiplyTemplate(Word[] table, int position) {
            //print_debug(position, stackTop(), stackSize(), Opcode.BINARY_MULTIPLY, 0);
            PyObject b = pop();
            PyObject a = pop();
            push(a._mul(b));
            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.BINARY_DIVIDE)
        public void binaryDivideTemplate(Word[] table, int position) {
            //print_debug(position, stackTop(), stackSize(), Opcode.BINARY_DIVIDE, 0);
            PyObject b = pop();
            PyObject a = pop();

            if (!co_flags.isFlagSet(CodeFlag.CO_FUTURE_DIVISION)) {
                push(a._div(b));
            } else {
                push(a._truediv(b));
            }

            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.BINARY_TRUE_DIVIDE)
        public void binaryTrueDivideTemplate(Word[] table, int position) {
            //print_debug(position, stackTop(), stackSize(), Opcode.BINARY_TRUE_DIVIDE, 0);
            PyObject b = pop();
            PyObject a = pop();
            push(a._truediv(b));
            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.BINARY_FLOOR_DIVIDE)
        public void binaryFloorDivideTemplate(Word[] table, int position) {
            //print_debug(position, stackTop(), stackSize(), Opcode.BINARY_FLOOR_DIVIDE, 0);
            PyObject b = pop();
            PyObject a = pop();
            push(a._floordiv(b));
            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.BINARY_MODULO)
        public void binaryModuloTemplate(Word[] table, int position) {
            //print_debug(position, stackTop(), stackSize(), Opcode.BINARY_MODULO, 0);
            PyObject b = pop();
            PyObject a = pop();
            push(a._mod(b));
            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.BINARY_ADD)
        public void binaryAddTemplate(Word[] table, int position) {
            //print_debug(position, stackTop(), stackSize(), Opcode.BINARY_ADD, 0);
            PyObject b = pop();
            PyObject a = pop();
            push(a._add(b));
            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.BINARY_SUBTRACT)
        public void binarySubtractTemplate(Word[] table, int position) {
            //print_debug(position, stackTop(), stackSize(), Opcode.BINARY_SUBTRACT, 0);
            PyObject b = pop();
            PyObject a = pop();
            push(a._sub(b));
            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.BINARY_SUBSCR)
        public void binarySubscrTemplate(Word[] table, int position) {
            //print_debug(position, stackTop(), stackSize(), Opcode.BINARY_SUBSCR, 0);
            PyObject b = pop();
            PyObject a = pop();
            push(a.__getitem__(b));
            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.BINARY_LSHIFT)
        public void binaryLshiftTemplate(Word[] table, int position) {
            //print_debug(position, stackTop(), stackSize(), Opcode.BINARY_LSHIFT, 0);
            PyObject b = pop();
            PyObject a = pop();
            push(a._lshift(b));
            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.BINARY_RSHIFT)
        public void binaryRshiftTemplate(Word[] table, int position) {
            //print_debug(position, stackTop(), stackSize(), Opcode.BINARY_RSHIFT, 0);
            PyObject b = pop();
            PyObject a = pop();
            push(a._rshift(b));
            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.BINARY_AND)
        public void binaryAndTemplate(Word[] table, int position) {
            //print_debug(position, stackTop(), stackSize(), Opcode.BINARY_ADD, 0);
            PyObject b = pop();
            PyObject a = pop();
            push(a._and(b));
            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.BINARY_XOR)
        public void binaryXorTemplate(Word[] table, int position) {
            //print_debug(position, stackTop(), stackSize(), Opcode.BINARY_XOR, 0);
            PyObject b = pop();
            PyObject a = pop();
            push(a._xor(b));
            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.BINARY_OR)
        public void binaryOrTemplate(Word[] table, int position) {
            //print_debug(position, stackTop(), stackSize(), Opcode.BINARY_OR, 0);
            PyObject b = pop();
            PyObject a = pop();
            push(a._or(b));
            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.LIST_APPEND)
        public void listAppendTemplate(Word[] table, int position) {
            //print_debug(position, stackTop(), stackSize(), Opcode.LIST_APPEND, 0);
            PyObject b = pop();
            PyList a = (PyList) (pop());
            a.append(b);
            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.INPLACE_POWER)
        public void listInPlacePowerTemplate(Word[] table, int position) {
            //print_debug(position, stackTop(), stackSize(), Opcode.INPLACE_POWER, 0);
            PyObject b = pop();
            PyObject a = pop();
            push(a._ipow(b));
            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.INPLACE_MULTIPLY)
        public void listInPlaceMultiplyTemplate(Word[] table, int position) {
            //print_debug(position, stackTop(), stackSize(), Opcode.INPLACE_MULTIPLY, 0);
            PyObject b = pop();
            PyObject a = pop();
            push(a._imul(b));
            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.INPLACE_DIVIDE)
        public void listInPlaceDivideTemplate(Word[] table, int position) {
            //print_debug(position, stackTop(), stackSize(), Opcode.INPLACE_DIVIDE, 0);
            PyObject b = pop();
            PyObject a = pop();
            if (!co_flags.isFlagSet(CodeFlag.CO_FUTURE_DIVISION)) {
                push(a._idiv(b));
            } else {
                push(a._itruediv(b));
            }

            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.INPLACE_TRUE_DIVIDE)
        public void listInPlaceTrueDivideTemplate(Word[] table, int position) {
            //print_debug(position, stackTop(), stackSize(), Opcode.INPLACE_TRUE_DIVIDE, 0);
            PyObject b = pop();
            PyObject a = pop();
            push(a._itruediv(b));
            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.INPLACE_FLOOR_DIVIDE)
        public void listInPlaceFloorDividePowerTemplate(Word[] table, int position) {
            //print_debug(position, stackTop(), stackSize(), Opcode.INPLACE_FLOOR_DIVIDE, 0);
            PyObject b = pop();
            PyObject a = pop();
            push(a._ifloordiv(b));
            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.INPLACE_MODULO)
        public void listInPlaceModuloTemplate(Word[] table, int position) {
            //print_debug(position, stackTop(), stackSize(), Opcode.INPLACE_MODULO, 0);
            PyObject b = pop();
            PyObject a = pop();
            push(a._imod(b));
            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.INPLACE_ADD)
        public void listInPlaceAddTemplate(Word[] table, int position) {
            //print_debug(position, stackTop(), stackSize(), Opcode.INPLACE_ADD, 0);
            PyObject b = pop();
            PyObject a = pop();
            push(a._iadd(b));
            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.INPLACE_SUBTRACT)
        public void listInPlaceSubtractTemplate(Word[] table, int position) {
            //print_debug(position, stackTop(), stackSize(), Opcode.INPLACE_SUBTRACT, 0);
            PyObject b = pop();
            PyObject a = pop();
            push(a._isub(b));
            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.INPLACE_LSHIFT)
        public void listInPlaceLshiftTemplate(Word[] table, int position) {
            //print_debug(position, stackTop(), stackSize(), Opcode.INPLACE_LSHIFT, 0);
            PyObject b = pop();
            PyObject a = pop();
            push(a._ilshift(b));
            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.INPLACE_RSHIFT)
        public void listInPlaceRshiftTemplate(Word[] table, int position) {
            //print_debug(position, stackTop(), stackSize(), Opcode.INPLACE_RSHIFT, 0);
            PyObject b = pop();
            PyObject a = pop();
            push(a._irshift(b));
            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.INPLACE_AND)
        public void listInPlaceAndTemplate(Word[] table, int position) {
            //print_debug(position, stackTop(), stackSize(), Opcode.INPLACE_AND, 0);
            PyObject b = pop();
            PyObject a = pop();
            push(a._iand(b));
            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.INPLACE_XOR)
        public void listInPlaceXorTemplate(Word[] table, int position) {
            //print_debug(position, stackTop(), stackSize(), Opcode.INPLACE_XOR, 0);
            PyObject b = pop();
            PyObject a = pop();
            push(a._ixor(b));
            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.INPLACE_OR)
        public void listInPlaceOrTemplate(Word[] table, int position) {
            //print_debug(position, stackTop(), stackSize(), Opcode.INPLACE_OR, 0);
            PyObject b = pop();
            PyObject a = pop();
            push(a._ior(b));
            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.SLICE)
        public void sliceTemplate(Word[] table, int position) {
            //print_debug(position, stackTop(), stackSize(), Opcode.SLICE, 0);
//            slice(Opcode.SLICE);
            int opcode = Opcode.SLICE;
            PyObject stop = (((opcode - Opcode.SLICE) & 2) != 0) ? pop() : null;
            PyObject start = (((opcode - Opcode.SLICE) & 1) != 0) ? pop() : null;
            PyObject obj = pop();
            push(obj.__getslice__(start, stop));
            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.SLICE + 1)
        public void slice1Template(Word[] table, int position) {
            //print_debug(position, stackTop(), stackSize(), Opcode.SLICE+1, 0);
//            slice(Opcode.SLICE + 1);
            int opcode = Opcode.SLICE + 1;
            PyObject stop = (((opcode - Opcode.SLICE) & 2) != 0) ? pop() : null;
            PyObject start = (((opcode - Opcode.SLICE) & 1) != 0) ? pop() : null;
            PyObject obj = pop();
            push(obj.__getslice__(start, stop));
            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.SLICE + 2)
        public void slice2Template(Word[] table, int position) {
            //print_debug(position, stackTop(), stackSize(), Opcode.SLICE+2, 0);
//            slice(Opcode.SLICE + 2);
            int opcode = Opcode.SLICE + 2;
            PyObject stop = (((opcode - Opcode.SLICE) & 2) != 0) ? pop() : null;
            PyObject start = (((opcode - Opcode.SLICE) & 1) != 0) ? pop() : null;
            PyObject obj = pop();
            push(obj.__getslice__(start, stop));
            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.SLICE + 3)
        public void slice3Template(Word[] table, int position) {
            //print_debug(position, stackTop(), stackSize(), Opcode.SLICE+3, 0);
//            slice(Opcode.SLICE + 3);
            int opcode = Opcode.SLICE + 3;
            PyObject stop = (((opcode - Opcode.SLICE) & 2) != 0) ? pop() : null;
            PyObject start = (((opcode - Opcode.SLICE) & 1) != 0) ? pop() : null;
            PyObject obj = pop();
            push(obj.__getslice__(start, stop));
            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

//        private void slice(int opcode) {
//            PyObject stop = (((opcode - Opcode.SLICE) & 2) != 0) ? pop() : null;
//            PyObject start = (((opcode - Opcode.SLICE) & 1) != 0) ? pop() : null;
//            PyObject obj = pop();
//            push(obj.__getslice__(start, stop));
//        }

        @PYBYTECODEIMPLEMENTATION(Opcode.STORE_SLICE)
        public void storeSliceTemplate(Word[] table, int position) {
            //print_debug(position, stackTop(), stackSize(), Opcode.STORE_SLICE, 0);
//            storeSlice(Opcode.STORE_SLICE);
            int opcode = Opcode.STORE_SLICE;
            PyObject stop = (((opcode - Opcode.STORE_SLICE) & 2) != 0) ? pop() : null;
            PyObject start = (((opcode - Opcode.STORE_SLICE) & 1) != 0) ? pop() : null;
            PyObject obj = pop();
            PyObject value = pop();
            obj.__setslice__(start, stop, value);
            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.STORE_SLICE + 1)
        public void storeSlice1Template(Word[] table, int position) {
            //print_debug(position, stackTop(), stackSize(), Opcode.STORE_SLICE+1, 0);
//            storeSlice(Opcode.STORE_SLICE + 1);
            int opcode = Opcode.STORE_SLICE + 1;
            PyObject stop = (((opcode - Opcode.STORE_SLICE) & 2) != 0) ? pop() : null;
            PyObject start = (((opcode - Opcode.STORE_SLICE) & 1) != 0) ? pop() : null;
            PyObject obj = pop();
            PyObject value = pop();
            obj.__setslice__(start, stop, value);
            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.STORE_SLICE + 2)
        public void storeSlice2Template(Word[] table, int position) {
            //print_debug(position, stackTop(), stackSize(), Opcode.STORE_SLICE+2, 0);
//            storeSlice(Opcode.STORE_SLICE + 2);
            int opcode = Opcode.STORE_SLICE + 2;
            PyObject stop = (((opcode - Opcode.STORE_SLICE) & 2) != 0) ? pop() : null;
            PyObject start = (((opcode - Opcode.STORE_SLICE) & 1) != 0) ? pop() : null;
            PyObject obj = pop();
            PyObject value = pop();
            obj.__setslice__(start, stop, value);
            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.STORE_SLICE + 3)
        public void storeSlice3Template(Word[] table, int position) {
            //print_debug(position, stackTop(), stackSize(), Opcode.STORE_SLICE+3, 0);
//            storeSlice(Opcode.STORE_SLICE + 3);
            int opcode = Opcode.STORE_SLICE + 3;
            PyObject stop = (((opcode - Opcode.STORE_SLICE) & 2) != 0) ? pop() : null;
            PyObject start = (((opcode - Opcode.STORE_SLICE) & 1) != 0) ? pop() : null;
            PyObject obj = pop();
            PyObject value = pop();
            obj.__setslice__(start, stop, value);
            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

//        private void storeSlice(int opcode) {
//            PyObject stop = (((opcode - Opcode.STORE_SLICE) & 2) != 0) ? pop() : null;
//            PyObject start = (((opcode - Opcode.STORE_SLICE) & 1) != 0) ? pop() : null;
//            PyObject obj = pop();
//            PyObject value = pop();
//            obj.__setslice__(start, stop, value);
//        }

        @PYBYTECODEIMPLEMENTATION(Opcode.DELETE_SLICE)
        public void deleteSliceTemplate(Word[] table, int position) {
            //print_debug(position, stackTop(), stackSize(), Opcode.DELETE_SLICE, 0);
            deleteSlice(Opcode.DELETE_SLICE);
            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.DELETE_SLICE + 1)
        public void deleteSlice1Template(Word[] table, int position) {
            //print_debug(position, stackTop(), stackSize(), Opcode.DELETE_SLICE+1, 0);
            deleteSlice(Opcode.DELETE_SLICE + 1);
            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.DELETE_SLICE + 2)
        public void deleteSlice2Template(Word[] table, int position) {
            //print_debug(position, stackTop(), stackSize(), Opcode.DELETE_SLICE+2, 0);
            deleteSlice(Opcode.DELETE_SLICE + 2);
            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.DELETE_SLICE + 3)
        public void deleteSlice3Template(Word[] table, int position) {
            //print_debug(position, stackTop(), stackSize(), Opcode.DELETE_SLICE+3, 0);
            deleteSlice(Opcode.DELETE_SLICE + 3);
            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        private void deleteSlice(int opcode) {
            PyObject stop = (((opcode - Opcode.DELETE_SLICE) & 2) != 0) ? pop() : null;
            PyObject start = (((opcode - Opcode.DELETE_SLICE) & 1) != 0) ? pop() : null;
            PyObject obj = pop();
            obj.__delslice__(start, stop);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.STORE_SUBSCR)
        public void storeSubscrTemplate(Word[] table, int position) {
            //print_debug(position, stackTop(), stackSize(), Opcode.STORE_SUBSCR, 0);
            PyObject key = pop();
            PyObject obj = pop();
            PyObject value = pop();
            obj.__setitem__(key, value);
            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.DELETE_SUBSCR)
        public void deleteSubscrTemplate(Word[] table, int position) {
            //print_debug(position, stackTop(), stackSize(), Opcode.DELETE_SUBSCR, 0);
            PyObject key = pop();
            PyObject obj = pop();
            obj.__delitem__(key);
            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.PRINT_EXPR)
        public void printExprTemplate(Word[] table, int position) {
            //print_debug(position, stackTop(), stackSize(), Opcode.PRINT_EXPR, 0);
            PySystemState.displayhook(pop());
            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.PRINT_ITEM_TO)
        public void printItemToTemplate(Word[] table, int position) {
            //print_debug(position, stackTop(), stackSize(), Opcode.PRINT_ITEM_TO, 0);
            Py.printComma(pop(), pop());
            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.PRINT_ITEM)
        public void printItemTemplate(Word[] table, int position) {
            //print_debug(position, stackTop(), stackSize(), Opcode.PRINT_ITEM, 0);
            Py.printComma(pop());
            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.PRINT_NEWLINE_TO)
        public void printNewLineToTemplate(Word[] table, int position) {
            //print_debug(position, stackTop(), stackSize(), Opcode.PRINT_NEWLINE_TO, 0);
            Py.printlnv(pop());
            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.PRINT_NEWLINE)
        public void printNewLineTemplate(Word[] table, int position) {
            //print_debug(position, stackTop(), stackSize(), Opcode.PRINT_NEWLINE, 0);
            Py.println();
            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        /*
         * TODO ADD RAISEVARARGS here
         */
        @PYBYTECODEIMPLEMENTATION(Opcode.LOAD_LOCALS)
        public void loadLocalsTemplate(Word[] table, int position) {
            //print_debug(position, stackTop(), stackSize(), Opcode.LOAD_LOCALS, 0);
            push(f.f_locals);
            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.RETURN_VALUE)
        public void returnValueTemplate(Word[] table, int position) {
            //print_debug(position, stackTop(), stackSize(), Opcode.RETURN_VALUE, 0);
            returnValue = pop();
            while (stackSize() > 0) {
                PyObject top = pop();
            }
            if (co_flags.isFlagSet(CodeFlag.CO_GENERATOR) && returnValue == Py.None) {
                f.f_lasti = -1;
            }
            Intrinsics.epilogue();
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.YIELD_VALUE)
        public void yieldValueTemplate(Word[] table, int position) {
            //print_debug(position, stackTop(), stackSize(), Opcode.YIELD_VALUE, 0);
            returnValue = pop();
            f.f_savedlocals = popN(stackSize());
            f.f_lasti = position;
            Intrinsics.epilogue();
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.EXEC_STMT)
        public void execStmtTemplate(Word[] table, int position) {
            //print_debug(position, stackTop(), stackSize(), Opcode.EXEC_STMT, 0);
            PyObject locals = pop();
            PyObject globals = pop();
            PyObject code = pop();
            Py.exec(code, globals == Py.None ? null : globals, locals == Py.None ? null : locals);
            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.POP_BLOCK)
        public void popBlockTemplate(Word[] table, int position) {
            //print_debug(position, stackTop(), stackSize(), Opcode.POP_BLOCK, 0);
            PyBlock b = popBlock(f);
            while (stackSize() > b.b_level) {
                pop();
            }
            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.BUILD_CLASS)
        public void buildClassTemplate(Word[] table, int position) {
            //print_debug(position, stackTop(), stackSize(), Opcode.BUILD_CLASS, 0);
            PyObject methods = pop();
            PyObject bases[] = ((PySequenceList) (pop())).getArray();
            String name = pop().toString();
            push(Py.makeClass(name, bases, methods));
            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.STORE_NAME)
        public void storeNameTemplate(Word[] table, int position) {
            int oparg = ArrayAccess.getWord(table, position++).asOffset().toInt();
            //print_debug(position, stackTop(), stackSize(), Opcode.STORE_NAME, oparg);
            f.setlocal(co_names[oparg], pop());
            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.DELETE_NAME)
        public void deleteNameTemplate(Word[] table, int position) {
            int oparg = ArrayAccess.getWord(table, position++).asOffset().toInt();
            //print_debug(position, stackTop(), stackSize(), Opcode.DELETE_NAME, oparg);
            f.dellocal(co_names[oparg]);
            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.UNPACK_SEQUENCE)
        public void unpackSequenceTemplate(Word[] table, int position) {
            int oparg = ArrayAccess.getWord(table, position++).asOffset().toInt();
            //print_debug(position, stackTop(), stackSize(), Opcode.UNPACK_SEQUENCE, oparg);
//            unpack_iterable(oparg);
            PyObject v = pop();
            int i = oparg;
            PyObject items[] = new PyObject[oparg];
            for (PyObject item : v.asIterable()) {
                if (i <= 0) {
                    throw Py.ValueError("too many values to unpack");
                }
                i--;
                items[i] = item;
            }
            if (i > 0) {
                throw Py.ValueError(String
                        .format("need more than %d value%s to unpack", i,
                                i == 1 ? "" : "s"));
            }
            for (i = 0; i < oparg; i++) {
                push(items[i]);
            }
            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.STORE_ATTR)
        public void storeAttrTemplate(Word[] table, int position) {
            int oparg = ArrayAccess.getWord(table, position++).asOffset().toInt();
            //print_debug(position, stackTop(), stackSize(), Opcode.STORE_ATTR, oparg);
            PyObject obj = pop();
            PyObject v = pop();
            obj.__setattr__(co_names[oparg], v);
            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.DELETE_ATTR)
        public void deleteAttrTemplate(Word[] table, int position) {
            int oparg = ArrayAccess.getWord(table, position++).asOffset().toInt();
            //print_debug(position, stackTop(), stackSize(), Opcode.DELETE_ATTR, oparg);
            pop().__delattr__(co_names[oparg]);
            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.STORE_GLOBAL)
        public void storeGlobalTemplate(Word[] table, int position) {
            int oparg = ArrayAccess.getWord(table, position++).asOffset().toInt();
            //print_debug(position, stackTop(), stackSize(), Opcode.STORE_GLOBAL, oparg);
            f.setglobal(co_names[oparg], pop());
            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.DELETE_GLOBAL)
        public void deleteGlobalTemplate(Word[] table, int position) {
            int oparg = ArrayAccess.getWord(table, position++).asOffset().toInt();
            //print_debug(position, stackTop(), stackSize(), Opcode.DELETE_GLOBAL, oparg);
            f.delglobal(co_names[oparg]);
            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.LOAD_NAME)
        public void loadNameTemplate(Word[] table, int position) {
            int oparg = ArrayAccess.getWord(table, position++).asOffset().toInt();
            //print_debug(position, stackTop(), stackSize(), Opcode.LOAD_NAME, oparg);
            push(f.getname(co_names[oparg]));
            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.LOAD_GLOBAL)
        public void loadGlobalTemplate(Word[] table, int position) {
            int oparg = ArrayAccess.getWord(table, position++).asOffset().toInt();
            //print_debug(position, stackTop(), stackSize(), Opcode.LOAD_GLOBAL, oparg);
            push(f.getglobal(co_names[oparg]));
            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.DELETE_FAST)
        public void deleteFastTemplate(Word[] table, int position) {
            int oparg = ArrayAccess.getWord(table, position++).asOffset().toInt();
            //print_debug(position, stackTop(), stackSize(), Opcode.DELETE_FAST, oparg);
            f.dellocal(oparg);
            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.LOAD_CLOSURE)
        public void loadClosureTemplate(Word[] table, int position) {
            int oparg = ArrayAccess.getWord(table, position++).asOffset().toInt();
            //print_debug(position, stackTop(), stackSize(), Opcode.LOAD_CLOSURE, oparg);
            PyCell cell = (PyCell) (f.getclosure(oparg));
            if (cell.ob_ref == null) {
                String name;
                if (oparg >= co_cellvars.length) {
                    name = co_freevars[oparg - co_cellvars.length];
                } else {
                    name = co_cellvars[oparg];
                }
                // XXX - consider some efficient lookup mechanism, like a hash
                // :),
                // at least if co_varnames is much greater than say a certain
                // size (but i would think, it's not going to happen in real
                // code. profile?)
                if (f.f_fastlocals != null) {
                    int i = 0;
                    boolean matched = false;
                    for (String match : co_varnames) {
                        if (match.equals(name)) {
                            matched = true;
                            break;
                        }
                        i++;
                    }
                    if (matched) {
                        cell.ob_ref = f.f_fastlocals[i];
                    }
                } else {
                    cell.ob_ref = f.f_locals.__finditem__(name);
                }
            }
            push(cell);
            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.LOAD_DEREF)
        public void loadDeRefTemplate(Word[] table, int position) {
            int oparg = ArrayAccess.getWord(table, position++).asOffset().toInt();
            //print_debug(position, stackTop(), stackSize(), Opcode.LOAD_DEREF, oparg);
            // common code from LOAD_CLOSURE
            PyCell cell = (PyCell) (f.getclosure(oparg));
            if (cell.ob_ref == null) {
                String name;
                if (oparg >= co_cellvars.length) {
                    name = co_freevars[oparg - co_cellvars.length];
                } else {
                    name = co_cellvars[oparg];
                }
                // XXX - consider some efficient lookup mechanism, like a hash
                // :),
                // at least if co_varnames is much greater than say a certain
                // size (but i would think, it's not going to happen in real
                // code. profile?)
                if (f.f_fastlocals != null) {
                    int i = 0;
                    boolean matched = false;
                    for (String match : co_varnames) {
                        if (match.equals(name)) {
                            matched = true;
                            break;
                        }
                        i++;
                    }
                    if (matched) {
                        cell.ob_ref = f.f_fastlocals[i];
                    }
                } else {
                    cell.ob_ref = f.f_locals.__finditem__(name);
                }
            }
            push(cell.ob_ref);
            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.STORE_DEREF)
        public void storeDeRefTemplate(Word[] table, int position) {
            int oparg = ArrayAccess.getWord(table, position++).asOffset().toInt();
            //print_debug(position, stackTop(), stackSize(), Opcode.STORE_DEREF, oparg);
            f.setderef(oparg, pop());
            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.BUILD_TUPLE)
        public void buildTupleTemplate(Word[] table, int position) {
            int oparg = ArrayAccess.getWord(table, position++).asOffset().toInt();
            //print_debug(position, stackTop(), stackSize(), Opcode.BUILD_TUPLE, oparg);
            push(new PyTuple(popN(oparg)));
            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.BUILD_LIST)
        public void buildListTemplate(Word[] table, int position) {
            int oparg = ArrayAccess.getWord(table, position++).asOffset().toInt();
            //print_debug(position, stackTop(), stackSize(), Opcode.BUILD_LIST, oparg);
            push(new PyList(popN(oparg)));
            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.BUILD_MAP)
        public void buildMapTemplate(Word[] table, int position) {
            int oparg = ArrayAccess.getWord(table, position++).asOffset().toInt();
            //print_debug(position, stackTop(), stackSize(), Opcode.BUILD_MAP, oparg);
            push(new PyDictionary());
            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.LOAD_ATTR)
        public void loadAttrTemplate(Word[] table, int position) {
            int oparg = ArrayAccess.getWord(table, position++).asOffset().toInt();
            //print_debug(position, stackTop(), stackSize(), Opcode.LOAD_ATTR, oparg);
            String name = co_names[oparg];
            push(pop().__getattr__(name));
            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.COMPARE_OP)
        public void compareOpTemplate(Word[] table, int position) {
            int oparg = ArrayAccess.getWord(table, position++).asOffset().toInt();
            //print_debug(position, stackTop(), stackSize(), Opcode.COMPARE_OP, oparg);
            PyObject b = pop();
            PyObject a = pop();

            switch (oparg) {
                case Opcode.PyCmp_LT:
                    push(a._lt(b));
                    break;
                case Opcode.PyCmp_LE:
                    push(a._le(b));
                    break;
                case Opcode.PyCmp_EQ:
                    push(a._eq(b));
                    break;
                case Opcode.PyCmp_NE:
                    push(a._ne(b));
                    break;
                case Opcode.PyCmp_GT:
                    push(a._gt(b));
                    break;
                case Opcode.PyCmp_GE:
                    push(a._ge(b));
                    break;
                case Opcode.PyCmp_IN:
                    push(a._in(b));
                    break;
                case Opcode.PyCmp_NOT_IN:
                    push(a._notin(b));
                    break;
                case Opcode.PyCmp_IS:
                    push(a._is(b));
                    break;
                case Opcode.PyCmp_IS_NOT:
                    push(a._isnot(b));
                    break;
                case Opcode.PyCmp_EXC_MATCH:
                    if (a instanceof PyStackException) {
                        PyException pye = ((PyStackException) a).exception;
                        push(Py.newBoolean(pye.match(b)));
                    } else {
                        push(Py.newBoolean(new PyException(a).match(b)));
                    }
                    break;
            }

            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.IMPORT_NAME)
        public void importNameTemplate(Word[] table, int position) {
            int oparg = ArrayAccess.getWord(table, position++).asOffset().toInt();
            //print_debug(position, stackTop(), stackSize(), Opcode.IMPORT_NAME, oparg);
            PyObject __import__ = f.f_builtins.__finditem__("__import__");
            if (__import__ == null) {
                throw Py.ImportError("__import__ not found");
            }
            PyString name = Py.newString(co_names[oparg]);
            PyObject fromlist = pop();
            PyObject level = pop();

            if (level.asInt() != -1) {
                push(__import__.__call__(new PyObject[] { name, f.f_globals, f.f_locals, fromlist, level }));
            } else {
                 push(__import__.__call__(new PyObject[] { name, f.f_globals, f.f_locals, fromlist }));
            }

            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.IMPORT_STAR)
        public void importStarTemplate(Word[] table, int position) {
            //print_debug(position, stackTop(), stackSize(), Opcode.IMPORT_STAR, 0);
            PyObject module = pop();
            imp.importAll(module, f);
            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.IMPORT_FROM)
        public void importFromTemplate(Word[] table, int position) {
            int oparg = ArrayAccess.getWord(table, position++).asOffset().toInt();
            //print_debug(position, stackTop(), stackSize(), Opcode.IMPORT_FROM, oparg);
            String name = co_names[oparg];
            try {
                push(top().__getattr__(name));

            } catch (PyException pye) {
                if (pye.match(Py.AttributeError)) {
                    throw Py.ImportError(String.format(
                            "cannot import name %.230s", name));
                } else {
                    throw pye;
                }
            }

            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.JUMP_FORWARD)
        public void jumpForwardTemplate(Word[] table, int position) {
            int oparg = ArrayAccess.getWord(table, position++).asOffset().toInt();
            //print_debug(position, stackTop(), stackSize(), Opcode.JUMP_FORWARD, oparg);
            position = oparg;
            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        /*
         * Will return true if it 's going to jump
         */
        @PYBYTECODEIMPLEMENTATION(Opcode.JUMP_IF_FALSE)
        public void jumpIfFalseTemplate(Word[] table, int position) {
            int oparg = ArrayAccess.getWord(table, position++).asOffset().toInt();
            //print_debug(position, stackTop(), stackSize(), Opcode.JUMP_IF_FALSE, oparg);

            if (!top().__nonzero__()) {
                position = oparg;
            }
            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.JUMP_IF_TRUE)
        public void jumpIfTrueTemplate(Word[] table, int position) {
            int oparg = ArrayAccess.getWord(table, position++).asOffset().toInt();
            //print_debug(position, stackTop(), stackSize(), Opcode.JUMP_IF_TRUE, oparg);
            if (top().__nonzero__()) {
                position = oparg;
            }

            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.JUMP_ABSOLUTE)
        public void jumpAbsoluteTemplate(Word[] table, int position) {
            int oparg = ArrayAccess.getWord(table, position++).asOffset().toInt();
            //print_debug(position, stackTop(), stackSize(), Opcode.JUMP_ABSOLUTE, oparg);
            position = oparg;
            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.GET_ITER)
        public void getIterTemplate(Word[] table, int position) {
            //print_debug(position, stackTop(), stackSize(), Opcode.GET_ITER, 0);
            PyObject it = top().__iter__();
            if (it != null) {
                pop();
                push(it);
            }

            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        /*
         * Return true when the iterator is exhausted, i.e. it's the end of the
         * iterator
         */
        @PYBYTECODEIMPLEMENTATION(Opcode.FOR_ITER)
        public void forIterTemplate(Word[] table, int position) {
            int oparg = ArrayAccess.getWord(table, position++).asOffset().toInt();
            //print_debug(position, stackTop(), stackSize(), Opcode.FOR_ITER, oparg);
            PyObject it = pop();
            try {
                PyObject x = it.__iternext__();
                if (x != null) {
                    push(it);
                    push(x);
                    Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
                }
            } catch (PyException pye) {
                if (!pye.match(Py.StopIteration)) {
                    throw pye;
                }
            }
            position = oparg;
            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.BREAK_LOOP)
        public void breakLoopTemplate(Word[] table, int position) {
            //print_debug(position, stackTop(), stackSize(), Opcode.BREAK_LOOP, 0);
            PyBlock b = popBlock(f);
            while (stackSize() > b.b_level) {
                pop();
            }
            position = b.b_handler;;
            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.CONTINUE_LOOP)
        public void continueLoopTemplate(int oparg) {
            // see PyBytecode
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.SETUP_LOOP)
        public void setUpLoopTemplate(Word[] table, int position) {
            int oparg = ArrayAccess.getWord(table, position++).asOffset().toInt();
            //print_debug(position, stackTop(), stackSize(), Opcode.SETUP_LOOP, oparg);
            pushBlock(f, new PyBlock(Opcode.SETUP_LOOP, oparg, stackSize()));
            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.SETUP_EXCEPT)
        public void setUpExceptTemplate(int oparg) {
            // see PyBytecode
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.SETUP_FINALLY)
        public void setUpFinallyTemplate(int oparg) {
            // see PyBytecode
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.WITH_CLEANUP)
        public void withCleanUpTemplate(Word[] table, int position) {
            // see PyBytecode
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.CALL_FUNCTION)
        public void callFunctionTemplate(Word[] table, int position) {
            int oparg = ArrayAccess.getWord(table, position++).asOffset().toInt();
            //print_debug(position, stackTop(), stackSize(), Opcode.CALL_FUNCTION, oparg);
            int na = oparg & 0xff;
            int nk = (oparg >> 8) & 0xff;

            if (nk == 0) {
                switch (na) {
                    case 0: {
                        PyObject callable = pop();
                        push(callable.__call__());
                        break;
                    }
                    case 1: {
                        PyObject arg = pop();
                        PyObject callable = pop();
                        push(callable.__call__(arg));
                        break;
                    }
                    case 2: {
                        PyObject arg1 = pop();
                        PyObject arg0 = pop();
                        PyObject callable = pop();
                        push(callable.__call__(arg0, arg1));
                        break;
                    }
                    case 3: {
                        PyObject arg2 = pop();
                        PyObject arg1 = pop();
                        PyObject arg0 = pop();
                        PyObject callable = pop();
                        push(callable.__call__(arg0, arg1, arg2));
                        break;
                    }
                    case 4: {
                        PyObject arg3 = pop();
                        PyObject arg2 = pop();
                        PyObject arg1 = pop();
                        PyObject arg0 = pop();
                        PyObject callable = pop();
                        push(callable.__call__(arg0, arg1, arg2, arg3));
                        break;
                    }
                    default: {
                        PyObject args[] = popN(na);
                        PyObject callable = pop();
                        push(callable.__call__(args));
                    }
                }
            } else {
                int n = na + nk * 2;
                PyObject params[] = popN(n);
                PyObject callable = pop();

                PyObject args[] = new PyObject[na + nk];
                String keywords[] = new String[nk];
                int i;
                for (i = 0; i < na; i++) {
                    args[i] = params[i];
                }
                for (int j = 0; i < n; i += 2, j++) {
                    keywords[j] = params[i].toString();
                    args[na + j] = params[i + 1];
                }
                push(callable.__call__(args, keywords));
            }

            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.MAKE_FUNCTION)
        public void makeFunctionTemplate(Word[] table, int position) {
            int oparg = ArrayAccess.getWord(table, position++).asOffset().toInt();
            //print_debug(position, stackTop(), stackSize(), Opcode.MAKE_FUNCTION, oparg);
            PyCode code = (PyCode) pop();
            PyObject[] defaults = popN(oparg);
            PyObject doc = null;
            if (code instanceof PyBytecode && ((PyBytecode) code).co_consts.length > 0) {
                doc = ((PyBytecode) code).co_consts[0];
            }
            PyFunction func = new PyFunction(f.f_globals, defaults, code, doc);
            push(func);
            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.MAKE_CLOSURE)
        public void makeClosureTemplate(Word[] table, int position) {
            int oparg = ArrayAccess.getWord(table, position++).asOffset().toInt();
            //print_debug(position, stackTop(), stackSize(), Opcode.MAKE_CLOSURE, oparg);
            PyCode code = (PyCode) pop();
            PyObject[] closure_cells = ((PySequenceList) (pop())).getArray();
            PyObject[] defaults = popN(oparg);
            PyObject doc = null;
            if (code instanceof PyBytecode && ((PyBytecode) code).co_consts.length > 0) {
                doc = ((PyBytecode) code).co_consts[0];
            }
            PyFunction func = new PyFunction(f.f_globals, defaults, code, doc, closure_cells);
            push(func);

            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.BUILD_SLICE)
        public void buildSliceTemplate(Word[] table, int position) {
            int oparg = ArrayAccess.getWord(table, position++).asOffset().toInt();
            //print_debug(position, stackTop(), stackSize(), Opcode.BUILD_SLICE, oparg);
            PyObject step = oparg == 3 ? pop() : null;
            PyObject stop = pop();
            PyObject start = pop();
            push(new PySlice(start, stop, step));

            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.EXTENDED_ARG)
        public void extendedArgTemplate(int oparg) {
            // see PyBytecode
        }

        void print_debug(int position, Address stackTop, int stackSize, int opcode, int oparg) {
//            if (debug) {
//                PyObject[] stack = new PyObject[stackSize];
//                for (int i = 0; i < stackSize; i++) {
//                    stack[i] = stackAt(stackTop, i);
//                }
//
//                System.err.println(co_name + " " + position + ": "
//                                + get_opname().__getitem__(Py.newInteger(opcode))
//                                + " (" + opcode + ") "
//                                + (opcode >= Opcode.HAVE_ARGUMENT ? " " + oparg : "")
//                                + ", stack:" + stackSize + " " + stackToString(stackSize, stack));
//            }
        }

        String stackToString(int stackSize, PyObject[] stack) {
            StringBuilder buffer = new StringBuilder();
            int size = stackSize;
            int N = size > 4 ? 4 : size;
            buffer.append("[");
            for (int i = 0; i < N; i++) {
                if (i > 0) {
                    buffer.append(", ");
                }

                PyObject item = stack[i];

                if (item == null || item.__repr__() == null) {
                    buffer.append("null |");
                } else {
                    buffer.append(PyStack.upto(item.__repr__().toString()));
                }
            }
            if (N < size) {
                buffer.append(String.format(", %d more...", size - N));
            }
            buffer.append("]");
            return buffer.toString();
        }

        private void rot2() {
            PyObject op1 = pop();
            PyObject op2 = pop();
            push(op1);
            push(op2);
        }

        private void rot3() {
            PyObject v = pop();
            PyObject w = pop();
            PyObject x = pop();
            push(v);
            push(x);
            push(w);
        }

        private void rot4() {
            PyObject u = pop();
            PyObject v = pop();
            PyObject w = pop();
            PyObject x = pop();
            push(u);
            push(x);
            push(w);
            push(v);
        }

        private void dup() {
            PyObject op = pop();
            push(op);
            push(op);
        }

        private void dup(int n) {
            Address top = stackTop();
            for (int i = n - 1; i >= 0; i--) {
                push(stackAt(top, i));
            }
        }

        private PyObject[] popN(int n) {
            PyObject ret[] = new PyObject[n];

            for (int i = 0; i < n; i++) {
                ret[n - i - 1] = pop();
            }

            return ret;
        }

        private PyObject top() {
            PyObject ret = pop();
            push(ret);
            return ret;
        }

        private void unpack_iterable(int oparg) {
            PyObject v = pop();
            int i = oparg;
            PyObject items[] = new PyObject[oparg];
            for (PyObject item : v.asIterable()) {
                if (i <= 0) {
                    throw Py.ValueError("too many values to unpack");
                }
                i--;
                items[i] = item;
            }
            if (i > 0) {
                throw Py.ValueError(String
                        .format("need more than %d value%s to unpack", i,
                                i == 1 ? "" : "s"));
            }
            for (i = 0; i < oparg; i++) {
                push(items[i]);
            }
        }
    }

}

