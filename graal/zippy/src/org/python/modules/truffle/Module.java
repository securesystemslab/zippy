package org.python.modules.truffle;

import java.lang.reflect.*;
import java.util.*;

import org.python.ast.datatypes.PCallable;
import org.python.modules.truffle.annotations.*;

import com.oracle.truffle.api.frame.PackedFrame;

/**
 * The Python <code>Module</code> class.
 */
public class Module {

    private final Map<String, PCallable> methods = new HashMap<>();
    private final Map<String, Object> constants = new HashMap<>();
    

    /**
     * Set the value of a constant, possibly redefining it.
     */
    public void setConstant(String constantName, Object value) {
        constants.put(constantName, value);
    }

    /**
     * Add a method to the module. Only adds it to the module itself - not the singleton class of
     * the module instance. If you want to do that you need attr_accessor in Ruby, or attrAccessor
     * in Java.
     */
    public void addMethod(PCallable method) {
        methods.put(method.getName(), method);
    }

    /**
     * Remove a method from this module.
     */
    public void removeMethod(String methodName) {
        methods.remove(methodName);
    }

    public Object lookupConstant(String constantName) {
        Object value;

        // Look in this module

        value = constants.get(constantName);

        if (value != null) {
            return value;
        }

        // Nothing found

        return null;
    }
    
    public Object lookup(String name) {
        PCallable method = lookupMethod(name);
        
        if (method != null)
            return method;
        
        return lookupConstant(name);
    }

    public PCallable lookupMethod(String methodName) {
        PCallable method;

        // Look in this module

        method = methods.get(methodName);

        if (method != null) {
            return method;
        }
        
        // Not found
        
        return null;
    }

    /**
     * Load constants that are marked with @ModuleConstant in this class into the module.
     */
    public void addConstants() {
        for (Field field : this.getClass().getFields()) {
            final int m = field.getModifiers();

            if (Modifier.isPublic(m) && Modifier.isStatic(m) && Modifier.isFinal(m) && field.getAnnotation(ModuleConstant.class) != null) {
                try {
                    setConstant(field.getName(), field.get(null));
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("access error when populating constants", e);
                }
            }
        }
    }

    /**
     * Load methods that are marked with @ModuleMethod in this class into the module.
     * @throws SecurityException 
     * @throws NoSuchMethodException 
     */
    public void addBuiltInMethods() throws NoSuchMethodException, SecurityException {
        addBuiltInMethods(this.getClass());
    }

    /**
     * Load methods that are marked with @ModuleMethod in the supplied class into the module.
     * @throws SecurityException 
     * @throws NoSuchMethodException 
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void addBuiltInMethods(final Class definingClass) throws NoSuchMethodException, SecurityException {
        
        for (Method method : definingClass.getMethods()) {
            
            final Object finalDefiningModule = this;
            
            final int m = method.getModifiers();

            if (Modifier.isPublic(m) && method.getAnnotation(ModuleMethod.class) != null) {
                final Method finalMethod = method;
                
                final Method method1 = definingClass.getMethod(finalMethod.getName(), new Class[]{Object.class});
                final Method method2 = definingClass.getMethod(finalMethod.getName(), new Class[]{Object.class, Object.class});

                                
                final PCallable pythonMethod = new PCallable(method.getName()) {
                    
                    @Override
                    public Object call(PackedFrame caller, Object arg) {
                        try {
                            return method1.invoke(finalDefiningModule, new Object[]{ arg });
                        } catch (IllegalAccessException | InvocationTargetException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    
                    @Override
                    public Object call(PackedFrame caller, Object arg0, Object arg1) {
                        try {
                            return method2.invoke(finalDefiningModule, new Object[]{ arg0, arg1 });
                        } catch (IllegalAccessException | InvocationTargetException e) {
                            throw new RuntimeException(e);
                        }
                    }

                    @Override
                    public Object call(PackedFrame frame, Object[] args, Object[] keywords) {
                        try {
                            return finalMethod.invoke(finalDefiningModule, new Object[]{ args, keywords });
                        } catch (IllegalAccessException | InvocationTargetException e) {
                            throw new RuntimeException(e);
                        }
                    }

                };
                addMethod(pythonMethod);
            }
        }
    }
    
    public void addAttributeMethods() throws NoSuchMethodException, SecurityException {
        addAttributeMethods(this.getClass());
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void addAttributeMethods(final Class definingClass) throws NoSuchMethodException, SecurityException {
        
        for (Method method : definingClass.getMethods()) {
            
            final Object finalDefiningModule = this;
            
            final int m = method.getModifiers();

            if (Modifier.isPublic(m) && method.getAnnotation(ModuleMethod.class) != null) {
                
                final Method finalMethod = method;
                
                final Method method1 = definingClass.getMethod(finalMethod.getName(), new Class[]{Object.class, Object.class});
                final Method method2 = definingClass.getMethod(finalMethod.getName(), new Class[]{Object.class, Object.class, Object.class});

                                
                final PCallable pythonMethod = new PCallable(method.getName()) {
                    
                    @Override
                    public Object call(PackedFrame caller, Object arg) {
                        try {
                            return method1.invoke(finalDefiningModule, new Object[]{ arg, this.self });                                    
                        } catch (IllegalAccessException | InvocationTargetException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    
                    @Override
                    public Object call(PackedFrame caller, Object arg0, Object arg1) {
                        try {
                            return method2.invoke(finalDefiningModule, new Object[]{ arg0, arg1, this.self });
                        } catch (IllegalAccessException | InvocationTargetException e) {
                            throw new RuntimeException(e);
                        }
                    }

                    @Override
                    public Object call(PackedFrame frame, Object[] args, Object[] keywords) {
                        try {
                            return finalMethod.invoke(finalDefiningModule, new Object[]{ args, this.self });
                        } catch (IllegalAccessException | InvocationTargetException e) {
                            throw new RuntimeException(e);
                        }
                    }

                };
                addMethod(pythonMethod);
            }
        }
    }
}

