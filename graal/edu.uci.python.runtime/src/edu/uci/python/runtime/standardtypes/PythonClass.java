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
package edu.uci.python.runtime.standardtypes;

import java.util.*;

import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;

import edu.uci.python.runtime.*;
import edu.uci.python.runtime.datatypes.*;
import edu.uci.python.runtime.objects.*;

public class PythonClass extends PythonBasicObject {

    private final String name;

    @CompilationFinal private PythonClass superClass;

    private final Set<PythonClass> subClasses = Collections.newSetFromMap(new WeakHashMap<PythonClass, Boolean>());

    private ObjectLayout objectLayoutForInstances = null;

    // The context is stored here - objects can obtain it via their class (which is a module)
    private final PythonContext context;

    private final Map<String, PFunction> methods = new HashMap<>();

    public PythonClass(PythonClass superClass, String name) {
        this(superClass.getContext(), superClass, name);
    }

    /**
     * This constructor supports initialization and solves boot-order problems and should not
     * normally be used from outside this class.
     */
    public PythonClass(PythonContext context, PythonClass superClass, String name) {
        super(null); // TODO: should really pass the class class
        this.context = context;
        this.name = name;

        if (superClass == null) {
            objectLayoutForInstances = ObjectLayout.EMPTY;
        } else {
            unsafeSetSuperclass(superClass);
        }
    }

    public PythonClass getSuperClass() {
        assert superClass != null;
        return superClass;
    }

    public String getName() {
        return name;
    }

    public PythonContext getContext() {
        return context;
    }

    public PFunction lookUpMethod(String methodName) {
        final PFunction method = methods.get(methodName);
        assert method != null;
        return method;
    }

    public void addMethod(PFunction method) {
        methods.put(method.getName(), method);
    }

    /**
     * Returns the object layout that objects of this class should use. Do not confuse with // *
     * {#getObjectLayout}, which for {@link PythonClass} will return the layout of the class object
     * itself.
     */
    public ObjectLayout getObjectLayoutForInstances() {
        return objectLayoutForInstances;
    }

    public void setObjectLayoutForInstances(ObjectLayout newObjectLayoutForInstances) {
        objectLayoutForInstances = newObjectLayoutForInstances;

        for (PythonClass subClass : subClasses) {
            subClass.renewObjectLayoutForInstances();
        }
    }

    private void renewObjectLayoutForInstances() {
        objectLayoutForInstances = objectLayoutForInstances.renew(getContext(), superClass.objectLayoutForInstances);

        for (PythonClass subClass : subClasses) {
            subClass.renewObjectLayoutForInstances();
        }
    }

    /**
     * This method supports initialization and solves boot-order problems and should not normally be
     * used.
     */
    public void unsafeSetSuperclass(PythonClass newSuperClass) {
        assert superClass == null;
        superClass = newSuperClass;
        superClass.subClasses.add(this);
        objectLayoutForInstances = new ObjectLayout(getName(), getContext(), superClass.objectLayoutForInstances);
    }

    @Override
    public String toString() {
        return "<class \'" + name + "\'>";
    }

}
