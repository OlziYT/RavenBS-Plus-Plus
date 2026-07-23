package xyz.ravenbs.module.setting.impl;

import com.google.gson.JsonObject;
import xyz.ravenbs.module.setting.Setting;

public class ModeSetting extends Setting {
    private String[] options;
    private int input;

    public ModeSetting(String name, String[] options, int defaultInput) {
        super(name);
        this.options = options;
        this.input = defaultInput;
    }

    public String[] getOptions() {
        return options;
    }

    public int getInput() {
        return input;
    }

    public void setInput(int input) {
        this.input = input;
    }

    public void cycle() {
        input++;
        if (input >= options.length) {
            input = 0;
        }
    }

    @Override
    public void loadProfile(JsonObject data) {
        if (data.has(name) && data.get(name).isJsonPrimitive()) {
            input = data.get(name).getAsInt();
        }
    }

    @Override
    public JsonObject toJson() {
        JsonObject data = new JsonObject();
        data.addProperty(name, input);
        return data;
    }
}
