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

import java.lang.reflect.*;
import java.util.*;

import com.oracle.truffle.api.*;
import com.oracle.truffle.api.utilities.*;

import edu.uci.python.runtime.*;
import edu.uci.python.runtime.datatype.*;
import edu.uci.python.runtime.module.annotation.*;
import edu.uci.python.runtime.object.*;

public class PythonModule extends PythonBasicObject {

    public static final String __NAME__ = "__name__";

    private final String name;
    private final List<AnnotatedBuiltinConstant> builtinConstants = new ArrayList<>();

    private final CyclicAssumption unmodifiedAssumption;

    private PythonContext context;

    public PythonModule(String name, PythonContext context) {
        super(context.getModuleClass());
        this.context = context;
        this.name = name;
        unmodifiedAssumption = new CyclicAssumption("unmodified");
        setAttribute(__NAME__, name);
        addBuiltinConstants(PythonModule.class);
    }

    public PythonModule(String name, PythonContext context, PythonModule builtins) {
        super(context.getModuleClass());
        this.context = context;
        this.name = name;
        unmodifiedAssumption = new CyclicAssumption("unmodified");
        setAttribute(__NAME__, name);

        setAttribute("__builtins__", builtins);

        context.getPythonBuiltinsLookup().addModule(name, this);
    }

    public PythonModule(String name, PythonModule module) {
        super(module.context.getModuleClass(), module);
        unmodifiedAssumption = module.unmodifiedAssumption;
        this.context = module.context;
        this.name = name;
        setAttribute(__NAME__, name);
        builtinConstants.addAll(module.builtinConstants);
    }

    @Override
    public Assumption getUnmodifiedAssumption() {
        return unmodifiedAssumption.getAssumption();
    }

    private void addBuiltinConstants(Class definingClass) {
        findBuiltinConstantsUsingReflection(definingClass);

        for (AnnotatedBuiltinConstant constant : builtinConstants) {
            Object value = constant.getValue();
            if (getAttribute(constant.getName()) == PNone.NONE) {
                setAttribute(constant.getName(), value);
            }
        }
    }

    private void findBuiltinConstantsUsingReflection(Class definingClass) {
        for (Field field : definingClass.getDeclaredFields()) {
            if (field.getAnnotation(BuiltinConstant.class) != null) {
                builtinConstants.add(buildConstant(field));
            }
        }
    }

    public static AnnotatedBuiltinConstant buildConstant(Field field) {
        assert Modifier.isPublic(field.getModifiers()) && Modifier.isStatic(field.getModifiers()) && Modifier.isFinal(field.getModifiers());
        assert field.getAnnotation(BuiltinConstant.class) != null;

        try {
            return new AnnotatedBuiltinConstant(field.getName(), field.get(null));
        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public String getModuleName() {
        return name;
    }

    @Override
    public void setAttribute(String name, Object value) {
        assert value != null;
        unmodifiedAssumption.invalidate();
        super.setAttribute(name, value);
    }

    @Override
    public String toString() {
        return "<module '" + this.getAttribute(__NAME__) + "'>";
    }

    /**
     * The default constant values of Python modules.
     */
    @BuiltinConstant public static final Object __name__ = PNone.NONE;

    @BuiltinConstant public static final Object __doc__ = PNone.NONE;

    @BuiltinConstant public static final Object __package__ = PNone.NONE;
}
