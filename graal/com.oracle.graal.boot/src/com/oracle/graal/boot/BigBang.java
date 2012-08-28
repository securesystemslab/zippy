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
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.*;

import com.oracle.graal.api.meta.*;
import com.oracle.graal.boot.meta.*;
import com.oracle.graal.graph.*;
import com.oracle.graal.nodes.*;
import com.oracle.graal.nodes.calc.*;
import com.oracle.graal.nodes.java.*;

public class BigBang {

    public static final PrintStream out;
    static {
        if (Boolean.getBoolean("BigBang.verbose")) {
            out = System.out;
        } else {
            OutputStream sink = new OutputStream() {
                @Override
                public void write(int b) throws IOException {
                }
            };
            out = new PrintStream(sink);
        }
    }

    private static final int THREADS = 4;

    private MetaAccessProvider metaAccessProvider;
    private int postedOperationCount;

    // Mappings from Graal IR and Graal meta-data to element instances.
    private Map<Node, Element> sinkMap = new IdentityHashMap<>();
    private Map<ResolvedJavaField, FieldElement> fieldMap = new IdentityHashMap<>();
    private Map<ResolvedJavaMethod, MethodElement> methodMap = new IdentityHashMap<>();
    private Map<ResolvedJavaType, ArrayTypeElement> arrayTypeMap = new IdentityHashMap<>();

    // Processing queue.
    private ThreadPoolExecutor executor = new ThreadPoolExecutor(THREADS, THREADS, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(), Executors.defaultThreadFactory());

    public BigBang(MetaAccessProvider metaAccessProvider) {
        this.metaAccessProvider = metaAccessProvider;
    }

    public synchronized FieldElement getProcessedField(ResolvedJavaField field) {
        assert field != null;
        if (!fieldMap.containsKey(field)) {
            fieldMap.put(field, new FieldElement(field));
        }
        return fieldMap.get(field);
    }

    public synchronized MethodElement getProcessedMethod(ResolvedJavaMethod method) {
        assert method != null;
        if (!methodMap.containsKey(method)) {
            methodMap.put(method, new MethodElement(method));
        }
        return methodMap.get(method);
    }

    public synchronized ArrayTypeElement getProcessedArrayType(ResolvedJavaType type) {
        assert type != null;
        if (!arrayTypeMap.containsKey(type)) {
            arrayTypeMap.put(type, new ArrayTypeElement(type));
        }
        return arrayTypeMap.get(type);
    }

    public synchronized Element getSinkElement(Node node, Node sourceNode) {
        if (!sinkMap.containsKey(node)) {
            Element resultElement = Element.BLACK_HOLE;
            if (node instanceof PhiNode) {
                PhiNode phiNode = (PhiNode) node;
                resultElement = new PhiElement(phiNode);
            } else if (node instanceof CheckCastNode) {
                CheckCastNode checkCastNode = (CheckCastNode) node;
                resultElement = new CastElement(checkCastNode);
            } else if (node instanceof ValueProxyNode) {
                ValueProxyNode proxyNode = (ValueProxyNode) node;
                resultElement = new ProxyElement(proxyNode);
            } else if (node instanceof StoreFieldNode) {
                StoreFieldNode storeFieldNode = (StoreFieldNode) node;
                resultElement = getProcessedField(storeFieldNode.field());
            } else if (node instanceof StoreIndexedNode) {
                StoreIndexedNode storeIndexedNode = (StoreIndexedNode) node;
                if (storeIndexedNode.elementKind() == Kind.Object) {
                    resultElement = getProcessedArrayType(metaAccessProvider.getResolvedJavaType(Object[].class));
                }
            } else if (node instanceof ReturnNode) {
                ReturnNode returnNode = (ReturnNode) node;
                ResolvedJavaMethod method = ((StructuredGraph) returnNode.graph()).method();
                resultElement = getProcessedMethod(method);
            } else {
                if (node instanceof FrameState || node instanceof MonitorEnterNode || node instanceof MonitorExitNode || node instanceof LoadFieldNode || node instanceof IsNullNode || node instanceof InstanceOfNode) {
                    // OK.
                } else {
                    BigBang.out.println("Unknown sink - black hole? " + node);
                }
            }

            sinkMap.put(node, resultElement);
        }

        if (node instanceof StoreIndexedNode) {
            StoreIndexedNode storeIndexedNode = (StoreIndexedNode) node;
            if (storeIndexedNode.value() != sourceNode) {
                return Element.BLACK_HOLE;
            }
        }

        if (node instanceof StoreFieldNode) {
            StoreFieldNode storeFieldNode = (StoreFieldNode) node;
            if (storeFieldNode.value() != sourceNode) {
                return Element.BLACK_HOLE;
            }
        }
        return sinkMap.get(node);
    }

    public synchronized void registerSourceCallTargetNode(MethodCallTargetNode methodCallTargetNode) {
        InvokeElement invokeElement = new InvokeElement(methodCallTargetNode);
        sinkMap.put(methodCallTargetNode, invokeElement);
        invokeElement.expandStaticMethod(this);
    }

