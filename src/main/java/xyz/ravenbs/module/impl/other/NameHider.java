package xyz.ravenbs.module.impl.other;

import xyz.ravenbs.module.Module;
import xyz.ravenbs.module.ModuleCategory;
import xyz.ravenbs.module.setting.impl.DescriptionSetting;
import xyz.ravenbs.module.setting.impl.SliderSetting;

public class NameHider extends Module {
    public static String n = "You";
    
    public NameHider() {
        super("NameHider", ModuleCategory.other);
        this.registerSetting(new DescriptionSetting("Hides your name in chat/render"));
    }
    
    public static String format(String s) {
        if (xyz.ravenbs.module.ModuleManager.nameHider != null && xyz.ravenbs.module.ModuleManager.nameHider.isEnabled()) {
            return s.replace(net.minecraft.client.MinecraftClient.getInstance().getSession().getUsername(), n);
        }
        return s;
    }
}
