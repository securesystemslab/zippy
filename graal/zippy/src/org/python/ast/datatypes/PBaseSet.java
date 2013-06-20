package org.python.ast.datatypes;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public abstract class PBaseSet extends PObject implements Iterable<Object> {

    protected Set<Object> set;

    public PBaseSet() {
        this.set = new HashSet<Object>();
    }

    public PBaseSet(Iterable<?> iterable) {
        this();
        Iterator<?> iter = iterable.iterator();

        while (iter.hasNext()) {
            this.set.add(iter.next());
        }
    }

    public PBaseSet(PBaseSet pBaseSet) {
        this();
        this.set.addAll(pBaseSet.set);
    }

    public Set<Object> getSet() {
        return set;
    }

    public int len() {
        return this.set.size();
    }

    public boolean contains(Object o) {
        return this.set.contains(o);
    }

    // disjoint
    public boolean isDisjoint(Iterable<Object> other) {
        return this.isDisjoint(new PSet(other));
    }

    public boolean isDisjoint(PBaseSet other) {
        return Collections.disjoint(this.set, other.set);
    }

    // subset
    public boolean isSubset(Iterable<Object> other) {
        return this.isSubset(new PSet(other)); // pack the iterable into a PSet
    }

    public boolean isSubset(PBaseSet other) {
        if (this.len() > other.len()) {
            return false;
        }
        for (Object p : this.set) {
            if (!other.set.contains(p)) {
                return false;
            }
        }
        return true;
    }

    public boolean isProperSubset(PBaseSet other) {
        return this.len() < other.len() && this.isSubset(other);
    }

    // superset
    public boolean isSuperset(Iterable<Object> other) {
        return this.isSuperset(new PSet(other));
    }

    public boolean isSuperset(PBaseSet other) {
        return other.isSubset(this); // use subset comparison with this/other
                                     // order changed
    }

    public boolean isProperSuperset(PBaseSet other) { // is proper superset
        return this.len() > other.len() && this.isSuperset(other);
    }

    // union
    public PBaseSet union(Iterable<Object> other) {
        return this.union(new PSet(other));
    }

    public PBaseSet union(PBaseSet other) {
        PBaseSet newSet = cloneThisSet();
        newSet.set.addAll(other.set);
        return newSet;
    }

    public PBaseSet union(Object[] args) {
        PBaseSet result = cloneThisSet();
        for (int i = 0; i < args.length; i++) {
            result.updateInternal(args[i]);
        }
        return result;
    }

    // TODO

    // intersection
    public PBaseSet intersection(Iterable<Object> other) {
        return this.intersection(new PSet(other));
    }

    public PBaseSet intersection(PBaseSet other) {
        PBaseSet newSet = cloneThisSet();
        newSet.set.retainAll(other.set);
        return newSet;
    }

    @SuppressWarnings("unchecked")
    public PBaseSet intersection(Object[] args) { // TODO
        PBaseSet result = cloneThisSet();
        for (int i = 0; i< args.length; i++) {
            if (args[i] instanceof PBaseSet) {
                result.intersection((PBaseSet) args[i]);
            } else if (args[i] instanceof Iterable) {
                result.intersection((Iterable<Object>) args[i]);
            }
        }
        return result;
    }

    // difference
    public PBaseSet difference(Iterable<Object> other) {
        return this.intersection(new PSet(other));
    }

    public PBaseSet difference(PBaseSet other) {
        PBaseSet newSet = cloneThisSet();
        newSet.set.removeAll(other.set);
        return newSet;
    }

    @SuppressWarnings("unchecked")
    public PBaseSet difference(Object[] args) { // TODO
        PBaseSet result = cloneThisSet();
        for (int i = 0; i < args.length; i++) {
            if (args[i] instanceof PBaseSet) {
                result.difference((PBaseSet) args[i]);
            } else if (args[i] instanceof Iterable) {
                result.difference((Iterable<Object>) args[i]);
            }
        }
        return result;
    }

    // symmetric_difference
    public PBaseSet symmetricDifference(Iterable<Object> other) {
        return this.intersection(new PSet(other));
    }

    public PBaseSet symmetricDifference(PBaseSet other) {
        // TODO
        return null;
    }

    public PBaseSet symmetricDifference(Object[] args) {
        // TODO
        return null;
    }

    // copy
    PBaseSet copy() {
        return cloneThisSet();
    }

    // update
    public abstract void update(Iterable<Object> other);

    public abstract void update(PBaseSet other);

    public abstract void update(Object[] args);

    // intersection_update
    public abstract void intersectionUpdate(Iterable<Object> other);

    public abstract void intersectionUpdate(PBaseSet other);

    public abstract void intersectionUpdate(Object[] args);

    // difference_update
    public abstract void differenceUpdate(Iterable<Object> other);

    public abstract void differenceUpdate(PBaseSet other);

    public abstract void differenceUpdate(Object[] args);

    // symmetric_difference_update
    public abstract void symmetricDifferenceUpdate(Iterable<Object> other);

    public abstract void symmetricDifferenceUpdate(PBaseSet other);

    // TODO

    // add
    public abstract boolean add(Object o);

    // remove
    public abstract boolean remove(Object o);

    // discard
    public abstract boolean discard(Object o);

    // pop
    public abstract boolean pop();

    // clear
    public abstract boolean clear();

    protected abstract PBaseSet cloneThisSet();

    // update methods needed for updating both sets and frozen sets, internally
    // "Binary operations that mix set instances with frozenset return
    // the type of the first operand.
    // For example: frozenset('ab') | set('bc') returns an instance of
    // frozenset."
    @SuppressWarnings("unchecked")
    protected void updateInternal(Object data) {
        if (data instanceof PBaseSet) {
            updateInternal((PBaseSet) data);
        } else if (data instanceof Iterable) {
            updateInternal((Iterable<Object>) data);
        }
    }

    protected void updateInternal(PBaseSet data) {
        // Skip the iteration if both are sets
        set.addAll(((PBaseSet) data).set);
    }

    protected void updateInternal(Iterable<Object> data) {
        for (Object item : data) {
            set.add(item);
        }
    }

    protected void updateInternal(Object[] data) {
        for (int i = 0; i < data.length; i++) {
            set.add(data[i]);
        }
    }

    public Iterator<Object> iterator() {
        return set.iterator();
    }
    
    @Override
    public Object getMin() {
        Object[] copy = this.set.toArray();
        Arrays.sort(copy);
        return copy[0];
    }
    
    @Override
    public Object getMax() {
        Object[] copy = this.set.toArray();
        Arrays.sort(copy);
        return copy[copy.length - 1];
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder().append("([");

        for (Iterator<Object> i = set.iterator(); i.hasNext();) {
            buf.append((i.next()).toString());

            if (i.hasNext()) {
                buf.append(", ");
            }
        }

        buf.append("])");
        return buf.toString();
    }

}
