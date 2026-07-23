package xyz.ravenbs.module.impl.render;

import xyz.ravenbs.module.Module;
import xyz.ravenbs.module.ModuleCategory;
import xyz.ravenbs.module.setting.impl.DescriptionSetting;
import xyz.ravenbs.module.setting.impl.SliderSetting;

public class ExtendCamera extends Module {
    public static SliderSetting distance;
    
    public ExtendCamera() {
        super("ExtendCamera", ModuleCategory.render);
        this.registerSetting(new DescriptionSetting("Extends camera in third person."));
        this.registerSetting(new DescriptionSetting("Default is 4 blocks."));
        this.registerSetting(distance = new SliderSetting("Distance", 4, 1, 40, 0.5));
    }
    
    // This is handled by MixinCamera to modify third person distance
    public static double getDistance() {
        return distance != null ? distance.getInput() : 4.0;
    }
}
