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

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class SeekableFile implements Closeable {

	private final RandomAccessFile raf;
	private List<FileLine> lines = new ArrayList<>();
	
	SeekableFile(File file) throws FileNotFoundException {
		raf = new RandomAccessFile(file, "r");
	}
	
	void addLine(long off, int len) {
		lines.add(new FileLine(this, off, len));
	}
	
	public int size() {
		return lines.size();
	}
	
	public FileLine get(int lineNum) throws IOException {
		FileLine line = lines.get(lineNum);
		return line;
	}
	
	String read(FileLine line) throws IOException {
		if(line == null) throw new IllegalArgumentException("line is null");
		raf.seek(line.off);

		byte [] buf = new byte[line.len];
		
		if(raf.read(buf) != line.len) throw new IOException("error while reading line");
		
		return new String(buf);
	}

	@Override
	public void close() throws IOException {
		raf.close();
	}
	
	
}
