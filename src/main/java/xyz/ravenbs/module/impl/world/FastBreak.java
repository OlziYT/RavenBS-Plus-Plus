package xyz.ravenbs.module.impl.world;

import xyz.ravenbs.module.Module;
import xyz.ravenbs.module.ModuleCategory;
import xyz.ravenbs.module.setting.impl.SliderSetting;

public class FastBreak extends Module {
    public static SliderSetting speed;
    
    public FastBreak() {
        super("FastBreak", ModuleCategory.world);
        this.registerSetting(speed = new SliderSetting("Speed", 1.4, 1.0, 2.0, 0.1));
    }
    
    @Override
    public void onUpdate() {
        if (mc.interactionManager != null) {
            if (mc.interactionManager.isBreakingBlock()) {
                 // Accessor needed for currentBreakingProgress usually
                 // Or mixin into ClientPlayerInteractionManager to accelerate break progress
            }
        }
    }
}
