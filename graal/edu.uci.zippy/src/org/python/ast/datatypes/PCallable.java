package org.python.ast.datatypes;

import com.oracle.truffle.api.frame.*;

public abstract class PCallable {

    protected String name;

    protected Object self = null;

    public PCallable(String name) {
        this.name = name;
    }

    public Object call(PackedFrame caller, Object[] args) {
        return call(caller, args, null);
    }

    public abstract Object call(PackedFrame caller, Object[] args, Object[] keywords);

    // Specialized. To be overwritten by PFunction
    public Object call(PackedFrame caller, Object arg) {
        return call(caller, new Object[] { arg });
    }

    public Object call(PackedFrame caller, Object arg0, Object arg1) {
        return call(caller, new Object[] { arg0, arg1 });
    }

    public void setSelf(Object self) {
        this.self = self;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + " " + name;
    }

}
