package edu.uci.python.runtime.objects;

/**
 * A singleton instance of this class represents an undefined Python value.
 */
public final class Undefined {

    public static final Undefined instance = new Undefined();

    public static final String name = "undefined";

    private Undefined() {
    }

    @Override
    public String toString() {
        return name;
    }
}
