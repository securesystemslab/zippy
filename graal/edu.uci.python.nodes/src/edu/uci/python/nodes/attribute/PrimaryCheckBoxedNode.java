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
import com.oracle.truffle.api.frame.*;
import com.oracle.truffle.api.nodes.*;

import edu.uci.python.runtime.object.*;
import edu.uci.python.runtime.standardtype.*;

public abstract class PrimaryCheckBoxedNode extends Node {

    public abstract boolean accept(VirtualFrame frame, PythonBasicObject primaryObj) throws InvalidAssumptionException;

    public static class ObjectLayoutCheckNode extends PrimaryCheckBoxedNode {

        private final ObjectLayout cachedLayout;
        private final Assumption unmodifiedAssumption;

        public ObjectLayoutCheckNode(PythonBasicObject pythonObj) {
            cachedLayout = pythonObj.getObjectLayout();
            unmodifiedAssumption = pythonObj.getStableAssumption();
        }

        @Override
        public boolean accept(VirtualFrame frame, PythonBasicObject primaryObj) throws InvalidAssumptionException {
            unmodifiedAssumption.check();
            return primaryObj.getObjectLayout() == cachedLayout;
        }
    }

    public static class PythonClassCheckNode extends PrimaryCheckBoxedNode {

        private final PythonClass cachedClass;
        private final Assumption classUnmodifiedAssumption;
        private final Assumption objectUnmodifiedAssumption;

        public PythonClassCheckNode(PythonClass superClass, Assumption classUnmodifiedAssumption, Assumption objectUnmodifiedAssumption) {
            this.cachedClass = superClass;
            this.classUnmodifiedAssumption = classUnmodifiedAssumption;
            this.objectUnmodifiedAssumption = objectUnmodifiedAssumption;
        }

        @Override
        public boolean accept(VirtualFrame frame, PythonBasicObject primaryObj) throws InvalidAssumptionException {
            PythonObject pobj = (PythonObject) primaryObj;

            if (pobj.getPythonClass() == cachedClass) {
                objectUnmodifiedAssumption.check();
                classUnmodifiedAssumption.check();
                return true;
            }

            return false;
        }
    }

    public static class ClassChainCheckNode extends PrimaryCheckBoxedNode {

        private final Assumption objectUnmodifiedAssumption;
        @Children private final ObjectLayoutCheckNode[] classChecks;

        public ClassChainCheckNode(PythonObject pythonObj, int depth) {
            this.objectUnmodifiedAssumption = pythonObj.getStableAssumption();
            ObjectLayoutCheckNode[] classCheckNodes = new ObjectLayoutCheckNode[depth];
            PythonClass current;

            for (int i = 0; i < depth; i++) {
                current = pythonObj.getPythonClass();
                classCheckNodes[i] = new ObjectLayoutCheckNode(current);
            }

            this.classChecks = classCheckNodes;
        }

        @Override
        public boolean accept(VirtualFrame frame, PythonBasicObject primaryObj) throws InvalidAssumptionException {
            objectUnmodifiedAssumption.check();
            PythonObject pobj = (PythonObject) primaryObj;
            PythonClass clazz = pobj.getPythonClass();

            for (ObjectLayoutCheckNode checkNode : classChecks) {
                if (checkNode.accept(frame, clazz)) {
                    return false;
                }
                clazz = clazz.getSuperClass();
            }

            return true;
        }
    }
}
