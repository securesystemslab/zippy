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
package edu.uci.python.nodes.object;

import com.oracle.truffle.api.frame.*;
import com.oracle.truffle.api.nodes.*;

import edu.uci.python.nodes.*;
import edu.uci.python.nodes.function.*;
import edu.uci.python.nodes.statement.*;
import edu.uci.python.runtime.function.*;
import edu.uci.python.runtime.standardtype.*;

@NodeInfo(shortName = "class-def")
public class ClassDefinitionNode extends StatementNode {

    private final String name;

    @Child protected PNode superClass;

    @Child protected FunctionDefinitionNode definitionFunction;

    public ClassDefinitionNode(String name, PNode superClass, FunctionDefinitionNode definitionFunction) {
        this.name = name;
        this.superClass = adoptChild(superClass);
        this.definitionFunction = adoptChild(definitionFunction);
    }

    @Override
    public Object execute(VirtualFrame frame) {
        PythonClass base = (PythonClass) superClass.execute(frame);
        PythonClass newClass = new PythonClass(base, name);
        PFunction definitionFunc = (PFunction) definitionFunction.execute(frame);
        definitionFunc.call(frame.pack(), new Object[]{newClass});
        return newClass;
    }
}
