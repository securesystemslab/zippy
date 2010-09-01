package com.sun.hotspot.c1x;

import com.sun.cri.ri.*;


public class HotSpotExceptionHandler implements RiExceptionHandler, CompilerObject {
    private int startBci;
    private int endBci;
    private int handlerBci;
    private int catchClassIndex;
    private RiType catchClass;

    @Override
    public int startBCI() {
        return startBci;
    }

    @Override
    public int endBCI() {
        return endBci;
    }

    @Override
    public int handlerBCI() {
        return handlerBci;
    }

    @Override
    public int catchClassIndex() {
        return catchClassIndex;
    }

    @Override
    public boolean isCatchAll() {
        return catchClassIndex == 0;
    }

    @Override
    public RiType catchKlass() {
        return catchClass;
    }

}