    public synchronized void registerSourceNode(Node node) {
        Element resultElement = null;
        if (node instanceof LoadFieldNode) {
            LoadFieldNode loadFieldNode = (LoadFieldNode) node;
            resultElement = getProcessedField(loadFieldNode.field());
        } else if (node instanceof LoadIndexedNode) {
            LoadIndexedNode loadIndexedNode = (LoadIndexedNode) node;
            if (loadIndexedNode.kind() == Kind.Object) {
                resultElement = getProcessedArrayType(metaAccessProvider.getResolvedJavaType(Object[].class));
            }
        } else if (node instanceof LocalNode) {
            LocalNode localNode = (LocalNode) node;
            if (localNode.kind() == Kind.Object) {
                ResolvedJavaMethod method = ((StructuredGraph) localNode.graph()).method();
                resultElement = getProcessedMethod(method).getParameter(localNode.index());
                BigBang.out.println("resultElement = " + resultElement + " index= " + localNode.index() + ", node=" + node);
            }
        }

        if (resultElement != null) {
            resultElement.postAddUsage(this, node);
        }
    }

    public synchronized void postOperation(UniverseExpansionOp operation) {
        BigBang.out.println("posting operation " + operation);
        executor.execute(operation);
        postedOperationCount++;
    }

    public MetaAccessProvider getMetaAccess() {
        return metaAccessProvider;
    }

    public void finish() {
        while (true) {
            try {
                Thread.sleep(10);
                boolean terminated;
                int oldPostedOperationCount;
                synchronized (this) {
                    terminated = (executor.getCompletedTaskCount() == postedOperationCount);
                    oldPostedOperationCount = postedOperationCount;
                }

                if (terminated) {
                    checkObjectGraph();
                    synchronized (this) {
                        if (postedOperationCount == oldPostedOperationCount) {
                            System.out.printf("Big bang simulation completed in %d operations.\n", postedOperationCount);
                            executor.shutdown();
                            break;
                        }
                    }
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void checkObjectGraph() {
        List<FieldElement> originalRoots = new ArrayList<>();
        synchronized (this) {
            for (FieldElement field : fieldMap.values()) {
                if (field.isStatic()) {
                    originalRoots.add(field);
                }
            }
        }

        Map<Object, Boolean> scannedObjects = new IdentityHashMap<>();
        for (FieldElement field : originalRoots) {
            assert field.isStatic();
            if (field.getUsageCount() > 0 && field.getJavaField().kind() == Kind.Object) {
                Object value = field.getJavaField().getValue(null).asObject();
                BigBang.out.printf("Root field %s: %s\n", field, value);
                scanField(scannedObjects, field, value);
            }
        }
    }

    private void scanField(Map<Object, Boolean> scannedObjects, FieldElement field, Object value) {
        if (value != null && field.getUsageCount() > 0) {
            field.registerNewValue(this, value);
            scan(scannedObjects, value);
        }
    }

    private void scan(Map<Object, Boolean> scannedObjects, Object value) {
        assert value != null;
        if (scannedObjects.containsKey(value)) {
            return;
        }

        scannedObjects.put(value, Boolean.TRUE);
        ResolvedJavaType type = getMetaAccess().getResolvedJavaType(value.getClass());
        scan(scannedObjects, value, type);
    }

    private void scan(Map<Object, Boolean> scannedObjects, Object value, ResolvedJavaType type) {
        if (type.superType() != null) {
            scan(scannedObjects, value, type.superType());
        }

        ResolvedJavaField[] declaredFields = type.declaredFields();
        for (ResolvedJavaField field : declaredFields) {
            if (field.kind() == Kind.Object) {
                FieldElement fieldElement = getProcessedField(field);
                Object fieldValue = field.getValue(Constant.forObject(value)).asObject();
                scanField(scannedObjects, fieldElement, fieldValue);
            }
        }

    }

    public synchronized int[] printState() {

        int nativeMethodCount = 0;
        for (MethodElement methodElement : methodMap.values()) {
            if (methodElement.hasGraph()) {
                if (Modifier.isNative(methodElement.getResolvedJavaMethod().accessFlags())) {
                    BigBang.out.println("Included native method: " + methodElement.getResolvedJavaMethod());
                    nativeMethodCount++;
                }
            }
        }

        int methodCount = 0;
        for (MethodElement methodElement : methodMap.values()) {
            if (methodElement.hasGraph()) {
                if (!Modifier.isNative(methodElement.getResolvedJavaMethod().accessFlags())) {
                    BigBang.out.println("Included method: " + methodElement.getResolvedJavaMethod());
                    methodCount++;
                }
            }
        }

        Set<ResolvedJavaType> includedTypes = new HashSet<>();
        int fieldCount = 0;
        for (FieldElement fieldElement : fieldMap.values()) {
            if (fieldElement.getUsageCount() > 0) {
                BigBang.out.print("Included field: " + fieldElement.getJavaField() + " / ");
                fieldElement.printSeenTypes();
                BigBang.out.println();
                fieldCount++;
                includedTypes.add(fieldElement.getJavaField().holder());
            }
        }

        for (ResolvedJavaType type : includedTypes) {
            BigBang.out.println("Included type: " + type);
        }

        System.out.println("Number of included native methods: " + nativeMethodCount);
        System.out.println("Number of included method: " + methodCount);
        System.out.println("Number of included fields: " + fieldCount);
        System.out.println("Number of included types: " + includedTypes.size());
        return new int[]{nativeMethodCount, methodCount, fieldCount, includedTypes.size()};
    }
}
