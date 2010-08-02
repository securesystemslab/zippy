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
        StringBuilder str = new StringBuilder();
        str.append(method.getReturnType().getSimpleName() + " " + method.getDeclaringClass().getSimpleName() + "." + method.getName() + "(");
        for (int i = 0; i < argCount; i++) {
            str.append(i == 0 ? "" : ", ");
            str.append(Logger.pretty(args[i]));
        }
        str.append(")");
        Logger.startScope(str.toString());
        final Object result;
        if (args == null) {
            result = method.invoke(delegate);
        } else {
            result = method.invoke(delegate, args);
        }
        Logger.endScope(" = " + Logger.pretty(result));
        return result;
    }

    public static <T> T getProxy(Class<T> interf, T delegate) {
        Object obj = Proxy.newProxyInstance(interf.getClassLoader(), new Class[] { interf}, new LoggingProxy<T>(delegate));
        return interf.cast(obj);
    }
}
