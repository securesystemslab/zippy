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
package com.sun.hotspot.igv.graal.filters;

import com.sun.hotspot.igv.data.Properties;
import com.sun.hotspot.igv.filter.AbstractFilter;
import com.sun.hotspot.igv.graph.Connection;
import com.sun.hotspot.igv.graph.Diagram;
import com.sun.hotspot.igv.graph.Figure;
import com.sun.hotspot.igv.graph.InputSlot;
import java.awt.Color;
import java.util.List;

/**
 * Filter that colors usage and successor edges differently.
 * 
 * @author Peter Hofer
 */
public class GraalEdgeColorFilter extends AbstractFilter {

    private Color successorColor = Color.BLUE;
    private Color usageColor = Color.RED;
    private Color memoryColor = Color.GREEN;

    public GraalEdgeColorFilter() {
    }

    public String getName() {
        return "Graal Edge Color Filter";
    }

    public void apply(Diagram d) {
        List<Figure> figures = d.getFigures();
        for (Figure f : figures) {
            Properties p = f.getProperties();
            int predCount;
            if (p.get("predecessorCount") != null) {
                predCount = Integer.parseInt(p.get("predecessorCount"));
            } else {
                predCount = 0;
            }
            for (InputSlot is : f.getInputSlots()) {
                Color color;
                if (is.getPosition() < predCount) {
                    color = successorColor;
                } else {
                    color = usageColor;
                }

                is.setColor(color);
                for (Connection c : is.getConnections()) {
                    if (c.getLabel() == null || !c.getLabel().endsWith("#NDF")) {
                        c.setColor(color);
                    } else if ("EndNode".equals(c.getOutputSlot().getFigure().getProperties().get("class"))
                            || "EndNode".equals(c.getOutputSlot().getProperties().get("class"))) {
                        c.setColor(successorColor);
                    }
                }
            }
        }
    }

    public Color getUsageColor() {
        return usageColor;
    }

    public void setUsageColor(Color usageColor) {
        this.usageColor = usageColor;
    }

    public void setMemoryColor(Color memoryColor) {
        this.memoryColor = memoryColor;
    }

    public Color getMemoryColor() {
        return memoryColor;
    }

    public Color getSuccessorColor() {
        return successorColor;
    }

    public void setSuccessorColor(Color successorColor) {
        this.successorColor = successorColor;
    }
}
