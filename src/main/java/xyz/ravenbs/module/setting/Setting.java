package xyz.ravenbs.module.setting;

import com.google.gson.JsonObject;

public abstract class Setting {
    public String name;
    public boolean visible = true;

    public Setting(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }
    
    public String getLocalizedName() {
         // raven.setting.name -> Translation
         String key = "raven.setting." + name.toLowerCase().replace(" ", "_");
         if (net.minecraft.client.resource.language.I18n.hasTranslation(key)) {
             return net.minecraft.client.resource.language.I18n.translate(key);
         }
         return net.minecraft.client.resource.language.I18n.hasTranslation(name) ? net.minecraft.client.resource.language.I18n.translate(name) : name;
    }

    // Abstract methods for saving/loading
    public abstract void loadProfile(JsonObject data);
    public abstract JsonObject toJson();
}
