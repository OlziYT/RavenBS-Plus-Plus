package xyz.ravenbs.module.impl.fun;

import xyz.ravenbs.module.Module;
import xyz.ravenbs.module.ModuleCategory;
import xyz.ravenbs.module.setting.impl.SliderSetting;
import xyz.ravenbs.utility.Utils;

public class Spin extends Module {
    private SliderSetting speed;
    private float yaw;

    public Spin() {
        super("Spin", ModuleCategory.fun);
        this.registerSetting(speed = new SliderSetting("Speed", 10, 1, 30, 1));
    }

    @Override
    public void onUpdate() {
        if (mc.player == null) return;
        
        yaw += speed.getInput();
        if (yaw > 360) yaw -= 360;
        
        mc.player.setYaw(yaw);
        mc.player.setBodyYaw(yaw);
        mc.player.setHeadYaw(yaw);
    }
}
