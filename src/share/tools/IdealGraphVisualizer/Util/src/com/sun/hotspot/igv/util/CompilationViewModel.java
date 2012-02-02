/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.hotspot.igv.util;

import com.sun.hotspot.igv.data.*;
import java.awt.Color;
import java.util.List;

public class CompilationViewModel implements ChangedEventProvider<CompilationViewModel> {
    
    private final ChangedEvent<CompilationViewModel> changedEvent = new ChangedEvent<>(this);
    private final RangeSliderModel model;
    private final Group group;

    @Override
    public ChangedEvent<CompilationViewModel> getChangedEvent() {
        return changedEvent;
    }
    
    public CompilationViewModel(RangeSliderModel model, Group group) {
        this.model = model;
        this.group = group;
        model.getChangedEvent().addListener(rangeSliderChangedListener);
    }
    
    private final ChangedListener<RangeSliderModel> rangeSliderChangedListener = new ChangedListener<RangeSliderModel>() {
        @Override
        public void changed(RangeSliderModel source) {
            changedEvent.fire();
        }
    };
    
    public InputGraph getFirstSnapshot() {
        return group.getGraphs().get(model.getFirstPosition());
    }
    
    public InputGraph getSecondSnapshot() {
        return group.getGraphs().get(model.getSecondPosition());
    }

    public void setColors(List<Color> colors) {
        model.setColors(colors);
    }
}
