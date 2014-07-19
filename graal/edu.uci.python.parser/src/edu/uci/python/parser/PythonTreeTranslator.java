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
import com.oracle.truffle.api.source.*;

import edu.uci.python.nodes.*;
import edu.uci.python.nodes.argument.*;
import edu.uci.python.nodes.call.*;
import edu.uci.python.nodes.control.*;
import edu.uci.python.nodes.expression.*;
import edu.uci.python.nodes.expression.BinaryBooleanNodeFactory.*;
import edu.uci.python.nodes.frame.*;
import edu.uci.python.nodes.function.*;
import edu.uci.python.nodes.literal.*;
import edu.uci.python.nodes.statement.*;
import edu.uci.python.nodes.subscript.*;
import edu.uci.python.runtime.*;
import edu.uci.python.runtime.datatype.*;
import edu.uci.python.runtime.function.*;
import edu.uci.python.runtime.standardtype.*;
import static edu.uci.python.parser.TranslationUtil.*;

public class PythonTreeTranslator extends Visitor {

    private final PythonContext context;
    private final NodeFactory factory;
    private final TranslationEnvironment environment;
    private final LoopsBookKeeper loops;
    private final AssignmentTranslator assigns;
    private final PythonParseResult result;
    private final PythonModule module;
    private final Source source;

    public PythonTreeTranslator(PythonContext context, TranslationEnvironment environment, PythonModule module, Source source) {
        this.context = context;
        this.factory = new NodeFactory();
        this.environment = environment.reset();
        this.loops = new LoopsBookKeeper();
        this.assigns = new AssignmentTranslator(environment, this);
        this.result = new PythonParseResult(environment.getModule());
        this.module = module;
        this.source = source;
    }

    public PythonParseResult translate(PythonTree root) {
        ModuleNode moduleNode;

        try {
            moduleNode = (ModuleNode) visit(root);
        } catch (Throwable t) {
            t.printStackTrace();
            throw new RuntimeException("Failed in " + this + " with error " + t);
        }

        result.setModule(moduleNode);
        result.setContext(context);
        return result;
    }

    @Override
    public Object visitModule(org.python.antlr.ast.Module node) throws Exception {
        environment.beginScope(node, ScopeInfo.ScopeKind.Module);
        List<PNode> body = visitStatements(node.getInternalBody());
        FrameDescriptor fd = environment.getCurrentFrame();
        environment.endScope(node);
        RootNode newNode = factory.createModule(module.getModuleName(), body, fd);
        return newNode;
    }

    public List<PNode> visitStatements(List<stmt> stmts) throws Exception {
        List<PNode> statements = new ArrayList<>();

        for (int i = 0; i < stmts.size(); i++) {
            stmt statement = stmts.get(i);
            PNode statementNode = (PNode) visit(statement);
            // Statements like Global is ignored
            if (EmptyNode.isEmpty(statementNode)) {
                continue;
            }

            if (environment.hasStatementPatch()) {
                statements.addAll(environment.getStatementPatch());
            }

            statements.add(statementNode);
        }

        return statements;
    }

    @Override
    public Object visitExpression(Expression node) throws Exception {
        environment.beginScope(node, ScopeInfo.ScopeKind.Function);
        PNode body = (PNode) visit(node.getInternalBody());
        FrameDescriptor fd = environment.getCurrentFrame();
        environment.endScope(node);
        return new ModuleNode("<expression>", body, fd);
    }

    @Override
    public Object visitFunctionDef(FunctionDef node) throws Exception {
        String name = node.getInternalName();
        if (PythonOptions.CatchZippyExceptionForUnitTesting) {
            /**
             * Some unittest test functions might fail in the translation phase in ZipPy. Therefore,
             * NotCovered is caught here. The result of this test function will be a Fail, and the
             * rest of the test units will continue.
             */
            try {
                return visitFunctionDefinition(node);
            } catch (Exception e) {
                environment.endScope(node);
                ZippyTranslationErrorNode t = ZippyTranslationErrorNode.getInstance();
                return environment.findVariable(name).makeWriteNode(t);
            }
        } else {
            return visitFunctionDefinition(node);
        }
    }

    private Object visitFunctionDefinition(FunctionDef node) throws Exception {
        String name = node.getInternalName();
        Name nameNode = node.getInternalNameNode();
        String enclosingClassName = environment.isInClassScope() ? environment.getCurrentScopeId() : null;

        /**
         * translate default arguments in FunctionDef's declaring scope.
         */
        List<expr> defaultExprs = node.getInternalArgs().getInternalDefaults();
        List<PNode> defaultArgs = walkExprList(defaultExprs);

        environment.beginScope(node, ScopeInfo.ScopeKind.Function);
        environment.setDefaultArgumentNodes(defaultArgs);

        /**
         * Parameters
         */
        Arity arity = createArity(name, node.getInternalArgs(), node.getInternalDecorator_list());
        PNode argumentLoads = visitArgs(node.getInternalArgs());

        /**
         * Function body
         */
        List<PNode> statements = visitStatements(node.getInternalBody());
        PNode body = factory.createBlock(statements);
        body = factory.createBlock(argumentLoads, body);
        body = new ReturnTargetNode(body, factory.createReadLocal(environment.getReturnSlot()));
        assignSourceFromNode(node, body);

        /**
         * Defaults
         */
        PNode defaults = createDefaultArgumentsNode();

        /**
         * Function root
         */
        FrameDescriptor fd = environment.getCurrentFrame();
        String fullName = enclosingClassName == null ? name : enclosingClassName + '.' + name;
        FunctionRootNode funcRoot = factory.createFunctionRoot(context, fullName, environment.isInGeneratorScope(), fd, body);
        assignSourceToRootNode(node, funcRoot);
        RootCallTarget ct = Truffle.getRuntime().createCallTarget(funcRoot);
        result.addParsedFunction(name, funcRoot);

        /**
         * Definition
         */
        PNode funcDef;
        if (environment.isInGeneratorScope()) {
            GeneratorTranslator gtran = new GeneratorTranslator(context, funcRoot);
            funcDef = GeneratorFunctionDefinitionNode.create(name, enclosingClassName, context, arity, defaults, gtran.translate(), fd, environment.needsDeclarationFrame(),
                            gtran.getNumOfActiveFlags(), gtran.getNumOfGeneratorBlockNode(), gtran.getNumOfGeneratorForNode());
        } else {
            funcDef = new FunctionDefinitionNode(name, enclosingClassName, context, arity, defaults, ct, fd, environment.needsDeclarationFrame());
        }
        environment.endScope(node);
        PNode functionNameWriteNode = environment.findVariable(name).makeWriteNode(funcDef);
        return assignSourceFromNode(nameNode, functionNameWriteNode);
    }

