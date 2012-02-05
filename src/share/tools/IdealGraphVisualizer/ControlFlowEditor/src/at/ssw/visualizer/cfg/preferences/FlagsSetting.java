package at.ssw.visualizer.cfg.preferences;

import java.awt.Color;
import java.io.Serializable;
import java.util.Hashtable;
import java.util.List;

/**
 *
 * @author Thomas Wuerthinger
 */
public class FlagsSetting implements Serializable {

    private Hashtable<String, Color> flag2color;
    private Hashtable<String, Integer> priority;
    private String flagString;


    public FlagsSetting(String flagString) {
        this.flagString = flagString;
        flag2color = new Hashtable<>();
        priority = new Hashtable<>();
        String[] flags = flagString.split(";");

        int z = 0;
        for(String s : flags) {
            String flag = s.split("\\(")[0];
            Color c = toColor(s);
            flag2color.put(flag, c);
            priority.put(flag, z);
            z++;
        }
    }

    public Color getColor(List<String> strings) {
        int minPriority = Integer.MAX_VALUE;
        Color result = null;

        for(String s : strings) {
            Color curColor = flag2color.get(s);
            if(curColor != null) {
                int curPriority = priority.get(s);
                if(curPriority < minPriority) {
                    minPriority = curPriority;
                    result = curColor;
                }
            }
        }

        return result;
    }

    public static Color toColor(String s) {
        String sArr[] = s.split("\\(");
        String Color = sArr[1].substring(0, sArr[1].length() - 1);
        String ColorArr[] = Color.split(",");
        int r = Integer.parseInt(ColorArr[0]);
        int g = Integer.parseInt(ColorArr[1]);
        int b = Integer.parseInt(ColorArr[2]);
        return new Color(r, g, b);
    }

    public String getFlagString() {
        return flagString;
    }
      
    @Override
    public boolean equals(Object o) {
        if(o==null) 
            return false;
        return this.toString().equals(o.toString());
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 19 * hash + (this.flagString != null ? this.flagString.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString(){
        return "FlagSetting[" + flagString + "]";
    }
}
