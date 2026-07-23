package xyz.ravenbs.module.impl.movement;

import xyz.ravenbs.module.Module;
import xyz.ravenbs.module.ModuleCategory;
import xyz.ravenbs.module.setting.impl.SliderSetting;

public class Timer extends Module {
    public static SliderSetting speed;
    
    public Timer() {
        super("Timer", ModuleCategory.movement);
        this.registerSetting(speed = new SliderSetting("Speed", 1.0, 0.1, 5.0, 0.1));
    }
    
    @Override
    public void onDisable() {
        // No explicit cleanup is required; RenderTickCounter reads live module state.
    }
    
    // Logic is handled in MixinRenderTickCounter.
}
