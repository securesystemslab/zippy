package org.python.ast.datatypes;

import java.util.Iterator;

public abstract class PSequence extends PObject implements Iterable<Object> {

    public abstract int len();

    public abstract Object getItem(int idx);

    public abstract void setItem(int idx, Object value);

    public abstract Object getSlice(int start, int stop, int step, int length);

    public abstract Object getSlice(PSlice slice);

    public abstract void setSlice(int start, int stop, int step, PSequence value);

    public abstract void setSlice(PSlice slice, PSequence value);

    public abstract void delItem(int idx);

    public abstract void delItems(int start, int stop);

    public abstract Iterator<Object> iterator();

    public abstract Object[] getSequence();

    public abstract boolean lessThan(PSequence sequence);

    /**
     * Make step a long in case adding the start, stop and step together
     * overflows an int.
     */
    public static final int sliceLength(int start, int stop, long step) {
        int ret;
        if (step > 0) {
            ret = (int) ((stop - start + step - 1) / step);
        } else {
            ret = (int) ((stop - start + step + 1) / step);
        }

        if (ret < 0) {
            return 0;
        }

        return ret;
    }

    /*
     * Compare the specified object/length pairs.
     * @return value >= 0 is the index where the sequences differs. 
     * -1: reached the end of sequence1 without a difference 
     * -2: reached the end of both seqeunces without a difference 
     * -3: reached the end of sequence2 without a difference
     */
    protected static int cmp(PSequence sequence1, PSequence sequence2) {
        int length1 = sequence1.len();
        int length2 = sequence2.len();

        for (int i = 0; i < length1 && i < length2; i++) {
            if (!sequence1.getItem(i).equals(sequence2.getItem(i))) {
                return i;
            }
        }
        if (length1 == length2) {
            return -2;
        }
        return length1 < length2 ? -1 : -3;
    }

}
