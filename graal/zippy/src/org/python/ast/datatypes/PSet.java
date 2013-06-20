package org.python.ast.datatypes;

public class PSet extends PBaseSet {

    public PSet() {
        super();
    }
    
    public PSet(Iterable<?> iterable) {
        super(iterable);
    }
    
    public PSet(PBaseSet pBaseSet) {
        super(pBaseSet);
    }

    // update
    @Override
    public void update(Iterable<Object> other) {
        this.updateInternal(other);
    }
    
    @Override
    public void update(PBaseSet other) {
        this.updateInternal(other);
    }
    
    @Override
    public void update(Object[] args) {
        for (int i = 0; i < args.length; i++) {
            this.updateInternal(args[i]);
        }
    }
    
    // intersection_update
    @Override
    public void intersectionUpdate(Iterable<Object> other) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void intersectionUpdate(PBaseSet other) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void intersectionUpdate(Object[] args) {
        // TODO Auto-generated method stub
        
    }

    // difference_update
    @Override
    public void differenceUpdate(Iterable<Object> other) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void differenceUpdate(PBaseSet other) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void differenceUpdate(Object[] args) {
        // TODO Auto-generated method stub
        
    }

    // symmetric_difference_update
    @Override
    public void symmetricDifferenceUpdate(Iterable<Object> other) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void symmetricDifferenceUpdate(PBaseSet other) {
        // TODO Auto-generated method stub
        
    }

    // add
    @Override
    public boolean add(Object o) {
        // TODO Auto-generated method stub
        return false;
    }

    // remove
    @Override
    public boolean remove(Object o) {
        // TODO Auto-generated method stub
        return false;
    }

    // discard
    @Override
    public boolean discard(Object o) {
        // TODO Auto-generated method stub
        return false;
    }

    // pop
    @Override
    public boolean pop() {
        // TODO Auto-generated method stub
        return false;
    }

    // clear
    @Override
    public boolean clear() {
        // TODO Auto-generated method stub
        return false;
    }
     
    @Override
    protected PBaseSet cloneThisSet() {
        return new PSet(this);
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
