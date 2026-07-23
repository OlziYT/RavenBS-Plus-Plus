package xyz.ravenbs.module.impl.movement;

import xyz.ravenbs.module.Module;
import xyz.ravenbs.module.ModuleCategory;
import xyz.ravenbs.module.setting.impl.SliderSetting;
import xyz.ravenbs.utility.Utils;

public class BHop extends Module {
    private SliderSetting speed;

    public BHop() {
        super("BHop", ModuleCategory.movement);
        this.registerSetting(speed = new SliderSetting("Speed", 2, 1, 5, 0.1));
    }

    @Override
    public void onUpdate() {
        if (mc.player == null) return;
        if ((mc.player.forwardSpeed != 0 || mc.player.sidewaysSpeed != 0) && !mc.player.isTouchingWater() && !mc.player.isInLava()) {
            if (mc.player.isOnGround()) {
                mc.player.jump();
            } else {
                 Utils.setSpeed(speed.getInput() * 0.1); // Simplified logic
            }
        }
    }
}
