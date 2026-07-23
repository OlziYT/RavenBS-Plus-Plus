package xyz.ravenbs.module.setting.impl;

import xyz.ravenbs.module.setting.Setting;
import java.awt.Color;
import xyz.ravenbs.utility.Utils;

public class ColorSetting extends Setting {
    private SliderSetting red;
    private SliderSetting green;
    private SliderSetting blue;
    private ButtonSetting rainbow;
    
    public ColorSetting(String name, int color) {
        super(name);
        Color c = new Color(color);
        this.red = new SliderSetting("Red", c.getRed(), 0, 255, 1);
        this.green = new SliderSetting("Green", c.getGreen(), 0, 255, 1);
        this.blue = new SliderSetting("Blue", c.getBlue(), 0, 255, 1);
        this.rainbow = new ButtonSetting("Rainbow", false);
    }
    
    public ColorSetting(String name, Color color) {
        this(name, color.getRGB());
    }

    public int getRGB() {
        if (rainbow.isToggled()) {
            return Utils.getChroma(2, 0);
        }
        return new Color((int)red.getInput(), (int)green.getInput(), (int)blue.getInput()).getRGB();
    }
    
    @Override
    public void loadProfile(com.google.gson.JsonObject data) {
        if (data.has(name) && data.get(name).isJsonObject()) {
            com.google.gson.JsonObject colorData = data.getAsJsonObject(name);
            red.setValue(colorData.get("r").getAsDouble());
            green.setValue(colorData.get("g").getAsDouble());
            blue.setValue(colorData.get("b").getAsDouble());
            if (colorData.has("rainbow")) rainbow.setEnabled(colorData.get("rainbow").getAsBoolean());
        }
    }

    @Override
    public com.google.gson.JsonObject toJson() {
        com.google.gson.JsonObject json = new com.google.gson.JsonObject();
        com.google.gson.JsonObject colorData = new com.google.gson.JsonObject();
        colorData.addProperty("r", red.getInput());
        colorData.addProperty("g", green.getInput());
        colorData.addProperty("b", blue.getInput());
        colorData.addProperty("rainbow", rainbow.isToggled());
        json.add(name, colorData);
        return json;
    }

    public SliderSetting getRed() { return red; }
    public SliderSetting getGreen() { return green; }
    public SliderSetting getBlue() { return blue; }
    public ButtonSetting getRainbow() { return rainbow; }
}
