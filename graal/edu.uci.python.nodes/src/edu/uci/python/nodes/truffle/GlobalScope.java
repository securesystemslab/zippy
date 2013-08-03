/*
 * Copyright (c) 2013, Regents of the University of California
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met: 
 * 
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer. 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution. 
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package edu.uci.python.nodes.truffle;

import java.util.*;

import org.python.core.*;

import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.frame.FrameSlotKind;
import com.oracle.truffle.api.frame.FrameSlotTypeException;
import com.oracle.truffle.api.frame.FrameUtil;
import com.oracle.truffle.api.frame.MaterializedFrame;

import edu.uci.python.runtime.modules.*;

public final class GlobalScope {

    PyStringMap jythonGlobals;

    PyStringMap jythonBuiltins;

    private final MaterializedFrame cachedGlobalFrame;

    private static GlobalScope instance;

    private static final BuiltInModule truffleBuiltIns = new BuiltInModule();

    /**
     * Augment PyStringMap to accommodate non-PyObject types. <br>
     * TODO: this should be gone!
     */
    Map<String, Object> scopeTable = new HashMap<>();

    private GlobalScope(PyStringMap globals, MaterializedFrame frame) {
        jythonGlobals = globals;
        jythonBuiltins = (PyStringMap) Py.getSystemState().getBuiltins();
        cachedGlobalFrame = frame;
    }

    protected static GlobalScope create(PyStringMap globals) {
        instance = new GlobalScope(globals, null);
        return instance;
    }

    public static GlobalScope getInstance() {
        if (instance == null) {
            throw new RuntimeException("Not created yet!");
        } else {
            return instance;
        }
    }

    public static GlobalScope getInstance(MaterializedFrame frame) {
        if (instance == null) {
            throw new RuntimeException("Not created yet!");
        } else {
            instance = new GlobalScope(instance.jythonGlobals, frame);
            return instance;
        }
    }

    public static BuiltInModule getTruffleBuiltIns() {
        return truffleBuiltIns;
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
            FrameSlotKind slotKind = cached.getKind();
            try {
                /**
                 * getObject checks if the accessKind is the same as the slotKind Different slot
                 * kinds need to be checked
                 */
                if (slotKind == FrameSlotKind.Int) {
                    return cachedGlobalFrame.getInt(cached);
                } else if (slotKind == FrameSlotKind.Boolean) {
                    return cachedGlobalFrame.getBoolean(cached);
                } else if (slotKind == FrameSlotKind.Double) {
                    return cachedGlobalFrame.getDouble(cached);
                } else if (slotKind == FrameSlotKind.Object) {
                    return cachedGlobalFrame.getObject(cached);
                }
            } catch (FrameSlotTypeException e) {
                throw new IllegalStateException();
            }
        }

        Object truffleBuiltIn = truffleBuiltIns.lookup(name);
        if (truffleBuiltIn != null) {
            return truffleBuiltIn;
        }

        Object ret = tryGetTruffleObject(name);
        if (ret != null) {
            return ret;
        }

        return PySystemState.builtins.__finditem__(name);
    }

    public void set(String name, Object value) {
        FrameSlot cached = findCachedGlobalFrameSlot(name);

        if (cached != null) {
            try {
                cachedGlobalFrame.setObject(cached, value);
            } catch (FrameSlotTypeException e) {
                FrameUtil.setObjectSafe(cachedGlobalFrame, cached, value);
            }
        } else {
            setTruffleOrJythonObject(name, PythonTypesUtil.adaptToPyObject(value));
        }
    }

    public FrameSlot findCachedGlobalFrameSlot(String id) {
        return cachedGlobalFrame.getFrameDescriptor().findFrameSlot(id);
    }

    /*
     * Should only use when setting non-PyObject values
     */
    public Object tryGetTruffleObject(String key) {
        Object val = scopeTable.get(key);

        if (val == null) {
            val = jythonGlobals.__finditem__(key);
        }
        return val;
    }

    boolean trySetTruffleObject(String key, Object value) {
        if (scopeTable.containsKey(key)) {
            scopeTable.put(key, value);
            return true;
        }

        return false;
    }

    public void setTruffleOrJythonObject(String key, Object value) {
        if (!trySetTruffleObject(key, value)) {
            jythonGlobals.__setitem__(key, (PyObject) value);
        }
    }

}
