package org.python.core;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface PYBYTECODEIMPLEMENTATION {
    int value();
}
