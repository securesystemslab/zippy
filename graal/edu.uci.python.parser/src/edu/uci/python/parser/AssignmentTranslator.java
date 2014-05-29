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

import edu.uci.python.nodes.*;
import edu.uci.python.nodes.frame.*;

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
         * Multi-assignment or unpacking assignment.
         */
        if (lhs.size() == 1 && isDecomposable(exprTarget)) {
            List<expr> targets = decompose(exprTarget);

            if (isDecomposable(rhs)) {
                PNode multiAssignmentNode = makeMultiAssignment(targets, decompose(rhs));
                return translator.assignSourceFromNode(node, multiAssignmentNode);
            } else {
                PNode unpackingAssignmentNode = makeUnpackingAssignment(targets, rhs);
                return translator.assignSourceFromNode(node, unpackingAssignmentNode);
            }
        }

        /**
         * Single or chained-assignment.
         */
        List<PNode> targets = translator.walkExprList(lhs);
        PNode right = (PNode) translator.visit(rhs);

        if (targets.size() == 1) {
            return translator.assignSourceFromNode(node, makeSingleAssignment(targets.get(0), right));
        } else {
            return translator.assignSourceFromNode(node, makeChainedAssignment(lhs, targets, right));
        }
    }

    /**
     * Chained assignments. <br>
     * Transform a = b = 42 <br>
     * To: a = 42; b = 42..
     */
    private PNode makeChainedAssignment(List<expr> targetExpressions, List<PNode> targets, PNode right) throws Exception {
        List<PNode> assignments = new ArrayList<>();
        ReadNode rightVal = environment.makeTempLocalVariable();
        assignments.add(rightVal.makeWriteNode(right));

        for (int i = 0; i < targets.size(); i++) {
            expr targetExpression = targetExpressions.get(i);
            PNode target = targets.get(i);
            PNode writeNode = makeSingleAssignment(target, (PNode) rightVal);
            translator.assignSourceFromNode(targetExpression, writeNode);
            assignments.add(writeNode);
        }

        return factory.createBlock(assignments);
    }

    /**
     * Multi-assignment: <br>
     * Transform a, b = c, d. <br>
     * To: temp_c = c; temp_d = d; a = temp_c; b = temp_d <br>
     * for each assignment in multiassignment, sourcesection is assigned in walkTarget method
     */

    private PNode makeMultiAssignment(List<expr> lhs, List<expr> rhs) throws Exception {
        if (lhs.size() != rhs.size()) {
            throw new IllegalStateException("Unbalanced multi-assignment");
        }

        List<PNode> rights = translator.walkExprList(rhs);
        List<PNode> writeToTempVars = environment.makeTempLocalVariables(rights);

        for (int i = 0; i < rhs.size(); i++) {
            WriteNode tempWrite = (WriteNode) writeToTempVars.get(i);
            writeToTempVars.addAll(walkTarget(lhs.get(i), tempWrite.makeReadNode()));
        }

        return factory.createBlock(writeToTempVars);
    }

    /**
     * Unpacking-assignment: <br>
     * Transform a, b = c. <br>
     * To: temp_c = c; a = temp_c[0]; b = temp_d[1]
     */
    private PNode makeUnpackingAssignment(List<expr> lhs, expr right) throws Exception {
        List<PNode> writes = new ArrayList<>();
        PNode rhs = (PNode) translator.visit(right);
        PNode tempVar = (PNode) environment.makeTempLocalVariable();
        writes.add(makeSingleAssignment(tempVar, rhs));
        writes.addAll(walkTargetList(lhs, factory.duplicate(tempVar, PNode.class)));
        return factory.createBlock(writes);
    }

    private List<PNode> walkTarget(expr target, PNode rightHandSide) throws Exception {
        List<PNode> writes = new ArrayList<>();

        if (isDecomposable(target)) {
            PNode tempVar = (PNode) environment.makeTempLocalVariable();
            PNode writeNode = makeSingleAssignment(tempVar, rightHandSide);
            translator.assignSourceFromNode(target, writeNode);
            writes.add(writeNode);
            List<PNode> intermediateTargets = walkTargetList(decompose(target), factory.duplicate(tempVar, PNode.class));
            writes.addAll(intermediateTargets);
        } else {
            PNode writeNode = makeSingleAssignment((PNode) translator.visit(target), rightHandSide);
            translator.assignSourceFromNode(target, writeNode);
            writes.add(writeNode);
        }

        return writes;
    }

    public List<PNode> walkTargetList(List<expr> lhs, PNode rightHandSide) throws Exception {
        if (lhs.size() == 1) {
            return walkTarget(lhs.get(0), rightHandSide);
        }

        List<PNode> writes = new ArrayList<>();
        List<PNode> additionalWrites = new ArrayList<>();

        for (int i = 0; i < lhs.size(); i++) {
            expr target = lhs.get(i);
            PNode splitRhs = factory.createSubscriptLoadIndex(factory.duplicate(rightHandSide, PNode.class), factory.createIntegerLiteral(i));
            translator.assignSourceFromNode(target, splitRhs);

            if (isDecomposable(target)) {
                PNode tempVar = (PNode) environment.makeTempLocalVariable();
                PNode writeNode = makeSingleAssignment(tempVar, splitRhs);
                translator.assignSourceFromNode(target, writeNode);
                writes.add(writeNode);
                additionalWrites.addAll(walkTargetList(decompose(target), factory.duplicate(tempVar, PNode.class)));
            } else {
                PNode writeNode = makeSingleAssignment((PNode) translator.visit(target), splitRhs);
                translator.assignSourceFromNode(target, writeNode);
                writes.add(writeNode);
            }
        }

        writes.addAll(additionalWrites);
        return writes;
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

    private static PNode makeSingleAssignment(PNode target, PNode right) throws Exception {
        if (target instanceof ReadNode) {
            return ((ReadNode) target).makeWriteNode(right);
        }

        throw notCovered();
    }
}
