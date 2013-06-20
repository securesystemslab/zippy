package org.python.ast;

import java.util.*;
import java.util.List;

import org.python.antlr.*;
import org.python.antlr.ast.*;
import org.python.antlr.base.*;
import org.python.ast.datatypes.*;
import org.python.ast.nodes.*;
import org.python.ast.nodes.expressions.*;
import org.python.ast.nodes.expressions.UnaryArithmeticNode.NotNode;
import org.python.ast.nodes.literals.BooleanLiteralNode;
import org.python.ast.nodes.statements.*;
import org.python.compiler.*;
import org.python.core.*;
import org.python.core.truffle.EnvironmentFrameSlot;
import org.python.core.truffle.GlobalScope;

import com.oracle.truffle.api.*;
import com.oracle.truffle.api.frame.*;
import com.oracle.truffle.api.nodes.*;

public class PythonTreeTranslator extends Visitor {

    private final NodeFactory nodeFactory = new NodeFactory();

    private boolean isLeftHandSide = false;

    private boolean isGenerator = false;

    private Stack<FunctionRootNode> funcRoots = new Stack<FunctionRootNode>();

    private Stack<StatementNode> loopHeaders = new Stack<StatementNode>();

    public PythonTreeTranslator() {}

    public RootNode translate(PythonTree root) {
        try {
            return (RootNode) visit(root);
        } catch (Throwable t) {
            t.printStackTrace();
            throw new RuntimeException("Failed in " + this + " with error " + t);
        }
    }

    private FunctionRootNode getCurrentFuncRoot() {
        if (funcRoots.isEmpty())
            return null;
        else
            return funcRoots.peek();
    }

    private StatementNode getCurrentLoopHeader() {
        if (loopHeaders.isEmpty())
            return null;
        else
            return loopHeaders.peek();
    }

    @Override
    public Object visitModule(org.python.antlr.ast.Module node) throws Exception {
        List<StatementNode> body = suite(node.getInternalBody());
        RootNode newNode = new NodeFactory().createModule(body, node.getFrameDescriptor());
        return newNode;
    }

    public List<StatementNode> suite(List<stmt> stmts) throws Exception {
        List<StatementNode> statements = new ArrayList<StatementNode>();

        for (int i = 0; i < stmts.size(); i++) {
            Object statement = visit(stmts.get(i));

            // Statements like Global is ignored
            if (statement != null) {
                statements.add((StatementNode) statement);
            }
        }

        return statements;
    }

    @Override
    public Object visitFunctionDef(FunctionDef node) throws Exception {
        isGenerator = false;

        FrameDescriptor fd = node.getFrameDescriptor();
        ParametersNode parameters = visitArgs(node.getInternalArgs(), fd);

        // cache
        FunctionRootNode funcRoot = nodeFactory.createFunctionRoot(parameters, null);
        funcRoots.push(funcRoot);

        List<StatementNode> statements = suite(node.getInternalBody());

        String name = node.getInternalName();
        FrameSlot slot = node.getInternalNameNode().getSlot();
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
        return nodeFactory.createFunctionDef(slot, name, parameters, ct, funcRoot);
    }

