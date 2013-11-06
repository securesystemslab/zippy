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

import java.util.*;
import java.util.List;
import java.util.Set;

import org.python.antlr.*;
import org.python.antlr.ast.*;
import org.python.antlr.base.*;
import org.python.compiler.*;
import org.python.core.*;
import org.python.google.common.collect.*;

import com.oracle.truffle.api.*;
import com.oracle.truffle.api.frame.*;
import com.oracle.truffle.api.nodes.*;

import edu.uci.python.nodes.*;
import edu.uci.python.nodes.access.*;
import edu.uci.python.nodes.calls.*;
import edu.uci.python.nodes.expressions.*;
import edu.uci.python.nodes.literals.*;
import edu.uci.python.nodes.loop.*;
import edu.uci.python.nodes.objects.*;
import edu.uci.python.nodes.statements.*;
import edu.uci.python.nodes.translation.*;
import edu.uci.python.nodes.truffle.*;
import edu.uci.python.runtime.*;
import edu.uci.python.runtime.datatypes.*;

public class PythonTreeTranslator extends Visitor {

    private final PythonContext context;
    private final NodeFactory factory;
    private final TranslationEnvironment environment;
    private final LoopsBookKeeper loops;
    private final PythonParseResult result;

    private boolean isLeftHandSide = false;

    private boolean isGenerator = false;

    private static final String TEMP_LOCAL_PREFIX = "temp_";

    public PythonTreeTranslator(TranslationEnvironment environment, PythonContext context) {
        this.context = context;
        this.factory = new NodeFactory();
        this.environment = environment.resetScopeLevel();
        this.loops = new LoopsBookKeeper();
        this.result = new PythonParseResult();
    }

    public PythonParseResult translate(PythonTree root) {
        ModuleNode module;

        try {
            module = (ModuleNode) visit(root);
        } catch (Throwable t) {
            t.printStackTrace();
            throw new RuntimeException("Failed in " + this + " with error " + t);
        }

        result.setModule(module);
        return result;
    }

    @Override
    public Object visitModule(org.python.antlr.ast.Module node) throws Exception {
        environment.beginScope(node, ScopeInfo.ScopeKind.Module);

        List<PNode> body = visitStatements(node.getInternalBody());
        FrameDescriptor fd = environment.endScope(node);
        RootNode newNode = new NodeFactory().createModule(body, fd);
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
        /**
         * translate default arguments in FunctionDef's declaring scope.
         */
        List<PNode> defaultArgs = walkExprList(node.getInternalArgs().getInternalDefaults());
        environment.setDefaultArgs(defaultArgs);

        String name = node.getInternalName();
        FrameSlot slot = environment.findSlot(name);
        ScopeInfo.ScopeKind definingScopeKind = environment.getScopeKind();
        environment.beginScope(node, ScopeInfo.ScopeKind.Function);
        isGenerator = false;

        ParametersNode parameters = visitArgs(node.getInternalArgs());
        List<PNode> statements = visitStatements(node.getInternalBody());
        StatementNode body = factory.createBlock(statements);

        if (isGenerator) {
            body = new ASTLinearizer((BlockNode) body).linearize();
            RootNode genRoot = factory.createGeneratorRoot(name, parameters, body, factory.createReadLocalVariable(environment.getReturnSlot()));
            PNode funcDef = wrapRootNodeInFunctionDefinitnion(name, genRoot, parameters);
            PNode writeOrStore = wrapWithWriteOrStore(funcDef, definingScopeKind, slot, name);
            environment.endScope(node);
            return writeOrStore;
        }

        FunctionRootNode funcRoot = factory.createFunctionRoot(name, parameters, body, factory.createReadLocalVariable(environment.getReturnSlot()));
        result.addParsedFunction(name, funcRoot);
        PNode funcDef = wrapRootNodeInFunctionDefinitnion(name, funcRoot, parameters);
        PNode writeOrStore = wrapWithWriteOrStore(funcDef, definingScopeKind, slot, name);
        environment.endScope(node);
        return writeOrStore;
    }

