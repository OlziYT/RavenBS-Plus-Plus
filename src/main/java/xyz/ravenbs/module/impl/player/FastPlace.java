package xyz.ravenbs.module.impl.player;

import xyz.ravenbs.module.Module;
import xyz.ravenbs.module.ModuleCategory;
import xyz.ravenbs.module.setting.impl.SliderSetting;

public class FastPlace extends Module {
    private SliderSetting delay;

    public FastPlace() {
        super("FastPlace", ModuleCategory.player);
        this.registerSetting(delay = new SliderSetting("Delay", 0, 0, 4, 1));
    }

    public double getDelay() {
        return delay.getInput();
    }
}
