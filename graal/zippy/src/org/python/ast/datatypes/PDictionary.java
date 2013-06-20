package org.python.ast.datatypes;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.python.modules.truffle.DictionaryAttribute;

public class PDictionary extends PObject {

    private final Map<Object, Object> map;

    static final public DictionaryAttribute dictModule = new DictionaryAttribute();

    public PDictionary() {
        map = new ConcurrentHashMap<Object, Object>();
    }

    public PDictionary(Map<Object, Object> map) {
        this();
        this.map.putAll(map);
    }

    public Object getItem(Object key) {
        return map.get(key);
    }

    public void setItem(Object key, Object value) {
        map.put(key, value);
    }

    public void delItem(Object key) {
        map.remove(key);
    }

    public Collection<Object> items() {
        return map.values();
    }

    public Collection<Object> keys() {
        return map.keySet();
    }

    public Map<Object, Object> getMap() {
        return map;
    }

    public boolean hasKey(Object[] args) {
        if (args.length == 1) {
            return this.map.containsKey(args[0]);
        } else {
            throw new RuntimeException("invalid arguments for has_key()");
        }
    }

    public PCallable findAttribute(String name) {
        PCallable method = dictModule.lookupMethod(name);
        method.setSelf(this);
        return method;    
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder("{");
        int length = map.size();
        int i = 0;

        for (Object key : map.keySet()) {
            buf.append(key.toString() + " : " + map.get(key));

            if (i < length - 1) {
                buf.append(", ");
            }

            i++;
        }

        buf.append("}");
        return buf.toString();
    }

    @Override
    public Object getMin() {
        throw new RuntimeException("unimplemented"); 
    }

    @Override
    public Object getMax() {
        throw new RuntimeException("unimplemented"); 

    }

    @Override
    public int len() {
        return map.size();
    }

    @Override
    public Object multiply(int value) {
        throw new RuntimeException("unimplemented"); 
    }

}
