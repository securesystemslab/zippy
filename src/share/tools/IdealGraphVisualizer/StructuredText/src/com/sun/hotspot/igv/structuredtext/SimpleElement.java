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
import javax.swing.text.Style;
import javax.swing.text.StyleContext;

/**
 *
 * @author Thomas
 */
public class SimpleElement extends Element {

    public static final Element EMPTY = new SimpleElement("");
    public static final Element LN = new SimpleElement("\n");
    public static final Element TAB = new SimpleElement("\t");
    
    private String text;
    
    public SimpleElement(String s) {
        this(s, null);
    }
    
    public SimpleElement(String s, Style style) {
        setText(s);
        setStyle(style);
        assert text != null;
    }
    
    private static String addPadding(String s, int minLength) {
        
        StringBuilder sb = new StringBuilder(s);
        while(sb.length() < minLength) {
            sb.insert(0, ' ');
        }
        return sb.toString();
    }
    
    public SimpleElement(String s, int length) {
        this(addPadding(s, length));
    }
    
    
    public String getText() {
        return text;
    }
    
    public void setText(String s) {
        this.text = s;
    }
    
    public void accept(ElementVisitor visitor) {
        visitor.visit(this);
    }
}
