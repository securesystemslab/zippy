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
package edu.uci.python.nodes.translation;

import java.util.List;
import java.util.ArrayList;

import org.python.antlr.*;
import org.python.antlr.ast.*;
import org.python.antlr.base.*;
import org.python.compiler.*;
import org.python.core.*;

import com.oracle.truffle.api.frame.*;

import edu.uci.python.nodes.truffle.*;

public class PythonTreeProcessor extends Visitor {

    private final TranslationEnvironment environment;

    public PythonTreeProcessor(TranslationEnvironment environment) {
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
        environment.beginScope(node);
        visitStatements(node.getInternalBody());

        FrameDescriptor fd = environment.endScope();
        environment.setFrameDescriptor(node, fd);
        return node;
    }

    public void visitStatements(List<stmt> stmts) throws Exception {
        for (int i = 0; i < stmts.size(); i++) {
            visit(stmts.get(i));
        }
    }

    private FrameSlot def(String name) {
        return environment.findOrAddFrameSlot(name);
    }

    private FrameSlot find(String name) {
        return environment.findFrameSlot(name);
    }

    private FrameSlot defGlobal(String name) {
        return environment.defGlobal(name);
    }

    private void setFrameSlot(PythonTree symbol, FrameSlot slot) {
        environment.setFrameSlot(symbol, slot);
    }

    @Override
    public Object visitFunctionDef(FunctionDef node) throws Exception {
        FrameSlot slot = def(node.getInternalName());
        setFrameSlot(node.getInternalNameNode(), slot);

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

        environment.beginScope(node);
        int n = ac.names.size();
        for (int i = 0; i < n; i++) {
            def(ac.names.get(i));
        }

        visitArgs(node.getInternalArgs(), ac);
        List<PythonTree> argsInit = TranslationUtil.castToPythonTreeList(ac.init_code);
        node.addChildren(argsInit);
        node.getInternalBody().addAll(0, ac.init_code);

        visitStatements(node.getInternalBody());
        FrameDescriptor fd = environment.endScope();
        environment.setFrameDescriptor(node, fd);
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
    }

    @Override
    public Object visitLambda(Lambda node) throws Exception {
        ArgListCompiler ac = new ArgListCompiler();
        ac.visitArgs(node.getInternalArgs());

        List<? extends PythonTree> defaults = ac.getDefaults();
        for (int i = 0; i < defaults.size(); i++) {
            visit(defaults.get(i));
        }

        environment.beginScope(node);

        for (Object o : ac.init_code) {
            visit((stmt) o);
        }

        visit(node.getInternalBody());
        environment.endScope();
        return null;
    }

    @Override
    public Object visitImport(Import node) throws Exception {
        for (int i = 0; i < node.getInternalNames().size(); i++) {
            alias a = node.getInternalNames().get(i);

            if (node.getInternalNames().get(i).getInternalAsname() != null) {
                String name = a.getInternalAsname();
                setFrameSlot(a, def(name));
            } else {
                String name = a.getInternalName();
                if (name.indexOf('.') > 0) {
                    name = name.substring(0, name.indexOf('.'));
                }
                setFrameSlot(a, def(name));
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
                String name = a.getInternalAsname();
                setFrameSlot(a, def(name));
            } else {
                String name = a.getInternalName();
                setFrameSlot(a, def(name));
            }
        }

        return null;
    }

    @Override
    public Object visitGlobal(Global node) throws Exception {
        int n = node.getInternalNames().size();

        for (int i = 0; i < n; i++) {
            String name = node.getInternalNames().get(i);
            defGlobal(name);
            environment.addLocalGlobals(name);
        }

        return null;
    }

    @Override
    public Object visitClassDef(ClassDef node) throws Exception {
        setFrameSlot(node, def(node.getInternalName()));
        int n = node.getInternalBases().size();

        for (int i = 0; i < n; i++) {
            visit(node.getInternalBases().get(i));
        }

        environment.beginScope(node);
        visitStatements(node.getInternalBody());
        environment.endScope();
        return null;
    }

    @Override
    public Object visitName(Name node) throws Exception {
        String name = node.getInternalId();

        if (node.getInternalCtx() != expr_contextType.Load) {
            if (environment.getScopeLevel() == 1) {
                // Module global scope
                /**
                 * Variables in module's scope are also treated as globals This is why slot is not
                 * set for variables in module's scope WriteGlobal or ReadGlobal
                 */
                if (!GlobalScope.getInstance().isGlobalOrBuiltin(name)) {
                    setFrameSlot(node, def(name));
                }
            } else if (!environment.isLocalGlobals(name)) {
                // function scope
                setFrameSlot(node, def(name));
            }
        } else {
            FrameSlot slot = find(name);

            if (slot == null && environment.getScopeLevel() > 1) {
                slot = environment.probeEnclosingScopes(name);
            }

            setFrameSlot(node, slot);
        }

        return null;
    }

    @Override
    public Object visitListComp(ListComp node) throws Exception {
        String tmp = "_[" + node.getLine() + "_" + node.getCharPositionInLine() + "]";
        traverse(node);
        setFrameSlot(node, def(tmp));
        transformComprehensions(node.getInternalGenerators(), node.getInternalElt());
        return null;
    }

    private void transformComprehensions(List<comprehension> generators, expr body) throws Exception {
        for (int i = 0; i < generators.size(); i++) {
            comprehension c = generators.get(i);
            if (i + 1 <= generators.size() - 1) { // has next
                environment.setInnerLoop(c, generators.get(i + 1));
            } else { // last/inner most
                environment.setLoopBody(c, body);
            }
        }

        // re-process inner most body
        visit(body);
    }

    @Override
    public Object visitSetComp(SetComp node) throws Exception {
        String tmp = "_{" + node.getLine() + "_" + node.getCharPositionInLine() + "}";
        def(tmp);
        traverse(node);
        return null;
    }

    @Override
    public Object visitDictComp(DictComp node) throws Exception {
        String tmp = "_{" + node.getLine() + "_" + node.getCharPositionInLine() + "}";
        def(tmp);
        traverse(node);
        return null;
    }

    @Override
    public Object visitGeneratorExp(GeneratorExp node) throws Exception {
        // The first iterator is evaluated in the outer scope
// if (node.getInternalGenerators() != null && node.getInternalGenerators().size() > 0) {
// visit(node.getInternalGenerators().get(0).getInternalIter());
// }
        String boundexp = "_(x)";
        String tmp = "_(" + node.getLine() + "_" + node.getCharPositionInLine() + ")";
        def(tmp);
        ArgListCompiler ac = new ArgListCompiler();
        List<expr> args = new ArrayList<>();
        args.add(new Name(node.getToken(), boundexp, expr_contextType.Param));
        ac.visitArgs(new arguments(node, args, null, null, new ArrayList<expr>()));
        environment.beginScope(node);

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
        transformComprehensions(node.getInternalGenerators(), node.getInternalElt());

        if (node.getInternalElt() != null) {
            visit(node.getInternalElt());
        }

        FrameDescriptor fd = environment.endScope();
        environment.setFrameDescriptor(node, fd);
        return null;
    }

    @Override
    public Object visitPrint(Print node) throws Exception {
        traverse(node);
        return null;
    }

}
