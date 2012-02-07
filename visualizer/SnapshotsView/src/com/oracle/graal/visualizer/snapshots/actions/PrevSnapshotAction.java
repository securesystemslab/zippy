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
package com.oracle.graal.visualizer.snapshots.actions;

import com.oracle.graal.visualizer.snapshots.SnapshotTopComponent;
import com.sun.hotspot.igv.data.ChangedListener;
import com.sun.hotspot.igv.util.RangeSliderModel;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;

@ActionID(id = "com.oracle.graal.visualizer.editor.actions.PrevSnapshotAction", category = "View")
@ActionRegistration(displayName = "Previous snapshot", iconBase = "com/oracle/graal/visualizer/snapshots/images/prev_snapshot.png")
@ActionReference(path = "Menu/View", position = 100)
public final class PrevSnapshotAction extends AbstractAction {

    private RangeSliderModel model;

    public PrevSnapshotAction() {
        SnapshotTopComponent.findInstance().getRangeSliderChangedEvent().addListenerAndFire(changeListener);
    }
    private final ChangedListener<RangeSliderModel> changeListener = new ChangedListener<RangeSliderModel>() {

        @Override
        public void changed(RangeSliderModel source) {
            model = source;
            setEnabled(model != null && model.getFirstPosition() != 0);
        }
    };

    @Override
    public void actionPerformed(ActionEvent e) {
        if (model != null) {
            int fp = model.getFirstPosition();
            int sp = model.getSecondPosition();
            if (fp != 0) {
                int nfp = fp - 1;
                int nsp = sp - 1;
                model.setPositions(nfp, nsp);
            }
        }
    }
}
