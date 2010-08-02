package com.sun.hotspot.c1x;

import java.lang.reflect.*;
import java.util.*;

public class Logger {

    private static final boolean ENABLED = true;
    private static final int SPACING = 2;
    private static Deque<Boolean> openStack = new LinkedList<Boolean>();
    private static boolean open = false;
    private static int level = 0;

    public static void log(String message) {
        if (ENABLED) {
            if (open) {
                System.out.println("...");
                open = false;
            }
            System.out.print(space(level));
            System.out.println(message);
        }
    }

    public static void startScope(String message) {
        if (ENABLED) {
            if (open) {
                System.out.println("...");
                open = false;
            }
            System.out.print(space(level));
            System.out.print(message);
            openStack.push(open);
            open = true;
            level++;
        }
    }

    public static void endScope(String message) {
        if (ENABLED) {
            level--;
            if (open)
                System.out.println(message);
            else
                System.out.println(space(level) + "..." + message);
            open = openStack.pop();
        }
    }

    private static String[] spaces = new String[50];

    private static String space(int count) {
        assert count >= 0;
        String result;
        if (count >= spaces.length || spaces[count] == null) {
            StringBuilder str = new StringBuilder();
            for (int i = 0; i < count * SPACING; i++)
                str.append(' ');
            result = str.toString();
            if (count < spaces.length)
                spaces[count] = result;
        } else {
            result = spaces[count];
        }
        return result;
    }

    public static String pretty(Object value) {
        if (value instanceof Method) {
            return "method \"" + ((Method) value).getName() + "\"";
        } else if (value instanceof Class<?>) {
            return "class \"" + ((Class<?>) value).getSimpleName() + "\"";
        } else if (value instanceof Void) {
            return "void";
        } else if (value instanceof Integer) {
            if ((Integer) value < 10)
                return value.toString();
            return value + " (0x" + Integer.toHexString((Integer) value) + ")";
        } else if (value instanceof Long) {
            if ((Long) value < 10)
                return value + "l";
            return value + "l (0x" + Long.toHexString((Long) value) + "l)";
        }
        return value == null ? "null" : value.toString();
    }
}
