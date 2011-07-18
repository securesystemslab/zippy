/*
 * Copyright (c) 2011, Oracle and/or its affiliates. All rights reserved.
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

package com.sun.hotspot.igv.structuredtext;

import com.sun.hotspot.igv.structuredtext.services.ElementVisitor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.text.Style;

/**
 *
 * @author Thomas
 */
public class MultiElement extends Element {

    private List<Element> children;
    private String foldString;
    
    public MultiElement() {
        this((String)null);
    }
       
    public MultiElement(String foldString) {
        this(foldString, null);
    }
    
    public MultiElement(Style style) {
        this(null, style);
    }
    
    public MultiElement(String foldString, Style style) {
        setStyle(style);
        this.foldString = foldString;
        children = new ArrayList<Element>();
    }
    
    public void print(String s) {
        print(s, null);
    }

    @Override
    public void setStyleRecursive(Style style) {
        super.setStyleRecursive(style);
        for(Element elem : this.getChildren()) {
            elem.setStyleRecursive(style);
        }
    }

    @Override
    public void setHighlightedStyleRecursive(Style style) {
        super.setStyleRecursive(style);
        for(Element elem : this.getChildren()) {
            elem.setHighlightedStyleRecursive(style);
        }
    }
    
    public void print(String s, Object source) {
        if (s == null) {
            s = "";
        }
        SimpleElement elem = new SimpleElement(s);
        if(source != null) {
            elem.addSource(source);
        }
        addChild(elem);
    }

    public void print(String s, Object source, int padding) {
        if (s == null) {
            s = "";
        }
        StringBuilder sb = new StringBuilder(s);
        while (sb.length() < padding) {
            sb.insert(0, ' ');
        }
        print(sb.toString(), source);
    }

    public void println() {
        println("");
    }
    
    public void println(String s) {
        print(s + "\n");
    }
    
    public void println(String s, Object source) {
        print(s + "\n", source);
    }
    
    
    public void println(String s, Object source, int padding) {
        print(s + "\n", source, padding);
    }
    
    public void addChild(Element element) {
        assert element != null;
        this.children.add(element);
    }
    
    public String getFoldString() {
        return foldString;
    }
    
    public void setFoldString(String s) {
        this.foldString = s;
    }
    
    public List<Element> getChildren() {
        return Collections.unmodifiableList(children);
    }
    
    public void accept(ElementVisitor visitor) {
        visitor.visit(this);
    }
}
