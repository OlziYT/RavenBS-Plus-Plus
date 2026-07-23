package xyz.ravenbs.module.impl.render;

import xyz.ravenbs.module.Module;
import xyz.ravenbs.module.ModuleCategory;
import xyz.ravenbs.module.setting.impl.DescriptionSetting;

public class NoHurtCam extends Module {
    public NoHurtCam() {
        super("NoHurtCam", ModuleCategory.render);
        this.registerSetting(new DescriptionSetting("Disables hurt effect"));
        this.setEnabled(true);
    }
}
