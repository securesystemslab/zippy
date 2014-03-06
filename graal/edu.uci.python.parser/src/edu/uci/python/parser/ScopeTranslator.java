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

import java.util.List;
import java.util.ArrayList;

import org.python.antlr.*;
import org.python.antlr.ast.*;
import org.python.antlr.base.*;
import org.python.compiler.*;
import org.python.core.*;

import static edu.uci.python.parser.TranslationUtil.*;

public class ScopeTranslator extends Visitor {

    private final TranslationEnvironment environment;

    public ScopeTranslator(TranslationEnvironment environment) {
        this.environment = environment.reset();
    }

    public mod process(PythonTree node) {
        try {
            return (mod) visit(node);
        } catch (Throwable t) {
            throw ParserFacade.fixParseError(null, t, this.toString());
        }
    }

    @Override
    public Object visitModule(org.python.antlr.ast.Module node) throws Exception {
        environment.beginScope(node, ScopeInfo.ScopeKind.Module);
        visitStatements(node.getInternalBody());
        environment.endScope(node);
        return node;
    }

    public void visitStatements(List<stmt> stmts) throws Exception {
        for (int i = 0; i < stmts.size(); i++) {
            visit(stmts.get(i));
        }
    }

    @Override
    public Object visitExpression(Expression node) throws Exception {
        traverse(node);
        return node;
    }

    @Override
    public Object visitFunctionDef(FunctionDef node) throws Exception {
        environment.createLocal(node.getInternalName());
        ArgListCompiler ac = new ArgListCompiler();
        ac.visitArgs(node.getInternalArgs());

        List<expr> defaults = ac.getDefaults();
        for (int i = 0; i < defaults.size(); i++) {
            visit(defaults.get(i));
        }

        List<expr> decs = node.getInternalDecorator_list();
        for (int i = decs.size() - 1; i >= 0; i--) {
            visit(decs.get(i));
        }

        environment.beginScope(node, ScopeInfo.ScopeKind.Function);

        visitArgs(node.getInternalArgs(), ac);
        List<PythonTree> argsInit = castToPythonTreeList(ac.init_code);
        node.addChildren(argsInit);
        node.getInternalBody().addAll(0, ac.init_code);

        visitStatements(node.getInternalBody());
        environment.endScope(node);
        return null;
    }

    @SuppressWarnings("unused")
    public void visitArgs(arguments node, ArgListCompiler ac) throws Exception {
        for (int i = 0; i < node.getInternalArgs().size(); i++) {
            expr arg = node.getInternalArgs().get(i);
            if (arg instanceof Name) {
                this.visitName((Name) arg);
            }
        }

        // Create a frame slot for var arg
        if (node.getInternalVararg() != null) {
            environment.createLocal(node.getInternalVararg());
        }
    }

    @Override
    public Object visitLambda(Lambda node) throws Exception {
        ArgListCompiler ac = new ArgListCompiler();
        ac.visitArgs(node.getInternalArgs());

        List<? extends PythonTree> defaults = ac.getDefaults();
        for (int i = 0; i < defaults.size(); i++) {
            visit(defaults.get(i));
        }

        environment.beginScope(node, ScopeInfo.ScopeKind.Function);
        visitArgs(node.getInternalArgs(), ac);
        List<PythonTree> argsInit = castToPythonTreeList(ac.init_code);
        node.addChildren(argsInit);

        for (Object o : ac.init_code) {
            visit((stmt) o);
        }

        visit(node.getInternalBody());
        environment.endScope(node);
        return null;
    }

    @Override
    public Object visitImport(Import node) throws Exception {
        for (int i = 0; i < node.getInternalNames().size(); i++) {
            alias a = node.getInternalNames().get(i);

            if (node.getInternalNames().get(i).getInternalAsname() != null) {
                environment.createLocal(a.getInternalAsname());
            } else {
                String name = a.getInternalName();
                if (name.indexOf('.') > 0) {
                    name = name.substring(0, name.indexOf('.'));
                }
                environment.createLocal(name);
            }
        }

        return null;
    }

