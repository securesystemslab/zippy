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

public class Scope {

	private final String name;
	private final Method method;
	private List<Node> nodes = new ArrayList<>();
	private List<LogLine> logs = new ArrayList<>();
	
	Scope(String name, Method method) {
		this.name = name;
		this.method = method;
	}
	
	/**
	 * Returns the scope name.
	 * @return the scope name
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Returns the parent method.
	 * @return the parent method
	 */
	public Method getMethod() {
		return method;
	}
	
	void add(Node node) {
		nodes.add(node);
	}
	
	/**
	 * Returns a list of all node-objects inside a scope.
	 * @return list of nodes
	 */
	public List<Node> getNodes() {
		return Collections.unmodifiableList(nodes);
	}
	
	void add(LogLine log) {
		logs.add(log);
	}
	
	/**
	 * Returns a list of all logs inside a scope.
	 * @return list of log lines
	 */
	public List<LogLine> getLogs() {
		return logs;
	}
	
	/**
	 * Returns the node inside a scope with the given number.
	 * @param number node number
	 * @return node-object
	 */
	public Node getNode(int number) {
		for(Node node : nodes) {
			if(node.getNumber() == number) return node;
		}
		return null;
	}
	
	/**
	 * The string representation of a scope is its name.
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return getName();
	}

}
