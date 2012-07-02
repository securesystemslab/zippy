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

package com.oracle.graal.visualizer.logviewer.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Node {

	private final int number;
	private final String name;
	private final Scope scope;
	private List<LogLine> logs = new ArrayList<>();
	
	
	Node(int number, String name, Scope scope) {
		this.number = number;
		this.name = name;
		this.scope = scope;
	}
	
	/**
	 * Returns the node number.
	 * @return the node number
	 */
	public int getNumber() {
		return number;
	}
	
	/**
	 * Returns the node name.
	 * @return the node name
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Returns the parent scope.
	 * @return the parent scope
	 */
	public Scope getScope() {
		return scope;
	}
	
	void add(LogLine log) {
		logs.add(log);
	}
	
	/**
	 * Returns a list of all logs within a nodes context.
	 * @return list of log lines
	 */
	public List<LogLine> getLogs() {
		return Collections.unmodifiableList(logs);
	}
	
	/**
	 * The string represenation of a node is &quot;&lt;name&gt; (&lt;number&gt;)&quot;.
	 */
	@Override
	public String toString() {
		return getName() + "(" + getNumber() + ")";
	}
}
