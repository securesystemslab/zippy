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
package edu.uci.python.nodes.attribute;

import com.oracle.truffle.api.*;
import com.oracle.truffle.api.nodes.*;

import edu.uci.python.runtime.object.*;
import edu.uci.python.runtime.standardtype.*;

public abstract class ShapeCheckNode extends Node {

    protected final ObjectLayout cachedObjectLayout;

    public ShapeCheckNode(ObjectLayout shape) {
        this.cachedObjectLayout = shape;
    }

    public abstract boolean accept(PythonObject primaryObj) throws InvalidAssumptionException;

    public static ShapeCheckNode create(PythonObject primaryObj, int depth) {
        if (depth == 0) {
            return new PythonObjectCheckNode(primaryObj);
        } else if (depth == 1) {
            return new PythonClassCheckNode(primaryObj);
        } else {
            return new ClassChainCheckNode(primaryObj, depth);
        }
    }

    public static final class PythonObjectCheckNode extends ShapeCheckNode {

        private final Assumption stableAssumption;

        public PythonObjectCheckNode(PythonObject pythonObj) {
            super(pythonObj.getObjectLayout());
            stableAssumption = pythonObj.getStableAssumption();
        }

        @Override
        public boolean accept(PythonObject primaryObj) throws InvalidAssumptionException {
            stableAssumption.check();

            if (primaryObj.getObjectLayout() == cachedObjectLayout) {
                return true;
            }

            return false;
        }
    }

    public static final class PythonClassCheckNode extends ShapeCheckNode {

        private final Assumption classStableAssumption;
        private final Assumption objectStableAssumption;

        public PythonClassCheckNode(PythonObject primaryObj) {
            super(primaryObj.getObjectLayout());
            this.classStableAssumption = primaryObj.getPythonClass().getStableAssumption();
            this.objectStableAssumption = primaryObj.getStableAssumption();
        }

        @Override
        public boolean accept(PythonObject primaryObj) throws InvalidAssumptionException {
            if (primaryObj.getObjectLayout() == cachedObjectLayout) {
                objectStableAssumption.check();
                classStableAssumption.check();
                return true;
            }

            return false;
        }
    }

    public static final class ClassChainCheckNode extends ShapeCheckNode {

        private final Assumption objectStableAssumption;
        private final Assumption[] classStableAssumptions;

        public ClassChainCheckNode(PythonObject primaryObj, int depth) {
            super(primaryObj.getObjectLayout());
            this.objectStableAssumption = primaryObj.getStableAssumption();
            Assumption[] classStables = new Assumption[depth];
            PythonClass current = primaryObj.getPythonClass();

            for (int i = 0; i < depth; i++) {
                classStables[i] = current.getStableAssumption();
                current = current.getSuperClass();
                assert current != null;

            }

            this.classStableAssumptions = classStables;
        }

        @Override
        public boolean accept(PythonObject primaryObj) throws InvalidAssumptionException {
            if (primaryObj.getObjectLayout() == cachedObjectLayout) {
                objectStableAssumption.check();

                for (Assumption classStable : classStableAssumptions) {
                    classStable.check();
                }
            }

            return true;
        }
    }

}
