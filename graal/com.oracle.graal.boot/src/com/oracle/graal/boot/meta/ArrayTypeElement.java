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

import java.util.*;

import com.oracle.graal.api.meta.*;
import com.oracle.graal.boot.*;
import com.oracle.graal.graph.*;
import com.oracle.graal.nodes.*;
import com.oracle.graal.nodes.java.*;


public class ArrayTypeElement extends Element {

    private ResolvedJavaType javaType;

    public ArrayTypeElement(ResolvedJavaType javaType) {
        super(javaType.componentType());
        this.javaType = javaType;
    }

    @Override
    public String toString() {
        return "arrayTypeElement: " + javaType;
    }

    @Override
    protected void propagateTypesToUsage(BigBang bb, Node use, Set<ResolvedJavaType> set, Element element) {
        LoadIndexedNode load = (LoadIndexedNode) use;
        ResolvedJavaType type = load.array().objectStamp().type();
        if (type == null) {
            System.out.println("FATAL error: Array access without type!");
            System.out.println(load.array());
            if (load.array() instanceof ValueProxyNode) {
                ValueProxyNode valueProxyNode = (ValueProxyNode) load.array();
                System.out.println("value proxy node stamp " + valueProxyNode.stamp());
                System.out.println("value proxy node stamp type " + valueProxyNode.objectStamp().type());
                System.out.println("value proxy source: " + valueProxyNode.value());
                System.out.println("value proxy source stamp: " + valueProxyNode.value().stamp());
            }
            System.out.println(((StructuredGraph) load.graph()).method());
            System.exit(-1);
        }
        ResolvedJavaType componentType = type.componentType();
        Set<ResolvedJavaType> newSet = new HashSet<>();
        for (ResolvedJavaType myType : set) {
            if (myType.isSubtypeOf(componentType)) {
                newSet.add(myType);
            }
        }
        if (newSet.size() > 0) {
            super.propagateTypesToUsage(bb, use, newSet, element);
        }
    }
}
