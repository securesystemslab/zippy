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

import org.python.core.*;

import com.oracle.truffle.api.*;
import com.oracle.truffle.api.nodes.*;

import edu.uci.python.runtime.object.*;
import edu.uci.python.runtime.standardtype.*;

public abstract class LayoutCheckNode extends Node {

    protected final ObjectLayout cachedObjectLayout;

    public LayoutCheckNode(ObjectLayout layout) {
        this.cachedObjectLayout = layout;
    }

    public abstract boolean accept(PythonObject primary) throws InvalidAssumptionException;

    public static LayoutCheckNode create(PythonObject primary, ObjectLayout storageLayout, int depth) {
        if (depth == 0) {
            return new PythonObjectCheckNode(primary);
        } else if (depth == 1) {
            return new PythonClassCheckNode(primary, storageLayout);
        } else {
            return new ClassChainCheckNode(primary, storageLayout, depth);
        }
    }

    public static LayoutCheckNode create(PythonObject primary, String attributeId, boolean isAttributeInPlace) {
        if (isAttributeInPlace) {
            assert primary.isOwnAttribute(attributeId);
            return new PythonObjectCheckNode(primary);
        }

        int depth = 0;
        PythonClass current;

        if (primary instanceof PythonClass) {
            current = (PythonClass) primary;
        } else {
            current = primary.getPythonClass();
            depth++;
        }

        // class chain lookup
        do {
            if (current.isOwnAttribute(attributeId)) {
                break;
            }

            current = current.getSuperClass();
            depth++;
        } while (current != null);

        if (current == null) {
            throw Py.AttributeError(primary + " object has no attribute " + attributeId);
        }

        if (depth == 0) {
            return new PythonObjectCheckNode(primary);
        } else if (depth == 1) {
            return new PythonClassCheckNode(primary, current.getObjectLayout());
        } else {
            return new ClassChainCheckNode(primary, current.getObjectLayout(), depth);
        }
    }

    public static final class PythonObjectCheckNode extends LayoutCheckNode {

        private final Assumption stableAssumption;

        public PythonObjectCheckNode(PythonObject python) {
            super(python.getObjectLayout());
            stableAssumption = python.getStableAssumption();
            assert stableAssumption.isValid();
        }

        @Override
        public boolean accept(PythonObject primary) throws InvalidAssumptionException {
            stableAssumption.check();
            return primary.getObjectLayout() == cachedObjectLayout;
        }
    }

    public static final class PythonClassCheckNode extends LayoutCheckNode {

        private final Assumption storageStableAssumption;
        private final Assumption objectStableAssumption;

        public PythonClassCheckNode(PythonObject primary, ObjectLayout storageLayout) {
            super(primary.getObjectLayout());
            this.storageStableAssumption = storageLayout.getValidAssumption();
            this.objectStableAssumption = primary.getStableAssumption();
            assert storageStableAssumption.isValid();
            assert objectStableAssumption.isValid();
        }

        @Override
        public boolean accept(PythonObject primary) throws InvalidAssumptionException {
            storageStableAssumption.check();
            objectStableAssumption.check();

            if (primary.getObjectLayout() == cachedObjectLayout) {
                return true;
            }

            return false;
        }
    }

    public static final class ClassChainCheckNode extends LayoutCheckNode {

        private final Assumption objectStableAssumption;
        private final Assumption[] classChainsStableAssumptions;
        private final Assumption storageStableAssumption;

        public ClassChainCheckNode(PythonObject primary, ObjectLayout storageLayout, int depth) {
            super(primary.getObjectLayout());
            this.objectStableAssumption = primary.getStableAssumption();
            assert objectStableAssumption.isValid();

            Assumption[] classStables = new Assumption[depth - 1];
            PythonClass current = primary.getPythonClass();

            for (int i = 0; i < depth - 1; i++) {
                classStables[i] = current.getStableAssumption();
                assert classStables[i].isValid();
                current = current.getSuperClass();
                assert current != null;
            }

            this.classChainsStableAssumptions = classStables;
            this.storageStableAssumption = storageLayout.getValidAssumption();
            assert storageStableAssumption == current.getStableAssumption();
            assert storageStableAssumption.isValid();
        }

        @ExplodeLoop
        @Override
        public boolean accept(PythonObject primary) throws InvalidAssumptionException {
            storageStableAssumption.check();
            objectStableAssumption.check();

            for (Assumption classStable : classChainsStableAssumptions) {
                classStable.check();
            }

            if (primary.getObjectLayout() == cachedObjectLayout) {
                return true;
            }

            return false;
        }
    }

}
