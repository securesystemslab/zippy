package org.python.ast.nodes.expressions;

import org.python.ast.nodes.TypedNode;
import org.python.core.truffle.*;

import com.oracle.truffle.api.codegen.*;
import com.oracle.truffle.api.frame.*;

public abstract class ReadGlobalNode extends TypedNode {

    private final String name;
    
    private final GlobalScope instance = GlobalScope.getInstance();

    public ReadGlobalNode(String name) {
        this.name = name;
    }

    @Specialization
    public Object doObject(VirtualFrame frame) {
        /*
         *  cache the instance instead of lookup it everytime 
         */
        Object ret = instance.get(name);

        /*
         *  we almost have all built-in modules for the benchmarks, no need to spend time checking
         */
        return ret;
    }
    
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "(" + name + ")";
    }

    @Override
    public void visualize(int level) {
        for (int i = 0; i < level; i++) {
            System.out.print("    ");
        }

        System.out.println(this);
    }

}