    @Override
    public Object visitLambda(Lambda node) throws Exception {
        /**
         * translate default arguments in FunctionDef's declaring scope.
         */
        List<expr> defaultExprs = node.getInternalArgs().getInternalDefaults();
        List<PNode> defaultArgs = walkExprList(defaultExprs);

        String name = "anonymous";
        environment.beginScope(node, ScopeInfo.ScopeKind.Function);
        environment.setDefaultArgumentNodes(defaultArgs);

        /**
         * Parameters
         */
        Arity arity = createArity(name, node.getInternalArgs(), new ArrayList<expr>());
        PNode argumentLoads = visitArgs(node.getInternalArgs());

        /**
         * Lambda body
         */
        expr body = node.getInternalBody();
        PNode bodyNode = (PNode) visit(body);
        bodyNode = factory.createBlock(argumentLoads, bodyNode);

        /**
         * Defaults
         */
        PNode defaults = createDefaultArgumentsNode();

        /**
         * Lambda function root
         */
        FrameDescriptor fd = environment.getCurrentFrame();
        FunctionRootNode funcRoot = factory.createFunctionRoot(context, name, environment.isInGeneratorScope(), fd, bodyNode);
        RootCallTarget ct = Truffle.getRuntime().createCallTarget(funcRoot);
        result.addParsedFunction(name, funcRoot);

        /**
         * Definition
         */
        PNode funcDef;
        if (environment.isInGeneratorScope()) {
            GeneratorTranslator gtran = new GeneratorTranslator(context, funcRoot);
            funcDef = GeneratorFunctionDefinitionNode.create(name, null, context, arity, defaults, gtran.translate(), fd, environment.needsDeclarationFrame(), gtran.getNumOfActiveFlags(),
                            gtran.getNumOfGeneratorBlockNode(), gtran.getNumOfGeneratorForNode());
        } else {
            funcDef = new FunctionDefinitionNode(name, null, context, arity, defaults, ct, fd, environment.needsDeclarationFrame());
        }

        environment.endScope(node);
        return funcDef;
    }

    private PNode createDefaultArgumentsNode() {
        if (environment.hasDefaultArguments()) {
            List<PNode> defaultParameters = environment.getDefaultArgumentNodes();
            ReadDefaultArgumentNode[] defaultReads = environment.getDefaultArgumentReads();
            return new DefaultParametersNode(defaultParameters.toArray(new PNode[defaultParameters.size()]), defaultReads);
        } else {
            return EmptyNode.create();
        }
    }

    private GeneratorExpressionNode createGeneratorExpressionDefinition(StatementNode body, int lineNum) {
        FrameDescriptor fd = environment.getCurrentFrame();
        String generatorName = "generator_exp:" + lineNum;
        FunctionRootNode funcRoot = factory.createFunctionRoot(context, generatorName, true, fd, body);
        result.addParsedFunction(generatorName, funcRoot);
        GeneratorTranslator gtran = new GeneratorTranslator(context, funcRoot);
        return new GeneratorExpressionNode(generatorName, context, gtran.translate(), fd, environment.needsDeclarationFrame(), gtran.getNumOfActiveFlags(), gtran.getNumOfGeneratorBlockNode(),
                        gtran.getNumOfGeneratorForNode());
    }

    public Arity createArity(String functionName, arguments node, List<expr> decorators) {
        boolean takesVarArgs = false;
        /**
         * takesKeywordArg is true by default, because in Python every parameter can be passed as a
         * keyword argument such as foo(a = 20)
         */
        boolean takesKeywordArg = true;
        boolean takesFixedNumOfArgs = true;

        int numOfArguments = node.getInternalArgs().size();
        int maxNumOfArgs = numOfArguments;

        if (node.getInternalVararg() != null) {
            maxNumOfArgs = -1;
            takesVarArgs = true;
            takesFixedNumOfArgs = false;
        }

        List<String> parameterIds = new ArrayList<>();

        int numOfDefaultArguments = node.getInternalDefaults().size();
        if (numOfDefaultArguments > 0) {
            takesFixedNumOfArgs = false;
        }

        for (expr arg : node.getInternalArgs()) {
            parameterIds.add(((Name) arg).getInternalId());
        }

        if (node.getInternalVararg() != null) {
            parameterIds.add(node.getInternalVararg());
        }

        int minNumOfArgs = numOfArguments - numOfDefaultArguments;

        /**
         * Decorators: classmethod or staticmethod.
         */
        String decoratorName = null;
        if (decorators.size() == 1 && decorators.get(0) instanceof Name) {
            Name decoratorId = (Name) decorators.get(0);
            decoratorName = decoratorId.getInternalId();
        }

        boolean isClassMethod = false;
        boolean isStaticMethod = false;
        if (decoratorName != null) {
            if (decoratorName.equals("classmethod")) {
                isClassMethod = true;
            } else if (decoratorName.equals("staticmethod")) {
                isStaticMethod = true;
            }
        }

        return new Arity(functionName, minNumOfArgs, maxNumOfArgs, takesFixedNumOfArgs, takesKeywordArg, takesVarArgs, isClassMethod, isStaticMethod, parameterIds);
    }

