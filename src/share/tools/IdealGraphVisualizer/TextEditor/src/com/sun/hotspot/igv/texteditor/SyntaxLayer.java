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

import com.sun.hotspot.igv.data.ChangedListener;
import com.sun.hotspot.igv.selectioncoordinator.SelectionCoordinator;
import com.sun.hotspot.igv.structuredtext.MultiElement;
import com.sun.hotspot.igv.structuredtext.SimpleElement;
import com.sun.hotspot.igv.structuredtext.StructuredText;
import com.sun.hotspot.igv.structuredtext.Element;
import com.sun.hotspot.igv.structuredtext.Range;
import com.sun.hotspot.igv.structuredtext.ToolTipProvider;
import com.sun.hotspot.igv.structuredtext.services.ElementVisitor;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import org.netbeans.spi.editor.highlighting.HighlightsLayerFactory;
import org.netbeans.spi.editor.highlighting.HighlightsSequence;
import org.netbeans.spi.editor.highlighting.support.AbstractHighlightsContainer;

/**
 *
 * @author Thomas
 */
public class SyntaxLayer extends AbstractHighlightsContainer implements ChangedListener<SelectionCoordinator> {

    private HighlightsLayerFactory.Context context;

    public SyntaxLayer(final HighlightsLayerFactory.Context context) {
        this.context = context;

        context.getDocument().addDocumentListener(new DocumentListener() {

            public void insertUpdate(DocumentEvent arg0) {
                update();
            }

            public void removeUpdate(DocumentEvent arg0) {
            }

            public void changedUpdate(DocumentEvent arg0) {
            }
        });

        SelectionCoordinator.getInstance().getSelectedChangedEvent().addListener(this);
        SelectionCoordinator.getInstance().getHighlightedChangedEvent().addListener(this);

        context.getComponent().addMouseMotionListener(new MouseMotionListener() {

            public void mouseDragged(MouseEvent e) {
            }

            public void mouseMoved(MouseEvent e) {
                // [tw] hack to prevent sidebar mouse over
                if (e.getPoint().getX() < 15) return;

                int index = context.getComponent().viewToModel(e.getPoint());
                Element elem = indexToElement(index);
                if (elem != null) {
                    Set<Object> highlightedSource = new HashSet<Object>(elem.getSource());
                    SelectionCoordinator.getInstance().setHighlightedObjects(highlightedSource);
                    context.getComponent().setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                }
            }
        });

        context.getComponent().addMouseListener(new MouseListener() {

            public void mouseClicked(MouseEvent e) {
                
                int index = context.getComponent().viewToModel(e.getPoint());
                Element elem = indexToElement(index);
                if (elem != null) {
                    Set<Object> selectedSource = new HashSet<Object>(elem.getSource());

                    for (Object o : selectedSource) {
                        if (o instanceof ToolTipProvider) {
                            String toolTip = ((ToolTipProvider) o).getToolTip();
                        }
                    }

                    if ((e.getModifiersEx() & MouseEvent.CTRL_DOWN_MASK) != 0) {

                        SelectionCoordinator.getInstance().addAllSelected(selectedSource);
                    } else {
                        SelectionCoordinator.getInstance().setSelectedObjects(selectedSource);
                    }
                    context.getComponent().setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                } else {
                    context.getComponent().setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));

                }
            }

            public void mousePressed(MouseEvent e) {
            }

            public void mouseReleased(MouseEvent e) {
            }

            public void mouseEntered(MouseEvent e) {
            }

            public void mouseExited(MouseEvent e) {
                SelectionCoordinator.getInstance().setHighlightedObjects(new HashSet<Object>());
            }
        });
    }

    public void changed(SelectionCoordinator source) {
        update();
    }

    private void update() {
        fireHighlightsChange(0, context.getDocument().getLength());
    }

    private Element indexToElement(int index) {
        StructuredText text = (StructuredText) context.getDocument().getProperty(StructuredText.class);
        if (text == null) {
            return null;
        }
        return text.findElementAt(index);
    }

    private static class HighlightsRange {

        private int start;
        private int end;
        private AttributeSet attributes;

        public HighlightsRange(int start, int length, AttributeSet attributes) {
            this.start = start;
            this.end = start + length;
            this.attributes = attributes;
        }

        public int getStart() {
            return start;
        }

        public int getEnd() {
            return end;
        }

        public AttributeSet getAttributes() {
            return attributes;
        }
    }

    private static class HighlightsSequenceImpl implements HighlightsSequence {

        private List<HighlightsRange> ranges;
        private int currentIndex;

        public HighlightsSequenceImpl() {
            this(new ArrayList<HighlightsRange>());
        }

        public HighlightsSequenceImpl(List<HighlightsRange> ranges) {
            this.ranges = ranges;
            this.currentIndex = -1;
        }

        public boolean moveNext() {
            currentIndex++;
            return currentIndex < ranges.size();
        }

        public int getStartOffset() {
            return ranges.get(currentIndex).getStart();
        }

        public int getEndOffset() {
            return ranges.get(currentIndex).getEnd();
        }

        public AttributeSet getAttributes() {
            return ranges.get(currentIndex).getAttributes();
        }
    }

    private boolean intersects(Set<Object> s1, Set<Object> s2) {
        for (Object o : s1) {
            if (s2.contains(o)) {
                return true;
            }
        }
        return false;
    }

    public HighlightsSequence getHighlights(final int start, final int end) {

        StructuredText text = (StructuredText) context.getDocument().getProperty(StructuredText.class);
        if (text == null) {
            return new HighlightsSequenceImpl();
        }
        final Map<Element, Range> ranges = text.calculateRanges();
        final List<HighlightsRange> highlightsRanges = new ArrayList<HighlightsRange>();
        final Range baseRange = new Range(start, end - start);

        text.accept(new ElementVisitor() {
            
            private Stack<Style> styleStack = new Stack<Style>();
            private Stack<Style> highlightedStyleStack = new Stack<Style>();
            private Stack<MultiElement> parentElements = new Stack<MultiElement>();
            
            @Override
            public void visit(MultiElement element) {
                Style curStyle = element.getStyle();
                Style curHighlightedStyle = element.getHighlightedStyle();
                if(curStyle != null) {
                    styleStack.push(curStyle);
                }
                if (curHighlightedStyle != null) {
                    highlightedStyleStack.push(curHighlightedStyle);
                }
                parentElements.push(element);
                super.visit(element);
                if(curStyle != null) {
                    styleStack.pop();
                }
                if (curHighlightedStyle != null) {
                    highlightedStyleStack.pop();
                }
                parentElements.pop();
            }
            

            @Override
            public void visit(SimpleElement element) {
                Range curRange = ranges.get(element);
                if (baseRange.overlaps(curRange)) {
                    Style style = element.getStyle();
                    if(style == null) {
                        if(styleStack.size() > 0) {
                            style = styleStack.peek();
                        } else {
                            style = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);
                            StyleConstants.setBackground(style, Color.WHITE);
                        }
                    }
                    
                    
                    Style highlightedStyle = element.getHighlightedStyle();
                    if (highlightedStyle == null) {
                        if (highlightedStyleStack.size() > 0) {
                            highlightedStyle = highlightedStyleStack.peek();
                        }
                    }

                    Set<Object> highlightedSource = SelectionCoordinator.getInstance().getHighlightedObjects();
                    if (highlightedSource != null) {
                        
                        boolean doesIntersect = intersects(element.getSource(), highlightedSource);
                        for (MultiElement parentElement : parentElements) {
                            if (doesIntersect) {
                                break;
                            }

                            doesIntersect = intersects(parentElement.getSource(), highlightedSource);
                        }

                        if (doesIntersect) {

                            if (highlightedStyle != null) {
                                style = highlightedStyle;
                            } else {
                                style = StyleContext.getDefaultStyleContext().addStyle(null, style);
                                Color bg = StyleConstants.getBackground(style);
                                Color fg = StyleConstants.getForeground(style);
                                StyleConstants.setBackground(style, new Color(255 - bg.getRed(), 255 - bg.getGreen(), 255 - bg.getBlue()));
                                StyleConstants.setForeground(style, new Color(255 - fg.getRed(), 255 - fg.getGreen(), 255 - fg.getBlue()));
                            }
                        }
                    }

                    Set<Object> selectedSource = SelectionCoordinator.getInstance().getSelectedObjects();
                    if (selectedSource != null && intersects(element.getSource(), selectedSource)) {
                        style = StyleContext.getDefaultStyleContext().addStyle(null, style);
                        StyleConstants.setBold(style, true);
                    }

                    highlightsRanges.add(new HighlightsRange(curRange.getStart(), curRange.getLength(), style));
                }
            }
        });

        return new HighlightsSequenceImpl(highlightsRanges);
    }
}
