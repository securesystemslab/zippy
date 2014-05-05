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
package edu.uci.python.runtime.standardtype;

import java.util.*;

import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.*;
import com.oracle.truffle.api.frame.*;

import edu.uci.python.runtime.*;
import edu.uci.python.runtime.datatype.*;
import edu.uci.python.runtime.function.*;
import edu.uci.python.runtime.object.*;

/**
 * Mutable class.
 */
public class PythonClass extends PythonObject implements PythonCallable {

    private final String className;
    private final PythonContext context;

    // TODO: Multiple inheritance and MRO...
    @CompilationFinal private PythonClass superClass;

    private final Set<PythonClass> subClasses = Collections.newSetFromMap(new WeakHashMap<PythonClass, Boolean>());

    /**
     * Object layout of the instances of this class.
     */
    @CompilationFinal private ObjectLayout instanceObjectLayout;

    public PythonClass(PythonClass superClass, String name) {
        this(superClass.getContext(), superClass, name);
    }

    /**
     * This constructor supports initialization and solves boot-order problems and should not
     * normally be used from outside this class.
     */
    public PythonClass(PythonContext context, PythonClass superClass, String name) {
        super(context.getTypeClass());
        this.context = context;
        this.className = name;

        if (superClass == null) {
            this.superClass = context.getObjectClass();
        } else {
            unsafeSetSuperClass(superClass);
        }

        // Does not inherit instanceObjectLayout from the TypeClass.
        setObjectLayout(ObjectLayout.empty());

        // Inherite InstanceObjectLayout when possible
        instanceObjectLayout = superClass == null ? ObjectLayout.empty() : new ObjectLayout(getName(), superClass.getInstanceObjectLayout());

        switchToPrivateLayout();
    }

    public PythonClass getSuperClass() {
        return superClass;
    }

    @Override
    public String getName() {
        return className;
    }

    public PythonContext getContext() {
        return context;
    }

    @Override
    public PythonObject getValidStorageFullLookup(String attributeId) {
        PythonObject storage = null;

        if (isOwnAttribute(attributeId)) {
            storage = this;
        } else if (superClass != null) {
            storage = superClass.getValidStorageFullLookup(attributeId);
        }

        return storage;
    }

    public PythonCallable lookUpMethod(String methodName) {
        Object attr = getAttribute(methodName);
        assert attr != null;

        if (attr instanceof PythonCallable) {
            return (PythonCallable) attr;
        }

        return null;
    }

    public void addMethod(PFunction method) {
        setAttribute(method.getName(), method);
    }

    @Override
    public Object getAttribute(String name) {
        // Find the storage location
        final StorageLocation storageLocation = getObjectLayout().findStorageLocation(name);

        // Continue the look up in PythonType.
        if (storageLocation == null) {
            return superClass == null ? PNone.NONE : superClass.getAttribute(name);
        }

        return storageLocation.read(this);
    }

    /**
     * This method supports initialization and solves boot-order problems and should not normally be
     * used.
     */
    public void unsafeSetSuperClass(PythonClass newSuperClass) {
        assert superClass == null;
        superClass = newSuperClass;
        superClass.subClasses.add(this);
    }

    public final Set<PythonClass> getSubClasses() {
        return subClasses;
    }

    public final ObjectLayout getInstanceObjectLayout() {
        return instanceObjectLayout;
    }

    public final void updateInstanceObjectLayout(ObjectLayout newLayout) {
        this.instanceObjectLayout = newLayout;
    }

    /**
     * The following are slow paths.
     */
    @Override
    public Object call(PackedFrame caller, Object[] args) {
        PythonObject newInstance = new PythonObject(this);
        PythonCallable ctor = lookUpMethod("__init__");
        ctor.call(caller, packSelfWithArguments(newInstance, args));
        return newInstance;
    }

    @Override
    public Object call(PackedFrame caller, Object[] args, PKeyword[] keywords) {
        PythonObject newInstance = new PythonObject(this);
        PythonCallable ctor = lookUpMethod("__init__");
        ctor.call(caller, packSelfWithArguments(newInstance, args), keywords);
        return newInstance;
    }

    private static Object[] packSelfWithArguments(PythonObject self, Object[] arguments) {
        Object[] packed = new Object[arguments.length + 1];
        packed[0] = self;

        for (int i = 0; i < arguments.length; i++) {
            packed[i + 1] = arguments[i];
        }

        return packed;
    }

    @Override
    public Arity getArity() {
        PythonCallable ctor = lookUpMethod("__init__");
        return ctor.getArity();
    }

    @Override
    public void arityCheck(int numOfArgs, int numOfKeywords, String[] keywords) {
        PythonCallable ctor = lookUpMethod("__init__");
        ctor.arityCheck(numOfArgs, numOfKeywords, keywords);
    }

    @Override
    public RootCallTarget getCallTarget() {
        PythonCallable ctor = lookUpMethod("__init__");
        return ctor.getCallTarget();
    }

    @Override
    public FrameDescriptor getFrameDescriptor() {
        PythonCallable ctor = lookUpMethod("__init__");
        return ctor.getFrameDescriptor();
    }

    @Override
    public String getCallableName() {
        return getName();
    }

    @Override
    public String toString() {
        return "<class \'" + className + "\'>";
    }

}
