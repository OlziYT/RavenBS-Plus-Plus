package xyz.ravenbs.module.impl.movement;

import xyz.ravenbs.module.Module;
import xyz.ravenbs.module.ModuleCategory;
import xyz.ravenbs.module.setting.impl.SliderSetting;
import xyz.ravenbs.utility.Utils;

public class Boost extends Module {
    private SliderSetting multiplier;
    private SliderSetting timer;

    public Boost() {
        super("Boost", ModuleCategory.movement);
        this.registerSetting(multiplier = new SliderSetting("Multiplier", 1.5, 1, 3, 0.1));
        this.registerSetting(timer = new SliderSetting("Timer", 1, 1, 3, 0.1));
    }

    @Override
    public void onEnable() {
        if (mc.player != null) {
            Utils.setSpeed(Utils.getHorizontalSpeed() * multiplier.getInput());
            // Timer logic would go here if we had a Timer manager accessible
        }
        super.onEnable();
    }
    
    @Override
    public void onUpdate() {
        // Auto disable after 1 tick or set duration?
        this.disable();
    }
}
