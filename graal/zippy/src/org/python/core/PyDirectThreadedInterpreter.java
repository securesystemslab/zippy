package org.python.core;

import com.sun.max.unsafe.Word;
import com.sun.max.vm.Intrinsics;
import com.sun.max.vm.compiler.target.TargetMethod;
import com.sun.max.vm.object.ArrayAccess;

public class PyDirectThreadedInterpreter extends PyBytecode {

    static {
        IOPsCompilation.instance().compileIOPs(PyDirectThreadedInterpreter.ActivationRecord.class);
    }

    DirectThreadingTableGenerator generator;
    DirectThreadingTable directThreadingTable;
    TargetMethod prologue;

    PyDirectThreadedInterpreter() {
        super(false);
        directThreadingTable = null;
    }

    public PyDirectThreadedInterpreter(PyBytecode pyBytecode) {
        super(pyBytecode, false);
    }

    @Override
    public PyObject interpret(PyFrame frame, ThreadState ts) {
        if (directThreadingTable == null) {
            long start = Options.startTimer(Options.timeThreadGen);
            generator = new DirectThreadingTableGenerator(this);
            directThreadingTable = generator.generateTable();
            PyDirectThreadedFrameInitializer frameInitializer = new PyDirectThreadedFrameInitializer();
            prologue = frameInitializer.generatePrologue();
            Options.stopTimer(Options.timeThreadGen, start, Options.TIMER.THREADGEN);
        }

        return new ActivationRecord(frame, new PyStack(co_stacksize), directThreadingTable).execute();
    }

    public class ActivationRecord {
        final PyFrame f;
        final PyStack stack;
        PyObject returnValue = null;
        Word[] table;

        // dummy constructor
        ActivationRecord() {
            this.f = null;
            this.stack = null;
            this.table = null;
        }

        ActivationRecord(PyFrame frame, PyStack stack, DirectThreadingTable directThreadingTable) {
            this.f = frame;
            this.stack = stack;
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
                    stack.push(v);
                }

                f.f_savedlocals = null;

