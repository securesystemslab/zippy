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
package org.python.ast;

import java.util.*;
import java.util.List;

import org.python.antlr.*;
import org.python.antlr.ast.*;
import org.python.antlr.base.*;
import org.python.ast.datatypes.*;
import org.python.ast.nodes.Amendable;
import org.python.ast.nodes.CallBuiltInWithOneArgNoKeywordNode;
import org.python.ast.nodes.CallBuiltInWithTwoArgsNoKeywordNode;
import org.python.ast.nodes.FunctionRootNode;
import org.python.ast.nodes.GeneratorNode;
import org.python.ast.nodes.NodeFactory;
import org.python.ast.nodes.PNode;
import org.python.ast.nodes.ReadArgumentNode;
import org.python.ast.nodes.WriteNode;
import org.python.ast.nodes.expressions.*;
import org.python.ast.nodes.literals.BooleanLiteralNode;
import org.python.ast.nodes.literals.NoneLiteralNode;
import org.python.ast.nodes.statements.*;
import org.python.compiler.*;
import org.python.core.*;
import org.python.core.truffle.EnvironmentFrameSlot;
import org.python.core.truffle.GlobalScope;

import com.oracle.truffle.api.*;
import com.oracle.truffle.api.frame.*;
import com.oracle.truffle.api.nodes.*;
import edu.uci.python.runtime.Options;

public class PythonTreeTranslator extends Visitor {

    private final NodeFactory nodeFactory = new NodeFactory();

    private final TranslationEnvironment environment;

    private Stack<FrameDescriptor> frames = new Stack<>();
    private FrameDescriptor currentFrame;

    private boolean isLeftHandSide = false;

    private boolean isGenerator = false;

    private Stack<FunctionRootNode> funcRoots = new Stack<>();

    private Stack<StatementNode> loopHeaders = new Stack<>();

    private static final String TEMP_LOCAL_PREFIX = "temp_";

    public PythonTreeTranslator(TranslationEnvironment environment) {
        this.environment = environment;
    }

    public RootNode translate(PythonTree root) {
        try {
            return (RootNode) visit(root);
        } catch (Throwable t) {
            t.printStackTrace();
            throw new RuntimeException("Failed in " + this + " with error " + t);
        }
    }

    private FunctionRootNode getCurrentFuncRoot() {
        if (funcRoots.isEmpty()) {
            return null;
        } else {
            return funcRoots.peek();
        }
    }

    private StatementNode getCurrentLoopHeader() {
        if (loopHeaders.isEmpty()) {
            return null;
        } else {
            return loopHeaders.peek();
        }
    }

    protected void beginScope(FrameDescriptor fd) {
        if (currentFrame != null) {
            frames.push(currentFrame);
        }

        currentFrame = fd;
    }

    public void endScope() throws Exception {
        if (!frames.empty()) {
            currentFrame = frames.pop();
        }
    }

    private FrameDescriptor getFrameDescriptor(PythonTree scopeEntity) {
        return environment.getFrameDescriptor(scopeEntity);
    }

    private FrameSlot getFrameSlot(PythonTree symbol) {
        return environment.getFrameSlot(symbol);
    }

    @Override
    public Object visitModule(org.python.antlr.ast.Module node) throws Exception {
        beginScope(getFrameDescriptor(node));

        List<PNode> body = visitStatements(node.getInternalBody());
        RootNode newNode = new NodeFactory().createModule(body, getFrameDescriptor(node));
        endScope();
        return newNode;
    }

    public List<PNode> visitStatements(List<stmt> stmts) throws Exception {
        List<PNode> statements = new ArrayList<>();

        for (int i = 0; i < stmts.size(); i++) {
            PNode statement = (PNode) visit(stmts.get(i));

            // Statements like Global is ignored
            if (statement != null) {
                statements.add(statement);
            }
        }

        return statements;
    }

