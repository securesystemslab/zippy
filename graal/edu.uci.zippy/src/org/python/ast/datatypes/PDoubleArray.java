package org.python.ast.datatypes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class PDoubleArray extends PArray implements Iterable<Double> {

    private final double[] array;

    public PDoubleArray() {
        array = new double[0];
    }

    public PDoubleArray(double[] elements) {
        if (elements == null) {
            array = new double[0];
        } else {
            array = new double[elements.length];
            System.arraycopy(elements, 0, array, 0, elements.length);
        }
    }

    /**
     * Note: This constructor assumes that <code>elements</code> is not null.
     * 
     * @param elements
     *            the tuple elements
     * @param copy
     *            whether to copy the elements into a new array or not
     */
    private PDoubleArray(double[] elements, boolean copy) {
        if (copy) {
            array = new double[elements.length];
            System.arraycopy(elements, 0, array, 0, elements.length);
        } else {
            array = elements;
        }
    }

    public double[] getSequence() {
        return array;
    }
    
    @Override
    public int len() {
        return array.length;
    }
    
    @Override
    public Object getItem(int idx) {
        return array[idx];
    }
    
    @Override
    public PDoubleArray getSlice(PSlice slice) {
        int length = slice.computeActualIndices(array.length);
        return getSlice(slice.getStart(), slice.getStop(), slice.getStep(), length);
    }

    @Override
    public PDoubleArray getSlice(int start, int stop, int step, int length) {
        double[] newArray = new double[length];

        if (step == 1) {
            System.arraycopy(array, start, newArray, 0, stop - start);
            return new PDoubleArray(newArray, false);
        }
        for (int i = start, j = 0; j < length; i += step, j++) {
            newArray[j] = array[i];
        }
        return new PDoubleArray(newArray, false);
    }

    @Override
    public void setItem(int idx, Object value) {
        if (idx < 0) {
            idx += array.length;
        }
        array[idx] = (double) value;
    }
    
    public void setSlice(PSlice slice, PDoubleArray value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object getMin() {
        double[] copy = Arrays.copyOf(this.array, this.array.length);
        Arrays.sort(copy);
        return copy[0];
    }

    @Override
    public Object getMax() {
        double[] copy = Arrays.copyOf(this.array, this.array.length);
        Arrays.sort(copy);
        return copy[copy.length - 1];
    }
    
    @Override
    public Object multiply(int value) {
        double[] newArray = new double[value * array.length];
        int count = 0;
        for (int i = 0; i < value; i++) {
            for (int j = 0; j < array.length; j++) {
                newArray[count++] = array[j];
            }
        }
        
        return new PDoubleArray(newArray);
    }
    
    @Override
    public PCallable findAttribute(String name) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder("(");
        for (int i = 0; i < array.length - 1; i++) {
            buf.append(array[i] + " ");
        }
        buf.append(array[array.length - 1]);
        buf.append(")");
        return buf.toString();
    }
    
    private List<Double> getList() {
        List<Double> list = new ArrayList<Double>();
        for (int i = 0; i < array.length; i++) {
            list.add(array[i]);
        }
        return list;
    }


    @Override
    public Iterator<Double> iterator() {
        return new Iterator<Double>() {

            private final Iterator<Double> iter = getList().iterator();

            public void remove() {
                throw new UnsupportedOperationException();
            }

            public boolean hasNext() {
                return iter.hasNext();
            }

            public Double next() {
                return iter.next();
            }
        };
    }
}
