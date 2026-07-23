package xyz.ravenbs.module.impl.render;

import xyz.ravenbs.module.Module;
import xyz.ravenbs.module.ModuleCategory;
import xyz.ravenbs.module.setting.impl.DescriptionSetting;

public class AntiShuffle extends Module {
    public AntiShuffle() {
        super("AntiShuffle", ModuleCategory.render);
        this.registerSetting(new DescriptionSetting("Removes obfuscated text"));
    }
    
    public static String removeObfuscation(String s) {
        // §k is obfuscation
        if (s == null) return null;
        return s.replace("§k", "");
    }
}
