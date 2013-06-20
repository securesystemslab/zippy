package org.python.ast.datatypes;

import org.python.core.PyObject;

/**
 * The purpose of this class is to pack non-PyObject objects in Jython's built-in collection types.
 * Like PyTuple, PyList.
 * Since at this point, our runtime object model heavily rely on the existing code of Jython, this 
 * is an easy way to hide Truffle object in Jython's built-in collections.
 * 
 * Later on, this hack should be gone, where we should primarly rely on our own object model.
 * @author zwei
 *
 */

@SuppressWarnings("serial")
public class PyObjectContainer extends PyObject {

    final Object innerObject;
    
    public PyObjectContainer(Object inner) {
        super();
        innerObject = inner;
    }
    
    @Override
    public boolean isContainer() {
        return true;
    }
    
    public static PyObject pack(Object object) {
        return new PyObjectContainer(object);
    }
    
    public Object unpack() {
        return innerObject;
    }
    
}