    public PNode visitArgs(arguments node) throws Exception {
        /**
         * parse arguments
         */
        new ArgListCompiler().visitArgs(node);

        /**
         * Argument reads.
         */
        List<expr> argExprs = node.getInternalArgs();
        List<PNode> argumentReads = new ArrayList<>();

        for (int i = 0; i < argExprs.size(); i++) {
            expr arg = node.getInternalArgs().get(i);
            assert arg instanceof Name;
            PNode argumentReadNode = (PNode) visit(arg);
            argumentReads.add(argumentReadNode);
        }

        /**
         * Varargs handled.
         */

        if (node.getInternalVararg() != null) {
            argumentReads.add(environment.getWriteVarArgsToLocal(node.getInternalVararg()));
        }

        /**
         * Positional arguments only. A simple block will do it.
         */
        if (node.getInternalDefaults().size() == 0) {
            return factory.createBlock(argumentReads);
        }

        /**
         * Default reads.
         */
        int sizeOfParams = node.getInternalArgs().size();
        int sizeOfDefaults = environment.getDefaultArgumentNodes().size();
        ReadDefaultArgumentNode[] defaultReads = new ReadDefaultArgumentNode[sizeOfDefaults];

        for (int i = 0; i < sizeOfDefaults; i++) {
            defaultReads[i] = new ReadDefaultArgumentNode();
        }

        environment.setDefaultArgumentReads(defaultReads);

        /**
         * Default writes. <br>
         * The alignment between default argument and parameter relies on the restriction that
         * default arguments are right aligned. Vararg case is not covered yet.
         */
        PNode[] defaultWrites = new PNode[sizeOfDefaults];
        int offset = sizeOfParams - sizeOfDefaults;
        for (int i = 0; i < sizeOfDefaults; i++) {
            FrameSlotNode slotNode = (FrameSlotNode) argumentReads.get(i + offset);
            PNode defaultWriteNode = factory.createWriteLocal(defaultReads[i], slotNode.getSlot());
            /**
             * Two WriteLocalNode are created for every default argument. We have to give different
             * source sections to these write nodes. <br>
             * WriteLocalVariableUninitializedNode ----> This WriteNode is created in this for loop <br>
             * rightNode = ReadDefaultArgumentNode <br>
             * ApplyArgumentsNode <br>
             * WriteLocalVariableUninitializedNode <br>
             * rightNode = UninitializedReadArgumentNode <br>
             * PNode defaultArgName = argumentReads.get(i+ offset); <br>
             * PNode defaultArgValue = (PNode) visit(defaultArgs.get(i));<br>
             * assignSourceFromChildren(defaultWriteNode, defaultArgName, defaultArgValue);
             */
            defaultWrites[i] = defaultWriteNode;
        }

        PNode loadDefaults = factory.createBlock(defaultWrites);
        BlockNode loadArguments = new ApplyArgumentsNode(argumentReads.toArray(new PNode[argumentReads.size()]));
        return factory.createBlock(loadDefaults, loadArguments);
    }

    List<PNode> walkExprList(List<expr> exprs) throws Exception {
        List<PNode> targets = new ArrayList<>();

        for (expr exp : exprs) {
            targets.add((PNode) visit(exp));
        }

        return targets;
    }

    List<KeywordLiteralNode> walkKeywordList(List<keyword> keywords) throws Exception {
        List<KeywordLiteralNode> targets = new ArrayList<>();

        for (keyword kw : keywords) {
            targets.add(visitKeyword(kw));
        }

        return targets;
    }

    private PNode createSingleImportStatement(alias aliaz) {
        String importName = aliaz.getInternalName();
        String target = aliaz.getInternalAsname() != null ? aliaz.getInternalAsname() : importName;
        PNode importNode = factory.createImport(context, importName);
        ReadNode read = environment.findVariable(target);
        return read.makeWriteNode(importNode);
    }

    private PNode createSingleImportFromStatement(alias aliaz, String fromModuleName) {
        PythonModule relativeto = this.module;

        String importName = aliaz.getInternalName();

        if (importName.equals("*")) {
            return createSingleImportStarStatement(relativeto, fromModuleName);
        }

        String target = aliaz.getInternalAsname() != null ? aliaz.getInternalAsname() : importName;
        Name importNameNode = aliaz.getInternalAsnameNode();
        PNode importNode = factory.createImportFrom(context, relativeto, fromModuleName, importName);
        ReadNode read = environment.findVariable(target);
        PNode writeNode = read.makeWriteNode(importNode);

        if (importNameNode != null) {
            assignSourceFromNode(importNameNode, writeNode);
        }

        return writeNode;
    }

    private PNode createSingleImportStarStatement(PythonModule relativeto, String fromModuleName) {
        PNode importNode = factory.createImportStar(context, relativeto, fromModuleName);
        return importNode;
    }

    @Override
    public Object visitImport(Import node) throws Exception {
        List<alias> aliases = node.getInternalNames();
        assert !aliases.isEmpty();

        if (aliases.size() == 1) {
            return assignSourceFromNode(node, createSingleImportStatement(aliases.get(0)));
        }

        List<PNode> imports = new ArrayList<>();
        for (int i = 0; i < aliases.size(); i++) {
            imports.add(createSingleImportStatement(aliases.get(i)));
        }

        return factory.createBlock(imports);
    }

    @Override
    public Object visitImportFrom(ImportFrom node) throws Exception {
        if (node.getInternalModule().compareTo("__future__") == 0) {
            return EmptyNode.create();
        }
        List<alias> aliases = node.getInternalNames();
        assert !aliases.isEmpty();

        if (aliases.size() == 1) {
            return createSingleImportFromStatement(aliases.get(0), node.getInternalModule());
        }

        List<PNode> imports = new ArrayList<>();
        for (int i = 0; i < aliases.size(); i++) {
            imports.add(createSingleImportFromStatement(aliases.get(i), node.getInternalModule()));
        }

        return factory.createBlock(imports);
    }

    protected KeywordLiteralNode visitKeyword(keyword node) throws Exception {
        PNode value = (PNode) visit(node.getInternalValue());
        return new KeywordLiteralNode(value, node.getInternalArg());
    }