    @Override
    public Object visitFunctionDef(FunctionDef node) throws Exception {
        beginScope(getFrameDescriptor(node));
        isGenerator = false;

        FrameDescriptor fd = getFrameDescriptor(node);
        ParametersNode parameters = visitArgs(node.getInternalArgs());

        // cache
        FunctionRootNode funcRoot = nodeFactory.createFunctionRoot(parameters, null);
        funcRoots.push(funcRoot);

        List<PNode> statements = visitStatements(node.getInternalBody());

        String name = node.getInternalName();
        FrameSlot slot = getFrameSlot(node.getInternalNameNode());
        StatementNode body = nodeFactory.createBlock(statements);

        // cache
        body.setFuncRootNode(getCurrentFuncRoot());

        if (isGenerator) {
            body = new ASTLinearizer((BlockNode) body).linearize();
            RootNode genRoot = nodeFactory.createGeneratorRoot(parameters, body);
            CallTarget ct = Truffle.getRuntime().createCallTarget(genRoot, fd);
            return nodeFactory.createFunctionDef(slot, name, parameters, ct, genRoot);
        }

        // cache
        funcRoots.pop();
        funcRoot.setBody(body);
        CallTarget ct = Truffle.getRuntime().createCallTarget(funcRoot, fd);
        endScope();
        return nodeFactory.createFunctionDef(slot, name, parameters, ct, funcRoot);
    }

    public ParametersNode visitArgs(arguments node) throws Exception {
        isLeftHandSide = true;

        // parse arguments
        ArgListCompiler ac = new ArgListCompiler();
        ac.visitArgs(node);

        List<PNode> defaults = walkExprList(node.getInternalDefaults());

        List<PNode> args = new ArrayList<>();

        List<String> paramNames = new ArrayList<>();

        for (int i = 0; i < node.getInternalArgs().size(); i++) {
            expr arg = node.getInternalArgs().get(i);

            if (arg instanceof Name) {
                args.add((PNode) visit(arg));
                paramNames.add(((Name) arg).getInternalId());
            } else {
                throw new RuntimeException("Unexpected parameter type " + arg.getClass().getSimpleName());
            }
        }

        isLeftHandSide = false;

        if (defaults.isEmpty()) {
            if (args.size() == 1) {
                return nodeFactory.createParametersOfSizeOne(args.get(0), paramNames);
            } else if (args.size() == 2) {
                return nodeFactory.createParametersOfSizeTwo(args.get(0), args.get(1), paramNames);
            } else {
                return nodeFactory.createParametersWithNoDefaults(args, paramNames);
            }
        }

        return nodeFactory.createParametersWithDefaults(args, defaults, paramNames);
    }

    List<PNode> walkExprList(List<expr> exprs) throws Exception {
        List<PNode> targets = new ArrayList<>();

        for (expr source : exprs) {
            targets.add((PNode) visit(source));
        }

        return targets;
    }

    List<PNode> walkKeywordList(List<keyword> keywords) throws Exception {
        List<PNode> targets = new ArrayList<>();

        for (keyword source : keywords) {
            targets.add((PNode) visit(source));
        }

        return targets;
    }

    FrameSlot[] walkAliasList(List<alias> aliases) throws Exception {
        FrameSlot[] slots = new FrameSlot[aliases.size()];

        for (int i = 0; i < aliases.size(); i++) {
            slots[i] = getFrameSlot(aliases.get(i));
        }

        return slots;
    }

    @Override
    public Object visitImport(Import node) throws Exception {
        List<alias> aliases = node.getInternalNames();
        FrameSlot[] slots = walkAliasList(aliases);
        String[] names = new String[aliases.size()];

        for (int i = 0; i < aliases.size(); i++) {
            names[i] = aliases.get(i).getInternalName();
        }

        return nodeFactory.createImport(slots, null, names);
    }

    @Override
    public Object visitImportFrom(ImportFrom node) throws Exception {
        List<alias> aliases = node.getInternalNames();
        FrameSlot[] slots = walkAliasList(aliases);
        String[] names = new String[aliases.size()];

        for (int i = 0; i < aliases.size(); i++) {
            names[i] = aliases.get(i).getInternalName();
        }

        return nodeFactory.createImport(slots, node.getInternalModule(), names);
    }

// @Override
    public Object visitKeyword(keyword node) throws Exception {
        PNode value = (PNode) visit(node.getInternalValue());

        return nodeFactory.createKeywordLiteral(value, node.getInternalArg());
    }

