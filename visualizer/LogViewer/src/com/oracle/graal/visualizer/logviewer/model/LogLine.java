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

public class LogLine {

    private final int lineNum;
	private final CharSequence text;
	private final Method method;
	private final Scope scope;
	private final Node node;
	
	LogLine(int lineNum, CharSequence text, Method method, Scope scope, Node node) {
		this.lineNum = lineNum;
        this.text = text;
		this.method = method;
		this.scope = scope;
		this.node = node;
	}
    
    /**
     * Returns the number of the line in the log file
     * @return file line number
     */
    public int getLineNumber() {
        return lineNum;
    }
	
	/**
	 * Returns the text of the log line
	 * @return log line text
	 */
	public String getText() {
		return text.toString();
	}
	
	/**
	 * Returns the parent method
	 * @return the parent method
	 */
	public Method getMethod() {
		return method;
	}
	
	/**
	 * Returns the parent scope
	 * @return the parent scope
	 */
	public Scope getScope() {
		return scope;
	}
	
	/**
	 * Returns the parent node
	 * @return the parent node (or <code>null</code> if the log is not in a node's context)
	 */
	public Node getNode() {
		return node;
	}
	
	/**
	 * The string representation of a log line is the text of it.
	 */
	@Override
	public String toString() {
		return getText();
	}
	
}
