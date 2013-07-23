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
package org.python.ast.datatypes;

import org.python.core.PyObject;

/**
 * The purpose of this class is to pack non-PyObject objects in Jython's built-in collection types.
 * Like PyTuple, PyList. Since at this point, our runtime object model heavily rely on the existing
 * code of Jython, this is an easy way to hide Truffle object in Jython's built-in collections.
 * 
 * Later on, this hack should be gone, where we should primarly rely on our own object model.
 * 
 * @author zwei
 * 
 */

@SuppressWarnings("serial")
public class PyObjectContainer extends PyObject {

    final Object innerObject;

    public PyObjectContainer(Object inner) {
        super();
        innerObject = inner;
    }

    @Override
    public boolean isContainer() {
        return true;
    }

    public static PyObject pack(Object object) {
        return new PyObjectContainer(object);
    }

    public Object unpack() {
        return innerObject;
    }

}
