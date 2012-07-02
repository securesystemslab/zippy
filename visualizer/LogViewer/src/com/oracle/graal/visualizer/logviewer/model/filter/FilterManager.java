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
import com.oracle.graal.visualizer.logviewer.model.LogModel;
import java.util.ArrayList;
import java.util.List;

public class FilterManager {
	
	private final List<Filter> filters = new ArrayList<>();
	private final Filter methodFilter, scopeFilter, nodeFilter, fulltextFilter;
	
	/**
	 * Creates a new instance of the filter manager.
	 */
	public FilterManager() {
		add(methodFilter = new MethodFilter());
		add(scopeFilter = new ScopeFilter());
		add(nodeFilter = new NodeFilter());
		add(fulltextFilter = new FullTextFilter());
	}
	
	/**
	 * Executes a filtering process with the given log model as subject. Inactive filters will be ignored.
	 * @param model subject
	 * @return filtered list of log lines
	 * @throws InterruptedException 
	 */
	public List<LogLine> execute(LogModel model) throws InterruptedException {
		List<LogLine> input = model.getLogs();
		List<LogLine> output = new ArrayList<>();
		
		for(LogLine line : input) {
			if(Thread.interrupted()) throw new InterruptedException();
			if(keep(line)) output.add(line);
		}
		
		if(Thread.interrupted()) throw new InterruptedException();
		
		return output;
	}
	
	private boolean keep(LogLine line) {
		boolean keep = true;
		for(Filter filter : filters) {
			keep = keep && (!filter.isActive() || filter.keep(line));
		}
		return keep;
	}
	
	private Filter add(Filter filter) {
		filters.add(filter);
		return filter;
	}
	
	/**
	 * Returns the instance of a method filter
	 * @return method filter
	 */
	public Filter getMethodFilter() {
		return methodFilter;
	}
	
	/**
	 * Returns the instance of a scope filter
	 * @return scope filter
	 */
	public Filter getScopeFilter() {
		return scopeFilter;
	}
	
	/**
	 * Returns the instance of a node filter
	 * @return node filter
	 */
	public Filter getNodeFilter() {
		return nodeFilter;
	}
	
	/**
	 * Returns the instance of a full text filter
	 * @return full text filter
	 */
	public Filter getFullTextFilter() {
		return fulltextFilter;
	}
	
}
