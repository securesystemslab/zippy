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

import java.io.*;

public class BootImageClassLoader extends ClassLoader {

    @Override
    protected java.lang.Class< ? > loadClass(String name, boolean resolve) throws ClassNotFoundException {
        synchronized (getClassLoadingLock(name)) {
            Class< ? > result = findLoadedClass(name);
            if (result == null) {
                result = super.loadClass(name, resolve);
                assert result.getName().equals(name);
                return duplicate(result);
            }
            return result;
        }
    }

    private Class< ? > duplicate(Class< ? > result) {
        // This is a class in the bootclasspath => share.
        if (result.getClassLoader() == null) {
            return result;
        }

        // Duplicate class definition.
        InputStream inputStream = result.getClassLoader().getResourceAsStream(result.getName().replace('.', '/').concat(".class"));
        try {
            byte[] byteCodes = new byte[inputStream.available()];
            inputStream.read(byteCodes);
            return this.defineClass(result.getName(), byteCodes, 0, byteCodes.length);
        } catch (IOException e) {
            throw new RuntimeException("Could not access class bytes for " + result.getName());
        }
    }

    public Class< ? > convert(Class< ? > clazz) {
        synchronized (getClassLoadingLock(clazz.getCanonicalName())) {
            // This class has this class loader => no conversion necessary.
            if (clazz.getClassLoader() == this) {
                return clazz;
            }

            Class< ? > thisClazz = findLoadedClass(clazz.getName());
            if (thisClazz != null) {
                return thisClazz;
            }

            return duplicate(clazz);
        }
    }
}
