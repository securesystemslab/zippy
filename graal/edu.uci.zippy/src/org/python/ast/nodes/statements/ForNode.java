package org.python.ast.nodes.statements;

import java.util.*;

import org.python.ast.*;
import org.python.ast.nodes.LeftHandSideNode;
import org.python.ast.nodes.TypedNode;
import org.python.ast.utils.*;
import org.python.core.PyObject;

import com.oracle.truffle.api.frame.*;

import static org.python.core.truffle.PythonTypesUtil.*;

public class ForNode extends StatementNode {

    @Child
    protected LeftHandSideNode target;

    @Child
    protected TypedNode iterator;

    @Child
    protected BlockNode body;

    @Child
    protected BlockNode orelse;

    public ForNode(LeftHandSideNode target, TypedNode iterator, BlockNode body, BlockNode orelse) {
        this.target = adoptChild(target);
        this.iterator = adoptChild(iterator);
        this.body = adoptChild(body);
        this.orelse = adoptChild(orelse);
    }
    
    public void setInternal(BlockNode body, BlockNode orelse) {
        this.body = adoptChild(body);
        this.orelse = adoptChild(orelse);
    }

    @Override
    public void executeVoid(VirtualFrame frame) {
        Object evaluatedIterator = iterator.executeGeneric(frame);

        if (evaluatedIterator instanceof Iterable) {
            loopOnIterable(frame, (Iterable<?>) evaluatedIterator);
        } else if (evaluatedIterator instanceof PyObject) {
            loopOnPyObject(frame, (PyObject) evaluatedIterator);
        } else {
            throw new RuntimeException("Unexpected iterator type " + evaluatedIterator.getClass());
        }
    }

    private void loopOnIterable(VirtualFrame frame, Iterable<?> iterable) {
        Iterator<?> iter = iterable.iterator();

        //try {
        while (iter.hasNext()) {
            target.doLeftHandSide(frame, iter.next());

            try {
                body.executeVoid(frame);
                if (reachedReturn() || isBreak()) {
                    this.isBreak = false;
                    return;
                }
            } catch (ContinueException ex) {
                // Fall through to next loop iteration.
            }
        }
        //} catch (BreakException ex) {
            // Done executing this loop.
            // If there is a break, orelse should not be executed
        //    return;
        //}

        orelse.executeVoid(frame);
    }

    private void loopOnPyObject(VirtualFrame frame, PyObject sequence) {
        PyObject iterator = sequence.__iter__();
        PyObject itValue;

        //try {
        while ((itValue = iterator.__iternext__()) != null) {
            target.doLeftHandSide(frame, unboxPyObject(itValue));

            try {
                body.executeVoid(frame);
                if (reachedReturn() || isBreak()) {
                    this.isBreak = false;
                    return;
                }
            } catch (ContinueException ex) {
                // Fall through to next loop iteration.
            }
        }
        //} catch (BreakException ex) {
            // Done executing this loop
            // If there is a break, orelse should not be executed
        //    return;
        //}
        
        orelse.executeVoid(frame);
    }

    @Override
    public String toString() {
        return super.toString() + "(" + target + ", " + iterator + ")";
    }

    public <R> R accept(StatementVisitor<R> visitor) {
        return visitor.visitForNode(this);
    }

    @Override
    public void visualize(int level) {
        for (int i = 0; i < level; i++) {
            System.out.print("    ");
        }
        System.out.println(this);

        level++;
//        PythonTree p = (PythonTree) target;
//        p.visualize(level);
        iterator.visualize(level);
        body.visualize(level);
    }

    public static class GeneratorForNode extends ForNode {

        private Iterator<?> iter;

        public GeneratorForNode(ForNode node) {
            super(node.target, node.iterator, node.body, node.orelse);
        }

        @Override
        public void executeVoid(VirtualFrame frame) {
            if (iter == null) {
                Iterable<?> it = (Iterable<?>) this.iterator.executeGeneric(frame);
                iter = (Iterator<?>) it.iterator();
            }

            try {
                while (iter.hasNext()) {
                    target.doLeftHandSide(frame, iter.next());

                    try {
                        body.executeVoid(frame);
                    } catch (ContinueException ex) {

                    }
                }
            } catch (BreakException ex) {
                return;
            }

            orelse.executeVoid(frame);
        }

    }

}
