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

public class Method {

	private String name;
	private boolean isStub;
	private final LogModel model;
	private List<Scope> scopes = new ArrayList<>();
	private List<LogLine> logs = new ArrayList<>();
	
	Method(LogModel model) {
		name = null;
		isStub = false;
		this.model = model;
	}
	
	void init(String name, boolean isStub) {
		this.name = name;
		this.isStub = isStub;
	}
	
	/**
	 * Returns the name of the method
	 * @return the method name
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Returns <code>true</code> if this is a stub method.
	 * @return <code>true</code> if is stub, <code>false</code> otherwise
	 */
	public boolean isStub() {
		return isStub;
	}
	
	/**
	 * Returns the model which holds this method.
	 * @return model-object
	 */
	public LogModel getModel() {
		return model;
	}
	
	void add(Scope scope) {
		scopes.add(scope);
	}
	
	/**
	 * Returns a list of all scopes inside a target method.
	 * @return list of scopes
	 */
	public List<Scope> getScopes() {
		return scopes;
	}
	
	void add(LogLine log) {
		logs.add(log);
	}
	
	/**
	 * Returns a list of all logs in the context of the method.
	 * @return list of log lines
	 */
	public List<LogLine> getLogs() {
		return Collections.unmodifiableList(logs);
	}
	
	/**
	 * The string representation of a method is its name.
	 */
	@Override
	public String toString() {
		return getName();
	}
	
}