    @Override
    public Object visitClassDef(ClassDef node) throws Exception {
        String name = node.getInternalName();
        Name nameNode = node.getInternalNameNode();
        List<PNode> bases = walkExprList(node.getInternalBases());
        assert bases.size() <= 1 : "Multiple super class is not supported yet!";

        environment.beginScope(node, ScopeInfo.ScopeKind.Class);
        PNode body = factory.createBlock(visitStatements(node.getInternalBody()));
        FunctionRootNode funcRoot = factory.createFunctionRoot(context, name, false, environment.getCurrentFrame(), body);
        RootCallTarget ct = Truffle.getRuntime().createCallTarget(funcRoot);
        FunctionDefinitionNode funcDef = new FunctionDefinitionNode(name, null, context, new Arity(name, 0, 0, new ArrayList<String>()), EmptyNode.create(), ct, environment.getCurrentFrame(),
                        environment.needsDeclarationFrame());
        environment.endScope(node);

        // The default super class is the <class 'object'>.
        PNode[] baseNodes;
        if (bases.size() == 0 || bases.get(0) == null) {
            baseNodes = new PNode[]{factory.createObjectLiteral(context.getObjectClass())};
        } else {
            baseNodes = bases.toArray(new PNode[bases.size()]);
        }

        PNode classDef = factory.createClassDef(context, this.module.getModuleName(), name, baseNodes, funcDef);
        ReadNode read = environment.findVariable(name);
        PNode writeNode = read.makeWriteNode(classDef);
        return assignSourceFromNode(nameNode, writeNode);
    }

    @Override
    public Object visitCall(Call node) throws Exception {
        PNode calleeNode = (PNode) visit(node.getInternalFunc());
        List<PNode> arguments = walkExprList(node.getInternalArgs());
        PNode[] argumentNodes = arguments.toArray(new PNode[arguments.size()]);
        List<KeywordLiteralNode> keywords = walkKeywordList(node.getInternalKeywords());
        KeywordLiteralNode[] keywordNodes = keywords.toArray(new KeywordLiteralNode[keywords.size()]);
        return assignSourceFromNode(node, PythonCallNode.create(context, calleeNode, argumentNodes, keywordNodes));
    }

    @Override
    public Object visitName(Name node) throws Exception {
        if (isBoolOrNone(node)) {
            return assignSourceFromNode(node, getBoolOrNode(node));
        }

        if (isParam(node)) {
            return assignSourceFromNode(node, environment.getWriteArgumentToLocal(node.getInternalId()));
        } else {
            ReadNode readNode = environment.findVariable(node.getInternalId());
            assert readNode instanceof PNode;
            PNode pnode = (PNode) readNode;
            return assignSourceFromNode(node, pnode);
        }
    }

    @Override
    public Object visitExpr(Expr node) throws Exception {
        return visit(node.getInternalValue());
    }

    @Override
    public Object visitList(org.python.antlr.ast.List node) throws Exception {
        List<PNode> elts = walkExprList(node.getInternalElts());
        return assignSourceFromNode(node, factory.createListLiteral(elts));
    }

    @Override
    public Object visitSet(org.python.antlr.ast.Set node) throws Exception {
        List<PNode> elts = walkExprList(node.getInternalElts());
        Set<PNode> setFromLost = new HashSet<>();

        for (PNode listNode : elts) {
            setFromLost.add(listNode);
        }

        return assignSourceFromNode(node, factory.createSetLiteral(setFromLost));
    }

    @Override
    public Object visitTuple(Tuple node) throws Exception {
        List<PNode> elts = walkExprList(node.getInternalElts());
        return assignSourceFromNode(node, factory.createTupleLiteral(elts));
    }

    @Override
    public Object visitDict(Dict node) throws Exception {
        List<PNode> keys = walkExprList(node.getInternalKeys());
        List<PNode> vals = walkExprList(node.getInternalValues());
        return assignSourceFromNode(node, factory.createDictLiteral(keys, vals));
    }

    // zwei TODO: Translate AugAssign to in-place operations ?
    @Override
    public Object visitAugAssign(AugAssign node) throws Exception {
        PNode target = (PNode) visit(node.getInternalTarget());
        PNode value = (PNode) visit(node.getInternalValue());
        PNode binaryOp = factory.createBinaryOperation(node.getInternalOp(), target, value);
        assignSourceToAugAssignNode(node, binaryOp, target, value);
        PNode read = factory.duplicate(target, PNode.class);
        PNodeUtil.clearSourceSections(read);
        PNode writeNode = ((ReadNode) read).makeWriteNode(binaryOp);
        return assignSourceFromNode(node, writeNode);
    }

    @Override
    public Object visitAssign(Assign node) throws Exception {
        /**
         * SourceSection assignment is performed in the AssignmentTranslator
         */
        return assigns.translate(node);
    }

    @Override
    public Object visitBinOp(BinOp node) throws Exception {
        PNode left = (PNode) visit(node.getInternalLeft());
        PNode right = (PNode) visit(node.getInternalRight());
        operatorType op = node.getInternalOp();
        PNode binaryNode = factory.createBinaryOperation(op, left, right);
        return assignSourceFromChildren(node, binaryNode, left, right);
    }

    @Override
    public Object visitBoolOp(BoolOp node) throws Exception {
        boolopType op = node.getInternalOp();
        List<PNode> values = walkExprList(node.getInternalValues());
        PNode left = values.get(0);
        List<PNode> rights = values.subList(1, values.size());
        /**
         * Source is assigned in createBooleanOperations
         */
        return createBooleanOperations(node, left, op, rights);
    }

    private PNode createBooleanOperations(BoolOp node, PNode left, boolopType operator, List<PNode> rights) {
        PNode current = factory.createBooleanOperation(operator, left, rights.get(0));
        assignSourceFromChildren(node, current, left, rights.get(0));

        for (int i = 1; i < rights.size(); i++) {
            PNode right = rights.get(i);
            // PNode previousNode = current;
            current = factory.createBooleanOperation(operator, current, right);
            // assignSourceFromChildren(current, previousNode, right);
        }

        return current;
    }

