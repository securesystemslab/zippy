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

public abstract class PrimaryCheckBoxedNode extends Node {

    public abstract boolean accept(PythonBasicObject primaryObj) throws InvalidAssumptionException;

    public static final class ObjectLayoutCheckNode extends PrimaryCheckBoxedNode {

        private final ObjectLayout cachedLayout;
        private final Assumption stableAssumption;

        public ObjectLayoutCheckNode(PythonBasicObject pythonObj) {
            cachedLayout = pythonObj.getObjectLayout();
            stableAssumption = pythonObj.getStableAssumption();
        }

        @Override
        public boolean accept(PythonBasicObject primaryObj) throws InvalidAssumptionException {
            stableAssumption.check();
            return primaryObj.getObjectLayout() == cachedLayout;
        }
    }

    public static final class PythonClassCheckNode extends PrimaryCheckBoxedNode {

        private final PythonClass cachedClass;
        private final Assumption classStableAssumption;
        private final Assumption objectStableAssumption;

        public PythonClassCheckNode(PythonClass superClass, Assumption classUnmodifiedAssumption, Assumption objectUnmodifiedAssumption) {
            this.cachedClass = superClass;
            this.classStableAssumption = classUnmodifiedAssumption;
            this.objectStableAssumption = objectUnmodifiedAssumption;
        }

        @Override
        public boolean accept(PythonBasicObject primaryObj) throws InvalidAssumptionException {
            PythonObject pobj = (PythonObject) primaryObj;

            if (pobj.getPythonClass() == cachedClass) {
                objectStableAssumption.check();
                classStableAssumption.check();
                return true;
            }

            return false;
        }
    }

    public static final class ClassChainCheckNode extends PrimaryCheckBoxedNode {

        private final Assumption objectStableAssumption;
        @Children private final ObjectLayoutCheckNode[] classChecks;

        public ClassChainCheckNode(PythonBasicObject primaryObj, int depth) {
            this.objectStableAssumption = primaryObj.getStableAssumption();
            ObjectLayoutCheckNode[] classCheckNodes = new ObjectLayoutCheckNode[depth];
            PythonClass current = primaryObj.getPythonClass();

            for (int i = 0; i < depth; i++) {
                classCheckNodes[i] = new ObjectLayoutCheckNode(current);
                current = current.getSuperClass();
            }

            this.classChecks = classCheckNodes;
        }

        @Override
        public boolean accept(PythonBasicObject primaryObj) throws InvalidAssumptionException {
            objectStableAssumption.check();
            PythonClass clazz = primaryObj.getPythonClass();

            for (ObjectLayoutCheckNode checkNode : classChecks) {
                if (!checkNode.accept(clazz)) {
                    return false;
                }
                clazz = clazz.getSuperClass();
            }

            return true;
        }
    }

}
