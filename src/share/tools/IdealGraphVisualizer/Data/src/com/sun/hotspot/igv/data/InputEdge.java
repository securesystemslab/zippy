/*
 * Copyright (c) 2008, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 *
 */
package com.sun.hotspot.igv.data;

import java.util.Comparator;

/**
 *
 * @author Thomas Wuerthinger
 */
public class InputEdge {

    public enum State {

        SAME,
        NEW,
        DELETED
    }
    
    public static final Comparator<InputEdge> OUTGOING_COMPARATOR = new Comparator<InputEdge>(){

        @Override
            public int compare(InputEdge o1, InputEdge o2) {
                if(o1.getFromIndex() == o2.getFromIndex()) {
                    return o1.getTo() - o2.getTo();
                }
                return o1.getFromIndex() - o2.getFromIndex();
            }
    };
    
    
    public static final Comparator<InputEdge> INGOING_COMPARATOR = new Comparator<InputEdge>(){

        @Override
            public int compare(InputEdge o1, InputEdge o2) {
                if(o1.getToIndex() == o2.getToIndex()) {
                    return o1.getFrom() - o2.getFrom();
                }
                return o1.getToIndex() - o2.getToIndex();
            }
    };

    private char toIndex;
    private char fromIndex;
    private int from;
    private int to;
    private State state;
    private String label;

    public InputEdge(char toIndex, int from, int to) {
        this((char) 0, toIndex, from, to, null);
    }

    public InputEdge(char fromIndex, char toIndex, int from, int to) {
        this(fromIndex, toIndex, from, to, null);
    }

    public InputEdge(char fromIndex, char toIndex, int from, int to, String label) {
        this.toIndex = toIndex;
        this.fromIndex = fromIndex;
        this.from = from;
        this.to = to;
        this.state = State.SAME;
        this.label = label;
    }

    public State getState() {
        return state;
    }

    public void setState(State x) {
        this.state = x;
    }

    public char getToIndex() {
        return toIndex;
    }
    
    public char getFromIndex() {
        return fromIndex;
    }

    public String getName() {
        return "in" + toIndex;
    }

    public int getFrom() {
        return from;
    }

    public int getTo() {
        return to;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof InputEdge)) {
            return false;
        }
        InputEdge conn2 = (InputEdge) o;
        return conn2.fromIndex == fromIndex && conn2.toIndex == toIndex && conn2.from == from && conn2.to == to;
    }

    @Override
    public String toString() {
        return "Edge from " + from + " to " + to + "(" + (int) fromIndex + ", " + (int) toIndex + ") ";
    }

    @Override
    public int hashCode() {
        return (from << 20 | to << 8 | toIndex << 4 | fromIndex);
    }
}
