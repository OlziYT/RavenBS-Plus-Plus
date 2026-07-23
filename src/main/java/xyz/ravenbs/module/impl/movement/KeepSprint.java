package xyz.ravenbs.module.impl.movement;

import xyz.ravenbs.module.Module;
import xyz.ravenbs.module.ModuleCategory;
import xyz.ravenbs.module.setting.impl.DescriptionSetting;

public class KeepSprint extends Module {
    public KeepSprint() {
        super("KeepSprint", ModuleCategory.movement);
        this.registerSetting(new DescriptionSetting("Don't stop sprinting when hit"));
    }
}
