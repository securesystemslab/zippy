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

import org.python.antlr.*;
import org.python.antlr.base.*;

import com.oracle.truffle.api.frame.*;
import com.oracle.truffle.api.impl.*;

import edu.uci.python.nodes.*;
import edu.uci.python.nodes.truffle.*;

public class TranslationEnvironment {

    private final mod module;

    private Map<PythonTree, ScopeInfo> scopeInfos;
    private Stack<ScopeInfo> scopeStack;
    private ScopeInfo currentScope;
    private ScopeInfo globalScope;
    private int scopeLevel;

    public static final String RETURN_SLOT_ID = "<return_val>";
    public static final String LIST_COMPREHENSION_SLOT_ID = "<list_comp_val>";
    private int listComprehensionSlotCounter = 0;

    public TranslationEnvironment(mod module) {
        this.module = module;
        scopeInfos = new HashMap<>();
        scopeStack = new Stack<>();
    }

    public TranslationEnvironment reset() {
        scopeLevel = 0;
        listComprehensionSlotCounter = 0;
        return this;
    }

    protected mod getModule() {
        return module;
    }

    public void beginScope(PythonTree scopeEntity, ScopeInfo.ScopeKind kind) {
        if (currentScope != null) {
            scopeStack.push(currentScope);
        }

        scopeLevel++;
        ScopeInfo info = scopeInfos.get(scopeEntity);
        currentScope = info != null ? info : new ScopeInfo(TranslationUtil.getScopeId(scopeEntity, kind), kind, new FrameDescriptor(DefaultFrameTypeConversion.getInstance()));

        if (globalScope == null) {
            globalScope = currentScope;
        }
    }

    public void endScope(PythonTree scopeEntity) throws Exception {
        scopeLevel--;
        scopeInfos.put(scopeEntity, currentScope);

        if (!scopeStack.isEmpty()) {
            currentScope = scopeStack.pop();
        }
    }

    public boolean atModuleLevel() {
        assert scopeLevel > 0;
        return scopeLevel == 1;
    }

    public boolean atNonModuleLevel() {
        assert scopeLevel > 0;
        return scopeLevel > 1;
    }

    public ScopeInfo.ScopeKind getScopeKind() {
        return currentScope.getScopeKind();
    }

    public boolean isInModuleScope() {
        return getScopeKind() == ScopeInfo.ScopeKind.Module;
    }

    public boolean isInFunctionScope() {
        return getScopeKind() == ScopeInfo.ScopeKind.Function;
    }

    public boolean isInClassScope() {
        return getScopeKind() == ScopeInfo.ScopeKind.Class;
    }

    public FrameDescriptor getCurrentFrame() {
        FrameDescriptor frameDescriptor = currentScope.getFrameDescriptor();
        assert frameDescriptor != null;
        return frameDescriptor;
    }

    public FrameSlot createLocal(String name) {
        assert name != null : "name is null!";
        return currentScope.getFrameDescriptor().findOrAddFrameSlot(name);
    }

    public FrameSlot findSlot(String name) {
        assert name != null : "name is null!";
        FrameSlot slot = currentScope.getFrameDescriptor().findFrameSlot(name);
        return slot != null ? slot : probeEnclosingScopes(name);
    }

    public FrameSlot createGlobal(String name) {
        assert name != null : "name is null!";
        return globalScope.getFrameDescriptor().findOrAddFrameSlot(name);
    }

    public void addLocalGlobals(String name) {
        assert name != null : "name is null!";
        currentScope.addExplicitGlobalVariable(name);
    }

    public boolean isLocalGlobals(String name) {
        assert name != null : "name is null!";
        return currentScope.isExplicitGlobalVariable(name);
    }

    protected FrameSlot probeEnclosingScopes(String name) {
        assert name != null : "name is null!";
        int level = 0;
        currentScope.needsDeclaringScope();

        for (int i = scopeStack.size() - 1; i > 0; i--) {
            level++;

            ScopeInfo info = scopeStack.get(i);
            if (info == globalScope) {
                break;
            }

            FrameSlot candidate = info.getFrameDescriptor().findFrameSlot(name);
            if (candidate != null) {
                return EnvironmentFrameSlot.pack(candidate, level);
            }

            info.needsDeclaringScope();
        }

        return null;
    }

    public int getCurrentFrameSize() {
        return currentScope.getFrameDescriptor().getSize();
    }

    protected void setDefaultArgumentNodes(List<PNode> defaultArgs) {
        currentScope.setDefaultArgumentNodes(defaultArgs);
    }

    protected List<PNode> getDefaultArgumentNodes() {
        List<PNode> defaultArgs = currentScope.getDefaultArgumentNodes();
        assert defaultArgs != null;
        return defaultArgs;
    }

    public FrameSlot getReturnSlot() {
        return currentScope.getFrameDescriptor().findOrAddFrameSlot(RETURN_SLOT_ID);
    }

    public FrameSlot nextListComprehensionSlot() {
        listComprehensionSlotCounter++;
        return getListComprehensionSlot();
    }

    public FrameSlot getListComprehensionSlot() {
        return currentScope.getFrameDescriptor().findOrAddFrameSlot(LIST_COMPREHENSION_SLOT_ID + listComprehensionSlotCounter);
    }
}
