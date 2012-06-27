package at.ssw.visualizer.logviewer.model.io;

/**
 *
 * @author Alexander Stipsits
 */
public interface ProgressMonitor {

	/**
	 * Triggered when some work has been done.<br><br>
	 * <b>Attention</b>: This method runs on the same thread as the reading process.
	 * If time-consuming tasks should be executed, please consider multithreading.
	 * @param percentage Value in range between 0 and 1 to indicate how much work has been done.
	 */
	public void worked(float percentage);
	
	/**
	 * Triggered when work is done completely.<br><br>
	 * <b>Attention</b>: This method runs on the same thread as the reading process.
	 * If time-consuming tasks should be executed, please consider multithreading.
	 */
	public void finished();
	
}
