/*
 * Copyright (c) 2011, Oracle and/or its affiliates. All rights reserved.
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


public class Element {

    public static final Element BLACK_HOLE = new Element(null);

    protected List<Node> usages = new ArrayList<>(4);
    protected Set<ResolvedJavaType> seenTypes = new HashSet<>();
    private ResolvedJavaType declaredType;

    protected Element(ResolvedJavaType declaredType) {
        this.declaredType = declaredType;
    }

    public void postUnionTypes(final BigBang bb, final Node sourceNode, final Set<ResolvedJavaType> newSeenTypes) {
        new UniverseExpansionOp() {
            @Override
            protected void expand() {
                unionTypes(bb, sourceNode, newSeenTypes);
            }

            @Override
            public String toString() {
                return String.format("Add new seen types %s from source node %s to element %s", newSeenTypes, sourceNode, Element.this);
            }
        }.post(bb);
    }

    public void postAddUsage(final BigBang bb, final Node usage) {
        new UniverseExpansionOp() {
            @Override
            protected void expand() {
                addUsage(bb, usage);
            }

            @Override
            public String toString() {
                return String.format("Add usage %s to element %s", usage, Element.this);
            }
        }.post(bb);
    }

    protected synchronized void unionTypes(BigBang bb, @SuppressWarnings("unused") Node sourceNode, Set<ResolvedJavaType> newSeenTypes) {
        if (!seenTypes.containsAll(newSeenTypes)) {
            if (declaredType != null) {
                for (ResolvedJavaType seenType : newSeenTypes) {
                    if (!seenType.isSubtypeOf(declaredType)) {
                        System.out.println("Wrong type found " + seenType + " where declared type of element " + this + " is " + declaredType);
                        System.exit(-1);
                    }
                }
            }
            seenTypes.addAll(newSeenTypes);
            propagateTypes(bb, newSeenTypes);
        }
    }

    protected synchronized void propagateTypes(BigBang bb, Set<ResolvedJavaType> newSeenTypes) {
        for (Node n : usages) {
            propagateTypes(bb, n, newSeenTypes);
        }
    }

    public synchronized int getUsageCount() {
        return usages.size();
    }

    protected synchronized void addUsage(BigBang bb, Node usage) {
        if (!usages.contains(usage)) {
            usages.add(usage);
            propagateTypes(bb, usage, seenTypes);
        }
    }

    public static void propagateTypes(BigBang bb, Node n, Set<ResolvedJavaType> types) {
        if (types.size() != 0) {
            Set<ResolvedJavaType> newSet = new HashSet<>(types);
            for (Node use : n.usages()) {
                Element element = bb.getSinkElement(use, n);
                assert element != null;
                if (element != BLACK_HOLE) {
                    element.postUnionTypes(bb, n, newSet);
                }
            }
        }
    }
}
