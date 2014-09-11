package edu.uci.python.profiler;

public class Counter {

    private long counter;

    public Counter() {
        counter = 0;
    }

    public Counter(long counter) {
        this.counter = counter;
    }

    public void increment() {
        counter++;
    }

    public long getCounter() {
        return counter;
    }
}
