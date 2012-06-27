package at.ssw.visualizer.logviewer.model.io;

import java.io.IOException;
import java.lang.ref.WeakReference;

/**
 *
 * @author Alexander Stipsits
 */
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