    @Override
    public Object visitCompare(Compare node) throws Exception {
        List<cmpopType> ops = node.getInternalOps();
        expr leftExpr = node.getInternalLeft();
        PNode left = (PNode) visit(leftExpr);
        List<expr> rightExprs = node.getInternalComparators();
        List<PNode> rights = walkExprList(rightExprs);

        /**
         * Source is assigned in createComparisonOperations
         */
        return createComparisonOperations(node, left, ops, rights);
    }

    private PNode createComparisonOperations(Compare node, PNode left, List<cmpopType> ops, List<PNode> rights) {
        PNode leftOp = left;
        PNode rightOp = rights.get(0);

        /**
         * Simple comparison.<br>
         * Only create source sections for simple comparison, and do not create source sections for
         * chained comparisons
         */
        if (ops.size() == 1 && rights.size() == 1) {
            PNode comparisonNode = factory.createComparisonOperation(ops.get(0), leftOp, rightOp);
            assignSourceFromChildren(node, comparisonNode, leftOp, rightOp);
            return comparisonNode;
        }

        /**
         * Chained comparisons. <br>
         * x < y <=z is equivalent to x < y and y <= z, except that y is evaluated only once
         */
        PNode assignment = null;
        PNode newComparison = null;
        PNode currentCompare = null;

        for (int i = 0; i < rights.size(); i++) {
            rightOp = rights.get(i);
            if (i == rights.size() - 1) {
                // Guard to prevent creating a temp variable for rightOp in the last comparison
                newComparison = factory.createComparisonOperation(ops.get(i), leftOp, rightOp);
            } else {
                if (!(rightOp instanceof LiteralNode || rightOp instanceof ReadNode)) {
                    ReadNode tempVar = environment.makeTempLocalVariable();
                    assignment = tempVar.makeWriteNode(rights.get(i));
                    rightOp = (PNode) tempVar;
                    newComparison = factory.createComparisonOperation(ops.get(i), leftOp, rightOp);
                    newComparison = factory.createBlock(assignment, newComparison);
                } else {
                    // Atomic comparison
                    newComparison = factory.createComparisonOperation(ops.get(i), leftOp, rightOp);
                }

                leftOp = factory.duplicate(rightOp, PNode.class);
                /**
                 * x < y <= z . We duplicate the node for y as x < y and y <= z. Since we duplicate
                 * the nodes, we clear the source section of it to avoid problems in the profiler.
                 */
                // PNodeUtil.clearSourceSections(leftOp);
            }

            if (i == 0) {
                currentCompare = newComparison;
            } else {
                currentCompare = AndNodeFactory.create(currentCompare, newComparison);
            }
        }

        return currentCompare;
    }

    @Override
    public Object visitUnaryOp(UnaryOp node) throws Exception {
        unaryopType op = node.getInternalOp();
        PNode operand = (PNode) visit(node.getInternalOperand());
        return assignSourceFromNode(node, factory.createUnaryOperation(op, operand));
    }

    @Override
    public Object visitAttribute(Attribute node) throws Exception {
        PNode primary = (PNode) visit(node.getInternalValue());
        Name attrName = node.getInternalAttrName();
        PNode getAttribute = factory.createGetAttribute(primary, node.getInternalAttr());
        assignSourceToGetAttribute(getAttribute, primary, attrName);
        return getAttribute;
    }

    @Override
    public Object visitSlice(Slice node) throws Exception {
        PNode lower = (PNode) (node.getInternalLower() == null ? EmptyNode.create() : visit(node.getInternalLower()));
        PNode upper = (PNode) (node.getInternalUpper() == null ? EmptyNode.create() : visit(node.getInternalUpper()));
        PNode step = (PNode) (node.getInternalStep() == null ? EmptyNode.create() : visit(node.getInternalStep()));
        return assignSourceFromNode(node, factory.createSlice(lower, upper, step));
    }

    @Override
    public Object visitIndex(Index node) throws Exception {
        PNode index = (PNode) visit(node.getInternalValue());
        return assignSourceFromNode(node, factory.createIndex(index));
    }

    @Override
    public Object visitSubscript(Subscript node) throws Exception {
        expr primary = node.getInternalValue();
        PNode primaryNode = (PNode) visit(primary);
        slice slice = node.getInternalSlice();
        PNode sliceNode = (PNode) visit(slice);

        if (!(node.getInternalSlice() instanceof Slice)) {
            PNode subscriptLoadIndexNode = factory.createSubscriptLoadIndex(primaryNode, sliceNode);
            return assignSourceFromChildren(node, subscriptLoadIndexNode, primaryNode, sliceNode);
        } else {
            PNode subscriptLoadSliceNode = factory.createSubscriptLoadSlice(primaryNode, sliceNode);
            return assignSourceFromChildren(node, subscriptLoadSliceNode, primaryNode, sliceNode);
        }
    }

    @Override
    public Object visitListComp(ListComp node) throws Exception {
        FrameSlot slot = environment.nextListComprehensionSlot();
        PNode body = factory.createListAppend(environment.getListComprehensionSlot(), (PNode) visit(node.getInternalElt()));
        PNode comp = visitComprehensions(node.getInternalGenerators(), body);
        return assignSourceFromNode(node, factory.createListComprehension(slot, comp));
    }

    @Override
    public Object visitDictComp(DictComp node) throws Exception {
        FrameSlot slot = environment.nextListComprehensionSlot();
        PNode key = (PNode) visit(node.getInternalKey());
        PNode value = (PNode) visit(node.getInternalValue());
        PNode body = factory.createMapPut(environment.getListComprehensionSlot(), key, value);
        PNode comp = visitComprehensions(node.getInternalGenerators(), body);
        return factory.createDictComprehension(slot, comp);
    }