    private PNode wrapRootNodeInFunctionDefinitnion(String name, RootNode root, ParametersNode parameters) {
        CallTarget ct = Truffle.getRuntime().createCallTarget(root, environment.getCurrentFrame());
        return factory.createFunctionDef(name, parameters, ct, environment.getCurrentFrame());
    }

    public PNode wrapWithWriteOrStore(PNode rhs, ScopeInfo.ScopeKind definingScope, FrameSlot slot, String name) {
        switch (definingScope) {
            case Module:
                PNode main = factory.createObjectLiteral(context.getPythonCore().getMainModule());
                return factory.createStoreAttribute(main, name, rhs);
            case GeneratorExpr:
            case Function:
                assert slot != null;
                return factory.createWriteLocalVariable(rhs, slot);
            case Class:
                return factory.createAddMethodNode((FunctionDefinitionNode) rhs);
            default:
                throw new IllegalStateException("Unexpected ScopeKind " + definingScope);
        }
    }

    public ParametersNode visitArgs(arguments node) throws Exception {
        // parse arguments
        new ArgListCompiler().visitArgs(node);

        List<PNode> args = new ArrayList<>();
        List<String> paramNames = new ArrayList<>();
        isLeftHandSide = true;
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

        int defaultArgsSize = node.getInternalDefaults().size();
        if (defaultArgsSize == 0) {
            if (args.size() == 1) {
                return factory.createParametersOfSizeOne(args.get(0), paramNames);
            } else if (args.size() == 2) {
                return factory.createParametersOfSizeTwo(args.get(0), args.get(1), paramNames);
            } else {
                return factory.createParametersWithNoDefaults(args, paramNames);
            }
        }

        return factory.createParametersWithDefaults(args, environment.getDefaultArgs(), paramNames);
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
            targets.add(visitKeyword(source));
        }

