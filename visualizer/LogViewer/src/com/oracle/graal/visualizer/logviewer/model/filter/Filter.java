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

package com.oracle.graal.visualizer.logviewer.model.filter;

import com.oracle.graal.visualizer.logviewer.model.LogLine;

public interface Filter {

	/**
	 * Sets the constraints for a filter.<br>
	 * They can be every type of objects. In general these are strings, but it can also be
	 * a Integer (like in the <code>NodeFilter</code>):<br>
	 * <code>Filter nodeFilter = filterManager.getNodeFilter();
	 * filter.setConstraints((String)nodeName, (Integer)nodeNumber);</code>
	 * @param constraints filter constraints
	 */
	void setConstraints(Object ... constraints);
	
	/**
	 * Indicates whether to keep a log line in the filter results, or not.
	 * @param line log line which should be checked
	 * @return <code>true</code> if the line should be kept, <code>false</code> otherwise
	 */
	boolean keep(LogLine line);
	
	/**
	 * Activates or deactivates a filter. Deactivated filters will be ignored.
	 * @param active <code>true</code> to activate, <code>false</code> to deactivate
	 */
	void setActive(boolean active);
	
	/**
	 * Returns the activation status
	 * @return <code>true</code> for an active filter, <code>false</code> otherwise
	 */
	boolean isActive();
	
}
