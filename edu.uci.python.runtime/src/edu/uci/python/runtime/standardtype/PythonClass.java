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

import java.lang.invoke.*;
import java.util.*;

import org.python.util.*;

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
public class PythonClass extends FixedPythonObjectStorage implements PythonCallable {

    private final String className;
    private final PythonContext context;

    /**
     * TODO: Compute complete MRO...
     */
    @CompilationFinal private PythonClass[] baseClasses;
    @CompilationFinal private PythonClass[] methodResolutionOrder;

    /**
     * Object layout of the instances of this class.
     */
    @CompilationFinal private ObjectLayout instanceObjectLayout;
    @CompilationFinal private MethodHandle instanceConstructor;

    private final Set<PythonClass> subClasses = Collections.newSetFromMap(new WeakHashMap<PythonClass, Boolean>());

    public PythonClass(PythonContext context, String name, PythonClass... baseClasses) {
        super(context.getTypeClass());
        this.context = context;
        this.className = name;

        if (baseClasses.length == 0) {
            this.baseClasses = new PythonClass[]{context.getObjectClass()};
        } else if (baseClasses.length == 1 && baseClasses[0] == null) {
            this.baseClasses = new PythonClass[]{};
        } else {
            unsafeSetSuperClass(baseClasses);
        }

        // Compute MRO
        computeMethodResolutionOrder();

        // Does not inherit instanceObjectLayout from the TypeClass.
        setObjectLayout(ObjectLayout.empty());

        // Inherite InstanceObjectLayout when possible
        instanceObjectLayout = this.baseClasses.length == 0 ? ObjectLayout.empty() : new ObjectLayout(getName(), this.baseClasses[0].getInstanceObjectLayout());

        switchToPrivateLayout();

        // The default constructor creates a {@link FixedPythonObjectStorage} object.
        instanceConstructor = PythonContext.getDefaultPythonObjectConstructor();
    }

    public PythonClass getSuperClass() {
        return baseClasses.length > 0 ? baseClasses[0] : null;
    }

    public PythonClass[] getMethodResolutionOrder() {
        return methodResolutionOrder;
    }

    @Override
    public String getName() {
        return className;
    }

    public PythonContext getContext() {
        return context;
    }

    public final MethodHandle getInstanceConstructor() {
        return instanceConstructor;
    }

    private void computeMethodResolutionOrder() {
        PythonClass[] currentMRO = null;

        if (baseClasses.length == 0) {
            currentMRO = new PythonClass[]{this};
        } else if (baseClasses.length == 1) {
            PythonClass[] baseMRO = baseClasses[0].getMethodResolutionOrder();

            if (baseMRO == null) {
                currentMRO = new PythonClass[]{this};
            } else {
                currentMRO = new PythonClass[baseMRO.length + 1];
                System.arraycopy(baseMRO, 0, currentMRO, 1, baseMRO.length);
                currentMRO[0] = this;
            }
        } else {
            MROMergeState[] toMerge = new MROMergeState[baseClasses.length + 1];

            for (int i = 0; i < baseClasses.length; i++) {
                toMerge[i] = new MROMergeState();
                toMerge[i].mro = baseClasses[i].getMethodResolutionOrder();
            }

            toMerge[baseClasses.length] = new MROMergeState();
            toMerge[baseClasses.length].mro = baseClasses;
            List<PythonClass> mro = Generic.list();
            mro.add(this);
            currentMRO = mergeMROs(toMerge, mro);
        }

        methodResolutionOrder = currentMRO;
    }

    PythonClass[] mergeMROs(MROMergeState[] toMerge, List<PythonClass> mro) {
        int idx;
        scan: for (idx = 0; idx < toMerge.length; idx++) {
            if (toMerge[idx].isMerged()) {
                continue scan;
            }

            PythonClass candidate = toMerge[idx].getCandidate();
            for (MROMergeState mergee : toMerge) {
                if (mergee.pastnextContains(candidate)) {
                    continue scan;
                }
            }

            mro.add(candidate);

            for (MROMergeState element : toMerge) {
                element.noteMerged(candidate);
            }

            // restart scan
            idx = -1;
        }

        for (MROMergeState mergee : toMerge) {
            if (!mergee.isMerged()) {
                throw new IllegalStateException();
            }
        }

        return mro.toArray(new PythonClass[mro.size()]);
    }

    @Override
    public PythonObject getValidStorageFullLookup(String attributeId) {
        PythonObject storage = null;

        if (isOwnAttribute(attributeId)) {
            storage = this;
        } else if (baseClasses.length > 0) {
            storage = baseClasses[0].getValidStorageFullLookup(attributeId);
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
            return baseClasses.length == 0 ? PNone.NONE : baseClasses[0].getAttribute(name);
        }

        return storageLocation.read(this);
    }

    /**
     * This method supports initialization and solves boot-order problems and should not normally be
     * used.
     */
    public void unsafeSetSuperClass(PythonClass... newBaseClasses) {
        assert baseClasses == null || baseClasses.length == 0;
        baseClasses = newBaseClasses;

        for (PythonClass base : baseClasses) {
            if (base != null) {
                base.subClasses.add(this);
            }
        }
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

    public final void switchToGeneratedStorageClass() {
        CompilerDirectives.transferToInterpreterAndInvalidate();
        StorageClassGenerator scg = new StorageClassGenerator(this);
        GeneratedPythonObjectStorage newStorage = scg.generate();
        instanceConstructor = newStorage.getConstructor();
    }

    /**
     * The following are slow paths.
     */
    @Override
    public Object call(Object[] args) {
        PythonObject newInstance = PythonContext.newPythonObjectInstance(this);
        PythonCallable ctor = lookUpMethod("__init__");
        ctor.call(PArguments.insertSelf(args, newInstance));
        return newInstance;
    }

    @Override
    public Object call(Object[] args, PKeyword[] keywords) {
        PythonObject newInstance = PythonContext.newPythonObjectInstance(this);
        PythonCallable ctor = lookUpMethod("__init__");
        ctor.call(PArguments.insertSelf(args, newInstance));
        return newInstance;
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
    public String toString() {
        return "<class \'" + className + "\'>";
    }

}
