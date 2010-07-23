package com.sun.hotspot.c1x;

import java.lang.reflect.*;

public class LoggingProxy<T> implements InvocationHandler {

    private T delegate;

    public LoggingProxy(T delegate) {
        this.delegate = delegate;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        int argCount = args == null ? 0 : args.length;
        if (method.getParameterTypes().length != argCount)
            throw new RuntimeException("wrong parameter count");
        System.out.println("method " + method + " called with " + argCount + " args");
        if (args == null)
            return method.invoke(delegate);
        return method.invoke(delegate, args);
    }

    public static <T> T getProxy(Class<T> interf, T delegate) {
        Object obj = Proxy.newProxyInstance(interf.getClassLoader(), new Class[] { interf}, new LoggingProxy<T>(delegate));
        return interf.cast(obj);
    }
}