    private PNode visitComprehensions(List<comprehension> comprehensions, PNode body) throws Exception {
        assert body != null;
        PNode current = body;
        List<comprehension> reversed = Lists.reverse(comprehensions);

        for (int i = 0; i < reversed.size(); i++) {
            comprehension comp = reversed.get(i);

            List<expr> conditions = comp.getInternalIfs();
            if (conditions != null && !conditions.isEmpty()) {
                PNode condition = (PNode) visit(conditions.get(0));
                current = factory.createIf(factory.toBooleanCastNode(condition), current, EmptyNode.create());
            }

            PNode iterWrite;
            if (comp.getInternalTarget() instanceof Tuple) {
                // Unpacking
                List<PNode> targets = assigns.walkTarget(comp.getInternalTarget(), EmptyNode.create());
                iterWrite = targets.remove(0);
                current = factory.createBlock(factory.createBlock(targets), current);
            } else {
                iterWrite = ((ReadNode) visit(comp.getInternalTarget())).makeWriteNode(EmptyNode.create());
            }

            PNode iterator = (PNode) visit(comp.getInternalIter());
            current = createForInScope(iterWrite, iterator, current);
        }

        assert current != null;
        return current;
    }

    private LoopNode createForInScope(PNode target, PNode iterator, PNode body) {
        GetIteratorNode getIterator = factory.createGetIterator(iterator);
        getIterator.assignSourceSection(iterator.getSourceSection());
        return ForNodeFactory.create(body, target, getIterator);
    }

    @Override
    public Object visitGeneratorExp(GeneratorExp node) throws Exception {
        environment.beginScope(node, ScopeInfo.ScopeKind.Generator);
        PNode body = factory.createYield((PNode) visit(node.getInternalElt()), environment.getReturnSlot());
        body = visitComprehensions(node.getInternalGenerators(), factory.createBlock(body));
        body = new ReturnTargetNode(body, factory.createReadLocal(environment.getReturnSlot()));
        int lineNum = node.getLine();
        GeneratorExpressionNode genExprDef = createGeneratorExpressionDefinition((StatementNode) body, lineNum);
        genExprDef.setEnclosingFrameDescriptor(environment.getEnclosingFrame());
        environment.endScope(node);
        return genExprDef;
    }

    @Override
    public Object visitReturn(Return node) throws Exception {
        expr returnValue = node.getInternalValue();
        PNode returnNode = null;

        if (returnValue == null) {
            returnNode = factory.createReturn();
        } else {
            PNode value = (PNode) visit(returnValue);
            PNode write = factory.createWriteLocal(value, environment.getReturnSlot());
            returnNode = factory.createFrameReturn(write);
        }

        return assignSourceFromNode(node, returnNode);
    }

    @Override
    public Object visitBreak(Break node) throws Exception {
        loops.addBreak();
        return assignSourceFromNode(node, factory.createBreak());
    }

    @Override
    public Object visitContinue(Continue node) throws Exception {
        loops.addContinue();
        return assignSourceFromNode(node, factory.createContinue());
    }

    @Override
    public Object visitIf(If node) throws Exception {
        List<stmt> thenStmt = node.getInternalBody();
        List<stmt> orElseStmt = node.getInternalOrelse();
        List<PNode> then = visitStatements(thenStmt);
        List<PNode> orelse = visitStatements(orElseStmt);
        PNode test = (PNode) visit(node.getInternalTest());
        PNode thenPart = factory.createBlock(then);

        if (thenPart instanceof EmptyNode) {
            thenPart = assignSourceFromNode(thenStmt.get(0), thenPart);
        } else {
            thenPart = assignSourceToBlockNode(thenPart, thenStmt);
        }

        PNode elsePart = factory.createBlock(orelse);
        if (!(elsePart instanceof EmptyNode)) {
            assignSourceToBlockNode(elsePart, orElseStmt);
        }

        CastToBooleanNode castToBooleanNode = factory.toBooleanCastNode(test);
        PNode ifNode = factory.createIf(castToBooleanNode, thenPart, elsePart);
        if (!(elsePart instanceof EmptyNode)) {
            assignSourceFromChildren(node, ifNode, test, elsePart);
        } else {
            assignSourceFromChildren(node, ifNode, test, thenPart);
        }

        return ifNode;
    }

    @Override
    public Object visitWhile(While node) throws Exception {
        loops.beginLoop(node);
        PNode test = (PNode) visit(node.getInternalTest());
        List<PNode> body = visitStatements(node.getInternalBody());
        List<PNode> orelse = visitStatements(node.getInternalOrelse());
        PNode bodyPart = factory.createBlock(body);
        PNode orelsePart = factory.createBlock(orelse);
        return createWhileNode(node, test, bodyPart, orelsePart, loops.endLoop());
    }

