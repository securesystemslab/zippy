package at.ssw.visualizer.modelimpl.cfg;

import at.ssw.visualizer.model.cfg.IRInstruction;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;

public class IRInstructionImpl implements IRInstruction {
    private LinkedHashMap<String, String> data;

    public IRInstructionImpl(LinkedHashMap<String, String> data) {
        this.data = data;
    }

    public IRInstructionImpl(String pinned, int bci, int useCount, String name, String text, String operand) {
        data = new LinkedHashMap<String, String>();
        data.put("p", pinned);
        data.put("bci", Integer.toString(bci));
        data.put("use", Integer.toString(useCount));
        data.put(HIR_NAME, name);
        if (operand != null) {
            data.put(HIR_OPERAND, operand);
        }
        data.put(HIR_TEXT, text);
    }

    public IRInstructionImpl(int number, String text) {
        data = new LinkedHashMap<String, String>();
        data.put(LIR_NUMBER, Integer.toString(number));
        data.put(LIR_TEXT, text);
    }

    public Collection<String> getNames() {
        return Collections.unmodifiableSet(data.keySet());
    }

    public String getValue(String name) {
        return data.get(name);
    }
}
