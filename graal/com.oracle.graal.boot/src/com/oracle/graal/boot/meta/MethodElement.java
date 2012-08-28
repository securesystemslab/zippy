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
import java.util.*;

import com.oracle.graal.api.meta.*;
import com.oracle.graal.boot.*;
import com.oracle.graal.compiler.*;
import com.oracle.graal.compiler.phases.*;
import com.oracle.graal.graph.*;
import com.oracle.graal.java.*;
import com.oracle.graal.java.GraphBuilderConfiguration.*;
import com.oracle.graal.nodes.*;
import com.oracle.graal.nodes.java.*;


public class MethodElement extends Element {

    private ParameterElement[] parameters;
    private Graph graph;
    private ResolvedJavaMethod resolvedJavaMethod;

    public MethodElement(ResolvedJavaMethod javaMethod) {
        super(javaMethod.signature().returnType(javaMethod.holder()).resolve(javaMethod.holder()));
        assert javaMethod != null;
        this.resolvedJavaMethod = javaMethod;
        int parameterCount = resolvedJavaMethod.signature().argumentCount(!Modifier.isStatic(resolvedJavaMethod.accessFlags()));
        parameters = new ParameterElement[parameterCount];
        for (int i = 0; i < parameters.length; ++i) {
            parameters[i] = new ParameterElement(resolvedJavaMethod, i);
        }
    }

    public ParameterElement getParameter(int index) {
        return parameters[index];
    }

    public synchronized boolean hasGraph() {
        return graph != null;
    }

    public void postParseGraph(final BigBang bb) {
        synchronized (this) {
            if (graph != null) {
                return;
            }
        }
        new UniverseExpansionOp() {
            @Override
            protected void expand() {
                parseGraph(bb);
            }

            @Override
            public String toString() {
                return String.format("Parsing method %s", resolvedJavaMethod);
            }
        }.post(bb);
    }

    protected void parseGraph(final BigBang bb) {
        StructuredGraph newGraph = null;
        synchronized (this) {
            if (graph != null) {
                // Graph already exists => quit operation.
                return;
            }
            newGraph = new StructuredGraph(resolvedJavaMethod);
            this.graph = newGraph;
        }

        if (Modifier.isNative(resolvedJavaMethod.accessFlags())) {
            BigBang.out.println("NATIVE METHOD " + resolvedJavaMethod);
            return;
        }

        BigBang.out.println("parsing graph " + resolvedJavaMethod + ", locals=" + resolvedJavaMethod.maxLocals());
        GraphBuilderConfiguration config = new GraphBuilderConfiguration(ResolvePolicy.Eager, null);
        GraphBuilderPhase graphBuilderPhase = new GraphBuilderPhase(bb.getMetaAccess(), config, OptimisticOptimizations.NONE);
        graphBuilderPhase.apply(newGraph);
        new PhiStampPhase().apply(newGraph);

        for (MethodCallTargetNode callTargetNode : newGraph.getNodes(MethodCallTargetNode.class)) {
            bb.registerSourceCallTargetNode(callTargetNode);
        }

        for (Node node : newGraph.getNodes()) {
            bb.registerSourceNode(node);
        }

        for (NewInstanceNode newInstance : newGraph.getNodes(NewInstanceNode.class)) {
            Set<ResolvedJavaType> types = new HashSet<>();
            types.add(newInstance.instanceClass());
            BigBang.out.println("propagate new instance " + newInstance + ", " + newInstance.instanceClass());
            for (Node use : newInstance.usages()) {
                Element element = bb.getSinkElement(use, newInstance);
                assert element != null;
                if (element != BLACK_HOLE) {
                    element.postUnionTypes(bb, newInstance, types);
                }
            }
        }
    }

    public ResolvedJavaMethod getResolvedJavaMethod() {
        return resolvedJavaMethod;
    }
}
