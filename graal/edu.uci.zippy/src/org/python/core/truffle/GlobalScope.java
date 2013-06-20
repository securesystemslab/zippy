package org.python.core.truffle;

import org.python.core.Options;
import org.python.core.Py;
import org.python.core.PyStringMap;
import org.python.core.PySystemState;
import org.python.modules.truffle.BuiltInModule;

import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.frame.VirtualFrame;

public class GlobalScope {

    PyStringMap jythonGlobals;

    PyStringMap jythonBuiltins;

    private VirtualFrame cachedGlobalFrame;

    private static GlobalScope instance;
    
    private final static BuiltInModule truffleBuiltIns = new BuiltInModule();
    
    private GlobalScope(PyStringMap globals) {
        jythonGlobals = globals;
        jythonBuiltins = (PyStringMap) Py.getSystemState().getBuiltins();        
    }

    protected static GlobalScope create(PyStringMap globals) {
        instance = new GlobalScope(globals);
        return instance;
    }

    public static GlobalScope getInstance() {
        if (instance == null) {
            throw new RuntimeException("Not created yet!");
        } else {
            return instance;
        }
    }
    
    public static BuiltInModule getTruffleBuiltIns() {
        return truffleBuiltIns;
    }

    public void setCachedGlobalFrame(VirtualFrame frame) {
        cachedGlobalFrame = frame;
    }

    public boolean isGlobalOrBuiltin(String name) {
        if (jythonGlobals.has_key(name)) {
            return true;
        }

        if (PySystemState.builtins.__finditem__(name) != null) {
            return true;
        }

        return false;
    }

    public Object get(String name) {
        FrameSlot cached = findCachedGlobalFrameSlot(name);

        if (cached != null) {
            return cachedGlobalFrame.getObject(cached);
        }
        
        Object truffleBuiltIn = truffleBuiltIns.lookup(name);
        if (truffleBuiltIn != null) {
            return truffleBuiltIn;
        }

        Object ret = jythonGlobals.tryGetTruffleObject(name);
        if (ret != null) {
            return ret;
        }
        
        return PySystemState.builtins.__finditem__(name);
    }

    public void set(String name, Object value) {
        FrameSlot cached = findCachedGlobalFrameSlot(name);

        if (cached != null) {
            cachedGlobalFrame.setObject(cached, value);
        } else {

            if (Options.specialize) {
                value = PythonTypesUtil.adaptToPyObject(value);
            }

            jythonGlobals.setTruffleOrJythonObject(name, value);
        }
    }

    public FrameSlot findCachedGlobalFrameSlot(String id) {
        return cachedGlobalFrame.getFrameDescriptor().findFrameSlot(id);
    }

}
