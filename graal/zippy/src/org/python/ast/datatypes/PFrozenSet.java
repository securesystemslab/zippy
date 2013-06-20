package org.python.ast.datatypes;

public class PFrozenSet extends PBaseSet {

    public PFrozenSet() {
        super();
    }

    public PFrozenSet(Iterable<?> iterable) {
        super(iterable);
    }

    public PFrozenSet(PBaseSet pBaseSet) {
        super(pBaseSet);
    }

    @Override
    public void update(Iterable<Object> other) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void update(PBaseSet other) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void update(Object[] args) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void intersectionUpdate(Iterable<Object> other) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void intersectionUpdate(PBaseSet other) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void intersectionUpdate(Object[] args) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void differenceUpdate(Iterable<Object> other) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void differenceUpdate(PBaseSet other) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void differenceUpdate(Object[] args) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void symmetricDifferenceUpdate(Iterable<Object> other) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void symmetricDifferenceUpdate(PBaseSet other) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean add(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean discard(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean pop() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    protected PBaseSet cloneThisSet() {
        return new PFrozenSet(this);
    }
    
    @Override
    public Object multiply(int value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public PCallable findAttribute(String name) {
        throw new UnsupportedOperationException();
    }

}
