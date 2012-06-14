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
package com.oracle.graal.boot;

import java.lang.reflect.*;

import com.oracle.graal.api.*;
import com.oracle.graal.api.meta.*;
import com.oracle.graal.debug.*;


public class BootImageGenerator {

    private final BootImageClassLoader classLoader = new BootImageClassLoader();
    private final MetaAccessProvider metaAccess = Graal.getRequiredCapability(MetaAccessProvider.class);

    public void addEntryMethod(Class<?> clazz, String name, Class<?> ... parameterTypes) {
        Class<?> convertedClass = classLoader.convert(clazz);
        Method method;
        try {
            method = convertedClass.getDeclaredMethod(name, parameterTypes);
        } catch (NoSuchMethodException | SecurityException e) {
            throw new RuntimeException("Could not find method " + name + " with parameter types " + parameterTypes + " in class " + convertedClass.getCanonicalName());
        }
        Debug.log("Adding method %s.%s to the boot image", method.getClass().getName(), method.getName());
        addEntryMethod(metaAccess.getResolvedJavaMethod(method));
    }


    private void addEntryMethod(ResolvedJavaMethod javaMethod) {

    }


    public void logState() {
        Debug.log("State");
    }
}
