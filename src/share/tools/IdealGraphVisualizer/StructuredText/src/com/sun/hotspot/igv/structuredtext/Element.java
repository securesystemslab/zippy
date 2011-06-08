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
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.swing.text.Style;
import javax.swing.text.StyleContext;

/**
 *
 * @author Thomas
 */
public abstract class Element {
    
    private Set<Object> source;
    private Style style;
    private Style highlightedStyle;
    
    public Element() {
        source = new HashSet<Object>();
        style = null;
        highlightedStyle = null;
    }
    
    public Style getStyle() {
        return style;
    }

    public Style getHighlightedStyle() {
        return highlightedStyle;
    }
    
    public void setStyle(Style style) {
        this.style = style;
    }

    public void setHighlightedStyle(Style style) {
        this.highlightedStyle = style;
    }
    
    public void setStyleRecursive(Style style) {
        this.style = style;
    }

    public void setHighlightedStyleRecursive(Style style) {
        this.highlightedStyle = style;
    }
    
    public Set<Object> getSource() {
        return Collections.unmodifiableSet(source);
    }
    
    public void addSource(Object o) {
        source.add(o);
    }
    
    public abstract void accept(ElementVisitor visitor);
}
