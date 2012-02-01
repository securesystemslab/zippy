/*
 * Copyright (c) 1998, 2007, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
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
 */
package com.sun.hotspot.igv.util;

import com.sun.hotspot.igv.data.ChangedListener;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.List;
import javax.swing.JComponent;

/**
 *
 * @author Thomas Wuerthinger
 */
public class RangeSlider extends JComponent implements ChangedListener<RangeSliderModel>, MouseListener, MouseMotionListener {

    public static final int BAR_THICKNESS = 2;
    public static final int BAR_CIRCLE_SIZE = 9;
    public static final int MOUSE_ENDING_OFFSET = 3;
    public static final Color BACKGROUND_COLOR = Color.white;
    public static final Color BAR_COLOR = Color.black;
    public static final Color BAR_SELECTION_COLOR = new Color(255, 0, 0, 120);
    public static final Color TEXT_SELECTION_COLOR = new Color(200, 200, 200, 255);
    public static final int ITEM_HEIGHT = 30;
    public static final int ITEM_WIDTH = 30;
    private RangeSliderModel model;
    private Point startPoint;
    private RangeSliderModel tempModel;
    private Point lastMouseMove;

    public RangeSlider() {
        this.addMouseMotionListener(this);
        this.addMouseListener(this);
    }

    public void setModel(RangeSliderModel newModel) {
        if (model != null) {
            model.getChangedEvent().removeListener(this);
            model.getColorChangedEvent().removeListener(this);
        }
        if (newModel != null) {
            newModel.getChangedEvent().addListener(this);
            newModel.getColorChangedEvent().addListener(this);
        }
        this.model = newModel;
        update();
    }

    private RangeSliderModel getPaintingModel() {
        if (tempModel != null) {
            return tempModel;
        }
        return model;
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        Graphics g = this.getGraphics();

        int maxWidth = 0;
        List<String> list = getPaintingModel().getPositions();
        for (int i = 0; i < list.size(); i++) {

            String curS = list.get(i);
            if (curS != null && curS.length() > 0) {
                FontMetrics metrics = g.getFontMetrics();
                Rectangle bounds = metrics.getStringBounds(curS, g).getBounds();
                maxWidth = Math.max(maxWidth, (int) bounds.getWidth());
            }
        }
        d.width = maxWidth + ITEM_WIDTH;
        d.height = ITEM_HEIGHT * list.size();
        return d;
    }

    @Override
    public void changed(RangeSliderModel source) {
        update();
    }

    private void update() {
        this.repaint();
    }

    private Rectangle getItemBounds(int index) {
        Rectangle r = new Rectangle();
        r.width = ITEM_WIDTH;
        r.height = ITEM_HEIGHT;
        r.x = 0;
        r.y = ITEM_HEIGHT * index;
        return r;
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        int width = getWidth();
        int height = getHeight();

        g2.setColor(BACKGROUND_COLOR);
        g2.fillRect(0, 0, width, height);

        // Nothing to paint?
        if (getPaintingModel() == null || getPaintingModel().getPositions().isEmpty()) {
            return;
        }

        paintSelected(g2);
        paintBar(g2);

    }

    private void fillRect(Graphics2D g, int startX, int startY, int endY, int thickness) {
        g.fillRect(startX - thickness / 2, startY, thickness, endY - startY);
    }

    private void paintBar(Graphics2D g) {
        List<String> list = getPaintingModel().getPositions();

        g.setColor(BAR_COLOR);
        Rectangle firstItemBounds = getItemBounds(0);
        Rectangle lastItemBounds = getItemBounds(list.size() - 1);
        fillRect(g, (int) firstItemBounds.getCenterX(), (int) firstItemBounds.getCenterY(), (int) lastItemBounds.getCenterY(), BAR_THICKNESS);

        for (int i = 0; i < list.size(); i++) {
            Rectangle curItemBounds = getItemBounds(i);
            g.setColor(getPaintingModel().getColors().get(i));
            g.fillOval((int) curItemBounds.getCenterX() - BAR_CIRCLE_SIZE / 2, (int) curItemBounds.getCenterY() - BAR_CIRCLE_SIZE / 2, BAR_CIRCLE_SIZE, BAR_CIRCLE_SIZE);
            g.setColor(Color.black);
            g.drawOval((int) curItemBounds.getCenterX() - BAR_CIRCLE_SIZE / 2, (int) curItemBounds.getCenterY() - BAR_CIRCLE_SIZE / 2, BAR_CIRCLE_SIZE, BAR_CIRCLE_SIZE);

            String curS = list.get(i);
            if (curS != null && curS.length() > 0) {
                FontMetrics metrics = g.getFontMetrics();
                Rectangle bounds = metrics.getStringBounds(curS, g).getBounds();
                g.setColor(Color.black);
                g.drawString(curS, curItemBounds.x + curItemBounds.width, (int) curItemBounds.getCenterY() + bounds.height / 2 - 2);
            }
        }

    }

    private void paintSelected(Graphics2D g) {
        List<String> list = getPaintingModel().getPositions();
        for (int i = 0; i < list.size(); i++) {
            Rectangle curItemBounds = getItemBounds(i);
            if (lastMouseMove != null && curItemBounds.y <= lastMouseMove.y && curItemBounds.y + curItemBounds.height > lastMouseMove.y) {
                g.setColor(TEXT_SELECTION_COLOR);
                g.fillRect(0, curItemBounds.y, getWidth(), curItemBounds.height);
            }
        }
        final Rectangle barBounds = getBarBounds();

        g.setColor(BAR_SELECTION_COLOR);
        g.fill(barBounds);
    }

    private Rectangle getBarBounds() {
        final Rectangle startItemBounds = getItemBounds(getPaintingModel().getFirstPosition());
        final Rectangle endItemBounds = getItemBounds(getPaintingModel().getSecondPosition());
        int startY = startItemBounds.y;
        int endY = endItemBounds.y + endItemBounds.height;
        return new Rectangle(0, startY, getWidth(), endY - startY);
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (startPoint != null) {
            int startIndex = getIndexFromPosition(startPoint.y);
            int curIndex = getIndexFromPosition(e.getPoint().y);
            tempModel.setPositions(startIndex, curIndex);
        }
    }

    private int getIndexFromPosition(int y) {
        for (int i = 0; i < getPaintingModel().getPositions().size() - 1; i++) {
            Rectangle bounds = getItemBounds(i);
            if (bounds.y <= y && bounds.y + bounds.height >= y) {
                return i;
            }
        }
        return getPaintingModel().getPositions().size() - 1;
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        lastMouseMove = e.getPoint();
        update();
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        int index = getIndexFromPosition(e.getPoint().y);
        model.setPositions(index, index);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        int index = getIndexFromPosition(e.getPoint().y);
        startPoint = e.getPoint();
        tempModel = model.copy();
        tempModel.getChangedEvent().addListener(this);
        tempModel.setPositions(index, index);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        model.setPositions(tempModel.getFirstPosition(), tempModel.getSecondPosition());
        tempModel = null;
        startPoint = null;
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
        lastMouseMove = null;
        repaint();
    }
}
