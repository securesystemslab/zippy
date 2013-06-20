package org.python.ast.datatypes;

public abstract class PArray extends PObject {
    
    public abstract int len();

    public abstract Object getItem(int idx);

    public abstract void setItem(int idx, Object value);

    public abstract Object getSlice(int start, int stop, int step, int length);

    public abstract Object getSlice(PSlice slice);

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
}
