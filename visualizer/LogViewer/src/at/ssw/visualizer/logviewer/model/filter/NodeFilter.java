package at.ssw.visualizer.logviewer.model.filter;

import at.ssw.visualizer.logviewer.model.LogLine;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Alexander Stipsits
 */
class NodeFilter implements Filter {

	private Pattern p;
	private Integer nodeNum;
	private boolean active = true;
	
	@Override
	public void setConstraints(Object ... constraints) {
        this.nodeNum = null;
        this.p = null;
		for(Object constraint : constraints) {
			setConstraint(constraint);
		}
	}
	
	private void setConstraint(Object constraint) {
		if(constraint instanceof String) {
		
			if(((String)constraint).trim().length() > 0) {
				this.p = Pattern.compile((String)constraint);
			}
			else {
				this.p = null;
			}
		}
		else if(constraint instanceof Integer) {
			this.nodeNum = (Integer)constraint;
		}
		
	}

	@Override
	public boolean keep(LogLine line) {
		if(p == null && nodeNum == null) return true; 
		
		if(line.getNode() == null) return false;
		
		boolean keep = false;
		
		if(p != null) {
			Matcher matcher = p.matcher(line.getNode().getName());
			keep = keep || matcher.find();
		}
		if(nodeNum != null) {
			keep = keep || nodeNum.equals(line.getNode().getNumber());
		}
		
		return keep;		
	}

	@Override
	public void setActive(boolean active) {
		this.active = active;
	}

	@Override
	public boolean isActive() {
		return active;
	}

}