    public ParametersNode visitArgs(arguments node, FrameDescriptor fd) throws Exception {
        isLeftHandSide = true;

        // parse arguments
        ArgListCompiler ac = new ArgListCompiler();
        ac.visitArgs(node);

        List<TypedNode> defaults = walkExprList(node.getInternalDefaults());

        List<TypedNode> args = new ArrayList<TypedNode>();

        List<String> paramNames = new ArrayList<String>();

        for (int i = 0; i < node.getInternalArgs().size(); i++) {
            expr arg = node.getInternalArgs().get(i);

            if (arg instanceof Name) {
                args.add((TypedNode) visit(arg));
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

    List<TypedNode> walkExprList(List<expr> exprs) throws Exception {
        List<TypedNode> targets = new ArrayList<TypedNode>();

        for (expr source : exprs) {
            targets.add((TypedNode) visit(source));
        }

        return targets;
    }

    List<TypedNode> walkKeywordList(List<keyword> keywords) throws Exception {
        List<TypedNode> targets = new ArrayList<TypedNode>();

        for (keyword source : keywords) {
            targets.add((TypedNode) visit(source));
        }

        return targets;
    }

    FrameSlot[] walkAliasList(List<alias> aliases) throws Exception {
        FrameSlot[] slots = new FrameSlot[aliases.size()];

        for (int i = 0; i < aliases.size(); i++) {
            slots[i] = aliases.get(i).getSlot();
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

    @Override
    public Object visitKeyword(keyword node) throws Exception {
        TypedNode value = (TypedNode) visit(node.getInternalValue());

        return nodeFactory.createKeywordLiteral(value, node.getInternalArg());
    }

    @Override
    public Object visitCall(Call node) throws Exception {
        TypedNode callee = (TypedNode) visit(node.getInternalFunc());
        List<TypedNode> arguments = walkExprList(node.getInternalArgs());
        TypedNode[] argumentsArray = arguments.toArray(new TypedNode[arguments.size()]);

        List<TypedNode> keywords = walkKeywordList(node.getInternalKeywords());
        TypedNode[] keywordsArray = keywords.toArray(new TypedNode[keywords.size()]);

        if (callee instanceof AttributeRefNode) {
            AttributeRefNode attr = (AttributeRefNode) callee;
            return nodeFactory.createAttributeCall(attr.getOperand(), argumentsArray, attr.getName());
        }

        // Specializing call node.
        if (argumentsArray.length == 1 && keywordsArray.length == 0) {
            if (Options.optimizeNode) {
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
            if (Options.optimizeNode) {
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

        if (Options.optimizeNode) {
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
        if (node.getInternalCtx() != expr_contextType.Load) {
            return convertWrite(node);
        }

        return convertRead(node);
    }

    @Override
    public Object visitNone(None node) throws Exception {
        return nodeFactory.createNoneLiteral();
    }

    @Override
    public Object visitTrue(True node) throws Exception {
        return nodeFactory.createBooleanLiteral(true);
    }

    @Override
    public Object visitFalse(False node) throws Exception {
        return nodeFactory.createBooleanLiteral(false);
    }

    TypedNode convertRead(Name node) {
        String name = node.getInternalId();
        FrameSlot slot = node.getSlot();

        if (slot != null) {
            if (slot instanceof EnvironmentFrameSlot) {
                return nodeFactory.createReadEnvironment(slot, ((EnvironmentFrameSlot) slot).getLevel());
            }

            return nodeFactory.createReadLocal(slot);
        } else {
            return nodeFactory.createReadGlobal(name);
        }
    }

    TypedNode convertWrite(Name node) {
        if (node.getSlot() != null) {
            PythonTree parent = (PythonTree) node.getParent();

            if (parent instanceof Assign) {
                return nodeFactory.createWriteLocal(TypedNode.DUMMY, node.getSlot());
            } else {
                return nodeFactory.createLHWriteLocal(node.getSlot());
            }
        } else {
            String name = node.getInternalId();
            return nodeFactory.createWriteGlobal(name, TypedNode.DUMMY);
        }
    }

    @Override
    public Object visitExpr(Expr node) throws Exception {
        return visit(node.getInternalValue());
    }

    @Override
    public Object visitList(org.python.antlr.ast.List node) throws Exception {
        List<TypedNode> elts = walkExprList(node.getInternalElts());

        if (isLeftHandSide) {
            if (!(node.getParent() instanceof Assign)) {
                return nodeFactory.createAssignmentTargetSequence(elts);
            } else {
                if (elts.size() == 2) {
                    return nodeFactory.createTwoAssignment(elts.get(0), elts.get(1), TypedNode.DUMMY);
                } else if (elts.size() == 3) {
                    return nodeFactory.createThreeAssignment(elts.get(0), elts.get(1), elts.get(2), TypedNode.DUMMY);
                } else {
                    return nodeFactory.createMultiAssignment(elts, TypedNode.DUMMY);
                }
            }
        } else {
            return nodeFactory.createListLiteral(elts);
        }
    }

    @Override
    public Object visitTuple(Tuple node) throws Exception {
        List<TypedNode> elts = walkExprList(node.getInternalElts());

        if (isLeftHandSide) {
            if (!(node.getParent() instanceof Assign)) {
                return nodeFactory.createAssignmentTargetSequence(elts);
            } else {
                if (elts.size() == 2) {
                    return nodeFactory.createTwoAssignment(elts.get(0), elts.get(1), TypedNode.DUMMY);
                } else if (elts.size() == 3) {
                    return nodeFactory.createThreeAssignment(elts.get(0), elts.get(1), elts.get(2), TypedNode.DUMMY);
                } else {
                    return nodeFactory.createMultiAssignment(elts, TypedNode.DUMMY);
                }
            }
        } else {
            return nodeFactory.createTupleLiteral(elts);
        }
    }

    @Override
    public Object visitDict(Dict node) throws Exception {
        List<TypedNode> keys = walkExprList(node.getInternalKeys());
        List<TypedNode> vals = walkExprList(node.getInternalValues());
        return nodeFactory.createDictLiteral(keys, vals);
    }

    @Override
    public Object visitAugAssign(AugAssign node) throws Exception {
        TypedNode target = (TypedNode) visit(node.getInternalTarget());
        TypedNode value = (TypedNode) visit(node.getInternalValue());

        TypedNode expr;
        if (target instanceof FrameSlotNode) {
            FrameSlot slot = ((FrameSlotNode) target).getSlot();
            // Only works for locals
            TypedNode read = nodeFactory.createReadLocal(slot);
            TypedNode binaryOp = nodeFactory.createBinaryOperation(node.getInternalOp(), read, value);
            expr = nodeFactory.createWriteLocal(binaryOp, slot);
        } else if (target instanceof SubscriptLoadNode) {
            SubscriptLoadNode read = (SubscriptLoadNode) target;

            TypedNode binaryOp = nodeFactory.createBinaryOperation(node.getInternalOp(), read, value);
            expr = nodeFactory.createSubscriptStore(read.getPrimary(), read.getSlice(), binaryOp);
        } else {
            throw new NotCovered();
        }

        return expr;
    }

    @Override
    public Object visitAssign(Assign node) throws Exception {
        TypedNode right = (TypedNode) visit(node.getInternalValue());

        isLeftHandSide = true;
        List<TypedNode> targets = walkExprList(node.getInternalTargets());
        isLeftHandSide = false;

        if (targets.size() == 1) {
            return convertSingleAndMultiAssignment(node, targets.get(0), right);
        } else {
            List<StatementNode> assignments = new ArrayList<StatementNode>();

            for (Node target : targets) {
                assignments.add((StatementNode) convertSingleAndMultiAssignment(node, target, right));
            }

            return nodeFactory.createBlock(assignments);
        }
    }

    private StatementNode convertSingleAndMultiAssignment(Assign node, Node target, TypedNode right) throws Exception {
        if (target instanceof LeftHandSideNode) {
            LeftHandSideNode lhTarget = (LeftHandSideNode) target;
            lhTarget.patchValue(right);
            return lhTarget;
        }

        throw new NotCovered();
    }

    @Override
    public Object visitBinOp(BinOp node) throws Exception {
        TypedNode left = (TypedNode) visit(node.getInternalLeft());
        TypedNode right = (TypedNode) visit(node.getInternalRight());
        operatorType op = node.getInternalOp();
        return nodeFactory.createBinaryOperation(op, left, right);
    }

    @Override
    public Object visitBoolOp(BoolOp node) throws Exception {
        boolopType op = node.getInternalOp();
        List<TypedNode> values = walkExprList(node.getInternalValues());
        TypedNode left = values.get(0);
        List<TypedNode> rights = values.subList(1, values.size());
        return nodeFactory.createBooleanOperations(left, op, rights);
    }

    @Override
    public Object visitCompare(Compare node) throws Exception {
        List<cmpopType> ops = node.getInternalOps();
        TypedNode left = (TypedNode) visit(node.getInternalLeft());
        List<TypedNode> rights = walkExprList(node.getInternalComparators());
        return nodeFactory.createComparisonOperations(left, ops, rights);
    }

    @Override
    public Object visitUnaryOp(UnaryOp node) throws Exception {
        unaryopType op = node.getInternalOp();
        TypedNode operand = (TypedNode) visit(node.getInternalOperand());
        return nodeFactory.createUnaryOperation(op, operand);
    }

    @Override
    public Object visitAttribute(Attribute node) throws Exception {
        TypedNode primary = (TypedNode) visit(node.getInternalValue());

        if (isLeftHandSide) {
            return nodeFactory.createAttributeUpdate(primary, node.getInternalAttr(), TypedNode.DUMMY);
        } else {
            return nodeFactory.createAttributeRef(primary, node.getInternalAttr());
        }
    }

    @Override
    public Object visitSlice(Slice node) throws Exception {
        TypedNode lower = (TypedNode) (node.getInternalLower() == null ? null : visit(node.getInternalLower()));
        TypedNode upper = (TypedNode) (node.getInternalUpper() == null ? null : visit(node.getInternalUpper()));
        TypedNode step = (TypedNode) (node.getInternalStep() == null ? null : visit(node.getInternalStep()));
        return nodeFactory.createSlice(lower, upper, step);
    }

    @Override
    public Object visitIndex(Index node) throws Exception {
        TypedNode index = (TypedNode) visit(node.getInternalValue());
        //return nodeFactory.createIndex(index);
        return index;
    }

    @Override
    public Object visitSubscript(Subscript node) throws Exception {
        TypedNode primary = (TypedNode) visit(node.getInternalValue());
        TypedNode slice = (TypedNode) visit(node.getInternalSlice());

        if (node.getInternalCtx() == expr_contextType.Load) {
            return nodeFactory.createSubscriptLoad(primary, slice);
        } else if (node.getInternalCtx() == expr_contextType.Store) {
            assert isLeftHandSide;
            return nodeFactory.createSubscriptStore((TypedNode) primary, (TypedNode) slice, TypedNode.DUMMY);
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

        LeftHandSideNode target = (LeftHandSideNode) visit(node.getInternalTarget());
        TypedNode iterator = (TypedNode) visit(node.getInternalIter());

        // inner loop
        comprehension inner = node.getInnerLoop();
        TypedNode innerLoop = inner != null ? (TypedNode) visitComprehension(inner) : null;
        isInner = inner != null ? false : true;

        // transformed loop body (only exist if it's inner most comprehension)
        expr body = node.getLoopBody();
        TypedNode loopBody = body != null ? (TypedNode) visit(node.getLoopBody()) : null;
        isInner = body != null ? true : false;

        // Just deal with one condition.
        List<expr> conditions = node.getInternalIfs();
        TypedNode condition = (conditions == null || conditions.isEmpty()) ? null : (TypedNode) visit(conditions.get(0));

        assert inner == null || body == null : "Cannot be inner and outer at the same time";

        if (isInner) {
            return nodeFactory.createInnerComprehension(target, iterator, condition, loopBody);
        } else {
            return nodeFactory.createOuterComprehension(target, iterator, condition, innerLoop);
        }
    }

    @Override
    public Object visitGeneratorExp(GeneratorExp node) throws Exception {
        ComprehensionNode comprehension = (ComprehensionNode) visitComprehension(node.getInternalGenerators().get(0));
        GeneratorNode gnode = nodeFactory.createGenerator(comprehension);
        return nodeFactory.createGeneratorExpression(gnode, node.getFrameDescriptor());
    }

    @Override
    public Object visitReturn(Return node) throws Exception {
        TypedNode value = null;
        StatementNode returnNode = null;

        if (node.getInternalValue() != null) {
            value = (TypedNode) visit(node.getInternalValue());
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
        List<StatementNode> then = suite(node.getInternalBody());
        List<StatementNode> orelse = suite(node.getInternalOrelse());
        TypedNode test = (TypedNode) visit(node.getInternalTest());
        BlockNode thenPart = nodeFactory.createBlock(then);
        BlockNode elsePart = nodeFactory.createBlock(orelse);

        if (test instanceof NotNode) {
            StatementNode ifNotNode = nodeFactory.createIfNot(((NotNode) test).getOperand(), thenPart, elsePart);

            ifNotNode.setFuncRootNode(getCurrentFuncRoot());
            thenPart.setFuncRootNode(getCurrentFuncRoot());
            elsePart.setFuncRootNode(getCurrentFuncRoot());

            thenPart.setLoopHeader(getCurrentLoopHeader());
            elsePart.setLoopHeader(getCurrentLoopHeader());

            return ifNotNode;
        } else {
            StatementNode ifNode = nodeFactory.createIf(test, thenPart, elsePart);

            ifNode.setFuncRootNode(getCurrentFuncRoot());
            thenPart.setFuncRootNode(getCurrentFuncRoot());
            elsePart.setFuncRootNode(getCurrentFuncRoot());

            thenPart.setLoopHeader(getCurrentLoopHeader());
            elsePart.setLoopHeader(getCurrentLoopHeader());

            return ifNode;
        }
    }

    @Override
    public Object visitWhile(While node) throws Exception {
        TypedNode test = (TypedNode) visit(node.getInternalTest());

        if (test instanceof BooleanLiteralNode && ((BooleanLiteralNode) test).getValue()) {
            StatementNode whileTrueNode = nodeFactory.createWhileTrue(null);

            loopHeaders.push(whileTrueNode);

            List<StatementNode> body = suite(node.getInternalBody());
            BlockNode bodyPart = nodeFactory.createBlock(body);

            bodyPart.setLoopHeader(getCurrentLoopHeader());

            bodyPart.setFuncRootNode(getCurrentFuncRoot());
            whileTrueNode.setFuncRootNode(getCurrentFuncRoot());

            ((WhileTrueNode) whileTrueNode).setInternal(bodyPart);
            loopHeaders.pop();

            return whileTrueNode;
        } else {
            // if (!(test instanceof NotNode)) {
            // test = nodeFactory.createYesNode(test);
            // }
            StatementNode whileNode = nodeFactory.createWhile(test, null, null);

            loopHeaders.push(whileNode);

            List<StatementNode> body = suite(node.getInternalBody());
            List<StatementNode> orelse = suite(node.getInternalOrelse());
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
        isLeftHandSide = true;
        LeftHandSideNode target = (LeftHandSideNode) visit(node.getInternalTarget());
        isLeftHandSide = false;

        TypedNode iter = (TypedNode) visit(node.getInternalIter());

        StatementNode forNode = null;

        if (Options.optimizeNode) {
            if (iter instanceof CallBuiltInWithOneArgNoKeywordNode && ((CallBuiltInWithOneArgNoKeywordNode) iter).getName().equals("range")) {
                forNode = nodeFactory.createForRangeWithOneValue(target, ((CallBuiltInWithOneArgNoKeywordNode) iter).getArgument(), null, null);
            } else if (iter instanceof CallBuiltInWithTwoArgsNoKeywordNode && ((CallBuiltInWithTwoArgsNoKeywordNode) iter).getName().equals("range")) {
                forNode = nodeFactory.createForRangeWithTwoValues(target, ((CallBuiltInWithTwoArgsNoKeywordNode) iter).getArgument0(), ((CallBuiltInWithTwoArgsNoKeywordNode) iter).getArgument1(), null, null);
            } else {
                forNode = nodeFactory.createFor(target, iter, null, null);
            }
        } else {
            forNode = nodeFactory.createFor(target, iter, null, null);
        }

        forNode.setFuncRootNode(getCurrentFuncRoot());

        loopHeaders.push(forNode);

        List<StatementNode> body = suite(node.getInternalBody());
        List<StatementNode> orelse = suite(node.getInternalOrelse());
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

    @Override
    public Object visitPrint(Print node) throws Exception {
        List<TypedNode> values = walkExprList(node.getInternalValues());
        StatementNode newNode = nodeFactory.createPrint(values, node.getInternalNl());

        if (node.getOutStream() != null) {
            ((PrintNode) newNode).setOutStream(node.getOutStream());
        }

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
        TypedNode right = (TypedNode) visit(node.getInternalValue());
        return nodeFactory.createYield(right);
    }

    @Override
    public Object visitStr(Str node) throws Exception {
        PyString s = (PyString) node.getInternalS();
        return nodeFactory.createStringLiteral(s);
    }

    @Override
    public Object visitIfExp(IfExp node) throws Exception {
        TypedNode test = (TypedNode) visit(node.getInternalTest());
        TypedNode body = (TypedNode) visit(node.getInternalBody());
        TypedNode orelse = (TypedNode) visit(node.getInternalOrelse());

        if (test instanceof NotNode) {
            return nodeFactory.createIfNotExpNode(((NotNode) test).getOperand(), body, orelse);
        } else {
            return nodeFactory.createIfExpNode(test, body, orelse);
        }

    }

    @Override
    protected Object unhandled_node(PythonTree node) throws Exception {
        throw new RuntimeException("Unhandled node " + node);
    }

    @SuppressWarnings("serial")
    class NotCovered extends RuntimeException {

        public NotCovered() {
            super("This case is not covered!");
        }

    }

}
