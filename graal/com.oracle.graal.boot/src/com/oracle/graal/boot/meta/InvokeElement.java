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


public class InvokeElement extends Element {

    private MethodCallTargetNode methodCallTarget;
    private Set<ResolvedJavaMethod> concreteTargets = new HashSet<>();
    private Set<ResolvedJavaType>[] parameterTypes;

    @SuppressWarnings("unchecked")
    public InvokeElement(MethodCallTargetNode methodCallTarget) {
        super(methodCallTarget.isStatic() ? null : methodCallTarget.targetMethod().holder());
        this.methodCallTarget = methodCallTarget;
        parameterTypes = new Set[methodCallTarget.arguments().size()];
    }

    @Override
    protected synchronized void unionTypes(BigBang bb, Node sourceNode, Set<ResolvedJavaType> newSeenTypes) {

        System.out.println("union invoke element " + this + " new types = " + newSeenTypes + " sourceNode= " + sourceNode);
        int index = 0;
        for (Node arg : methodCallTarget.arguments()) {
            if (arg == sourceNode) {
                System.out.println("source node " + sourceNode + " is at index " + index + " declaredType=" + ((ValueNode) sourceNode).stamp().declaredType());
                unionTypes(bb, sourceNode, newSeenTypes, index);
            }
            ++index;
        }
    }

    @Override
    public String toString() {
        return "Invoke[bci=" + methodCallTarget.invoke().stateAfter().method() + "," + methodCallTarget.targetMethod() + "]";
    }

    public synchronized void expandStaticMethod(BigBang bb) {
        if (methodCallTarget.isStatic()) {
            ResolvedJavaMethod method = methodCallTarget.targetMethod();
            concreteTargets.add(method);
            MethodElement processedMethod = bb.getProcessedMethod(method);
            processedMethod.postParseGraph(bb);
        }
    }

    private void unionTypes(BigBang bb, @SuppressWarnings("unused") Node sourceNode, Set<ResolvedJavaType> newSeenTypes, int index) {
        if (index == 0 && !methodCallTarget.isStatic()) {
            for (ResolvedJavaType type : newSeenTypes) {
                if (seenTypes.add(type)) {
                    // There is a new receiver type!
                    ResolvedJavaMethod method = type.resolveMethodImpl(methodCallTarget.targetMethod());
                    System.out.println("resolved method " + method + " for type " + type + " and method " + methodCallTarget.targetMethod());
                    if (method == null) {
                        System.out.println("!!! type = " + type + " / " + methodCallTarget.targetMethod());
                    }
                    if (!concreteTargets.contains(method)) {
                        concreteTargets.add(method);
                        // New concrete method.
                        MethodElement processedMethod = bb.getProcessedMethod(method);
                        processedMethod.postParseGraph(bb);
                        // Propagate types that were previously found for the parameters.
                        for (int i = 0; i < parameterTypes.length; ++i) {
                            if (parameterTypes[i] != null) {
                                HashSet<ResolvedJavaType> newSeenTypesTemp = new HashSet<>(parameterTypes[i]);
                                bb.getProcessedMethod(method).getParameter(i).postUnionTypes(bb, null, newSeenTypesTemp);
                            }
                        }
                    }

                    // Register new type for receiver.
                    HashSet<ResolvedJavaType> newSeenTypesTemp = new HashSet<>();
                    newSeenTypesTemp.add(type);
                    bb.getProcessedMethod(method).getParameter(index).postUnionTypes(bb, null, newSeenTypesTemp);
                }
            }
        } else {
            if (parameterTypes[index] == null) {
                parameterTypes[index] = new HashSet<>();
            }
            if (parameterTypes[index].addAll(newSeenTypes)) {
                for (ResolvedJavaMethod method : concreteTargets) {
                    HashSet<ResolvedJavaType> newSeenTypesTemp = new HashSet<>(newSeenTypes);
                    bb.getProcessedMethod(method).getParameter(index).postUnionTypes(bb, null, newSeenTypesTemp);
                }
            }
        }
    }
}
