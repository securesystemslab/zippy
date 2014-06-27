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
package edu.uci.python.runtime.standardtype;

import edu.uci.python.runtime.*;
import edu.uci.python.runtime.datatype.*;
import edu.uci.python.runtime.object.*;

public final class PythonModule extends FixedPythonObjectStorage {

    private final String name;
    private final String file;

    public PythonModule(PythonContext context, String name, String file) {
        super(context.getModuleClass());
        this.name = name;
        this.file = file;
        switchToPrivateLayout();
        addDefaultConstants(name);
    }

    private void addDefaultConstants(String moduleName) {
        setAttribute("__name__", moduleName);
        setAttribute("__doc__", PNone.NONE);
        setAttribute("__package__", PNone.NONE);

        if (file != null) {
            setAttribute("__file__", file);
        }
    }

    public String getModuleName() {
        return name;
    }

    public String getModulePath() {
        return file;
    }

    @Override
    public PythonObject getValidStorageFullLookup(String attributeId) {
        PythonObject storage = null;

        if (isOwnAttribute(attributeId)) {
            storage = this;
        }

        return storage;
    }

    @Override
    public String toString() {
        assert name.equals(getAttribute("__name__"));
        return "<module '" + this.getAttribute("__name__") + "'>";
    }

}
