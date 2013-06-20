package org.python.modules.truffle;

import org.python.ast.datatypes.PCharArray;
import org.python.ast.datatypes.PSequence;
import org.python.modules.truffle.annotations.ModuleMethod;

public class StringAttribute extends Module {
    
    public StringAttribute() {
        try {
            addAttributeMethods();
        } catch (NoSuchMethodException | SecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    @ModuleMethod
    public String join(Object[] args, Object self) {
        if (args.length == 1) {
            return join(args[0], self);
        } else {
            throw new RuntimeException("wrong number of arguments for join()");
        }
    }
    
    public String join(Object arg, Object self) {
        if (arg instanceof String) {
            StringBuilder sb = new StringBuilder();
            char[] joinString = ((String) arg).toCharArray();
            for (int i = 0; i < joinString.length - 1; i++) {
                sb.append(Character.toString(joinString[i]));
                sb.append((String) self);
            }
            sb.append(Character.toString(joinString[joinString.length - 1]));
        
            return sb.toString();
        } else if (arg instanceof PSequence) {
            StringBuilder sb = new StringBuilder();
            Object[] stringList = ((PSequence) arg).getSequence();
            for (int i = 0; i < stringList.length - 1; i++) {
                sb.append((String) stringList[i]);
                sb.append((String) self);
            }
            sb.append((String) stringList[stringList.length - 1]);
        
            return sb.toString();
        } else if (arg instanceof PCharArray) {
            StringBuilder sb = new StringBuilder();
            char[] stringList = ((PCharArray) arg).getSequence();
            for (int i = 0; i < stringList.length - 1; i++) {
                sb.append(Character.toString(stringList[i]));
                sb.append((String) self);
            }
            sb.append(Character.toString(stringList[stringList.length - 1]));
        
            return sb.toString();
        } else {
            throw new RuntimeException("invalid arguments type for join()");
        }
    }
    
    public String join(Object arg0, Object arg1, Object self) {
        throw new RuntimeException("wrong number of arguments for join()");
    }
}
