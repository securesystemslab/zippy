package org.python.core;

import java.util.Arrays;

import com.sun.max.unsafe.Address;
import com.sun.max.unsafe.Word;

/*
 * ModularVM
 * Inspired by com.oracle.max.asm.Buffer
 */
public class PyDirectThreadedCode {
    protected Address[] data;
    protected int position;
    String name;
    
    public PyDirectThreadedCode(PyBytecode bytecode) {
        data = new Address[bytecode.co_code.length];
        name = bytecode.co_name;
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
    
    public void close(boolean trimmedCopy) {
        Address[] result = trimmedCopy ? Arrays.copyOf(data, position()) : data;
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
    
    public void emitAddress(Address addr) {
        data[position++] = addr;
    }
    
    public void emitInt(int val) {
        data[position++] = Address.fromInt(val);
    }
    
    Address getAddress() {
        assert position <= data.length - 1 : "Position is out of bounce";
        return data[position++].asAddress();
    }
    
    int getInt() {
        assert position <= data.length - 1 : "Position is out of bounce";
        return data[position++].toInt();
    }
    
    boolean ends() {
        return position > data.length - 1;
    }
    
    public String toString() {
        return "Direct Threaded Code of " + name;
    }
}
