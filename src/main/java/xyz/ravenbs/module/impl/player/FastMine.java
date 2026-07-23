package xyz.ravenbs.module.impl.player;

import xyz.ravenbs.module.Module;
import xyz.ravenbs.module.ModuleCategory;
import xyz.ravenbs.module.setting.impl.ButtonSetting;
import xyz.ravenbs.module.setting.impl.DescriptionSetting;
import xyz.ravenbs.module.setting.impl.SliderSetting;

public class FastMine extends Module {
    private SliderSetting delay;
    private SliderSetting multiplier;
    private ButtonSetting creativeDisable;

    public FastMine() {
        super("FastMine", ModuleCategory.player);
        this.registerSetting(new DescriptionSetting("Vanilla is 5 delay & 1x speed."));
        this.registerSetting(delay = new SliderSetting("Break delay", 5.0, 0.0, 5.0, 1.0));
        this.registerSetting(multiplier = new SliderSetting("Break speed", 1.0, 1.0, 2.0, 0.1));
        this.registerSetting(creativeDisable = new ButtonSetting("Disable in creative", true));
    }

    @Override
    public void onUpdate() {
        // Logic is applied in MixinClientPlayerInteractionManager.
    }
    
    public double getMultiplier() {
        return multiplier.getInput();
    }
    
    public int getDelay() {
        return (int) delay.getInput();
    }

    public boolean isActiveNow() {
        if (!isEnabled() || mc.player == null || mc.interactionManager == null) {
            return false;
        }

        return !creativeDisable.isToggled() || !mc.player.isCreative();
    }
}
