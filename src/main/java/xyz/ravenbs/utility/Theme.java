package xyz.ravenbs.utility;

import java.awt.Color;

public class Theme {
    public static String[] themes = new String[] { "Rainbow", "Red", "Blue", "Green", "Orange", "Purple", "White" };

    public static int getGradient(int index, double delay) {
        if (index == 0) { // Rainbow
            return Utils.getChroma(2, (long) delay);
        }
        // Fallback simple colors for now
        switch (index) {
            case 1: return Color.RED.getRGB();
            case 2: return Color.BLUE.getRGB();
            case 3: return Color.GREEN.getRGB();
            case 4: return Color.ORANGE.getRGB();
            case 5: return new Color(128, 0, 128).getRGB();
            case 6: return Color.WHITE.getRGB();
            default: return -1;
        }
    }
}