                Object generatorInput = f.getGeneratorInput();
                if (generatorInput instanceof PyException) {
                    throw (PyException) generatorInput;
                }
                stack.push((PyObject) generatorInput);
            }

            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.NOP)
        public void nopTemplate(Word[] table, int position) {
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.LOAD_FAST)
        public void loadFastTemplate(Word[] table, int position) {
            int oparg = ArrayAccess.getWord(table, position++).asOffset().toInt();
            //print_debug(Opcode.LOAD_FAST, oparg);
            stack.push(f.getlocal(oparg));
            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.LOAD_CONST)
        public void loadConstTemplate(Word[] table, int position) {
            int oparg = ArrayAccess.getWord(table, position++).asOffset().toInt();
            //print_debug(Opcode.LOAD_CONST, oparg);
            stack.push(co_consts[oparg]);
            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.STORE_FAST)
        public void storeFastTemplate(Word[] table, int position) {
            int oparg = ArrayAccess.getWord(table, position++).asOffset().toInt();
            //print_debug(Opcode.STORE_FAST, oparg);
            f.setlocal(oparg, stack.pop());
            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.POP_TOP)
        public void popTopTemplate(Word[] table, int position) {
            //print_debug(Opcode.POP_TOP, 0);
            stack.pop();
            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.ROT_TWO)
        public void rotTwoTemplate(Word[] table, int position) {
            //print_debug(Opcode.ROT_TWO, 0);
            stack.rot2();
            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.ROT_THREE)
        public void rotThreeTemplate(Word[] table, int position) {
            //print_debug(Opcode.ROT_THREE, 0);
            stack.rot3();
            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.ROT_FOUR)
        public void rotFourTemplate(Word[] table, int position) {
            //print_debug(Opcode.ROT_FOUR, 0);
            stack.rot4();
            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.DUP_TOP)
        public void dupTopTemplate(Word[] table, int position) {
            //print_debug(Opcode.DUP_TOP, 0);
            stack.dup();
            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.DUP_TOPX)
        public void dupTopXTemplate(Word[] table, int position) {
            int oparg = ArrayAccess.getWord(table, position++).asOffset().toInt();
            //print_debug(Opcode.DUP_TOPX, oparg);
            if (oparg == 2 || oparg == 3) {
                stack.dup(oparg);
            } else {
                throw Py.RuntimeError("invalid argument to DUP_TOPX" + " (bytecode corruption?)");
            }

            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.UNARY_POSITIVE)
        public void unaryPositiveTemplate(Word[] table, int position) {
            //print_debug(Opcode.UNARY_POSITIVE, 0);
            stack.push(stack.pop().__pos__());
            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.UNARY_NEGATIVE)
        public void unaryNegativeTemplate(Word[] table, int position) {
            //print_debug(Opcode.UNARY_NEGATIVE, 0);
            stack.push(stack.pop().__neg__());
            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.UNARY_NOT)
        public void unaryNotTemplate(Word[] table, int position) {
            //print_debug(Opcode.UNARY_NOT, 0);
            stack.push(stack.pop().__not__());
            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.UNARY_CONVERT)
        public void unaryConvertTemplate(Word[] table, int position) {
            //print_debug(Opcode.UNARY_CONVERT, 0);
            stack.push(stack.pop().__repr__());
            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.UNARY_INVERT)
        public void unaryInvertTemplate(Word[] table, int position) {
            //print_debug(Opcode.UNARY_INVERT, 0);
            stack.push(stack.pop().__invert__());
            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.BINARY_POWER)
        public void binaryPowerTemplate(Word[] table, int position) {
            //print_debug(Opcode.BINARY_POWER, 0);
            PyObject b = stack.pop();
            PyObject a = stack.pop();
            stack.push(a._pow(b));
            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.BINARY_MULTIPLY)
        public void binaryMultiplyTemplate(Word[] table, int position) {
            //print_debug(Opcode.BINARY_MULTIPLY, 0);
            PyObject b = stack.pop();
            PyObject a = stack.pop();
            stack.push(a._mul(b));
            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.BINARY_DIVIDE)
        public void binaryDivideTemplate(Word[] table, int position) {
            //print_debug(Opcode.BINARY_DIVIDE, 0);
            PyObject b = stack.pop();
            PyObject a = stack.pop();

            if (!co_flags.isFlagSet(CodeFlag.CO_FUTURE_DIVISION)) {
                stack.push(a._div(b));
            } else {
                stack.push(a._truediv(b));
            }

            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.BINARY_TRUE_DIVIDE)
        public void binaryTrueDivideTemplate(Word[] table, int position) {
            //print_debug(Opcode.BINARY_TRUE_DIVIDE, 0);
            PyObject b = stack.pop();
            PyObject a = stack.pop();
            stack.push(a._truediv(b));
            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.BINARY_FLOOR_DIVIDE)
        public void binaryFloorDivideTemplate(Word[] table, int position) {
            //print_debug(Opcode.BINARY_FLOOR_DIVIDE, 0);
            PyObject b = stack.pop();
            PyObject a = stack.pop();
            stack.push(a._floordiv(b));
            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.BINARY_MODULO)
        public void binaryModuloTemplate(Word[] table, int position) {
            //print_debug(Opcode.BINARY_MODULO, 0);
            PyObject b = stack.pop();
            PyObject a = stack.pop();
            stack.push(a._mod(b));
            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.BINARY_ADD)
        public void binaryAddTemplate(Word[] table, int position) {
            //print_debug(Opcode.BINARY_ADD, 0);
            PyObject b = stack.pop();
            PyObject a = stack.pop();
            stack.push(a._add(b));
            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.BINARY_SUBTRACT)
        public void binarySubtractTemplate(Word[] table, int position) {
            //print_debug(Opcode.BINARY_SUBTRACT, 0);
            PyObject b = stack.pop();
            PyObject a = stack.pop();
            stack.push(a._sub(b));
            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.BINARY_SUBSCR)
        public void binarySubscrTemplate(Word[] table, int position) {
            //print_debug(Opcode.BINARY_SUBSCR, 0);
            PyObject b = stack.pop();
            PyObject a = stack.pop();
            stack.push(a.__getitem__(b));
            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.BINARY_LSHIFT)
        public void binaryLshiftTemplate(Word[] table, int position) {
            //print_debug(Opcode.BINARY_LSHIFT, 0);
            PyObject b = stack.pop();
            PyObject a = stack.pop();
            stack.push(a._lshift(b));
            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.BINARY_RSHIFT)
        public void binaryRshiftTemplate(Word[] table, int position) {
            //print_debug(Opcode.BINARY_RSHIFT, 0);
            PyObject b = stack.pop();
            PyObject a = stack.pop();
            stack.push(a._rshift(b));
            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.BINARY_AND)
        public void binaryAndTemplate(Word[] table, int position) {
            //print_debug(Opcode.BINARY_ADD, 0);
            PyObject b = stack.pop();
            PyObject a = stack.pop();
            stack.push(a._and(b));
            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.BINARY_XOR)
        public void binaryXorTemplate(Word[] table, int position) {
            //print_debug(Opcode.BINARY_XOR, 0);
            PyObject b = stack.pop();
            PyObject a = stack.pop();
            stack.push(a._xor(b));
            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.BINARY_OR)
        public void binaryOrTemplate(Word[] table, int position) {
            //print_debug(Opcode.BINARY_OR, 0);
            PyObject b = stack.pop();
            PyObject a = stack.pop();
            stack.push(a._or(b));
            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.LIST_APPEND)
        public void listAppendTemplate(Word[] table, int position) {
            //print_debug(Opcode.LIST_APPEND, 0);
            PyObject b = stack.pop();
            PyList a = (PyList) (stack.pop());
            a.append(b);
            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.INPLACE_POWER)
        public void listInPlacePowerTemplate(Word[] table, int position) {
            //print_debug(Opcode.INPLACE_POWER, 0);
            PyObject b = stack.pop();
            PyObject a = stack.pop();
            stack.push(a._ipow(b));
            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.INPLACE_MULTIPLY)
        public void listInPlaceMultiplyTemplate(Word[] table, int position) {
            //print_debug(Opcode.INPLACE_MULTIPLY, 0);
            PyObject b = stack.pop();
            PyObject a = stack.pop();
            stack.push(a._imul(b));
            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.INPLACE_DIVIDE)
        public void listInPlaceDivideTemplate(Word[] table, int position) {
            //print_debug(Opcode.INPLACE_DIVIDE, 0);
            PyObject b = stack.pop();
            PyObject a = stack.pop();
            if (!co_flags.isFlagSet(CodeFlag.CO_FUTURE_DIVISION)) {
                stack.push(a._idiv(b));
            } else {
                stack.push(a._itruediv(b));
            }

            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.INPLACE_TRUE_DIVIDE)
        public void listInPlaceTrueDivideTemplate(Word[] table, int position) {
            //print_debug(Opcode.INPLACE_TRUE_DIVIDE, 0);
            PyObject b = stack.pop();
            PyObject a = stack.pop();
            stack.push(a._itruediv(b));
            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.INPLACE_FLOOR_DIVIDE)
        public void listInPlaceFloorDividePowerTemplate(Word[] table, int position) {
            //print_debug(Opcode.INPLACE_FLOOR_DIVIDE, 0);
            PyObject b = stack.pop();
            PyObject a = stack.pop();
            stack.push(a._ifloordiv(b));
            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.INPLACE_MODULO)
        public void listInPlaceModuloTemplate(Word[] table, int position) {
            //print_debug(Opcode.INPLACE_MODULO, 0);
            PyObject b = stack.pop();
            PyObject a = stack.pop();
            stack.push(a._imod(b));
            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.INPLACE_ADD)
        public void listInPlaceAddTemplate(Word[] table, int position) {
            //print_debug(Opcode.INPLACE_ADD, 0);
            PyObject b = stack.pop();
            PyObject a = stack.pop();
            stack.push(a._iadd(b));
            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.INPLACE_SUBTRACT)
        public void listInPlaceSubtractTemplate(Word[] table, int position) {
            //print_debug(Opcode.INPLACE_SUBTRACT, 0);
            PyObject b = stack.pop();
            PyObject a = stack.pop();
            stack.push(a._isub(b));
            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.INPLACE_LSHIFT)
        public void listInPlaceLshiftTemplate(Word[] table, int position) {
            //print_debug(Opcode.INPLACE_LSHIFT, 0);
            PyObject b = stack.pop();
            PyObject a = stack.pop();
            stack.push(a._ilshift(b));
            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.INPLACE_RSHIFT)
        public void listInPlaceRshiftTemplate(Word[] table, int position) {
            //print_debug(Opcode.INPLACE_RSHIFT, 0);
            PyObject b = stack.pop();
            PyObject a = stack.pop();
            stack.push(a._irshift(b));
            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.INPLACE_AND)
        public void listInPlaceAndTemplate(Word[] table, int position) {
            //print_debug(Opcode.INPLACE_AND, 0);
            PyObject b = stack.pop();
            PyObject a = stack.pop();
            stack.push(a._iand(b));
            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.INPLACE_XOR)
        public void listInPlaceXorTemplate(Word[] table, int position) {
            //print_debug(Opcode.INPLACE_XOR, 0);
            PyObject b = stack.pop();
            PyObject a = stack.pop();
            stack.push(a._ixor(b));
            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.INPLACE_OR)
        public void listInPlaceOrTemplate(Word[] table, int position) {
            //print_debug(Opcode.INPLACE_OR, 0);
            PyObject b = stack.pop();
            PyObject a = stack.pop();
            stack.push(a._ior(b));
            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.SLICE)
        public void sliceTemplate(Word[] table, int position) {
            //print_debug(Opcode.SLICE, 0);
            slice(Opcode.SLICE);
            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.SLICE + 1)
        public void slice1Template(Word[] table, int position) {
            //print_debug(Opcode.SLICE+1, 0);
            slice(Opcode.SLICE + 1);
            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.SLICE + 2)
        public void slice2Template(Word[] table, int position) {
            //print_debug(Opcode.SLICE+2, 0);
            slice(Opcode.SLICE + 2);
            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.SLICE + 3)
        public void slice3Template(Word[] table, int position) {
            //print_debug(Opcode.SLICE+3, 0);
            slice(Opcode.SLICE + 3);
            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        private void slice(int opcode) {
            PyObject stop = (((opcode - Opcode.SLICE) & 2) != 0) ? stack.pop() : null;
            PyObject start = (((opcode - Opcode.SLICE) & 1) != 0) ? stack.pop() : null;
            PyObject obj = stack.pop();
            stack.push(obj.__getslice__(start, stop));
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.STORE_SLICE)
        public void storeSliceTemplate(Word[] table, int position) {
            //print_debug(Opcode.STORE_SLICE, 0);
            storeSlice(Opcode.STORE_SLICE);
            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.STORE_SLICE + 1)
        public void storeSlice1Template(Word[] table, int position) {
            //print_debug(Opcode.STORE_SLICE+1, 0);
            storeSlice(Opcode.STORE_SLICE + 1);
            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.STORE_SLICE + 2)
        public void storeSlice2Template(Word[] table, int position) {
            //print_debug(Opcode.STORE_SLICE+2, 0);
            storeSlice(Opcode.STORE_SLICE + 2);
            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.STORE_SLICE + 3)
        public void storeSlice3Template(Word[] table, int position) {
            //print_debug(Opcode.STORE_SLICE+3, 0);
            storeSlice(Opcode.STORE_SLICE + 3);
            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        private void storeSlice(int opcode) {
            PyObject stop = (((opcode - Opcode.STORE_SLICE) & 2) != 0) ? stack.pop() : null;
            PyObject start = (((opcode - Opcode.STORE_SLICE) & 1) != 0) ? stack.pop() : null;
            PyObject obj = stack.pop();
            PyObject value = stack.pop();
            obj.__setslice__(start, stop, value);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.DELETE_SLICE)
        public void deleteSliceTemplate(Word[] table, int position) {
            //print_debug(Opcode.DELETE_SLICE, 0);
            deleteSlice(Opcode.DELETE_SLICE);
            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.DELETE_SLICE + 1)
        public void deleteSlice1Template(Word[] table, int position) {
            //print_debug(Opcode.DELETE_SLICE+1, 0);
            deleteSlice(Opcode.DELETE_SLICE + 1);
            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.DELETE_SLICE + 2)
        public void deleteSlice2Template(Word[] table, int position) {
            //print_debug(Opcode.DELETE_SLICE+2, 0);
            deleteSlice(Opcode.DELETE_SLICE + 2);
            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.DELETE_SLICE + 3)
        public void deleteSlice3Template(Word[] table, int position) {
            //print_debug(Opcode.DELETE_SLICE+3, 0);
            deleteSlice(Opcode.DELETE_SLICE + 3);
            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        private void deleteSlice(int opcode) {
            PyObject stop = (((opcode - Opcode.DELETE_SLICE) & 2) != 0) ? stack.pop() : null;
            PyObject start = (((opcode - Opcode.DELETE_SLICE) & 1) != 0) ? stack.pop() : null;
            PyObject obj = stack.pop();
            obj.__delslice__(start, stop);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.STORE_SUBSCR)
        public void storeSubscrTemplate(Word[] table, int position) {
            //print_debug(Opcode.STORE_SUBSCR, 0);
            PyObject key = stack.pop();
            PyObject obj = stack.pop();
            PyObject value = stack.pop();
            obj.__setitem__(key, value);
            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.DELETE_SUBSCR)
        public void deleteSubscrTemplate(Word[] table, int position) {
            //print_debug(Opcode.DELETE_SUBSCR, 0);
            PyObject key = stack.pop();
            PyObject obj = stack.pop();
            obj.__delitem__(key);
            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.PRINT_EXPR)
        public void printExprTemplate(Word[] table, int position) {
            //print_debug(Opcode.PRINT_EXPR, 0);
            PySystemState.displayhook(stack.pop());
            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.PRINT_ITEM_TO)
        public void printItemToTemplate(Word[] table, int position) {
            //print_debug(Opcode.PRINT_ITEM_TO, 0);
            Py.printComma(stack.pop(), stack.pop());
            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.PRINT_ITEM)
        public void printItemTemplate(Word[] table, int position) {
            //print_debug(Opcode.PRINT_ITEM, 0);
            Py.printComma(stack.pop());
            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.PRINT_NEWLINE_TO)
        public void printNewLineToTemplate(Word[] table, int position) {
            //print_debug(Opcode.PRINT_NEWLINE_TO, 0);
            Py.printlnv(stack.pop());
            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.PRINT_NEWLINE)
        public void printNewLineTemplate(Word[] table, int position) {
            //print_debug(Opcode.PRINT_NEWLINE, 0);
            Py.println();
            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        /*
         * TODO ADD RAISEVARARGS here
         */
        @PYBYTECODEIMPLEMENTATION(Opcode.LOAD_LOCALS)
        public void loadLocalsTemplate(Word[] table, int position) {
            //print_debug(Opcode.LOAD_LOCALS, 0);
            stack.push(f.f_locals);
            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.RETURN_VALUE)
        public void returnValueTemplate(Word[] table, int position) {
            //print_debug(Opcode.RETURN_VALUE, 0);
            returnValue = stack.pop();

            while (stack.size() > 0) {
                stack.pop();
            }

            // from endOfFunction()
            if (co_flags.isFlagSet(CodeFlag.CO_GENERATOR) && returnValue == Py.None) {
                f.f_lasti = -1;
            }

            Intrinsics.epilogue();
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.YIELD_VALUE)
        public void yieldValueTemplate(Word[] table, int position) {
            //print_debug(Opcode.YIELD_VALUE, 0);
            returnValue = stack.pop();
            // fromEndofFunction()
            // store the stack in the frame for reentry from the yield;
            f.f_savedlocals = stack.popN(stack.size());
            f.f_lasti = position;
            Intrinsics.epilogue();
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.EXEC_STMT)
        public void execStmtTemplate(Word[] table, int position) {
            //print_debug(Opcode.EXEC_STMT, 0);
            PyObject locals = stack.pop();
            PyObject globals = stack.pop();
            PyObject code = stack.pop();
            Py.exec(code, globals == Py.None ? null : globals, locals == Py.None ? null : locals);
            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.POP_BLOCK)
        public void popBlockTemplate(Word[] table, int position) {
            //print_debug(Opcode.POP_BLOCK, 0);
            //PyTryBlock b = popBlock(f);
            PyBlock b = popBlock(f);
            while (stack.size() > b.b_level) {
                stack.pop();
            }

            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        /*
         * TODO END_FINALLY
         */

        @PYBYTECODEIMPLEMENTATION(Opcode.BUILD_CLASS)
        public void buildClassTemplate(Word[] table, int position) {
            //print_debug(Opcode.BUILD_CLASS, 0);
            PyObject methods = stack.pop();
            PyObject bases[] = ((PySequenceList) (stack.pop())).getArray();
            String name = stack.pop().toString();
            stack.push(Py.makeClass(name, bases, methods));
            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.STORE_NAME)
        public void storeNameTemplate(Word[] table, int position) {
            int oparg = ArrayAccess.getWord(table, position++).asOffset().toInt();
            //print_debug(Opcode.STORE_NAME, oparg);
            f.setlocal(co_names[oparg], stack.pop());
            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.DELETE_NAME)
        public void deleteNameTemplate(Word[] table, int position) {
            int oparg = ArrayAccess.getWord(table, position++).asOffset().toInt();
            //print_debug(Opcode.DELETE_NAME, oparg);
            f.dellocal(co_names[oparg]);
            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.UNPACK_SEQUENCE)
        public void unpackSequenceTemplate(Word[] table, int position) {
            int oparg = ArrayAccess.getWord(table, position++).asOffset().toInt();
            //print_debug(Opcode.UNPACK_SEQUENCE, oparg);
            unpack_iterable(oparg, stack);
            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.STORE_ATTR)
        public void storeAttrTemplate(Word[] table, int position) {
            int oparg = ArrayAccess.getWord(table, position++).asOffset().toInt();
            //print_debug(Opcode.STORE_ATTR, oparg);
            PyObject obj = stack.pop();
            PyObject v = stack.pop();
            obj.__setattr__(co_names[oparg], v);
            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.DELETE_ATTR)
        public void deleteAttrTemplate(Word[] table, int position) {
            int oparg = ArrayAccess.getWord(table, position++).asOffset().toInt();
            //print_debug(Opcode.DELETE_ATTR, oparg);
            stack.pop().__delattr__(co_names[oparg]);
            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.STORE_GLOBAL)
        public void storeGlobalTemplate(Word[] table, int position) {
            int oparg = ArrayAccess.getWord(table, position++).asOffset().toInt();
            //print_debug(Opcode.STORE_GLOBAL, oparg);
            f.setglobal(co_names[oparg], stack.pop());
            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.DELETE_GLOBAL)
        public void deleteGlobalTemplate(Word[] table, int position) {
            int oparg = ArrayAccess.getWord(table, position++).asOffset().toInt();
            //print_debug(Opcode.DELETE_GLOBAL, oparg);
            f.delglobal(co_names[oparg]);
            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.LOAD_NAME)
        public void loadNameTemplate(Word[] table, int position) {
            int oparg = ArrayAccess.getWord(table, position++).asOffset().toInt();
            //print_debug(Opcode.LOAD_NAME, oparg);
            stack.push(f.getname(co_names[oparg]));
            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.LOAD_GLOBAL)
        public void loadGlobalTemplate(Word[] table, int position) {
            int oparg = ArrayAccess.getWord(table, position++).asOffset().toInt();
            //print_debug(Opcode.LOAD_GLOBAL, oparg);
            stack.push(f.getglobal(co_names[oparg]));
            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.DELETE_FAST)
        public void deleteFastTemplate(Word[] table, int position) {
            int oparg = ArrayAccess.getWord(table, position++).asOffset().toInt();
            //print_debug(Opcode.DELETE_FAST, oparg);
            f.dellocal(oparg);
            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.LOAD_CLOSURE)
        public void loadClosureTemplate(Word[] table, int position) {
            int oparg = ArrayAccess.getWord(table, position++).asOffset().toInt();
            //print_debug(Opcode.LOAD_CLOSURE, oparg);
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
            stack.push(cell);
            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.LOAD_DEREF)
        public void loadDeRefTemplate(Word[] table, int position) {
            int oparg = ArrayAccess.getWord(table, position++).asOffset().toInt();
            //print_debug(Opcode.LOAD_DEREF, oparg);
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
            stack.push(cell.ob_ref);
            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.STORE_DEREF)
        public void storeDeRefTemplate(Word[] table, int position) {
            int oparg = ArrayAccess.getWord(table, position++).asOffset().toInt();
            //print_debug(Opcode.STORE_DEREF, oparg);
            f.setderef(oparg, stack.pop());
            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.BUILD_TUPLE)
        public void buildTupleTemplate(Word[] table, int position) {
            int oparg = ArrayAccess.getWord(table, position++).asOffset().toInt();
            //print_debug(Opcode.BUILD_TUPLE, oparg);
            stack.push(new PyTuple(stack.popN(oparg)));
            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.BUILD_LIST)
        public void buildListTemplate(Word[] table, int position) {
            int oparg = ArrayAccess.getWord(table, position++).asOffset().toInt();
            //print_debug(Opcode.BUILD_LIST, oparg);
            stack.push(new PyList(stack.popN(oparg)));
            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.BUILD_MAP)
        public void buildMapTemplate(Word[] table, int position) {
            int oparg = ArrayAccess.getWord(table, position++).asOffset().toInt();
            //print_debug(Opcode.BUILD_MAP, oparg);
            stack.push(new PyDictionary());
            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.LOAD_ATTR)
        public void loadAttrTemplate(Word[] table, int position) {
            int oparg = ArrayAccess.getWord(table, position++).asOffset().toInt();
            //print_debug(Opcode.LOAD_ATTR, oparg);
            String name = co_names[oparg];
            stack.push(stack.pop().__getattr__(name));
            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.COMPARE_OP)
        public void compareOpTemplate(Word[] table, int position) {
            int oparg = ArrayAccess.getWord(table, position++).asOffset().toInt();
            //print_debug(Opcode.COMPARE_OP, oparg);
            PyObject b = stack.pop();
            PyObject a = stack.pop();

            switch (oparg) {
                case Opcode.PyCmp_LT:
                    stack.push(a._lt(b));
                    break;
                case Opcode.PyCmp_LE:
                    stack.push(a._le(b));
                    break;
                case Opcode.PyCmp_EQ:
                    stack.push(a._eq(b));
                    break;
                case Opcode.PyCmp_NE:
                    stack.push(a._ne(b));
                    break;
                case Opcode.PyCmp_GT:
                    stack.push(a._gt(b));
                    break;
                case Opcode.PyCmp_GE:
                    stack.push(a._ge(b));
                    break;
                case Opcode.PyCmp_IN:
                    stack.push(a._in(b));
                    break;
                case Opcode.PyCmp_NOT_IN:
                    stack.push(a._notin(b));
                    break;
                case Opcode.PyCmp_IS:
                    stack.push(a._is(b));
                    break;
                case Opcode.PyCmp_IS_NOT:
                    stack.push(a._isnot(b));
                    break;
                case Opcode.PyCmp_EXC_MATCH:
                    if (a instanceof PyStackException) {
                        PyException pye = ((PyStackException) a).exception;
                        stack.push(Py.newBoolean(pye.match(b)));
                    } else {
                        stack.push(Py.newBoolean(new PyException(a).match(b)));
                    }
                    break;
            }

            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.IMPORT_NAME)
        public void importNameTemplate(Word[] table, int position) {
            int oparg = ArrayAccess.getWord(table, position++).asOffset().toInt();
            //print_debug(Opcode.IMPORT_NAME, oparg);
            PyObject __import__ = f.f_builtins.__finditem__("__import__");
            if (__import__ == null) {
                throw Py.ImportError("__import__ not found");
            }
            PyString name = Py.newString(co_names[oparg]);
            PyObject fromlist = stack.pop();
            PyObject level = stack.pop();

            if (level.asInt() != -1) {
                stack.push(__import__.__call__(new PyObject[] { name, f.f_globals, f.f_locals, fromlist, level }));
            } else {
                 stack.push(__import__.__call__(new PyObject[] { name, f.f_globals, f.f_locals, fromlist }));
            }

            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.IMPORT_STAR)
        public void importStarTemplate(Word[] table, int position) {
            //print_debug(Opcode.IMPORT_STAR, 0);
            PyObject module = stack.pop();
            imp.importAll(module, f);
            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.IMPORT_FROM)
        public void importFromTemplate(Word[] table, int position) {
            int oparg = ArrayAccess.getWord(table, position++).asOffset().toInt();
            //print_debug(Opcode.IMPORT_FROM, oparg);
            String name = co_names[oparg];
            try {
                stack.push(stack.top().__getattr__(name));

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
            //print_debug(Opcode.JUMP_FORWARD, oparg);
            position = oparg;
            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        /*
         * Will return true if it 's going to jump
         */
        @PYBYTECODEIMPLEMENTATION(Opcode.JUMP_IF_FALSE)
        public void jumpIfFalseTemplate(Word[] table, int position) {
            int oparg = ArrayAccess.getWord(table, position++).asOffset().toInt();
            //print_debug(Opcode.JUMP_IF_FALSE, oparg);

            if (!stack.top().__nonzero__()) {
                position = oparg;
            }
            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.JUMP_IF_TRUE)
        public void jumpIfTrueTemplate(Word[] table, int position) {
            int oparg = ArrayAccess.getWord(table, position++).asOffset().toInt();
            //print_debug(Opcode.JUMP_IF_TRUE, oparg);
            if (stack.top().__nonzero__()) {
                position = oparg;
            }

            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.JUMP_ABSOLUTE)
        public void jumpAbsoluteTemplate(Word[] table, int position) {
            int oparg = ArrayAccess.getWord(table, position++).asOffset().toInt();
            //print_debug(Opcode.JUMP_ABSOLUTE, oparg);
            position = oparg;
            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.GET_ITER)
        public void getIterTemplate(Word[] table, int position) {
            //print_debug(Opcode.GET_ITER, 0);
            PyObject it = stack.top().__iter__();
            if (it != null) {
                stack.set_top(it);
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
            //print_debug(Opcode.FOR_ITER, oparg);
            PyObject it = stack.pop();
            try {
                PyObject x = it.__iternext__();
                if (x != null) {
                    stack.push(it);
                    stack.push(x);
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
            //print_debug(Opcode.BREAK_LOOP, 0);
            PyBlock b = popBlock(f);
            while (stack.size() > b.b_level) {
                stack.pop();
            }
            position = b.b_handler;
            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.CONTINUE_LOOP)
        public void continueLoopTemplate(int oparg) {
            // see PyBytecode
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.SETUP_LOOP)
        public void setUpLoopTemplate(Word[] table, int position) {
            int oparg = ArrayAccess.getWord(table, position++).asOffset().toInt();
            //print_debug(Opcode.SETUP_LOOP, oparg);
            pushBlock(f, new PyBlock(Opcode.SETUP_LOOP, oparg, stack.size()));
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
            //print_debug(Opcode.WITH_CLEANUP, 0);
            /*
             * TOP is the context.__exit__ bound method. Below that are 1-3
             * values indicating how/why we entered the finally clause: - SECOND
             * = None - (SECOND, THIRD) = (WHY_{RETURN,CONTINUE}), retval -
             * SECOND = WHY_*; no retval below it - (SECOND, THIRD, FOURTH) =
             * exc_info() In the last case, we must call TOP(SECOND, THIRD,
             * FOURTH) otherwise we must call TOP(None, None, None)
             *
             * In addition, if the stack represents an exception,and* the
             * function call returns a 'true' value, we "zap" this information,
             * to prevent END_FINALLY from re-raising the exception. (But
             * non-local gotos should still be resumed.)
             */
            PyObject exit = stack.top();
            PyObject u = stack.top(2);
            PyObject v;
            PyObject w;
            if (u == Py.None || u instanceof PyStackWhy) {
                u = v = w = Py.None;
            } else {
                v = stack.top(3);
                w = stack.top(4);
            }
            PyObject x = null;
            if (u instanceof PyStackException) {
                PyException exc = ((PyStackException) u).exception;
                x = exit.__call__(exc.type, exc.value, exc.traceback);
            } else {
                x = exit.__call__(u, v, w);
            }

            if (u != Py.None && x != null && x.__nonzero__()) {
                stack.popN(4); // XXX - consider stack.stackadj op
                stack.push(Py.None);
            } else {
                stack.pop(); // this should be popping off a block
            }

            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.CALL_FUNCTION)
        public void callFunctionTemplate(Word[] table, int position) {
            int oparg = ArrayAccess.getWord(table, position++).asOffset().toInt();
            //print_debug(Opcode.CALL_FUNCTION, oparg);
            int na = oparg & 0xff;
            int nk = (oparg >> 8) & 0xff;

            if (nk == 0) {
                call_function(na, stack);
            } else {
                call_function(na, nk, stack);
            }

            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.MAKE_FUNCTION)
        public void makeFunctionTemplate(Word[] table, int position) {
            int oparg = ArrayAccess.getWord(table, position++).asOffset().toInt();
            //print_debug(Opcode.MAKE_FUNCTION, oparg);
            PyCode code = (PyCode) stack.pop();
            PyObject[] defaults = stack.popN(oparg);
            PyObject doc = null;
            if (code instanceof PyBytecode && ((PyBytecode) code).co_consts.length > 0) {
                doc = ((PyBytecode) code).co_consts[0];
            }
            PyFunction func = new PyFunction(f.f_globals, defaults, code, doc);
            stack.push(func);
            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.MAKE_CLOSURE)
        public void makeClosureTemplate(Word[] table, int position) {
            int oparg = ArrayAccess.getWord(table, position++).asOffset().toInt();
            //print_debug(Opcode.MAKE_CLOSURE, oparg);
            PyCode code = (PyCode) stack.pop();
            PyObject[] closure_cells = ((PySequenceList) (stack.pop())).getArray();
            PyObject[] defaults = stack.popN(oparg);
            PyObject doc = null;
            if (code instanceof PyBytecode && ((PyBytecode) code).co_consts.length > 0) {
                doc = ((PyBytecode) code).co_consts[0];
            }
            PyFunction func = new PyFunction(f.f_globals, defaults, code, doc, closure_cells);
            stack.push(func);

            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.BUILD_SLICE)
        public void buildSliceTemplate(Word[] table, int position) {
            int oparg = ArrayAccess.getWord(table, position++).asOffset().toInt();
            //print_debug(Opcode.BUILD_SLICE, oparg);
            PyObject step = oparg == 3 ? stack.pop() : null;
            PyObject stop = stack.pop();
            PyObject start = stack.pop();
            stack.push(new PySlice(start, stop, step));

            Intrinsics.next(ArrayAccess.getWord(table, position++).asAddress(), this, table, position);
        }

        @PYBYTECODEIMPLEMENTATION(Opcode.EXTENDED_ARG)
        public void extendedArgTemplate(int oparg) {
            //print_debug(Opcode.EXTENDED_ARG, oparg);
        }

        void print_debug(int opcode, int oparg) {
            //if (debug) {
//                System.err.println(co_name + " "
//                        + get_opname().__getitem__(Py.newInteger(opcode))
//                        + " opcode " + opcode + " "
//                        + (opcode >= Opcode.HAVE_ARGUMENT ? " " + oparg : "")
//                        + ", stack: " + stack.toString());
            //}
        }
    }
}
