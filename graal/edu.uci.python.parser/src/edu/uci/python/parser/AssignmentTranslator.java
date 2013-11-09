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
package edu.uci.python.parser;

import static edu.uci.python.parser.TranslationUtil.*;

import java.util.*;
import java.util.List;

import org.python.antlr.ast.*;
import org.python.antlr.base.*;

import com.oracle.truffle.api.nodes.*;

import edu.uci.python.nodes.*;
import edu.uci.python.nodes.access.*;
import edu.uci.python.nodes.statements.*;

public class AssignmentTranslator {

    private final NodeFactory factory;
    private final TranslationEnvironment environment;
    private final PythonTreeTranslator translator;

    public AssignmentTranslator(TranslationEnvironment environment, PythonTreeTranslator translator) {
        this.factory = NodeFactory.getInstance();
        this.environment = environment;
        this.translator = translator;
    }

    public PNode translate(Assign node) throws Exception {
        expr rhs = node.getInternalValue();
        List<expr> lhs = node.getInternalTargets();
        expr exprTarget = lhs.get(0);

        /**
         * Multi-assignment or unpacking assignment. In other words, multiple assignment target
         * exist.
         */
        if (lhs.size() == 1 && AssignmentTranslator.isDecomposable(exprTarget)) {
            List<expr> targets = AssignmentTranslator.decompose(exprTarget);

            if (AssignmentTranslator.isDecomposable(rhs)) {
                return makeMultiAssignment(targets, AssignmentTranslator.decompose(rhs));
            } else {
                return makeUnpackingAssignment(targets, rhs);
            }
        }

        /**
         * Single or chained-assignment.
         */
        PNode right = (PNode) translator.visit(node.getInternalValue());
        List<PNode> targets = translator.walkExprList(node.getInternalTargets());

        if (targets.size() == 1) {
            return makeSingleAssignment(targets.get(0), right);
        } else {
            return makeChainedAssignment(right, targets);
        }
    }

    /**
     * Chained assignments. <br>
     * Transform a = b = 42 <br>
     * To: a = 42; b = 42..
     */
    private PNode makeChainedAssignment(PNode right, List<PNode> targets) throws Exception {
        List<PNode> assignments = new ArrayList<>();

        for (Node target : targets) {
            assignments.add(makeSingleAssignment(target, right));
        }

        return factory.createBlock(assignments);
    }

    /**
     * Multi-assignment: <br>
     * Transform a, b = c, d. <br>
     * To: temp_c = c; temp_d = d; a = temp_c; b = temp_d
     */
    private BlockNode makeMultiAssignment(List<expr> lhs, List<expr> rhs) throws Exception {
        if (lhs.size() != rhs.size()) {
            throw new IllegalStateException("Unbalanced multi-assignment");
        }

        List<PNode> rights = translator.walkExprList(rhs);
        List<PNode> tempVars = environment.makeTempLocalVariables(rights);
        List<PNode> targets = walkLeftHandSideList(lhs);

        for (int i = 0; i < targets.size(); i++) {
            if (i < lhs.size()) {
                PNode read = ((WriteNode) tempVars.get(i)).makeReadNode();
                PNode tempWrite = ((ReadNode) targets.get(i)).makeWriteNode(read);
                tempVars.add(tempWrite);
            } else {
                tempVars.add(targets.get(i));
            }
        }

        return factory.createBlock(tempVars);
    }

    /**
     * Unpacking-assignment: <br>
     * Transform a, b = c. <br>
     * To: temp_c = c; a = temp_c[0]; b = temp_d[1]
     */
    private BlockNode makeUnpackingAssignment(List<expr> lhs, expr right) throws Exception {
        List<PNode> writes = new ArrayList<>();
        PNode rhs = (PNode) translator.visit(right);
        PNode tempVar = (PNode) environment.makeTempLocalVariable();
        writes.add(((ReadNode) tempVar).makeWriteNode(rhs));

        List<PNode> targets = walkLeftHandSideList(lhs);

        writes.addAll(processDecomposedTargetList(targets, lhs.size(), tempVar, true));
        return factory.createBlock(writes);
    }

    public List<PNode> walkLeftHandSideList(List<expr> lhs) throws Exception {
        List<PNode> writes = new ArrayList<>();
        List<PNode> additionalWrites = new ArrayList<>();

        for (int i = 0; i < lhs.size(); i++) {
            expr target = lhs.get(i);

            if (AssignmentTranslator.isDecomposable(target)) {
                PNode tempWrite = (PNode) environment.makeTempLocalVariable();
                writes.add(tempWrite);
                List<expr> targets = AssignmentTranslator.decompose(target);
                List<PNode> nestedWrites = walkLeftHandSideList(targets);
                additionalWrites.addAll(processDecomposedTargetList(nestedWrites, targets.size(), tempWrite, true));
            } else {
                writes.add((PNode) translator.visit(target));
            }
        }

        writes.addAll(additionalWrites);
        return writes;
    }

    private List<PNode> processDecomposedTargetList(List<PNode> nestedWrites, int sizeOfCurrentLevelLeftHandSide, PNode tempWrite, boolean isUnpacking) {
        for (int idx = 0; idx < nestedWrites.size(); idx++) {
            if (idx < sizeOfCurrentLevelLeftHandSide) {
                PNode transformedRhs = isUnpacking ? makeSubscriptLoad(tempWrite, idx) : tempWrite;
                PNode write = ((ReadNode) nestedWrites.get(idx)).makeWriteNode(transformedRhs);
                nestedWrites.set(idx, write);
            }
        }

        return nestedWrites;
    }

    private PNode makeSubscriptLoad(PNode read, int index) {
        PNode indexNode = factory.createIntegerLiteral(index);
        PNode sload = factory.createSubscriptLoad(read, indexNode);
        return sload;
    }

    private static boolean isDecomposable(expr node) {
        return node instanceof org.python.antlr.ast.List || node instanceof Tuple;
    }

    private static List<expr> decompose(expr node) {
        if (node instanceof org.python.antlr.ast.List) {
            org.python.antlr.ast.List list = (org.python.antlr.ast.List) node;
            return list.getInternalElts();
        } else if (node instanceof Tuple) {
            Tuple tuple = (Tuple) node;
            return tuple.getInternalElts();
        } else {
            throw TranslationUtil.notCovered("Unexpected decomposable type");
        }
    }

    private static PNode makeSingleAssignment(Node target, PNode right) throws Exception {
        if (target instanceof ReadNode) {
            return ((ReadNode) target).makeWriteNode(right);
        }

        throw notCovered();
    }
}