    @Override
    public Object visitImportFrom(ImportFrom node) throws Exception {
        // TODO: we have a problem with future module.
        // Future.checkFromFuture(node); // future stmt support
        int n = node.getInternalNames().size();

        if (n == 0) {
            return null;
        }

        for (int i = 0; i < n; i++) {
            alias a = node.getInternalNames().get(i);
            if (node.getInternalNames().get(i).getInternalAsname() != null) {
                environment.createLocal(a.getInternalAsname());
            } else {
                environment.createLocal(a.getInternalName());
            }
        }

        return null;
    }

    @Override
    public Object visitGlobal(Global node) throws Exception {
        int n = node.getInternalNames().size();

        for (int i = 0; i < n; i++) {
            String name = node.getInternalNames().get(i);
            environment.createGlobal(name);
            environment.addLocalGlobals(name);
        }

        return null;
    }

    @Override
    public Object visitClassDef(ClassDef node) throws Exception {
        environment.createLocal(node.getInternalName());
        int n = node.getInternalBases().size();

        for (int i = 0; i < n; i++) {
            visit(node.getInternalBases().get(i));
        }

        environment.beginScope(node, ScopeInfo.ScopeKind.Class);
        visitStatements(node.getInternalBody());
        environment.endScope(node);
        return null;
    }

    @Override
    public Object visitName(Name node) throws Exception {
        String name = node.getInternalId();
        if (!isLoad(node)) {
            if (environment.atModuleLevel()) {
                // Module/global scope. No frame info needed.
            } else if (!environment.isLocalGlobals(name)) {
                // function scope
                environment.createLocal(name);
            }
        }

        return null;
    }

    @Override
    public Object visitListComp(ListComp node) throws Exception {
        String tmp = "_[" + node.getLine() + "_" + node.getCharPositionInLine() + "]";
        traverse(node);
        environment.createLocal(tmp);
        visit(node.getInternalElt());
        return null;
    }

    @Override
    public Object visitSetComp(SetComp node) throws Exception {
        String tmp = "_{" + node.getLine() + "_" + node.getCharPositionInLine() + "}";
        environment.createLocal(tmp);
        traverse(node);
        return null;
    }

    @Override
    public Object visitDictComp(DictComp node) throws Exception {
        String tmp = "_{" + node.getLine() + "_" + node.getCharPositionInLine() + "}";
        environment.createLocal(tmp);
        traverse(node);
        return null;
    }

    @Override
    public Object visitGeneratorExp(GeneratorExp node) throws Exception {
        String boundexp = "_(x)";
        String tmp = "_(" + node.getLine() + "_" + node.getCharPositionInLine() + ")";
        environment.createLocal(tmp);
        ArgListCompiler ac = new ArgListCompiler();
        List<expr> args = new ArrayList<>();
        args.add(new Name(node.getToken(), boundexp, expr_contextType.Param));
        ac.visitArgs(new arguments(node, args, null, null, new ArrayList<expr>()));
        environment.beginScope(node, ScopeInfo.ScopeKind.Generator);

        // visit first iterator in the new scope
        if (node.getInternalGenerators() != null && node.getInternalGenerators().size() > 0) {
            visit(node.getInternalGenerators().get(0).getInternalIter());
        }

        if (node.getInternalGenerators() != null) {
            for (int i = 0; i < node.getInternalGenerators().size(); i++) {
                if (node.getInternalGenerators().get(i) != null) {
                    if (i == 0) {
                        visit(node.getInternalGenerators().get(i).getInternalTarget());
                        if (node.getInternalGenerators().get(i).getInternalIfs() != null) {
                            for (expr cond : node.getInternalGenerators().get(i).getInternalIfs()) {
                                if (cond != null) {
                                    visit(cond);
                                }
                            }
                        }
                    } else {
                        visit(node.getInternalGenerators().get(i));
                    }
                }
            }
        }

        /*
         * The order and how different generator parts are parsed here makes zero sense to me.
         * 
         * Why the first iterator is evaluated in the outer scope? Answer: The first iterator should
         * be evaluated in the outer scope, and the returned sequence shall be passed to the closure
         * of the generator expression.
         * 
         * What is the purpose of this hard coded bound_exp
         * 
         * Why is generator targets parsed after the first elt? Answer: Elt need to be parsed after
         * generators. This is logical and in line with the interpretation order.
         */
        if (node.getInternalElt() != null) {
            visit(node.getInternalElt());
        }

        environment.endScope(node);
        return null;
    }

    @Override
    public Object visitPrint(Print node) throws Exception {
        traverse(node);
        return null;
    }

}
