package xyz.ravenbs.module.impl.render;

import xyz.ravenbs.module.Module;
import xyz.ravenbs.module.ModuleCategory;
import xyz.ravenbs.module.setting.impl.DescriptionSetting;

public class NoCameraClip extends Module {
    public NoCameraClip() {
        super("NoCameraClip", ModuleCategory.render);
        this.registerSetting(new DescriptionSetting("Camera clips through blocks"));
    }
}
