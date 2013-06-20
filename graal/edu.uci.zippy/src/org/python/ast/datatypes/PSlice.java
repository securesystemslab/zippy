package org.python.ast.datatypes;

import org.python.core.*;

public class PSlice {

    private int start;
    private int stop;
    private int step;

    public PSlice(int start, int stop) {
       this(start, stop, 1);
    }

    public PSlice(int start, int stop, int step) {
        this.start = start;
        this.stop = stop;
        this.step = step;
    }
    
    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getStop() {
        return stop;
    }

    public void setStop(int stop) {
        this.stop = stop;
    }

    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        this.step = step;
    }
    
    public int computeActualIndices(int len) {
        int length;

        if (step == 0) {
            throw Py.ValueError("slice step cannot be zero");
        }
        
        if (start == Integer.MIN_VALUE) {
            start = step < 0 ? len - 1 : 0;
        } else {                
            if (start < 0) {
                start += len;
            }
            if (start < 0) {
                start = step < 0 ? -1 : 0;
            }
            if (start >= len) {
                start = step < 0 ? len - 1 : len;
            }
        }

        if (stop == Integer.MIN_VALUE) {
            stop = step < 0 ? -1 : len;
        } else {           
            if (stop < 0) {
                stop += len;
            }
            if (stop < 0) {
                stop = -1;
            }
            if (stop > len) {
                stop = len;
            }
        }

        if (step > 0 && stop < start) {
            stop = start;
        }

        if (step > 0) {
            length = (int) ((stop - start + step - 1) / step);
        } else {
           length = (int) ((stop - start + step + 1) / step);
        }

        if (length < 0) {
            length = 0;
        }
        
        return length;
    }

}
