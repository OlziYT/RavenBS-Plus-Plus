package xyz.ravenbs.module.impl.player;

import xyz.ravenbs.module.Module;
import xyz.ravenbs.module.ModuleCategory;
import xyz.ravenbs.module.setting.impl.ButtonSetting;
import xyz.ravenbs.module.setting.impl.DescriptionSetting;

public class DelayRemover extends Module {
    public static ButtonSetting clickDelay;
    public static ButtonSetting jumpDelay;
    public static ButtonSetting hitDelay;

    public DelayRemover() {
        super("DelayRemover", ModuleCategory.player);
        this.registerSetting(new DescriptionSetting("Removes various delays."));
        this.registerSetting(clickDelay = new ButtonSetting("Click delay", true));
        this.registerSetting(jumpDelay = new ButtonSetting("Jump delay", false));
        this.registerSetting(hitDelay = new ButtonSetting("Hit delay", true));
    }
    
    // These are handled by mixins:
    // - Click delay: MixinMinecraftClient removing leftClickCounter
    // - Jump delay: Removing jumpTicks check
    // - Hit delay: Removing attack cooldown
}