    @Override
    public Object visitCall(Call node) throws Exception {
        PNode callee = (PNode) visit(node.getInternalFunc());
        List<PNode> arguments = walkExprList(node.getInternalArgs());
        PNode[] argumentsArray = arguments.toArray(new PNode[arguments.size()]);

        List<PNode> keywords = walkKeywordList(node.getInternalKeywords());
        PNode[] keywordsArray = keywords.toArray(new PNode[keywords.size()]);

        if (callee instanceof AttributeRefNode) {
            AttributeRefNode attr = (AttributeRefNode) callee;
            return nodeFactory.createAttributeCall(attr.getOperand(), argumentsArray, attr.getName());
        }

        // Specializing call node.
        if (argumentsArray.length == 1 && keywordsArray.length == 0) {
            if (Options.OptimizeNode) {
                PCallable builtIn = null;
                if (callee instanceof ReadGlobalNode && (builtIn = GlobalScope.getTruffleBuiltIns().lookupMethod(((ReadGlobalNode) callee).getName())) != null) {
                    return nodeFactory.createCallBuiltInWithOneArgNoKeyword(builtIn, ((ReadGlobalNode) callee).getName(), argumentsArray[0]);
                } else {
                    return nodeFactory.createCallWithOneArgumentNoKeyword(callee, argumentsArray[0]);
                }
            } else {
                return nodeFactory.createCallWithOneArgumentNoKeyword(callee, argumentsArray[0]);
            }
        } else if (argumentsArray.length == 2 && keywordsArray.length == 0) {
            if (Options.OptimizeNode) {
                PCallable builtIn = null;
                if (callee instanceof ReadGlobalNode && (builtIn = GlobalScope.getTruffleBuiltIns().lookupMethod(((ReadGlobalNode) callee).getName())) != null) {
                    return nodeFactory.createCallBuiltInWithTwoArgsNoKeyword(builtIn, ((ReadGlobalNode) callee).getName(), argumentsArray[0], argumentsArray[1]);
                } else {
                    return nodeFactory.createCallWithTwoArgumentsNoKeyword(callee, argumentsArray[0], argumentsArray[1]);
                }
            } else {
                return nodeFactory.createCallWithTwoArgumentsNoKeyword(callee, argumentsArray[0], argumentsArray[1]);
            }
        }

        if (Options.OptimizeNode) {
            PCallable builtIn = null;
            if (callee instanceof ReadGlobalNode && (builtIn = GlobalScope.getTruffleBuiltIns().lookupMethod(((ReadGlobalNode) callee).getName())) != null) {
                return nodeFactory.createCallBuiltIn(builtIn, ((ReadGlobalNode) callee).getName(), argumentsArray, keywordsArray);
            } else {
                return nodeFactory.createCall(callee, argumentsArray, keywordsArray);
            }
        } else {
            return nodeFactory.createCall(callee, argumentsArray, keywordsArray);
        }
    }

    @Override
    public Object visitName(Name node) throws Exception {
        expr_contextType context = node.getInternalCtx();

        if (context == expr_contextType.Param) {
            FrameSlot slot = getFrameSlot(node);
            ReadArgumentNode right = new ReadArgumentNode(slot.getIndex());
            return nodeFactory.createWriteLocal(right, getFrameSlot(node));
        }

        if (node.getInternalCtx() != expr_contextType.Load) {
            return convertWrite(node);
        }

        return convertRead(node);
    }

// @Override
// public Object visitNone(None node) throws Exception {
// return nodeFactory.createNoneLiteral();
// }
//
// @Override
// public Object visitTrue(True node) throws Exception {
// return nodeFactory.createBooleanLiteral(true);
// }
//
// @Override
// public Object visitFalse(False node) throws Exception {
// return nodeFactory.createBooleanLiteral(false);
// }

    PNode convertRead(Name node) {
        String name = node.getInternalId();
        FrameSlot slot = getFrameSlot(node);

        if (slot != null) {
            if (slot instanceof EnvironmentFrameSlot) {
                return nodeFactory.createReadEnvironment(slot, ((EnvironmentFrameSlot) slot).getLevel());
            }

            return nodeFactory.createReadLocal(slot);
        } else {
            return nodeFactory.createReadGlobal(name);
        }
    }

    PNode convertWrite(Name node) {
        FrameSlot slot = getFrameSlot(node);

        if (slot != null) {
            return nodeFactory.createWriteLocal(PNode.DUMMY_NODE, slot);
        } else {
            String name = node.getInternalId();
            return nodeFactory.createWriteGlobal(name, PNode.DUMMY_NODE);
        }
    }

    @Override
    public Object visitExpr(Expr node) throws Exception {
        return visit(node.getInternalValue());
    }

    @Override
    public Object visitList(org.python.antlr.ast.List node) throws Exception {
        List<PNode> elts = walkExprList(node.getInternalElts());
        assert !isLeftHandSide : "Left hand side node should not reach here!";
        return nodeFactory.createListLiteral(elts);
    }

