package edu.uci.python.profiler;

import java.util.*;
import java.util.Map.Entry;

public class Profiler {

    private static Profiler INSTANCE = new Profiler();

    private HashMap<String, Long> invocationCounts;

    private Profiler() {
        invocationCounts = new HashMap<>();
    }

    public void increment(String callableName) {
        if (invocationCounts.containsKey(callableName)) {
            long counter = invocationCounts.get(callableName);
            invocationCounts.put(callableName, counter + 1);
        } else {
            invocationCounts.put(callableName, 1L);
        }
    }

    public void printProfilerResults() {
        Map<String, Long> sorted = sortByValue(invocationCounts);
        Iterator<Entry<String, Long>> it = sorted.entrySet().iterator();
        // Iterator<Entry<String, Integer>> it = invocationCounts.entrySet().iterator();

        while (it.hasNext()) {
            Entry<String, Long> pairs = it.next();
            // CheckStyle: stop system..print check
            System.out.println(pairs.getKey() + " = " + pairs.getValue());
            // CheckStyle: resume system..print check
            it.remove();
        }

    }

    public static Map<String, Long> sortByValue(Map<String, Long> map) {
        List<Map.Entry<String, Long>> list = new LinkedList<>(map.entrySet());

        Collections.sort(list, new Comparator<Map.Entry<String, Long>>() {

            public int compare(Map.Entry<String, Long> m1, Map.Entry<String, Long> m2) {
                return (m2.getValue()).compareTo(m1.getValue());
            }
        });

        Map<String, Long> result = new LinkedHashMap<>();
        for (Map.Entry<String, Long> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

    public static Profiler getInstance() {
        return INSTANCE;
    }

}
