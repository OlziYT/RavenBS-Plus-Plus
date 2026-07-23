package xyz.ravenbs.module.setting.impl;

import com.google.gson.JsonObject;
import xyz.ravenbs.module.setting.Setting;

public class DescriptionSetting extends Setting {
    private String desc;

    public DescriptionSetting(String desc) {
        super(desc);
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }

    @Override
    public void loadProfile(JsonObject data) {
        // Description hasn't state to save
    }

    @Override
    public JsonObject toJson() {
        return null;
    }
}
