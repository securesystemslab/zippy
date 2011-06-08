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
import java.util.HashMap;
import java.util.Map;


/**
 *
 * @author Thomas
 */
public class StructuredText extends MultiElement {
    
    private String name;
    
    public StructuredText(String name) {
        this.name = name;
    }
    
    public String getName() {
        return name;
    }
    
    public Element findElementAt(final int searchIndex) {
        
        final Element[] result = new Element[1];
        this.accept(new ElementVisitor() {
            
            private int index;
            
            @Override
            public void visit(MultiElement element) {
                int startIndex = index;
                super.visit(element);
            }

            @Override
            public void visit(SimpleElement element) {
                if(searchIndex >= index && searchIndex < index + element.getText().length()) {
                    assert result[0] == null;
                    result[0] = element;
                }
                index += element.getText().length();
            }
        });
        
        return result[0];
    }
    
    public Map<Element, Range> calculateRanges() {
        
        final Map<Element, Range> result = new HashMap<Element, Range>();
        
        this.accept(new ElementVisitor() {
            
            private int index;
            
            @Override
            public void visit(MultiElement element) {
                int startIndex = index;
                super.visit(element);
                result.put(element, new Range(startIndex, index - startIndex));
            }

            @Override
            public void visit(SimpleElement element) {
                result.put(element, new Range(index, element.getText().length()));
                index += element.getText().length();
            }
        });
        
        
        return result;
        
    }
    
    public String convertToString() {
        final StringBuilder result = new StringBuilder();
        this.accept(new ElementVisitor() {
            @Override
            public void visit(SimpleElement element) {
                result.append(element.getText());
            }
        }
        );
        return result.toString();
    }
}
