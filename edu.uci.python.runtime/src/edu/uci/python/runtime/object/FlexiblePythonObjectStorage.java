/*
 * Copyright (c) 2015, Regents of the University of California
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
package edu.uci.python.runtime.object;

import java.util.*;

import edu.uci.python.runtime.standardtype.*;

public class FlexiblePythonObjectStorage extends PythonObject {

    public FlexiblePythonObjectStorage(PythonClass pythonClass) {
        super(pythonClass);
        FlexibleObjectLayout layout = null;

        if (pythonClass == null) {
            layout = FlexibleObjectLayout.empty(this.getClass());
        } else {
            assert pythonClass.getInstanceObjectLayout() instanceof FlexibleObjectLayout;
            layout = (FlexibleObjectLayout) pythonClass.getInstanceObjectLayout();
        }

        objectLayout = layout;
    }

    @Override
    public void syncObjectLayoutWithClass() {
        assert verifyLayout();
        return;
    }

    @Override
    public void updateLayout(ObjectLayout newLayout) {
        assert verifyLayout();

        // Get the current values of instance variables
        final Map<String, Object> instanceVariableMap = getAttributes();

        // Use new Layout
        objectLayout = newLayout;

        // Make all primitives as unset
        setPrimitiveSetMap(0);

        // Create a new array for objects
        allocateSpillArray();

        // Restore values
        setAttributes(instanceVariableMap);

        assert verifyLayout();
    }

}
