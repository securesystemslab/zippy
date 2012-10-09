/*
 * Copyright (c) 2012, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package com.oracle.graal.boot.meta;

import java.lang.reflect.*;

import com.oracle.graal.api.meta.*;


public class ParameterElement extends Element {

    private int index;
    private ResolvedJavaMethod method;

    public ParameterElement(ResolvedJavaMethod method, int index) {
        super(calculateDeclaredType(method, index));
        this.method = method;
        this.index = index;
    }

    private static ResolvedJavaType calculateDeclaredType(ResolvedJavaMethod m, int i) {
        if (Modifier.isStatic(m.getModifiers())) {
            return m.getSignature().getParameterType(i, m.getDeclaringClass()).resolve(m.getDeclaringClass());
        } else {
            if (i == 0) {
                return m.getDeclaringClass();
            }
            return m.getSignature().getParameterType(i - 1, m.getDeclaringClass()).resolve(m.getDeclaringClass());
        }
    }

    @Override
    public String toString() {
        return "[Parameter, index= " + index + " of method " + method + "]";
    }

}
