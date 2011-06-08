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

package com.sun.hotspot.igv.texteditor;

import com.sun.hotspot.igv.structuredtext.Element;
import com.sun.hotspot.igv.structuredtext.MultiElement;
import com.sun.hotspot.igv.structuredtext.SimpleElement;
import com.sun.hotspot.igv.structuredtext.StructuredText;
import com.sun.hotspot.igv.structuredtext.services.ElementVisitor;
import java.awt.Component;
import java.util.HashSet;
import java.util.Set;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Position;
import javax.swing.text.SimpleAttributeSet;
import org.netbeans.modules.editor.NbEditorDocument;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.openide.text.Annotation;
import org.openide.text.Line;
import org.openide.util.Exceptions;

/**
 *
 * @author Thomas Wuerthinger
 */
public class TextEditor {
    
    private JEditorPane editorPane;
    private Component component;
    private Set<Annotation> addedAnnotations;
    
    public TextEditor() {
        editorPane = new JEditorPane();
        component = createEditor(editorPane);
        addedAnnotations = new HashSet<Annotation>();
        
    }
    
    public Component getComponent() {
        return component;
    }

    public void setStructuredText(StructuredText text) {
        assert text != null;
        
        setStructuredText(text, null);
    }
    
    public void setStructuredText(final StructuredText text, final Element focusedElement) {
        
        assert text != null;

        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                Document doc = editorPane.getDocument();
                doc.putProperty(StructuredText.class, text);

                for (Annotation a : addedAnnotations) {
                    ((NbEditorDocument) editorPane.getDocument()).removeAnnotation(a);
                }

                try {
                    editorPane.getDocument().remove(0, editorPane.getDocument().getLength());
                    editorPane.getDocument().insertString(0, text.convertToString(), SimpleAttributeSet.EMPTY);

                    text.accept(new ElementVisitor() {

                        private int pos = 0;

                        private void checkForFocus(Element element) {
                            if (element == focusedElement) {
                                editorPane.setCaretPosition(pos);
                            }
                        }

                        @Override
                        public void visit(MultiElement element) {
                            super.visit(element);
                            checkForFocus(element);
                        }

                        @Override
                        public void visit(SimpleElement element) {
                            checkForFocus(element);
                            for (Object o : element.getSource()) {
                                if (o instanceof Annotation) {
                                    Annotation a = (Annotation) o;
                                    final Line line = NbEditorUtilities.getLine(editorPane.getDocument(), pos, false);

                                    ((NbEditorDocument) editorPane.getDocument()).addAnnotation(new PositionImpl(pos), element.getText().length(), a);
                                    addedAnnotations.add(a);
                                }
                            }
                            pos += element.getText().length();
                        }

                        class PositionImpl implements Position {

                            private int position;

                            public PositionImpl(int position) {
                                this.position = position;
                            }

                            public int getOffset() {
                                return position;
                            }
                        }
                    });

                } catch (BadLocationException ex) {
                    Exceptions.printStackTrace(ex);
                }
                
            }
        });
       
    }

    private Component createEditor(JEditorPane pane) {
        TextEditorKit kit = new TextEditorKit();
        pane.setEditable(false);
        NbEditorDocument doc = (NbEditorDocument) kit.createDefaultDocument();
        pane.setEditorKit(kit);
        pane.setDocument(doc);
        return doc.createEditor(pane);
    }
}
