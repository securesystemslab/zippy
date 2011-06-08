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
package com.sun.hotspot.igv.texteditor;

import com.sun.hotspot.igv.structuredtext.MultiElement;
import com.sun.hotspot.igv.structuredtext.SimpleElement;
import javax.swing.event.DocumentEvent;
import javax.swing.text.BadLocationException;
import org.netbeans.api.editor.fold.Fold;
import org.netbeans.spi.editor.fold.FoldHierarchyTransaction;
import org.netbeans.spi.editor.fold.FoldManager;
import org.netbeans.spi.editor.fold.FoldOperation;
import com.sun.hotspot.igv.structuredtext.Element;
import com.sun.hotspot.igv.structuredtext.Range;
import com.sun.hotspot.igv.structuredtext.StructuredText;
import com.sun.hotspot.igv.structuredtext.services.ElementVisitor;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import javax.swing.text.Document;
import org.netbeans.api.editor.fold.FoldType;

/**
 *
 * @author Thomas Wuerthinger
 */
public class TextFoldManager implements FoldManager {

    private FoldOperation operation;
    private final FoldType defaultFoldType = new FoldType("default");
    private Set<Fold> currentFolds;

    public TextFoldManager() {
        currentFolds = new HashSet<Fold>();
    }

    public void init(FoldOperation operation) {
        this.operation = operation;
    }

    public void initFolds(final FoldHierarchyTransaction transaction) {
    }

    private void update(Document document, final FoldHierarchyTransaction transaction) {

        
        StructuredText text = (StructuredText) document.getProperty(StructuredText.class);
        if (text == null) {
            // No StructuredText object behind the document object.
            return;
        }
        
        if(document.getLength() == 0) {
            return;
        }
        
        final Map<Element, Range> ranges = text.calculateRanges();
        currentFolds.clear();

        text.accept(new ElementVisitor() {
            @Override
            public void visit(MultiElement element) {
                super.visit(element);
                Range curRange = ranges.get(element);

                if (element.getFoldString() != null) {
                    try {
                        Fold f = operation.addToHierarchy(defaultFoldType, element.getFoldString(), false,
                                curRange.getStart(), curRange.getStart() + curRange.getLength(), 0, 0,
                                null, transaction);
                        currentFolds.add(f);
                    } catch (BadLocationException ex) {
                        assert false : "Structured text not in sync with document content " + ex.getStackTrace();
                    }
                }
            }

            @Override
            public void visit(SimpleElement element) {
            }
        });
    }

    public void insertUpdate(DocumentEvent event, FoldHierarchyTransaction transaction) {
        update(event.getDocument(), transaction);
    }

    public void removeUpdate(DocumentEvent event, FoldHierarchyTransaction transaction) {
        update(event.getDocument(), transaction);
    }

    public void changedUpdate(DocumentEvent event, FoldHierarchyTransaction transaction) {
//        update(event.getDocument(), transaction);
    }

    public void removeEmptyNotify(Fold arg0) {
    }

    public void removeDamagedNotify(Fold arg0) {
    }

    public void expandNotify(Fold arg0) {
    }

    public void release() {
    }
}
