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
package edu.uci.python.parser;

import java.util.*;

import com.oracle.truffle.api.frame.*;

import edu.uci.python.nodes.*;
import edu.uci.python.nodes.argument.*;

public class ScopeInfo {

    public static enum ScopeKind {
        Module, Function, Class,
        // generator expression or generator function
        Generator,
        // list comprehension
        ListComp
    }

    private final String scopeId;
    private final FrameDescriptor frameDescriptor;
    private ScopeKind scopeKind;
    private final ScopeInfo parent;
    private boolean needsDeclaringScope;

    /**
     * Symbols declared using 'global' statement.
     */
    private List<String> explicitGlobalVariables;

    /**
     * An optional field that stores translated nodes of default argument values.
     * {@link #defaultArgumentNodes} is not null only when {@link #scopeKind} is Function, and the
     * function has default arguments.
     */
    private List<PNode> defaultArgumentNodes;
    private ReadDefaultArgumentNode[] defaultArgumentReads;

    public ScopeInfo(String scopeId, ScopeKind kind, FrameDescriptor frameDescriptor, ScopeInfo parent) {
        this.scopeId = scopeId;
        this.scopeKind = kind;
        this.frameDescriptor = frameDescriptor;
        this.parent = parent;
        this.needsDeclaringScope = false;
    }

    public String getScopeId() {
        return scopeId;
    }

    public ScopeKind getScopeKind() {
        return scopeKind;
    }

    public void setAsGenerator() {
        assert scopeKind == ScopeKind.Function || scopeKind == ScopeKind.Generator;
        scopeKind = ScopeKind.Generator;
    }

    public FrameDescriptor getFrameDescriptor() {
        return frameDescriptor;
    }

    public ScopeInfo getParent() {
        return parent;
    }

    public void setNeedsDeclaringScope() {
        needsDeclaringScope = true;
    }

    public boolean needsDeclaringScope() {
        return needsDeclaringScope;
    }

    public void addExplicitGlobalVariable(String identifier) {
        if (explicitGlobalVariables == null) {
            explicitGlobalVariables = new ArrayList<>();
        }

        explicitGlobalVariables.add(identifier);
    }

    public boolean isExplicitGlobalVariable(String identifier) {
        return explicitGlobalVariables != null ? explicitGlobalVariables.contains(identifier) : false;
    }

    public void setDefaultArgumentNodes(List<PNode> defaultArgumentNodes) {
        this.defaultArgumentNodes = defaultArgumentNodes;
    }

    public List<PNode> getDefaultArgumentNodes() {
        return defaultArgumentNodes;
    }

    public void setDefaultArgumentReads(ReadDefaultArgumentNode[] defaultArgumentReads) {
        this.defaultArgumentReads = defaultArgumentReads;
    }

    public ReadDefaultArgumentNode[] getDefaultArgumentReads() {
        return this.defaultArgumentReads;
    }

    @Override
    public String toString() {
        return scopeKind.toString() + " " + scopeId;
    }

}
