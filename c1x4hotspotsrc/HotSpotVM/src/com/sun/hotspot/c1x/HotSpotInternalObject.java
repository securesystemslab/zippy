package com.sun.hotspot.c1x;

public class HotSpotInternalObject {

    private long id;

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (id ^ (id >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        HotSpotInternalObject other = (HotSpotInternalObject) obj;
        if (id != other.id)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "HotSpotInternalObject [id=" + id + "]";
    }

}