        return targets;
    }

    FrameSlot[] walkAliasList(List<alias> aliases) throws Exception {
        FrameSlot[] slots = new FrameSlot[aliases.size()];

        for (int i = 0; i < aliases.size(); i++) {
            slots[i] = environment.findSlot(aliases.get(i).getInternalName());
        }

        return slots;
    }

    private PNode createSingleImportStatement(alias aliaz, String fromModuleName, FrameSlot slot) {
        String importName = aliaz.getInternalName();
        String asName = aliaz.getInternalAsname();
        PNode importNode = factory.createImport(fromModuleName, importName);

        if (asName != null) {
            return wrapWithWriteOrStore(importNode, environment.getScopeKind(), slot, asName);
        } else {
            return wrapWithWriteOrStore(importNode, environment.getScopeKind(), slot, importName);
        }
    }

    @Override
    public Object visitImport(Import node) throws Exception {
        List<alias> aliases = node.getInternalNames();
        FrameSlot[] slots = walkAliasList(aliases);
        assert !aliases.isEmpty();

        if (aliases.size() == 1) {
            return createSingleImportStatement(aliases.get(0), null, slots[0]);
        }

        List<PNode> imports = new ArrayList<>();
        for (int i = 0; i < aliases.size(); i++) {
            imports.add(createSingleImportStatement(aliases.get(i), null, slots[i]));
        }

        return factory.createBlock(imports);
    }

    @Override
    public Object visitImportFrom(ImportFrom node) throws Exception {
        List<alias> aliases = node.getInternalNames();
        FrameSlot[] slots = walkAliasList(aliases);
        assert !aliases.isEmpty();

        if (aliases.size() == 1) {
            return createSingleImportStatement(aliases.get(0), node.getInternalModule(), slots[0]);
        }

        List<PNode> imports = new ArrayList<>();
        for (int i = 0; i < aliases.size(); i++) {
            imports.add(createSingleImportStatement(aliases.get(i), node.getInternalModule(), slots[i]));
        }

        return factory.createBlock(imports);
    }

    protected PNode visitKeyword(keyword node) throws Exception {
        PNode value = (PNode) visit(node.getInternalValue());
        return factory.createKeywordLiteral(value, node.getInternalArg());
    }

    @Override
    public Object visitClassDef(ClassDef node) throws Exception {
        String name = node.getInternalName();
        List<PNode> bases = walkExprList(node.getInternalBases());
        assert bases.size() <= 1 : "Multiple super class is not supported yet!";
        ScopeInfo.ScopeKind definingScopeKind = environment.getScopeKind();

        environment.beginScope(node, ScopeInfo.ScopeKind.Class);
        List<PNode> statements = visitStatements(node.getInternalBody());
        BlockNode body = factory.createBlock(statements);
        FunctionRootNode methodRoot = factory.createFunctionRoot(name, ParametersNode.EMPTY_PARAMS, body, PNode.EMPTYNODE);
        CallTarget ct = Truffle.getRuntime().createCallTarget(methodRoot, environment.getCurrentFrame());
        FunctionDefinitionNode funcDef = (FunctionDefinitionNode) factory.createFunctionDef("(" + name + "-def)", ParametersNode.EMPTY_PARAMS, ct, environment.getCurrentFrame());
        environment.endScope(node);

        // The default super class is the <class 'object'>.
        PNode base;
        if (bases.size() == 0 || bases.get(0) == null) {
            base = factory.createObjectLiteral(context.getPythonCore().getObjectClass());
        } else {
            base = bases.get(0);
        }

        PNode classDef = factory.createClassDef(name, base, funcDef);
        return wrapWithWriteOrStore(classDef, definingScopeKind, null, name);
    }

    @Override
    public Object visitCall(Call node) throws Exception {
        PNode callee = (PNode) visit(node.getInternalFunc());
        List<PNode> arguments = walkExprList(node.getInternalArgs());
        PNode[] argumentsArray = arguments.toArray(new PNode[arguments.size()]);

        List<PNode> keywords = walkKeywordList(node.getInternalKeywords());
        PNode[] keywordsArray = keywords.toArray(new PNode[keywords.size()]);

        if (callee instanceof LoadAttributeNode) {
            LoadAttributeNode attr = (LoadAttributeNode) callee;
            return factory.createAttributeCall(attr.getPrimary(), argumentsArray, attr.getAttributeId());
        }

        return factory.createCallFunction(callee, argumentsArray, keywordsArray);
    }

    @Override
    public Object visitName(Name node) throws Exception {
        String name = node.getInternalId();

        if (name.equals("None")) {
            return factory.createNoneLiteral();
        } else if (name.equals("True")) {
            return factory.createBooleanLiteral(true);
        } else if (name.equals("False")) {
            return factory.createBooleanLiteral(false);
        }

        expr_contextType econtext = node.getInternalCtx();

        if (econtext == expr_contextType.Param) {
            FrameSlot slot = environment.findSlot(node.getInternalId());
            ReadArgumentNode right = new ReadArgumentNode(slot.getIndex());
            return factory.createWriteLocalVariable(right, slot);
        }

        if (node.getInternalCtx() != expr_contextType.Load) {
            return convertWrite(node);
        }

        return convertRead(node);
    }

    PNode convertRead(Name node) {
        String name = node.getInternalId();
        FrameSlot slot = environment.findSlot(name);

        switch (environment.getScopeKind()) {
            case Module:
                return factory.createReadGlobalScope(context, context.getPythonCore().getMainModule(), name);
            case GeneratorExpr:
            case Function:
                if (slot == null) {
                    return factory.createReadGlobalScope(context, context.getPythonCore().getMainModule(), name);
                }

                if (slot instanceof EnvironmentFrameSlot) {
                    return factory.createReadLevelVariable(slot, ((EnvironmentFrameSlot) slot).getLevel());
                }

                return factory.createReadLocalVariable(slot);
            case Class:
            default:
                throw new IllegalStateException("Unexpected scopeKind " + environment.getScopeKind());
        }
    }

    PNode convertWrite(Name node) {
        String name = node.getInternalId();
        FrameSlot slot = environment.findSlot(name);
        PNode main = factory.createObjectLiteral(context.getPythonCore().getMainModule());

        switch (environment.getScopeKind()) {
            case Module:
                return factory.createStoreAttribute(main, name, PNode.EMPTYNODE);
            case GeneratorExpr:
            case Function:
                if (slot == null) {
                    return factory.createStoreAttribute(main, name, PNode.EMPTYNODE);
                }
                return factory.createWriteLocalVariable(PNode.EMPTYNODE, slot);
            case Class:
                return factory.createAddClassAttribute(name, PNode.EMPTYNODE);
            default:
                throw new IllegalStateException("Unexpected scopeKind " + environment.getScopeKind());
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
        return factory.createListLiteral(elts);
    }

    @Override
    public Object visitSet(org.python.antlr.ast.Set node) throws Exception {
        List<PNode> elts = walkExprList(node.getInternalElts());
        assert !isLeftHandSide : "Left hand side node should not reach here!";
        Set<PNode> setFromLost = new HashSet<>();
        for (PNode listNode : elts) {
            setFromLost.add(listNode);
        }

        return factory.createSetLiteral(setFromLost);
    }

    @Override
    public Object visitTuple(Tuple node) throws Exception {
        List<PNode> elts = walkExprList(node.getInternalElts());
        assert !isLeftHandSide : "Left hand side node should not reach here!";
        return factory.createTupleLiteral(elts);
    }

    @Override
    public Object visitDict(Dict node) throws Exception {
        List<PNode> keys = walkExprList(node.getInternalKeys());
        List<PNode> vals = walkExprList(node.getInternalValues());
        return factory.createDictLiteral(keys, vals);
    }

    @Override
    public Object visitAugAssign(AugAssign node) throws Exception {
        isLeftHandSide = true;
        PNode target = (PNode) visit(node.getInternalTarget());
        isLeftHandSide = false;
        PNode value = (PNode) visit(node.getInternalValue());

        /**
         * TODO: AugAssign should be translated to in-place operations.<br>
         * The assignment should be ScopeKind sensitive too!
         */
        PNode expr;
        if (target instanceof FrameSlotNode) {
            FrameSlot slot = ((FrameSlotNode) target).getSlot();
            // Only works for locals
            PNode read = factory.createReadLocalVariable(slot);
            PNode binaryOp = factory.createBinaryOperation(node.getInternalOp(), read, value);
            expr = factory.createWriteLocalVariable(binaryOp, slot);
        } else if (target instanceof SubscriptLoadNode) {
            SubscriptLoadNode read = (SubscriptLoadNode) target;
            PNode binaryOp = factory.createBinaryOperation(node.getInternalOp(), read, value);
            expr = factory.createSubscriptStore(read.getPrimary(), read.getSlice(), binaryOp);
        } else if (target instanceof StoreAttributeNode) {
            LoadAttributeNode read = (LoadAttributeNode) ((StoreAttributeNode) target).makeReadNode();
            PNode binaryOp = factory.createBinaryOperation(node.getInternalOp(), read, value);
            expr = factory.createStoreAttribute(read.getPrimary(), read.getAttributeId(), binaryOp);
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

            return factory.createBlock(assignments);
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
                PNode tempWrite = ((Amendable) targets.get(i)).updateRhs(read);
                tempWrites.add(tempWrite);
            } else {
                tempWrites.add(targets.get(i));
            }
        }

        return factory.createBlock(tempWrites);
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
        return factory.createBlock(writes);
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

                PNode write = ((Amendable) nestedWrites.get(idx)).updateRhs(transformedRhs);
                nestedWrites.set(idx, write);
            }
        }

        return nestedWrites;
    }

    private List<PNode> makeTemporaryWrites(List<PNode> rights) {
        List<PNode> tempWrites = new ArrayList<>();

        for (int i = 0; i < rights.size(); i++) {
            PNode right = rights.get(i);
            PNode tempWrite = ((Amendable) makeTemporaryWrite()).updateRhs(right);
            tempWrites.add(tempWrite);
        }

        return tempWrites;
    }

    private PNode makeTemporaryWrite() {
        String tempName = TEMP_LOCAL_PREFIX + environment.getCurrentFrameSize();
        FrameSlot tempSlot = environment.createLocal(tempName);
        PNode tempWrite = factory.createWriteLocalVariable(PNode.EMPTYNODE, tempSlot);
        return tempWrite;
    }

    private PNode makeSubscriptLoad(WriteLocalNode write, int index) {
        PNode read = write.makeReadNode();
        PNode indexNode = factory.createIntegerLiteral(index);
        PNode sload = factory.createSubscriptLoad(read, indexNode);
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
        return factory.createBinaryOperation(op, left, right);
    }

    @Override
    public Object visitBoolOp(BoolOp node) throws Exception {
        boolopType op = node.getInternalOp();
        List<PNode> values = walkExprList(node.getInternalValues());
        PNode left = values.get(0);
        List<PNode> rights = values.subList(1, values.size());
        return factory.createBooleanOperations(left, op, rights);
    }

    @Override
    public Object visitCompare(Compare node) throws Exception {
        List<cmpopType> ops = node.getInternalOps();
        PNode left = (PNode) visit(node.getInternalLeft());
        List<PNode> rights = walkExprList(node.getInternalComparators());
        return factory.createComparisonOperations(left, ops, rights);
    }

    @Override
    public Object visitUnaryOp(UnaryOp node) throws Exception {
        unaryopType op = node.getInternalOp();
        PNode operand = (PNode) visit(node.getInternalOperand());
        return factory.createUnaryOperation(op, operand);
    }

    @Override
    public Object visitAttribute(Attribute node) throws Exception {
        PNode primary = (PNode) visit(node.getInternalValue());

        if (isLeftHandSide) {
            return factory.createStoreAttribute(primary, node.getInternalAttr(), PNode.EMPTYNODE);
        } else {
            return factory.createLoadAttribute(primary, node.getInternalAttr());
        }
    }

    @Override
    public Object visitSlice(Slice node) throws Exception {
        PNode lower = (PNode) (node.getInternalLower() == null ? null : visit(node.getInternalLower()));
        PNode upper = (PNode) (node.getInternalUpper() == null ? null : visit(node.getInternalUpper()));
        PNode step = (PNode) (node.getInternalStep() == null ? null : visit(node.getInternalStep()));

        if (lower == null || lower instanceof NoneLiteralNode) {
            lower = factory.createIntegerLiteral(Integer.MIN_VALUE);
        }
        if (upper == null || upper instanceof NoneLiteralNode) {
            upper = factory.createIntegerLiteral(Integer.MIN_VALUE);
        }
        if (step == null || step instanceof NoneLiteralNode) {
            step = factory.createIntegerLiteral(1);
        }
        return factory.createSlice(lower, upper, step);
    }

    @Override
    public Object visitIndex(Index node) throws Exception {
        PNode index = (PNode) visit(node.getInternalValue());
        return factory.createIndex(index);
    }

    @Override
    public Object visitSubscript(Subscript node) throws Exception {
        PNode primary = (PNode) visit(node.getInternalValue());
        PNode slice = (PNode) visit(node.getInternalSlice());

        if (node.getInternalCtx() == expr_contextType.Load) {
            return factory.createSubscriptLoad(primary, slice);
        } else if (node.getInternalCtx() == expr_contextType.Store) {
            assert isLeftHandSide;

            if (primary instanceof StoreAttributeNode) {
                primary = ((StoreAttributeNode) primary).makeReadNode();
            }

            return factory.createSubscriptStore(primary, slice, PNode.EMPTYNODE);
        } else {
            return factory.createSubscriptLoad(primary, slice);
        }
    }

    @Override
    public Object visitListComp(ListComp node) throws Exception {
        ComprehensionNode comp = (ComprehensionNode) visitComprehensions(node.getInternalGenerators(), node.getInternalElt());
        return factory.createListComprehension(comp);
    }

    private Object visitComprehensions(List<comprehension> generators, expr body) throws Exception {
        assert body != null;
        List<comprehension> reversed = Lists.reverse(generators);
        PNode current = null;

        for (int i = 0; i < reversed.size(); i++) {
            comprehension comp = reversed.get(i);

            // target and iterator
            Amendable incomplete = (Amendable) visit(comp.getInternalTarget());
            PNode target = incomplete.updateRhs(factory.createRuntimeValueNode());
            PNode iterator = (PNode) visit(comp.getInternalIter());

            // Just deal with one condition.
            List<expr> conditions = comp.getInternalIfs();
            PNode condition = (conditions == null || conditions.isEmpty()) ? null : (PNode) visit(conditions.get(0));

            if (i == 0) {
                // inner most
                PNode loopBody = (PNode) visit(body);
                current = factory.createInnerComprehension(target, iterator, factory.toBooleanCastNode(condition), loopBody);
            } else if (i < reversed.size() - 1) {
                // inner
                current = factory.createInnerComprehension(target, iterator, factory.toBooleanCastNode(condition), current);
            } else {
                // outer
                current = factory.createOuterComprehension(target, iterator, factory.toBooleanCastNode(condition), current);
            }
        }

        assert current != null;
        return current;
    }

    @Override
    public Object visitGeneratorExp(GeneratorExp node) throws Exception {
        environment.beginScope(node, ScopeInfo.ScopeKind.GeneratorExpr);
        ComprehensionNode comprehension = (ComprehensionNode) visitComprehensions(node.getInternalGenerators(), node.getInternalElt());
        GeneratorNode gnode = factory.createGenerator(comprehension, factory.createReadLocalVariable(environment.getReturnSlot()));
        FrameDescriptor fd = environment.endScope(node);
        return factory.createGeneratorExpression(gnode, fd);
    }

    @Override
    public Object visitReturn(Return node) throws Exception {
        PNode value = null;
        StatementNode returnNode = null;

        if (node.getInternalValue() != null) {
            value = (PNode) visit(node.getInternalValue());
        }

        if (value == null) {
            returnNode = factory.createReturn();
        } else if (TranslationOptions.RETURN_VALUE_IN_FRAME) {
            PNode write = factory.createWriteLocalVariable(value, environment.getReturnSlot());
            returnNode = factory.createFrameReturn(write);
        } else {
            returnNode = factory.createExplicitReturn(value);
        }

        return returnNode;
    }

    @Override
    public Object visitBreak(Break node) throws Exception {
        loops.addBreak();
        return factory.createBreak();
    }

    @Override
    public Object visitContinue(Continue node) throws Exception {
        loops.addContinue();
        return factory.createContinue();
    }

    @Override
    public Object visitIf(If node) throws Exception {
        List<PNode> then = visitStatements(node.getInternalBody());
        List<PNode> orelse = visitStatements(node.getInternalOrelse());
        PNode test = (PNode) visit(node.getInternalTest());
        BlockNode thenPart = factory.createBlock(then);
        BlockNode elsePart = factory.createBlock(orelse);
        return factory.createIf(factory.toBooleanCastNode(test), thenPart, elsePart);
    }

    @Override
    public Object visitWhile(While node) throws Exception {
        loops.beginLoop(node);
        PNode test = (PNode) visit(node.getInternalTest());
        List<PNode> body = visitStatements(node.getInternalBody());
        List<PNode> orelse = visitStatements(node.getInternalOrelse());
        BlockNode bodyPart = factory.createBlock(body);
        BlockNode orelsePart = factory.createBlock(orelse);
        return createWhileNode(test, bodyPart, orelsePart, loops.endLoop());
    }

    private StatementNode createWhileNode(PNode test, BlockNode body, BlockNode orelse, LoopInfo info) {
        StatementNode wrappedBody = body;
        if (info.hasContinue()) {
            wrappedBody = factory.createContinueTarget(body);
        }

        StatementNode whileNode = factory.createWhile(factory.toBooleanCastNode(test), wrappedBody);

        if (!orelse.isEmpty()) {
            whileNode = factory.createElse(whileNode, orelse);
        }

        if (info.hasBreak()) {
            return factory.createBreakTarget(whileNode);
        } else {
            return whileNode;
        }
    }

    @Override
    public Object visitFor(For node) throws Exception {
        loops.beginLoop(node);
        List<expr> lhs = new ArrayList<>();
        lhs.add(node.getInternalTarget());

        isLeftHandSide = true;
        List<PNode> targets = walkLeftHandSideList(lhs);
        isLeftHandSide = false;

        Amendable incomplete = (Amendable) targets.remove(0);
        PNode runtimeValue = factory.createRuntimeValueNode();
        PNode iteratorWrite = incomplete.updateRhs(runtimeValue);

        PNode iter = (PNode) visit(node.getInternalIter());
        List<PNode> body = visitStatements(node.getInternalBody());
        List<PNode> orelse = visitStatements(node.getInternalOrelse());
        body.addAll(0, targets);
        BlockNode bodyPart = factory.createBlock(body);
        BlockNode orelsePart = factory.createBlock(orelse);
        return createForNode(iteratorWrite, iter, bodyPart, orelsePart, loops.endLoop());
    }

    private StatementNode createForNode(PNode target, PNode iter, BlockNode body, BlockNode orelse, LoopInfo info) {
        StatementNode wrappedBody = body;
        if (info.hasContinue()) {
            wrappedBody = factory.createContinueTarget(body);
        }

        StatementNode forNode;
        if (environment.isInFunctionScope() && target instanceof WriteLocalNode) {
            WriteLocalNode wtarget = (WriteLocalNode) target;
            wtarget = (WriteLocalNode) wtarget.updateRhs(null);
            forNode = factory.createForWithLocalTarget(wtarget, iter, wrappedBody);
        } else {
            forNode = factory.createFor(target, iter, wrappedBody);
        }

        if (!orelse.isEmpty()) {
            forNode = factory.createElse(forNode, orelse);
        }

        if (info.hasBreak()) {
            return factory.createBreakTarget(forNode);
        } else {
            return forNode;
        }
    }

    @Override
    public Object visitPrint(Print node) throws Exception {
        /**
         * This is a workaround for what produced by Jython's parser. <br>
         * It parses print to a statement with multiple arguments nested in a tuple. It causes the
         * output to always be a tuple string. Therefore, unwrap the tuple is necessary. <br>
         * However, we cannot distinguish between a real tuple parameter and an artificial one. The
         * real fix should be in the parser.
         */
        List<expr> exprs = node.getInternalValues();
        if (exprs.size() == 1 && exprs.get(0) instanceof Tuple) {
            Tuple tuple = (Tuple) exprs.get(0);
            List<PNode> values = walkExprList(tuple.getInternalElts());
            return factory.createPrint(values, node.getInternalNl(), context);
        } else {
            List<PNode> values = walkExprList(node.getInternalValues());
            StatementNode newNode = factory.createPrint(values, node.getInternalNl(), context);
            return newNode;
        }
    }

    @Override
    public Object visitNum(Num node) throws Exception {
        Object value = node.getInternalN();

        if (value instanceof PyInteger) {
            return factory.createIntegerLiteral(((PyInteger) value).getValue());
        } else if (value instanceof PyLong) {
            return factory.createBigIntegerLiteral(((PyLong) value).getValue());
        } else if (value instanceof PyFloat) {
            return factory.createDoubleLiteral(((PyFloat) value).getValue());
        } else if (value instanceof PyComplex) {
            PyComplex pyComplex = (PyComplex) value;
            PComplex complex = new PComplex(pyComplex.real, pyComplex.imag);
            return factory.createComplexLiteral(complex);
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
        return factory.createYield(right);
    }

    @Override
    public Object visitStr(Str node) throws Exception {
        PyString s = (PyString) node.getInternalS();
        return factory.createStringLiteral(s);
    }

    @Override
    public Object visitIfExp(IfExp node) throws Exception {
        PNode test = (PNode) visit(node.getInternalTest());
        PNode body = (PNode) visit(node.getInternalBody());
        PNode orelse = (PNode) visit(node.getInternalOrelse());

        return factory.createIfExpNode(test, body, orelse);
    }

    @Override
    public Object visitPass(Pass node) throws Exception {
        return null;
    }

    @Override
    public Object visitTryExcept(TryExcept node) throws Exception {
        StatementNode retVal = null;
        List<PNode> b = visitStatements(node.getInternalBody());
        List<PNode> o = visitStatements(node.getInternalOrelse());
        BlockNode body = null;
        BlockNode orelse = null;

        List<excepthandler> excepts = node.getInternalHandlers();

        for (int i = 0; i < excepts.size(); i++) {
            if (i == 0) {
                body = factory.createBlock(b);
            } else {
                List<PNode> trynode = new ArrayList<>();
                trynode.add(retVal);
                body = factory.createBlock(trynode);
            }

            if (i == excepts.size() - 1) {
                orelse = factory.createBlock(o);
            }

            ExceptHandler except = (ExceptHandler) excepts.get(i);
            PNode exceptType = (PNode) visit(except.getInternalType());
            PNode exceptName = (except.getInternalName() == null) ? null : ((PNode) visit(except.getInternalName()));
            List<PNode> exceptbody = visitStatements(except.getInternalBody());
            BlockNode exceptBody = factory.createBlock(exceptbody);

            retVal = factory.createTryExceptNode(body, orelse, exceptType, exceptName, exceptBody);
        }

        return retVal;
    }

    @Override
    public Object visitTryFinally(TryFinally node) throws Exception {
        List<PNode> b = visitStatements(node.getInternalBody());
        List<PNode> f = visitStatements(node.getInternalFinalbody());
        BlockNode body = factory.createBlock(b);
        BlockNode finalbody = factory.createBlock(f);

        return factory.createTryFinallyNode(body, finalbody);
    }

    @Override
    public Object visitRaise(Raise node) throws Exception {
        PNode type = (PNode) visit(node.getInternalType());
        PNode inst = (node.getInternalInst() == null) ? null : (PNode) visit(node.getInternalInst());
// PNode tback = (node.getInternalTback() == null) ? null : (PNode) visit(node.getInternalTback());
        return factory.createRaiseNode(type, inst);
    }

    @Override
    public Object visitAssert(Assert node) throws Exception {
        PNode test = (PNode) visit(node.getInternalTest());
        BooleanCastNode condition = factory.toBooleanCastNode(test);
        PNode msg = node.getInternalMsg() == null ? null : (PNode) visit(node.getInternalMsg());
        return factory.createAssert(condition, msg);
    }

    // Checkstyle: resume

    @SuppressWarnings("serial")
    class NotCovered extends RuntimeException {

        public NotCovered() {
            super("This case is not covered!");
        }

    }
}
