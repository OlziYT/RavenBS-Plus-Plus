package xyz.ravenbs.module.setting.impl;

import com.google.gson.JsonObject;
import xyz.ravenbs.module.setting.Setting;

public class ButtonSetting extends Setting {
    private boolean isEnabled;

    public ButtonSetting(String name, boolean isEnabled) {
        super(name);
        this.isEnabled = isEnabled;
    }

    public boolean isToggled() {
        return this.isEnabled;
    }

    public void setEnabled(boolean b) {
        this.isEnabled = b;
    }

    public void toggle() {
        this.isEnabled = !this.isEnabled;
    }

    @Override
    public void loadProfile(JsonObject data) {
        if (data.has(name) && data.get(name).isJsonPrimitive()) {
            this.isEnabled = data.get(name).getAsBoolean();
        }
    }

    @Override
    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.addProperty(name, isEnabled);
        return json;
    }
}
