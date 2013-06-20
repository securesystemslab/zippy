package org.python.ast.datatypes;

/**
 * 
 * PObject is the base class for all Truffle data types PObject contains
 * commonly used methods those should be shared by all Truffle data types
 * 
 */

public abstract class PObject {

    public abstract Object getMin();

    public abstract Object getMax();

    public abstract int len();

    public abstract Object multiply(int value);

    public abstract PCallable findAttribute(String name);
}
