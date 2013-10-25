package edu.uci.python.builtins;

import java.lang.annotation.*;

/**
 * @author Gulfem
 */

@Retention(RetentionPolicy.RUNTIME)
public @interface Builtin {

    String name() default "";

    int id() default -1;

    int numOfArguments() default -1;

    boolean varArgs() default false;

}