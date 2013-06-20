package org.python.core;

import java.util.Arrays;

import com.sun.max.unsafe.Address;
import com.sun.max.unsafe.Offset;
import com.sun.max.unsafe.Word;
import com.sun.max.vm.object.ArrayAccess;

/*
 * ModularVM
 * Inspired by com.oracle.max.asm.Buffer
 */
public class DirectThreadingTable {
    protected Word[] data;
    protected int position;
    String name;
    
    // dummy
    public DirectThreadingTable() {
        this.data = null;
        this.name = null;
    }
    
    public DirectThreadingTable(PyBytecode bytecode) {
        this.data = new Word[bytecode.co_code.length];
        this.name = bytecode.co_name;
    }
    
    public void reset() {
        position = 0;
    }

    public int position() {
        return position;
    }
    
    public void setPosition(int position) {
        assert position >= 0 && position <= data.length;
        this.position = position;
    }
    
    public void setArgument(int argument) {
        assert position >= 0 && position <= data.length;
        ArrayAccess.setWord(data, position, Offset.fromInt(argument));
    }
    
    public void close(boolean trimmedCopy) {
        Word[] result = trimmedCopy ? Arrays.copyOf(data, position()) : data;
        data = result;
    }
    
    public Word[] copyData(int start, int end) {
        return Arrays.copyOfRange(data, start, end);
    }
    
    protected void ensureSize(int length) {
        if (length >= data.length) {
            data = Arrays.copyOf(data, length * 4);
        }
    }
    
    public void putAddress(Address addr) {
        ArrayAccess.setWord(data, position++, addr);
    }
    
    public void putArgument(int val) {
        Word argument = Offset.fromInt(val);
        ArrayAccess.setWord(data, position++, argument);
    }
    
    Address getAddress(int pos) {
//        assert pos >= 0 && pos < data.length -1 : "pos is out of bound";
        Address address = ArrayAccess.getWord(data, pos).asAddress();
        return address;
    }
    
    int getArgument(int pos) {
//        assert pos >= 0 && pos < data.length -1 : "pos is out of bound";
        int oparg = ArrayAccess.getWord(data, pos).asOffset().toInt();
        return oparg;
    }
    
    boolean ends() {
        return position > data.length - 1;
    }
    
    public String toString() {
        return "Direct Threaded Table of " + name;
    }
}
