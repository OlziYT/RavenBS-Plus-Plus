package xyz.ravenbs.module.setting.impl;

import com.google.gson.JsonObject;
import xyz.ravenbs.module.setting.Setting;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class SliderSetting extends Setting {
    private double value;
    private double min;
    private double max;
    private double interval;

    public SliderSetting(String name, double value, double min, double max, double interval) {
        super(name);
        this.value = value;
        this.min = min;
        this.max = max;
        this.interval = interval;
    }

    public SliderSetting(String name, int value, String[] modes) {
        super(name);
        this.value = value;
        this.min = 0;
        this.max = modes.length - 1;
        this.interval = 1;
    }

    public double getInput() {
        return round(this.value, 2);
    }

    public double getMin() {
        return this.min;
    }

    public double getMax() {
        return this.max;
    }

    public void setValue(double n) {
        n = checkValue(n, min, max);
        n = Math.round(n * (1.0 / interval)) / (1.0 / interval);
        this.value = n;
    }

    public static double checkValue(double v, double min, double max) {
        v = Math.max(min, v);
        v = Math.min(max, v);
        return v;
    }

    public static double round(double value, int places) {
        if (places < 0) {
            throw new IllegalArgumentException();
        }
        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    @Override
    public void loadProfile(JsonObject data) {
        if (data.has(name) && data.get(name).isJsonPrimitive()) {
            setValue(data.get(name).getAsDouble());
        }
    }

    @Override
    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.addProperty(name, value);
        return json;
    }
}
