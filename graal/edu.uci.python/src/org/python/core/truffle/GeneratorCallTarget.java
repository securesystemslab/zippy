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
package org.python.core.truffle;

import java.util.ArrayList;
import java.util.List;

import org.python.ast.datatypes.PArguments;
import org.python.ast.utils.*;
import org.python.core.PyList;
import org.python.core.PyObject;

import com.oracle.truffle.api.Arguments;
import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.frame.PackedFrame;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.impl.DefaultVirtualFrame;
import com.oracle.truffle.api.nodes.RootNode;

/**
 * It is only used by the non-specialized interpreter.
 * 
 * @author zwei
 * 
 */
public class GeneratorCallTarget extends CallTarget {

    protected final RootNode rootNode;
    protected final FrameDescriptor frameDescriptor;
    VirtualFrame frame;
    Object iterator;

    public GeneratorCallTarget(RootNode generatorNode, FrameDescriptor frameDescriptor, Object iterator) {
        this.rootNode = generatorNode;
        this.frameDescriptor = frameDescriptor;
        this.iterator = iterator;
    }

    @Override
    public Object call(PackedFrame caller, Arguments arguments) {
        frame = new DefaultVirtualFrame(frameDescriptor, caller, arguments);
        List<PyObject> results = new ArrayList<>();

        while (true) {
            try {
                Object item = iterNext();
                if (item != null) {
                    results.add((PyObject) item);
                }
            } catch (IteratorTerminationException e) {
                break;
            }
        }

        return new PyList(results);
    }

    // __iter__
    public Object iter(VirtualFrame caller) {
        frame = new DefaultVirtualFrame(frameDescriptor, caller.pack(), new PArguments());

        assert iterator != null;
        return this;
    }

    // broken
    public Object iterNext() {
// assert rootNode instanceof comprehension;
// comprehension comp = (comprehension) rootNode;
// return comp.iterNext(frame, iterator);
        return null;
    }

    @Override
    public String toString() {
        return "Generator ";
    }
}
