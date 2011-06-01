package at.ssw.visualizer.core.selection;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 *
 * @author Christian Wimmer
 */
public class Selection {
    private Map<Class, Object> elements;
    private List<ChangeListener> listeners;
    private Timer eventTimer;

    private ActionListener eventTimerListener = new ActionListener() {
        public void actionPerformed(ActionEvent event) {
            doFireChangeEvent();
        }
    };

    public Selection() {
        elements = new HashMap<Class, Object>();
        listeners = new ArrayList<ChangeListener>();
        eventTimer = new Timer(100, eventTimerListener);
        eventTimer.setRepeats(false);
    }

    private void doPut(Class<?> clazz, Object element) {
        elements.put(clazz, element);
        for (Class<?> i : clazz.getInterfaces()) {
            doPut(i, element);
        }
    }
    
    public void put(Object element) {
        doPut(element.getClass(), element);
        fireChangeEvent();
        SelectionManager.getDefault().fireChangeEvent();
    }

    @SuppressWarnings(value = "unchecked")
    public <T> T get(Class<T> clazz) {
        return (T) elements.get(clazz);
    }


    protected void doFireChangeEvent() {
        ChangeEvent event = new ChangeEvent(this);
        for (ChangeListener listener : listeners.toArray(new ChangeListener[listeners.size()])) {
            listener.stateChanged(event);
        }
    }

    protected void fireChangeEvent() {
        eventTimer.restart();
    }

    public void addChangeListener(ChangeListener listener) {
        listeners.add(listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        listeners.remove(listener);
    }
}