    @Override
    public Object visitTuple(Tuple node) throws Exception {
        List<PNode> elts = walkExprList(node.getInternalElts());
        assert !isLeftHandSide : "Left hand side node should not reach here!";
        return nodeFactory.createTupleLiteral(elts);
    }

    @Override
    public Object visitDict(Dict node) throws Exception {
        List<PNode> keys = walkExprList(node.getInternalKeys());
        List<PNode> vals = walkExprList(node.getInternalValues());
        return nodeFactory.createDictLiteral(keys, vals);
    }

    @Override
    public Object visitAugAssign(AugAssign node) throws Exception {
        PNode target = (PNode) visit(node.getInternalTarget());
        PNode value = (PNode) visit(node.getInternalValue());

        PNode expr;
        if (target instanceof FrameSlotNode) {
            FrameSlot slot = ((FrameSlotNode) target).getSlot();
            // Only works for locals
            PNode read = nodeFactory.createReadLocal(slot);
            PNode binaryOp = nodeFactory.createBinaryOperation(node.getInternalOp(), read, value);
            expr = nodeFactory.createWriteLocal(binaryOp, slot);
        } else if (target instanceof SubscriptLoadNode) {
            SubscriptLoadNode read = (SubscriptLoadNode) target;
            PNode binaryOp = nodeFactory.createBinaryOperation(node.getInternalOp(), read, value);
            expr = nodeFactory.createSubscriptStore(read.getPrimary(), read.getSlice(), binaryOp);
        } else if (target instanceof WriteGlobalNode) {
            WriteGlobalNode writeGlobal = (WriteGlobalNode) target;
            PNode readGlobal = nodeFactory.createReadGlobal(writeGlobal.getName());
            PNode binaryOp = nodeFactory.createBinaryOperation(node.getInternalOp(), readGlobal, value);
            expr = nodeFactory.createWriteGlobal(writeGlobal.getName(), binaryOp);
        } else {
            throw new NotCovered();
        }

        return expr;
    }

