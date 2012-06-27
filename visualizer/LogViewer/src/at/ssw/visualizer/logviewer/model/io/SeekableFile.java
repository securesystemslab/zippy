package at.ssw.visualizer.logviewer.model.io;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Alexander Stipsits
 */
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
