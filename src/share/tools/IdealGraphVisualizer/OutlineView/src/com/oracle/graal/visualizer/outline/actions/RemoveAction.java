/*
 * Copyright (c) 2012, Oracle and/or its affiliates. All rights reserved.
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
package com.oracle.graal.visualizer.outline.actions;

import com.oracle.graal.visualizer.outline.OutlineTopComponent;
import com.sun.hotspot.igv.data.FolderElement;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;

@ActionID(id = "com.oracle.graal.visualizer.outline.actions.RemoveAction", category = "Edit")
@ActionRegistration(displayName = "Remove", iconBase = "com/oracle/graal/visualizer/outline/images/remove.png")
@ActionReferences(value = {
    @ActionReference(path = "Menu/File", position = 400),
    @ActionReference(path = OutlineTopComponent.NODE_ACTIONS_FOLDER)})
public final class RemoveAction implements ActionListener {

    List<FolderElement> elements;

    public RemoveAction(List<FolderElement> elements) {
        this.elements = elements;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        for (FolderElement element : elements) {
            element.getParent().removeElement(element);
        }
    }
}
