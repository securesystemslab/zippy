package org.python.ast.datatypes;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.python.modules.truffle.StringAttribute;

public class PString extends PObject implements Iterable<Object> {
    private final String value;

    private static StringAttribute stringModule = new StringAttribute();

    public PString(String value) {
        this.value = value;
    }

    public PCallable findAttribute(String name) {
        PCallable method = stringModule.lookupMethod(name);
        method.setSelf(value);
        return method;
    }

    @Override
    public Object getMin() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object getMax() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int len() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object multiply(int value) {
        throw new UnsupportedOperationException();
    }
    
    public List<String> getList() {
        ArrayList<String> list = new ArrayList<String>();
        
        char[] array = value.toCharArray();
        for (int i = 0; i < array.length; i++) {
            list.add(String.valueOf(array[i]));
        }
        
        return list;
    }

    @Override
    public Iterator<Object> iterator() {
        return new Iterator<Object>() {

            private final Iterator<String> iter = getList().iterator();

            public void remove() {
                throw new UnsupportedOperationException();
            }

            public boolean hasNext() {
                return iter.hasNext();
            }

            public Object next() {
                return iter.next();
            }
        };
    }
}
