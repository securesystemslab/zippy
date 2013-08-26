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
package edu.uci.python.nodes.translation;

import java.util.*;
import java.util.List;

import org.python.antlr.*;
import org.python.antlr.base.*;

import com.oracle.truffle.api.frame.*;
import com.oracle.truffle.api.impl.*;

import edu.uci.python.nodes.*;
import edu.uci.python.nodes.truffle.*;

public class TranslationEnvironment {

    private final mod module;

    private Map<PythonTree, FrameDescriptor> frameDescriptors = new HashMap<>();

    private Stack<FrameDescriptor> frames;

    private int scopeLevel;

    private FrameDescriptor currentFrame;

    private FrameDescriptor globalFrame;

    private List<PNode> defaultArgs;

    public static final String RETURN_SLOT_ID = "<return_val>";

    /*
     * used to keep track of explicitly declared globals in the current scope
     */
    private List<String> localGlobals = new ArrayList<>();

    public TranslationEnvironment(mod module) {
        this.module = module;
    }

    public TranslationEnvironment resetScopeLevel() {
        frames = new Stack<>();
        scopeLevel = 0;
        return this;
    }

    protected mod getModule() {
        return module;
    }

    protected int getScopeLevel() {
        return scopeLevel;
    }

    public void beginScope(PythonTree scopeEntity) {
        scopeLevel++;

        if (currentFrame != null) {
            frames.push(currentFrame);
        }

        // FIXME: temporary fix!
        FrameDescriptor fd = frameDescriptors.get(scopeEntity);
        if (fd != null) {
            currentFrame = fd;
        } else {
            currentFrame = new FrameDescriptor(DefaultFrameTypeConversion.getInstance());
        }

        if (globalFrame == null) {
            globalFrame = currentFrame;
        }
    }

    public FrameDescriptor endScope() throws Exception {
        scopeLevel--;
        FrameDescriptor fd = currentFrame;
        if (!frames.empty()) {
            currentFrame = frames.pop();
        }

        // reset locally declared globals
        localGlobals.clear();
        return fd;
    }

    public FrameDescriptor getCurrentFrame() {
        return currentFrame;
    }

    public FrameSlot createLocal(String name) {
        assert name != null : "name is null!";
        return currentFrame.findOrAddFrameSlot(name);
    }

    public FrameSlot findSlot(String name) {
        assert name != null : "name is null!";
        FrameSlot slot = currentFrame.findFrameSlot(name);
        return slot != null ? slot : probeEnclosingScopes(name);
    }

    public FrameSlot createGlobal(String name) {
        assert name != null : "name is null!";
        return globalFrame.findOrAddFrameSlot(name);
    }

    public void addLocalGlobals(String name) {
        assert name != null : "name is null!";
        localGlobals.add(name);
    }

    public boolean isLocalGlobals(String name) {
        assert name != null : "name is null!";
        return localGlobals.contains(name);
    }

    protected FrameSlot probeEnclosingScopes(String name) {
        assert name != null : "name is null!";
        int level = 0;
        for (int i = frames.size() - 1; i > 0; i--) {
            FrameDescriptor fd = frames.get(i);
            level++;

            if (fd == globalFrame) {
                break;
            }

            FrameSlot candidate = fd.findFrameSlot(name);
            if (candidate != null) {
                return EnvironmentFrameSlot.pack(candidate, level);
            }
        }

        return null;
    }

    public int getCurrentFrameSize() {
        return currentFrame.getSize();
    }

    protected void setFrameDescriptor(PythonTree scopeEntity, FrameDescriptor descriptor) {
        frameDescriptors.put(scopeEntity, descriptor);
    }

    protected FrameDescriptor getFrameDescriptor(PythonTree scopeEntity) {
        return frameDescriptors.get(scopeEntity);
    }

    // TODO: Not pretty. Probably to be removed...
    protected void setDefaultArgs(List<PNode> defaultArgs) {
        this.defaultArgs = defaultArgs;
    }

    protected List<PNode> getDefaultArgs() {
        assert defaultArgs != null : "default args is null";
        return defaultArgs;
    }

    public FrameSlot getReturnSlot() {
        return currentFrame.findOrAddFrameSlot(RETURN_SLOT_ID);
    }

}
