package xyz.ravenbs.module.impl.combat;

import xyz.ravenbs.module.Module;
import xyz.ravenbs.module.ModuleCategory;
import xyz.ravenbs.module.setting.impl.SliderSetting;

public class HitBox extends Module {
    public static SliderSetting expand;
    
    public HitBox() {
        super("HitBox", ModuleCategory.combat);
        this.registerSetting(expand = new SliderSetting("Expand", 0.0, 0.0, 1.0, 0.05));
    }
    
    // Logic is handled in MixinEntity / Target loop
    // In 1.20, we can mixin into `getBox` or interaction checks.
    // simpler way for Fabric: MixinEntity.getTargetingMargin()
}
