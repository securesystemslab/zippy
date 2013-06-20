package org.python.ast.datatypes;

public class PKeyword {

    private final Object value;

    private final String name;

    public PKeyword(Object value, String object) {
        this.value = value;
        this.name = object;
    }

    public String getName() {
        return name;
    }

    public Object getValue() {
        return value;
    }

}
