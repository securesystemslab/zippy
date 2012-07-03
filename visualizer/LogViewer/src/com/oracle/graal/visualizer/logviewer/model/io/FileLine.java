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

package com.oracle.graal.visualizer.logviewer.model.io;

import java.io.IOException;
import java.lang.ref.WeakReference;

public class FileLine implements CharSequence {

	private final SeekableFile seekableFile;
	private WeakReference<String> cache;
	public final long off;
	public final int len;
	
	public FileLine(SeekableFile seekableFile, long off, int len) {
		this.seekableFile = seekableFile;
		this.off = off;
		this.len = len;
	}
	
	@Override
	public String toString() {
		String line = null;
		
		if(cache != null) line = cache.get();
		
		if(line == null) {
			try {
				line = seekableFile.read(this);
				cache = new WeakReference<>(line);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return line;
	}

	@Override
	public int length() {
		return len;
	}

	@Override
	public char charAt(int index) {
		String line = toString();
		return line != null ? line.charAt(index) : 0;
	}

	@Override
	public CharSequence subSequence(int start, int end) {
		String line = toString();
		return line != null ? line.subSequence(start, end) : "";
	}
	
}
