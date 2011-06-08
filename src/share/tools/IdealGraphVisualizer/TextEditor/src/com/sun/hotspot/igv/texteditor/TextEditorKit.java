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

import java.util.ArrayList;
import java.util.List;
import javax.swing.Action;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.editor.NbEditorKit;

/**
 *
 * @author Thomas Wuerthinger
 */
public class TextEditorKit extends NbEditorKit {

    public static final String MIME_TYPE = "text/text-igv";
    
    /**
     * Actions to be not available in the editor.
     */
    private static final String[] ACTION_FILTER = {
        NbEditorKit.shiftLineLeftAction,
        NbEditorKit.shiftLineRightAction,
        NbEditorKit.jumpListNextAction,
        NbEditorKit.jumpListNextComponentAction,
        NbEditorKit.jumpListPrevAction,
        NbEditorKit.jumpListPrevComponentAction,
        NbEditorKit.pasteAction,
        NbEditorKit.removeLineAction,
        NbEditorKit.cutAction,
        NbEditorKit.findAction,
        NbEditorKit.findNextAction,
        NbEditorKit.findPreviousAction,
        NbEditorKit.toggleHighlightSearchAction,
        NbEditorKit.findSelectionAction,
        "jump-list-last-edit"
    };
    
    @Override
    protected Action[] createActions() {
        Action[] actions = super.createActions();
        List<Action> returnedActions = new ArrayList<Action>();
        for(Action a : actions) {
            System.out.println("action: " + a.getValue(Action.NAME));
            
            boolean found = false;
            for(String s : ACTION_FILTER) {
                if(s.equals(a.getValue(Action.NAME))) {
                    found = true;
                }
            }
            if(!found) {
                returnedActions.add(a);
            }
        }
        
        Action[] result = new Action[returnedActions.size()];
        for(int i=0; i<returnedActions.size(); i++) {
            result[i] = returnedActions.get(i);
        }
        
        return result;
    }

    @Override
    protected void initDocument(BaseDocument doc) {
        super.initDocument(doc);
        System.out.println("Initializing document: " + doc);
        
    }

    @Override
    public String getContentType() {
        return MIME_TYPE;
    }
    
    
}
