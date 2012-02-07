package at.ssw.visualizer.model.cfg;

import java.util.Collection;

/**
 *
 * @author Christian Wimmer
 */
public interface IRInstruction {
    public static String HIR_NAME = "tid";
    public static String HIR_TEXT = "instruction";
    public static String HIR_OPERAND = "result";

    public static String LIR_NUMBER = "nr";
    public static String LIR_TEXT = "instruction";

    public Collection<String> getNames();
    public String getValue(String name);
}
