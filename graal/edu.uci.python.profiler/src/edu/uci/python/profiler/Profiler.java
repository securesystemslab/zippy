package edu.uci.python.profiler;

import java.util.*;
import java.util.Map.Entry;

public class Profiler {

    private static Profiler INSTANCE = new Profiler();

    private HashMap<Object, Integer> invocationCounts;

    private Profiler() {
        invocationCounts = new HashMap<>();
    }

    public static Profiler getInstance() {
        return INSTANCE;
    }

    public void increment(Object callable) {
        if (invocationCounts.containsKey(callable)) {
            int counter = invocationCounts.get(callable);
            invocationCounts.put(callable, counter + 1);
        } else {
            invocationCounts.put(callable, 1);
        }
    }

    public void removeAfterInlining(Object callable) {
        invocationCounts.remove(callable);
        // CheckStyle: stop system..print check
        System.out.println("[ZipPy Profiler] removing from profiling after inlining " + callable);
        // CheckStyle: resume system..print check
    }

    public void printProfilerResults() {
        Iterator<Entry<Object, Integer> > it = invocationCounts.entrySet().iterator();
        
        while (it.hasNext()) {
            Entry<Object, Integer> pairs = it.next();
            // CheckStyle: stop system..print check
            System.out.println(pairs.getKey() + " = " + pairs.getValue());
            // CheckStyle: resume system..print check
            it.remove();
        }
    }

}
