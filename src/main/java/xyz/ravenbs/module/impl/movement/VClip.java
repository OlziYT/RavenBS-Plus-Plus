package xyz.ravenbs.module.impl.movement;

import xyz.ravenbs.module.Module;
import xyz.ravenbs.module.ModuleCategory;
import xyz.ravenbs.module.setting.impl.SliderSetting;

public class VClip extends Module {
    private SliderSetting dist;

    public VClip() {
        super("VClip", ModuleCategory.movement);
        this.registerSetting(dist = new SliderSetting("Distance", 2, -10, 10, 0.5));
    }

    @Override
    public void onEnable() {
        if (mc.player != null) {
            mc.player.setPosition(mc.player.getX(), mc.player.getY() + dist.getInput(), mc.player.getZ());
        }
        this.disable();
    }
}
