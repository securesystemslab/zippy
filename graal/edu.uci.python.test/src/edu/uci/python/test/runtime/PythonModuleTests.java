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
package edu.uci.python.test.runtime;

import static org.junit.Assert.*;

import org.junit.*;

import com.oracle.truffle.api.frame.*;
import com.oracle.truffle.api.impl.*;

import edu.uci.python.runtime.*;
import edu.uci.python.runtime.datatypes.*;
import edu.uci.python.runtime.standardtypes.*;

public class PythonModuleTests {

    @Test
    public void pythonModuleTest() {
        final PythonContext context = new PythonContext(new PythonOptions());
        PythonModule module = new PythonModule(new PythonClass(context, null, "module"));

        module.addBuiltinMethodsAndConstants(PythonModule.class);
        assertEquals("", module.getAttribute("__name__").toString());
        assertEquals("", module.getAttribute("__doc__").toString());
        assertEquals("", module.getAttribute("__package__").toString());
    }

    @Test
    public void builtinsMinTest() {
        final PythonContext context = new PythonContext(new PythonOptions());
        final PythonModule builtins = context.getPythonCore().getBuiltinsModule();
        PBuiltinFunction min = (PBuiltinFunction) builtins.getAttribute("min");
        FrameDescriptor fd = new FrameDescriptor();
        Object returnValue = min.call(new DefaultVirtualFrame(fd, null, null).pack(), new Object[]{4, 2, 1});
        assertEquals(1, returnValue);
    }

    @Test
    public void builtinsIntTest() {
        final PythonContext context = new PythonContext(new PythonOptions());
        final PythonModule builtins = context.getPythonCore().getBuiltinsModule();
        PBuiltinFunction intFunc = (PBuiltinFunction) builtins.getAttribute("int");
        FrameDescriptor fd = new FrameDescriptor();
        Object returnValue = intFunc.call(new DefaultVirtualFrame(fd, null, null).pack(), new Object[]{"42"});
        assertEquals(42, returnValue);
    }

    @Test
    public void mainModuleTest() {
        final PythonContext context = new PythonContext(new PythonOptions());
        PythonModule main = context.getPythonCore().getMainModule();
        PythonModule builtins = (PythonModule) main.getAttribute("__builtins__");
        PBuiltinFunction abs = (PBuiltinFunction) builtins.getAttribute("abs");
        FrameDescriptor fd = new FrameDescriptor();
        Object returned = abs.call(new DefaultVirtualFrame(fd, null, null).pack(), new Object[]{-42});
        assertEquals(42, returned);
    }
}