    @Override
    public Object visitAssign(Assign node) throws Exception {
        expr rhs = node.getInternalValue();
        List<expr> lhs = node.getInternalTargets();
        expr exprTarget = lhs.get(0);

        /**
         * Multi-assignment or unpacking assignment. In other words, multiple assignment target
         * exist.
         */
        if (lhs.size() == 1 && isDecomposable(exprTarget)) {
            List<expr> targets = decompose(exprTarget);

            if (isDecomposable(rhs)) {
                List<expr> rights = decompose(rhs);

                if (targets.size() == rights.size()) {
                    return transformBalancedMultiAssignment(targets, rights);
                } else {
                    throw new RuntimeException("Unbalanced multi-assignment");
                }
            } else {
                return transformUnpackingAssignment(targets, rhs);
            }
        }

        PNode right = (PNode) visit(node.getInternalValue());

        isLeftHandSide = true;
        List<PNode> targets = walkExprList(node.getInternalTargets());
        isLeftHandSide = false;

        if (targets.size() == 1) {
            return processSingleAssignment(targets.get(0), right);
        } else {
            /**
             * Chained assignments. <br>
             * a = b = 42
             */
            List<PNode> assignments = new ArrayList<>();

            for (Node target : targets) {
                assignments.add(processSingleAssignment(target, right));
            }

            return nodeFactory.createBlock(assignments);
        }
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
            throw new RuntimeException("Unexpected decomposable type");
        }
    }

    private BlockNode transformBalancedMultiAssignment(List<expr> lhs, List<expr> rhs) throws Exception {
        /**
         * Transform a, b = c, d. <br>
         * To: temp_c = c; temp_d = d; a = temp_c; b = temp_d
         */
        List<PNode> rights = walkExprList(rhs);
        List<PNode> tempWrites = makeTemporaryWrites(rights);

        isLeftHandSide = true;
        List<PNode> targets = walkLeftHandSideList(lhs);
        isLeftHandSide = false;

        for (int i = 0; i < targets.size(); i++) {
            if (i < lhs.size()) {
                PNode read = ((WriteNode) tempWrites.get(i)).makeReadNode();
                StatementNode tempWrite = ((Amendable) targets.get(i)).updateRhs(read);
                tempWrites.add(tempWrite);
            } else {
                tempWrites.add(targets.get(i));
            }
        }

        return nodeFactory.createBlock(tempWrites);
    }

    private BlockNode transformUnpackingAssignment(List<expr> lhs, expr right) throws Exception {
        /**
         * Transform a, b = c. <br>
         * To: temp_c = c; a = temp_c[0]; b = temp_d[1]
         */
        List<PNode> writes = new ArrayList<>();
        PNode rhs = (PNode) visit(right);
        Amendable tempWrite = (Amendable) makeTemporaryWrite();
        writes.add(tempWrite.updateRhs(rhs));

        isLeftHandSide = true;
        List<PNode> targets = walkLeftHandSideList(lhs);
        isLeftHandSide = false;

        writes.addAll(processDecomposedTargetList(targets, lhs.size(), (PNode) tempWrite, true));
        return nodeFactory.createBlock(writes);
    }

    private List<PNode> walkLeftHandSideList(List<expr> lhs) throws Exception {
        List<PNode> writes = new ArrayList<>();
        List<PNode> additionalWrites = new ArrayList<>();

        for (int i = 0; i < lhs.size(); i++) {
            expr target = lhs.get(i);

            if (isDecomposable(target)) {
                PNode tempWrite = makeTemporaryWrite();
                writes.add(tempWrite);
                List<expr> targets = decompose(target);
                List<PNode> nestedWrites = walkLeftHandSideList(targets);
                additionalWrites.addAll(processDecomposedTargetList(nestedWrites, targets.size(), tempWrite, true));
            } else {
                writes.add((PNode) visit(target));
            }
        }

        writes.addAll(additionalWrites);
        return writes;
    }

    private List<PNode> processDecomposedTargetList(List<PNode> nestedWrites, int sizeOfCurrentLevelLeftHandSide, PNode tempWrite, boolean isUnpacking) {
        for (int idx = 0; idx < nestedWrites.size(); idx++) {
            if (idx < sizeOfCurrentLevelLeftHandSide) {
                PNode transformedRhs;

                if (isUnpacking) {
                    transformedRhs = makeSubscriptLoad((WriteLocalNode) tempWrite, idx);
                } else {
                    transformedRhs = ((WriteNode) tempWrite).makeReadNode();
                }

                StatementNode write = ((Amendable) nestedWrites.get(idx)).updateRhs(transformedRhs);
                nestedWrites.set(idx, write);
            }
        }

        return nestedWrites;
    }

    private List<PNode> makeTemporaryWrites(List<PNode> rights) {
        List<PNode> tempWrites = new ArrayList<>();

        for (int i = 0; i < rights.size(); i++) {
            PNode right = rights.get(i);
            StatementNode tempWrite = ((Amendable) makeTemporaryWrite()).updateRhs(right);
            tempWrites.add(tempWrite);
        }

        return tempWrites;
    }

    private PNode makeTemporaryWrite() {
        String tempName = TEMP_LOCAL_PREFIX + currentFrame.getSize();
        FrameSlot tempSlot = currentFrame.addFrameSlot(tempName);
        PNode tempWrite = nodeFactory.createWriteLocal(PNode.DUMMY_NODE, tempSlot);
        return tempWrite;
    }

    private PNode makeSubscriptLoad(WriteLocalNode write, int index) {
        PNode read = write.makeReadNode();
        PNode indexNode = nodeFactory.createIntegerLiteral(index);
        PNode sload = nodeFactory.createSubscriptLoad(read, indexNode);
        return sload;
    }

    private PNode processSingleAssignment(Node target, PNode right) throws Exception {
        if (target instanceof Amendable) {
            Amendable lhTarget = (Amendable) target;
            return lhTarget.updateRhs(right);
        }

        throw new NotCovered();
    }

    @Override
    public Object visitBinOp(BinOp node) throws Exception {
        PNode left = (PNode) visit(node.getInternalLeft());
        PNode right = (PNode) visit(node.getInternalRight());
        operatorType op = node.getInternalOp();
        return nodeFactory.createBinaryOperation(op, left, right);
    }

    @Override
    public Object visitBoolOp(BoolOp node) throws Exception {
        boolopType op = node.getInternalOp();
        List<PNode> values = walkExprList(node.getInternalValues());
        PNode left = values.get(0);
        List<PNode> rights = values.subList(1, values.size());
        return nodeFactory.createBooleanOperations(left, op, rights);
    }

    @Override
    public Object visitCompare(Compare node) throws Exception {
        List<cmpopType> ops = node.getInternalOps();
        PNode left = (PNode) visit(node.getInternalLeft());
        List<PNode> rights = walkExprList(node.getInternalComparators());
        return nodeFactory.createComparisonOperations(left, ops, rights);
    }

    @Override
    public Object visitUnaryOp(UnaryOp node) throws Exception {
        unaryopType op = node.getInternalOp();
        PNode operand = (PNode) visit(node.getInternalOperand());
        return nodeFactory.createUnaryOperation(op, operand);
    }

    @Override
    public Object visitAttribute(Attribute node) throws Exception {
        PNode primary = (PNode) visit(node.getInternalValue());

        if (isLeftHandSide) {
            return nodeFactory.createAttributeUpdate(primary, node.getInternalAttr(), PNode.DUMMY_NODE);
        } else {
            return nodeFactory.createAttributeRef(primary, node.getInternalAttr());
        }
    }

    @Override
    public Object visitSlice(Slice node) throws Exception {
        PNode lower = (PNode) (node.getInternalLower() == null ? null : visit(node.getInternalLower()));
        PNode upper = (PNode) (node.getInternalUpper() == null ? null : visit(node.getInternalUpper()));
        PNode step = (PNode) (node.getInternalStep() == null ? null : visit(node.getInternalStep()));

        if (lower == null || lower instanceof NoneLiteralNode) {
            lower = nodeFactory.createIntegerLiteral(Integer.MIN_VALUE);
        }
        if (upper == null || upper instanceof NoneLiteralNode) {
            upper = nodeFactory.createIntegerLiteral(Integer.MIN_VALUE);
        }
        if (step == null || step instanceof NoneLiteralNode) {
            step = nodeFactory.createIntegerLiteral(1);
        }
        return nodeFactory.createSlice(lower, upper, step);
    }

    @Override
    public Object visitIndex(Index node) throws Exception {
        PNode index = (PNode) visit(node.getInternalValue());
        return nodeFactory.createIndex(index);
    }

    @Override
    public Object visitSubscript(Subscript node) throws Exception {
        PNode primary = (PNode) visit(node.getInternalValue());
        PNode slice = (PNode) visit(node.getInternalSlice());

        if (node.getInternalCtx() == expr_contextType.Load) {
            return nodeFactory.createSubscriptLoad(primary, slice);
        } else if (node.getInternalCtx() == expr_contextType.Store) {
            assert isLeftHandSide;
            return nodeFactory.createSubscriptStore(primary, slice, PNode.DUMMY_NODE);
        } else {
            return nodeFactory.createSubscriptLoad(primary, slice);
        }
    }

    @Override
    public Object visitListComp(ListComp node) throws Exception {
        assert node.getInternalGenerators().size() <= 1 : "More than one generator!";
        ComprehensionNode comprehension = (ComprehensionNode) visitComprehension(node.getInternalGenerators().get(0));
        return nodeFactory.createListComprehension(comprehension);
    }

    public Object visitComprehension(comprehension node) throws Exception {
        boolean isInner = true;

        Amendable incomplete = (Amendable) visit(node.getInternalTarget());
        StatementNode target = incomplete.updateRhs(nodeFactory.createRuntimeValueNode());
        PNode iterator = (PNode) visit(node.getInternalIter());

        // inner loop
        comprehension inner = environment.getInnerLoop(node);
        PNode innerLoop = inner != null ? (PNode) visitComprehension(inner) : null;
        isInner = inner != null ? false : true;

        // transformed loop body (only exist if it's inner most comprehension)
        expr body = environment.getLoopBody(node);
        PNode loopBody = body != null ? (PNode) visit(environment.getLoopBody(node)) : null;
        isInner = body != null ? true : false;

        // Just deal with one condition.
        List<expr> conditions = node.getInternalIfs();
        PNode condition = (conditions == null || conditions.isEmpty()) ? null : (PNode) visit(conditions.get(0));

        assert inner == null || body == null : "Cannot be inner and outer at the same time";

        if (isInner) {
            return nodeFactory.createInnerComprehension(target, iterator, nodeFactory.toBooleanCastNode(condition), loopBody);
        } else {
            return nodeFactory.createOuterComprehension(target, iterator, nodeFactory.toBooleanCastNode(condition), innerLoop);
        }
    }

    @Override
    public Object visitGeneratorExp(GeneratorExp node) throws Exception {
        beginScope(getFrameDescriptor(node));
        ComprehensionNode comprehension = (ComprehensionNode) visitComprehension(node.getInternalGenerators().get(0));
        GeneratorNode gnode = nodeFactory.createGenerator(comprehension);
        endScope();
        return nodeFactory.createGeneratorExpression(gnode, getFrameDescriptor(node));
    }

    @Override
    public Object visitReturn(Return node) throws Exception {
        PNode value = null;
        StatementNode returnNode = null;

        if (node.getInternalValue() != null) {
            value = (PNode) visit(node.getInternalValue());
        }

        if (value == null) {
            returnNode = nodeFactory.createReturn();
        } else {
            returnNode = nodeFactory.createExplicitReturn(value);
        }

        returnNode.setFuncRootNode(getCurrentFuncRoot());
        return returnNode;
    }

    @Override
    public Object visitBreak(Break node) throws Exception {
        StatementNode breakNode = nodeFactory.createBreak();
        breakNode.setLoopHeader(getCurrentLoopHeader());
        return breakNode;
    }

    @Override
    public Object visitIf(If node) throws Exception {
        List<PNode> then = visitStatements(node.getInternalBody());
        List<PNode> orelse = visitStatements(node.getInternalOrelse());
        PNode test = (PNode) visit(node.getInternalTest());
        BlockNode thenPart = nodeFactory.createBlock(then);
        BlockNode elsePart = nodeFactory.createBlock(orelse);

        StatementNode ifNode = nodeFactory.createIf(nodeFactory.toBooleanCastNode(test), thenPart, elsePart);

        ifNode.setFuncRootNode(getCurrentFuncRoot());
        thenPart.setFuncRootNode(getCurrentFuncRoot());
        elsePart.setFuncRootNode(getCurrentFuncRoot());

        thenPart.setLoopHeader(getCurrentLoopHeader());
        elsePart.setLoopHeader(getCurrentLoopHeader());

        return ifNode;
    }

    @Override
    public Object visitWhile(While node) throws Exception {
        PNode test = (PNode) visit(node.getInternalTest());

        /**
         * Special case for while true
         */
        if (test instanceof BooleanLiteralNode && ((BooleanLiteralNode) test).getValue()) {
            StatementNode whileTrueNode = nodeFactory.createWhileTrue(null);

            loopHeaders.push(whileTrueNode);

            List<PNode> body = visitStatements(node.getInternalBody());
            BlockNode bodyPart = nodeFactory.createBlock(body);

            bodyPart.setLoopHeader(getCurrentLoopHeader());

            bodyPart.setFuncRootNode(getCurrentFuncRoot());
            whileTrueNode.setFuncRootNode(getCurrentFuncRoot());

            ((WhileTrueNode) whileTrueNode).setInternal(bodyPart);
            loopHeaders.pop();

            return whileTrueNode;
        } else {
            StatementNode whileNode = nodeFactory.createWhile(nodeFactory.toBooleanCastNode(test), null, null);

            loopHeaders.push(whileNode);

            List<PNode> body = visitStatements(node.getInternalBody());
            List<PNode> orelse = visitStatements(node.getInternalOrelse());
            BlockNode bodyPart = nodeFactory.createBlock(body);
            BlockNode orelsePart = nodeFactory.createBlock(orelse);

            bodyPart.setLoopHeader(getCurrentLoopHeader());
            orelsePart.setLoopHeader(getCurrentLoopHeader());

            bodyPart.setFuncRootNode(getCurrentFuncRoot());
            orelsePart.setFuncRootNode(getCurrentFuncRoot());
            whileNode.setFuncRootNode(getCurrentFuncRoot());

            ((WhileNode) whileNode).setInternal(bodyPart, orelsePart);
            loopHeaders.pop();

            return whileNode;
        }
    }

    @Override
    public Object visitFor(For node) throws Exception {
        List<expr> lhs = new ArrayList<>();
        lhs.add(node.getInternalTarget());

        isLeftHandSide = true;
        List<PNode> targets = walkLeftHandSideList(lhs);
        isLeftHandSide = false;

        Amendable incomplete = (Amendable) targets.remove(0);
        PNode runtimeValue = nodeFactory.createRuntimeValueNode();
        StatementNode iteratorWrite = incomplete.updateRhs(runtimeValue);

        PNode iter = (PNode) visit(node.getInternalIter());
        StatementNode forNode = dirtySpecialization(iteratorWrite, iter);
        forNode.setFuncRootNode(getCurrentFuncRoot());

        loopHeaders.push(forNode);

        List<PNode> body = visitStatements(node.getInternalBody());
        List<PNode> orelse = visitStatements(node.getInternalOrelse());
        body.addAll(0, targets);
        BlockNode bodyPart = nodeFactory.createBlock(body);
        BlockNode orelsePart = nodeFactory.createBlock(orelse);

        bodyPart.setLoopHeader(getCurrentLoopHeader());
        orelsePart.setLoopHeader(getCurrentLoopHeader());

        bodyPart.setFuncRootNode(getCurrentFuncRoot());
        orelsePart.setFuncRootNode(getCurrentFuncRoot());
        forNode.setFuncRootNode(getCurrentFuncRoot());

        if (forNode instanceof ForNode) {
            ((ForNode) forNode).setInternal(bodyPart, orelsePart);
        } else if (forNode instanceof ForRangeWithOneValueNode) {
            ((ForRangeWithOneValueNode) forNode).setInternal(bodyPart, orelsePart);
        } else {
            ((ForRangeWithTwoValuesNode) forNode).setInternal(bodyPart, orelsePart);
        }

        loopHeaders.pop();
        return forNode;
    }

    private StatementNode dirtySpecialization(StatementNode target, PNode iter) {
        StatementNode forNode;
        if (Options.OptimizeNode) {
            if (iter instanceof CallBuiltInWithOneArgNoKeywordNode && ((CallBuiltInWithOneArgNoKeywordNode) iter).getName().equals("range")) {
                forNode = nodeFactory.createForRangeWithOneValue(target, ((CallBuiltInWithOneArgNoKeywordNode) iter).getArgument(), null, null);
            } else if (iter instanceof CallBuiltInWithTwoArgsNoKeywordNode && ((CallBuiltInWithTwoArgsNoKeywordNode) iter).getName().equals("range")) {
                forNode = nodeFactory.createForRangeWithTwoValues(target, ((CallBuiltInWithTwoArgsNoKeywordNode) iter).getArg0(), ((CallBuiltInWithTwoArgsNoKeywordNode) iter).getArg1(), null, null);
            } else {
                forNode = nodeFactory.createFor(target, iter, null, null);
            }
        } else {
            forNode = nodeFactory.createFor(target, iter, null, null);
        }
        return forNode;
    }

    @Override
    public Object visitPrint(Print node) throws Exception {
        List<PNode> values = walkExprList(node.getInternalValues());
        StatementNode newNode = nodeFactory.createPrint(values, node.getInternalNl());

// if (node.getOutStream() != null) {
// ((PrintNode) newNode).setOutStream(node.getOutStream());
// }

        return newNode;
    }

    @Override
    public Object visitNum(Num node) throws Exception {
        Object value = node.getInternalN();

        if (value instanceof PyInteger) {
            return nodeFactory.createIntegerLiteral(((PyInteger) value).getValue());
        } else if (value instanceof PyLong) {
            return nodeFactory.createBigIntegerLiteral(((PyLong) value).getValue());
        } else if (value instanceof PyFloat) {
            return nodeFactory.createDoubleLiteral(((PyFloat) value).getValue());
        } else if (value instanceof PyComplex) {
            PyComplex pyComplex = (PyComplex) value;
            PComplex complex = new PComplex(pyComplex.real, pyComplex.imag);
            return nodeFactory.createComplexLiteral(complex);
        } else {
            throw new NotCovered();
        }
    }

    @Override
    public Object visitGlobal(Global node) throws Exception {
        return null;
    }

    @Override
    public Object visitYield(Yield node) throws Exception {
        isGenerator = true;
        PNode right = (PNode) visit(node.getInternalValue());
        return nodeFactory.createYield(right);
    }

    @Override
    public Object visitStr(Str node) throws Exception {
        PyString s = (PyString) node.getInternalS();
        return nodeFactory.createStringLiteral(s);
    }

    @Override
    public Object visitIfExp(IfExp node) throws Exception {
        PNode test = (PNode) visit(node.getInternalTest());
        PNode body = (PNode) visit(node.getInternalBody());
        PNode orelse = (PNode) visit(node.getInternalOrelse());

        return nodeFactory.createIfExpNode(test, body, orelse);
    }

// @Override
// protected Object unhandled_node(PythonTree node) throws Exception {
// throw new RuntimeException("Unhandled node " + node);
// }

    @SuppressWarnings("serial")
    class NotCovered extends RuntimeException {

        public NotCovered() {
            super("This case is not covered!");
        }

    }

}