    private StatementNode createWhileNode(While node, PNode test, PNode body, PNode orelse, LoopInfo info) {
        List<stmt> bodyStmt = node.getInternalBody();
        PNode wrappedBody = body;

        if (info.hasContinue()) {
            wrappedBody = factory.createContinueTarget(body);
        }

        assignSourceToBlockNode(wrappedBody, bodyStmt);
        StatementNode whileNode = factory.createWhile(factory.toBooleanCastNode(test), wrappedBody);
        assignSourceFromNode(node, whileNode);

        if (!EmptyNode.isEmpty(orelse)) {
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

        List<PNode> targets = assigns.walkTargetList(lhs, EmptyNode.create());
        PNode iteratorWrite = targets.remove(0);
        PNode iter = (PNode) visit(node.getInternalIter());
        List<PNode> body = visitStatements(node.getInternalBody());
        List<PNode> orelse = visitStatements(node.getInternalOrelse());
        body.addAll(0, targets);
        PNode bodyPart = factory.createBlock(body);
        PNode orelsePart = factory.createBlock(orelse);
        return createForNode(node, iteratorWrite, iter, bodyPart, orelsePart, loops.endLoop());
    }

    private StatementNode createForNode(For node, PNode target, PNode iter, PNode body, PNode orelse, LoopInfo info) {
        List<stmt> bodyStmt = node.getInternalBody();
        PNode wrappedBody = body;
        if (info.hasContinue()) {
            wrappedBody = factory.createContinueTarget(body);
        }

        assignSourceToBlockNode(wrappedBody, bodyStmt);
        StatementNode forNode = createForInScope(target, iter, wrappedBody);
        assignSourceFromNode(node, forNode);

        if (!EmptyNode.isEmpty(orelse)) {
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
        if (!PythonOptions.UsePrintFunction) {
            Name print = new Name(node.getToken(), "print", expr_contextType.Load);
            List<expr> exprs = node.getInternalValues();
            if (exprs.size() == 1 && exprs.get(0) instanceof Tuple) {
                Tuple tuple = (Tuple) node.getInternalValues().get(0);
                exprs = tuple.getInternalElts();
            }

            Call call = new Call(node.getToken(), print, exprs, new ArrayList<keyword>(), null, null);
            return visitCall(call);
        } else {
            List<expr> exprs = node.getInternalValues();
            if (exprs.size() == 1 && exprs.get(0) instanceof Tuple) {
                Tuple tuple = (Tuple) exprs.get(0);
                List<PNode> values = walkExprList(tuple.getInternalElts());
                return assignSourceFromNode(node, factory.createPrint(values, node.getInternalNl(), context));
            } else {
                List<PNode> values = walkExprList(node.getInternalValues());
                StatementNode newNode = factory.createPrint(values, node.getInternalNl(), context);
                return assignSourceFromNode(node, newNode);
            }
        }
    }

    @Override
    public Object visitNum(Num node) throws Exception {
        Object value = node.getInternalN();

        if (value instanceof PyInteger) {
            return assignSourceFromNode(node, factory.createIntegerLiteral(((PyInteger) value).getValue()));
        } else if (value instanceof PyLong) {
            return assignSourceFromNode(node, factory.createBigIntegerLiteral(((PyLong) value).getValue()));
        } else if (value instanceof PyFloat) {
            return assignSourceFromNode(node, factory.createDoubleLiteral(((PyFloat) value).getValue()));
        } else if (value instanceof PyComplex) {
            PyComplex pyComplex = (PyComplex) value;
            PComplex complex = new PComplex(pyComplex.real, pyComplex.imag);
            return assignSourceFromNode(node, factory.createComplexLiteral(complex));
        } else {
            throw notCovered();
        }
    }

    @Override
    public Object visitGlobal(Global node) throws Exception {
        return EmptyNode.create();
    }

    @Override
    public Object visitYield(Yield node) throws Exception {
        environment.setToGeneratorScope();
        expr value = node.getInternalValue();
        PNode right = value != null ? (PNode) visit(value) : EmptyNode.create();
        return assignSourceFromNode(node, factory.createYield(right, environment.getReturnSlot()));
    }

    @Override
    public Object visitStr(Str node) throws Exception {
        PyString s = (PyString) node.getInternalS();
        return assignSourceFromNode(node, factory.createStringLiteral(s.getString()));
    }

    @Override
    public Object visitIfExp(IfExp node) throws Exception {
        PNode test = (PNode) visit(node.getInternalTest());
        PNode then = (PNode) visit(node.getInternalBody());
        PNode orelse = (PNode) visit(node.getInternalOrelse());
        return assignSourceFromNode(node, factory.createIf(factory.toBooleanCastNode(test), then, orelse));
    }

    @Override
    public Object visitPass(Pass node) throws Exception {
        return EmptyNode.create();
    }

    @Override
    public Object visitTryExcept(TryExcept node) throws Exception {
        List<PNode> b = visitStatements(node.getInternalBody());
        List<PNode> o = visitStatements(node.getInternalOrelse());

        PNode body = factory.createBlock(b);
        PNode orelse = factory.createBlock(o);

        List<excepthandler> excepts = node.getInternalHandlers();
        ExceptNode[] exceptNodes = new ExceptNode[excepts.size()];

        /**
         * Specialize except StopIteration to StopIterationTargetNode.
         */
        if (excepts.size() == 1 && EmptyNode.isEmpty(orelse)) {
            ExceptHandler handler = (ExceptHandler) excepts.get(0);

            if (handler.getInternalType() instanceof Name) {
                Name name = (Name) handler.getInternalType();

                if (name != null && name.getInternalId().equals("StopIteration")) {
                    List<PNode> exceptbody = visitStatements(handler.getInternalBody());
                    PNode exceptBody = factory.createBlock(exceptbody);
                    return new StopIterationTargetNode(body, exceptBody);
                }
            }
        }

        for (int i = 0; i < excepts.size(); i++) {
            ExceptHandler except = (ExceptHandler) excepts.get(i);
            PNode[] exceptType = null;

            if (except.getInternalType() != null) {
                if (except.getInternalType() instanceof Tuple) {
                    List<PNode> types = walkExprList(((Tuple) except.getInternalType()).getInternalElts());
                    exceptType = types.toArray(new PNode[types.size()]);
                } else {
                    exceptType = new PNode[]{(PNode) visit(except.getInternalType())};
                }
            }

            PNode exceptName = (except.getInternalName() == null) ? null : ((ReadNode) visit(except.getInternalName())).makeWriteNode(EmptyNode.create());
            List<PNode> exceptbody = visitStatements(except.getInternalBody());
            PNode exceptBody = factory.createBlock(exceptbody);
            ExceptNode exceptNode = new ExceptNode(context, exceptBody, exceptType, exceptName);
            exceptNodes[i] = exceptNode;
        }

        return assignSourceFromNode(node, new TryExceptNode(body, exceptNodes, orelse));
    }

    @Override
    public Object visitTryFinally(TryFinally node) throws Exception {
        List<PNode> b = visitStatements(node.getInternalBody());
        List<PNode> f = visitStatements(node.getInternalFinalbody());
        PNode body = factory.createBlock(b);
        PNode finalbody = factory.createBlock(f);
        return assignSourceFromNode(node, factory.createTryFinallyNode(body, finalbody));
    }

    @Override
    public Object visitRaise(Raise node) throws Exception {
        PNode type = (node.getInternalType() == null) ? null : (PNode) visit(node.getInternalType());
        PNode inst = (node.getInternalInst() == null) ? null : (PNode) visit(node.getInternalInst());
        return assignSourceFromNode(node, new RaiseNode(context, type, inst));
    }

    @Override
    public Object visitAssert(Assert node) throws Exception {
        PNode test = (PNode) visit(node.getInternalTest());
        CastToBooleanNode condition = factory.toBooleanCastNode(test);
        PNode msg = node.getInternalMsg() == null ? null : (PNode) visit(node.getInternalMsg());
        return assignSourceFromNode(node, factory.createAssert(condition, msg));
    }

    @Override
    public Object visitDelete(Delete node) throws Exception {
        /**
         * TODO Delete node has not been fully implemented
         */
        PNode target = (PNode) visit(node.getInternalTargets().get(0));

        if (target instanceof SubscriptLoadNode) {
            SubscriptLoadNode load = (SubscriptLoadNode) target;
            SubscriptDeleteNode subscriptDeleteNode = SubscriptDeleteNodeFactory.create(load.getPrimary(), load.getSlice());
            return assignSourceFromNode(node, subscriptDeleteNode);
        } else {
            return assignSourceFromNode(node, DeleteNodeFactory.create(target));
        }
    }

    /**
     * TODO: This needs clean up.
     */
    @Override
    public Object visitWith(With node) throws Exception {
        PNode withContext = (PNode) visit(node.getInternalContext_expr());
        PNode body = factory.createBlock(visitStatements(node.getInternalBody()));

        if (node.getInternalOptional_vars() != null) {
            if (node.getInternalOptional_vars() instanceof Tuple) {
                List<PNode> readNames = walkExprList(((Tuple) node.getInternalOptional_vars()).getInternalElts());
                List<PNode> asNames = new ArrayList<>();
                for (PNode read : readNames) {
                    asNames.add(((ReadNode) read).makeWriteNode(null));
                }

                return factory.createWithNode(withContext, asNames.toArray(new PNode[asNames.size()]), body);
            } else {
                PNode asNameNode = (PNode) visit(node.getInternalOptional_vars());
                PNode asName = ((ReadNode) asNameNode).makeWriteNode(null);
                return assignSourceFromNode(node, factory.createWithNode(withContext, new PNode[]{asName}, body));
            }
        }

        return assignSourceFromNode(node, factory.createWithNode(withContext, new PNode[]{}, body));
    }

    @Override
    public Object visitExec(Exec node) throws Exception {
        throw notCovered();
    }

    public PNode assignSourceFromNode(PythonTree node, PNode truffleNode) {
        String identifier = node.getText();
        int charStartIndex = node.getCharStartIndex();
        int charStopIndex = node.getCharStopIndex();
        int charLength = charStopIndex - charStartIndex;
        SourceSection sourceSection = source.createSection(identifier, charStartIndex, charLength);
        truffleNode.assignSourceSection(sourceSection);
        return truffleNode;
    }

    private PNode assignSourceFromChildren(PythonTree jythonNode, PNode truffleNode, PNode leftChild, PNode rightChild) {
        String identifier = jythonNode.getText();

        try {
            if (leftChild.getSourceSection() == null) {
                throw new RuntimeException("Node " + truffleNode.getClass().getSimpleName() + "'s left node " + leftChild.getClass().getSimpleName() + "does not have a source section");
            }

            if (rightChild.getSourceSection() == null) {
                throw new RuntimeException("Node " + truffleNode.getClass().getSimpleName() + "'s right node " + rightChild.getClass().getSimpleName() + "does not have a source section");
            }

            int charStartIndex = leftChild.getSourceSection().getCharIndex();
            int charStopIndex = rightChild.getSourceSection().getCharEndIndex();
            int charLength = charStopIndex - charStartIndex;
            SourceSection sourceSection = source.createSection(identifier, charStartIndex, charLength);
            truffleNode.assignSourceSection(sourceSection);
            return truffleNode;
        } catch (RuntimeException e) {
            return truffleNode;
        }
    }

    private PNode assignSourceToGetAttribute(PNode truffleNode, PNode primary, Name attributeName) {
        String identifier = "identifier";
        int charStartIndex = primary.getSourceSection().getCharIndex();
        int charStopIndex = attributeName.getCharStopIndex();
        int charLength = charStopIndex - charStartIndex;
        SourceSection sourceSection = source.createSection(identifier, charStartIndex, charLength);
        truffleNode.assignSourceSection(sourceSection);
        return truffleNode;
    }

    private RootNode assignSourceToRootNode(PythonTree node, RootNode rootNode) {
        String identifier = node.getText();
        int charStartIndex = node.getCharStartIndex();
        int charStopIndex = node.getCharStopIndex();
        int charLength = charStopIndex - charStartIndex;
        SourceSection sourceSection = source.createSection(identifier, charStartIndex, charLength);
        rootNode.assignSourceSection(sourceSection);
        return rootNode;
    }

    private PNode assignSourceToBlockNode(PNode node, List<stmt> statements) {
        if (node.getSourceSection() == null) {
            stmt firstChild = statements.get(0);
            stmt lastChild = statements.get(statements.size() - 1);
            int charStartIndex = firstChild.getCharStartIndex();
            int charStopIndex = lastChild.getCharStopIndex();
            int charLength = charStopIndex - charStartIndex;
            SourceSection sourceSection = source.createSection("block", charStartIndex, charLength);
            node.assignSourceSection(sourceSection);
        }

        return node;
    }

    private PNode assignSourceToAugAssignNode(AugAssign node, PNode augAssignNode, PNode beforeNode, PNode afterNode) {
        String identifier = node.getInternalOp().name();

        try {
            if (beforeNode.getSourceSection() == null) {
                throw new RuntimeException("Node " + augAssignNode.getClass().getSimpleName() + "'s target node " + beforeNode.getClass().getSimpleName() + "does not have a source section");
            }

            if (beforeNode.getSourceSection() == null) {
                throw new RuntimeException("Node " + augAssignNode.getClass().getSimpleName() + "'s value node " + beforeNode.getClass().getSimpleName() + "does not have a source section");
            }

            int charStartIndex = beforeNode.getSourceSection().getCharEndIndex();
            int charStopIndex = afterNode.getSourceSection().getCharIndex();
            int charLength = charStopIndex - charStartIndex;
            SourceSection sourceSection = source.createSection(identifier, charStartIndex, charLength);
            augAssignNode.assignSourceSection(sourceSection);
            return augAssignNode;
        } catch (RuntimeException e) {
            return augAssignNode;
        }
    }

}
