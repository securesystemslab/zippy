package org.python.core.truffle;

import java.util.ArrayList;
import java.util.List;

import org.python.antlr.ast.comprehension;
import org.python.ast.datatypes.PArguments;
import org.python.ast.utils.*;
import org.python.core.PyList;
import org.python.core.PyObject;

import com.oracle.truffle.api.Arguments;
import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.frame.PackedFrame;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.impl.DefaultVirtualFrame;
import com.oracle.truffle.api.nodes.RootNode;

/**
 * It is only used by the non-specialized interpreter
 * 
 * @author zwei
 * 
 */
public class GeneratorCallTarget extends CallTarget {

    protected final RootNode rootNode;
    protected final FrameDescriptor frameDescriptor;
    VirtualFrame frame;
    Object iterator;

    public GeneratorCallTarget(RootNode generatorNode, FrameDescriptor frameDescriptor, Object iterator) {
        this.rootNode = generatorNode;
        this.frameDescriptor = frameDescriptor;
        this.iterator = iterator;
    }

    @Override
    public Object call(PackedFrame caller, Arguments arguments) {
        frame = new DefaultVirtualFrame(frameDescriptor, caller, arguments);
        List<PyObject> results = new ArrayList<PyObject>();

        while (true) {
            try {
                Object item = iterNext();
                if (item != null) {
                    results.add((PyObject) item);
                }
            } catch (IteratorTerminationException e) {
                break;
            }
        }

        return new PyList(results);
    }

    public Object __iter__(VirtualFrame caller) {
        frame = new DefaultVirtualFrame(frameDescriptor, caller.pack(), new PArguments());

        assert iterator != null;
        return this;
    }

    public Object iterNext() {
        assert rootNode instanceof comprehension;
        comprehension comp = (comprehension) rootNode;
        return comp.iterNext(frame, iterator);
    }

    @Override
    public String toString() {
        return "Generator ";
    }
}
