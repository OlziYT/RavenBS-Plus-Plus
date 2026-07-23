package xyz.ravenbs.module.impl.render;

import xyz.ravenbs.module.Module;
import xyz.ravenbs.module.ModuleCategory;
import xyz.ravenbs.module.setting.impl.ButtonSetting;

public class Potions extends Module {
    public static ButtonSetting removeBlindness;
    public static ButtonSetting removeNausea;

    public Potions() {
        super("Potions", ModuleCategory.render);
        this.registerSetting(removeBlindness = new ButtonSetting("Remove blindness", true));
        this.registerSetting(removeNausea = new ButtonSetting("Remove nausea", true));
    }
    
    // These effects are handled by mixins:
    // - Blindness: MixinLivingEntity or MixinGameRenderer to skip blindness shader
    // - Nausea: MixinGameRenderer to skip nausea shader
}
