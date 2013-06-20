package org.python.ast.datatypes;

import java.util.*;

import org.python.ast.nodes.*;
import org.python.ast.utils.*;

public class PGenerator implements Iterable<Object>, Iterator<Object> {

    private final GeneratorRootNode root;

    private boolean hasNext = true;

    public PGenerator(GeneratorRootNode root) {
        this.root = root;
    }

    @Override
    public Object next() {
        try {
            return root.next();
        } catch (ImplicitReturnException ire) {
            hasNext = false;
            throw new BreakException();
        }
    }

    @Override
    public boolean hasNext() {
        return hasNext;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<Object> iterator() {
        return this;
    }

}
