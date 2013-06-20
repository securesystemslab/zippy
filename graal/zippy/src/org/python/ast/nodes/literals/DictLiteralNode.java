package org.python.ast.nodes.literals;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.python.ast.datatypes.*;
import org.python.ast.nodes.TypedNode;
import org.python.core.truffle.*;

import com.oracle.truffle.api.codegen.*;
import com.oracle.truffle.api.frame.*;

@ExecuteChildren({ "keys, values" })
public abstract class DictLiteralNode extends TypedNode {

    @Children
    protected TypedNode[] keys;

    @Children
    protected TypedNode[] values;

    public DictLiteralNode(TypedNode[] keys, TypedNode[] values) {
        this.keys = adoptChildren(keys);
        this.values = adoptChildren(values);
    }

    protected DictLiteralNode(DictLiteralNode node) {
        this(node.keys, node.values);
    }

    @Specialization
    public PDictionary doTruffleDictionary(VirtualFrame frame) {
        return (PDictionary) doGeneric(frame);
    }

    @Generic
    public Object doGeneric(VirtualFrame frame) {
        List<Object> resolvedKeys = new ArrayList<>();
        for (int i = 0; i < keys.length; i++) {
            TypedNode e = keys[i];
            resolvedKeys.add(e.executeGeneric(frame));
        }

        List<Object> resolvedValues = new ArrayList<>();
        for (int i = 0; i < values.length; i++) {
            TypedNode e = values[i];
            resolvedValues.add(e.executeGeneric(frame));
        }

        Map<Object, Object> map = new ConcurrentHashMap<Object, Object>();
        for (int i = 0; i < resolvedKeys.size(); i++) {
            map.put(resolvedKeys.get(i), resolvedValues.get(i));
        }

        return PythonTypesUtil.createDictionary(map);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }

    @Override
    public void visualize(int level) {
        for (int i = 0; i < level; i++) {
            System.out.print("    ");
        }
        System.out.println(this);

        level++;

        for (TypedNode k : keys) {
            k.visualize(level);
        }

        for (TypedNode v : values) {
            v.visualize(level);
        }
    }

}
