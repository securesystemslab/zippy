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

package com.oracle.graal.visualizer.util;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Action;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.Lookup.Provider;
import org.openide.util.lookup.Lookups;
import org.openide.windows.TopComponent;

/**
 * Utilities that build upon the Lookup API.
 */
public class LookupUtils {

    
    /**
     * Creates a new lookup that will delegate to the last open window of a specified top component class. If the window is closed, the lookup will be empty.
     * @param clazz the class identifying the top component type
     * @return a delegating lookup
     */
    public static Lookup getLastActiveDelegatingLookup(Class<?> clazz) {
        final TopComponentLookup topComponentLookupImpl = new TopComponentLookup(clazz);
        TopComponent.getRegistry().addPropertyChangeListener(topComponentLookupImpl);
        return topComponentLookupImpl.lookup;
    }
    
    public static Iterable<Action> lookupActions(String path) {
        return lookupActions(path, null);
    }

    public static Iterable<Action> lookupActions(String path, Lookup context) {
        List<Action> actions = new ArrayList<>();
        for (Action a : Lookups.forPath(path).lookupAll(Action.class)) {
            Action newAction = a;
            if (a instanceof ContextAwareAction && context != null) {
                newAction = ((ContextAwareAction) a).createContextAwareInstance(context);
            }
            newAction.putValue(Action.SHORT_DESCRIPTION, newAction.getValue(Action.NAME));
            actions.add(newAction);
            
        }
        return actions;
    }
    
    private static class TopComponentLookup implements PropertyChangeListener {
        private final Class<?> clazz;
        private final Lookup lookup;
        private TopComponent lastActive;
        
        private final Provider lookupProvider = new Provider() {

            @Override
            public Lookup getLookup() {
                if (lastActive == null) {
                    return Lookup.EMPTY;
                } else {
                    return lastActive.getLookup();
                }
            }
        };
        
        public TopComponentLookup(Class<?> clazz) {
            this.clazz = clazz;
            lookup = Lookups.proxy(lookupProvider);
            update();
        }
        
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            update();
        }

        private void update() {
            TopComponent curActivated = TopComponent.getRegistry().getActivated();
            if (curActivated != lastActive) {
                if (clazz.isAssignableFrom(curActivated.getClass())) {
                    // We have a new top component for our lookup.
                    lastActive = curActivated;
                    refreshLookup();
                } else {
                    // We have no new top component. Check if the old one is still opened.
                    if (lastActive != null && !TopComponent.getRegistry().getOpened().contains(lastActive)) {
                        // The top component was closed => Remove lookup.
                        lastActive = null;
                        refreshLookup();
                    }
                }
            }
        }

        private void refreshLookup() {
            lookup.lookup(Object.class);
        }
    };
}
