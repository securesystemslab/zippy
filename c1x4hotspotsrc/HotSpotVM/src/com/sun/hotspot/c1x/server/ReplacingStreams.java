/*
 * Copyright (c) 2011 Sun Microsystems, Inc.  All rights reserved.
 *
 * Sun Microsystems, Inc. has intellectual property rights relating to technology embodied in the product
 * that is described in this document. In particular, and without limitation, these intellectual property
 * rights may include one or more of the U.S. patents listed at http://www.sun.com/patents and one or
 * more additional patents or pending patent applications in the U.S. and in other countries.
 *
 * U.S. Government Rights - Commercial software. Government users are subject to the Sun
 * Microsystems, Inc. standard license agreement and applicable provisions of the FAR and its
 * supplements.
 *
 * Use is subject to license terms. Sun, Sun Microsystems, the Sun logo, Java and Solaris are trademarks or
 * registered trademarks of Sun Microsystems, Inc. in the U.S. and other countries. All SPARC trademarks
 * are used under license and are trademarks or registered trademarks of SPARC International, Inc. in the
 * U.S. and other countries.
 *
 * UNIX is a registered trademark in the U.S. and other countries, exclusively licensed through X/Open
 * Company, Ltd.
 */
package com.sun.hotspot.c1x.server;

import java.io.*;
import java.util.*;

import com.sun.cri.ci.*;
import com.sun.hotspot.c1x.Compiler;

public class ReplacingStreams {
    IdentityHashMap<Object, Long> objectMap = new IdentityHashMap<Object, Long>();
    ArrayList<Object> objectList = new ArrayList<Object>();

    public static class Container implements Serializable {

        public final Class<?> clazz;
        public final Object[] values;

        public Container(Class<?> clazz, Object... values) {
            this.clazz = clazz;
            this.values = values;
        }
    }

    public static enum PlaceholderType {
        CI_CONSTANT_CONTENTS, RI_TYPE
    }

    public static class Placeholder implements Serializable {

        public final int id;
        public final PlaceholderType type;

        public Placeholder(int id, PlaceholderType type) {
            this.id = id;
            this.type = type;
        }

    }

    /**
     * Replaces certain cir objects that cannot easily be made Serializable.
     */
    public class ReplacingInputStream extends ObjectInputStream {

        private Compiler compiler;

        public ReplacingInputStream(InputStream in) throws IOException {
            super(in);
            enableResolveObject(true);
        }

        public void setCompiler(Compiler compiler) {
            this.compiler = compiler;
        }

        @Override
        protected Object resolveObject(Object obj) throws IOException {
            if (obj instanceof Container) {
                Container c = (Container) obj;
                if (c.clazz == CiConstant.class) {
                    return CiConstant.forBoxed((CiKind) c.values[0], c.values[1]);
                } else if (c.clazz == CiValue.class) {
                    return CiValue.IllegalValue;
                } else if (c.clazz == Compiler.class) {
                    assert compiler != null;
                    return compiler;
                }
                throw new RuntimeException("unexpected container class: " + c.clazz);
            } /*else if (obj instanceof Placeholder) {
                Placeholder ph = (Placeholder)obj;
                if (ph.id >= objectList.size()) {
                    assert ph.id == objectList.size();
                    switch (ph.type) {
                        case CI_CONSTANT_CONTENTS:
                            objectList.add(ph);
                            break;
                        case RI_TYPE:
                            objectList.add(e)
                            break;
                    }
                }
                return objectList.get(ph.id);

            }*/
            return obj;
        }
    }

    /**
     * Replaces certain cir objects that cannot easily be made Serializable.
     */
    public class ReplacingOutputStream extends ObjectOutputStream {

        public ReplacingOutputStream(OutputStream out) throws IOException {
            super(out);
            enableReplaceObject(true);
        }

        @Override
        protected Object replaceObject(Object obj) throws IOException {
            Class<? extends Object> clazz = obj.getClass();
            if (clazz == CiConstant.class) {
                CiConstant o = (CiConstant) obj;
                return new Container(clazz, o.kind, o.boxedValue());
            } else if (obj == CiValue.IllegalValue) {
                return new Container(CiValue.class);
            } else if (obj instanceof Compiler) {
                return new Container(Compiler.class);
            }
            return obj;
        }
    }

}
