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

import org.python.antlr.*;
import org.python.antlr.ast.*;
import org.python.antlr.base.*;

import edu.uci.python.nodes.*;

public class TranslationUtil {

    public static List<PythonTree> castToPythonTreeList(List<stmt> argsInit) {
        List<PythonTree> pythonTreeList = new ArrayList<>();

        for (stmt s : argsInit) {
            pythonTreeList.add(s);
        }

        return pythonTreeList;
    }

    public static boolean isLoad(Subscript subcript) {
        return subcript.getInternalCtx() == expr_contextType.Load;
    }

    public static boolean isStore(Subscript subcript) {
        return subcript.getInternalCtx() == expr_contextType.Store;
    }

    public static boolean isLoad(Name name) {
        return name.getInternalCtx() == expr_contextType.Load;
    }

    public static boolean isParam(Name name) {
        return name.getInternalCtx() == expr_contextType.Param;
    }

    public static boolean isBoolOrNone(Name name) {
        String symbol = name.getInternalId();
        return symbol.equals("None") || symbol.equals("True") || symbol.equals("False");
    }

    public static PNode getBoolOrNode(Name node) {
        String name = node.getInternalId();
        NodeFactory factory = NodeFactory.getInstance();

        if (name.equals("None")) {
            return EmptyNode.create();
        } else if (name.equals("True")) {
            return factory.createBooleanLiteral(true);
        } else if (name.equals("False")) {
            return factory.createBooleanLiteral(false);
        }

        throw notCovered();
    }

    public static String getScopeId(PythonTree scopeEntity, ScopeInfo.ScopeKind kind) {
        String scopeId = "unknown scope";

        if (kind == ScopeInfo.ScopeKind.Module) {
            scopeId = scopeEntity.toString();
        } else if (kind == ScopeInfo.ScopeKind.Function) {
            if (scopeEntity instanceof FunctionDef) {
                scopeId = ((FunctionDef) scopeEntity).getInternalName();
            } else if (scopeEntity instanceof Lambda) {
                scopeId = "lambda";
            }
        } else if (kind == ScopeInfo.ScopeKind.Class) {
            scopeId = ((ClassDef) scopeEntity).getInternalName();
        } else if (kind == ScopeInfo.ScopeKind.Generator) {
            scopeId = scopeEntity.toString();
        } else if (kind == ScopeInfo.ScopeKind.ListComp) {
            scopeId = scopeEntity.toString();
        }

        return scopeId;
    }

    public static NotCovered notCovered() {
        throw new NotCovered();
    }

    public static NotCovered notCovered(String message) {
        throw new NotCovered(message);
    }

    private static class NotCovered extends RuntimeException {

        private static final long serialVersionUID = 2485134940559018951L;

        public NotCovered() {
            super("This case is not covered!");
        }

        public NotCovered(String message) {
            super(message);
        }

    }
}
