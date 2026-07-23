package xyz.ravenbs.module.impl.movement;

import xyz.ravenbs.module.Module;
import xyz.ravenbs.module.ModuleCategory;
import xyz.ravenbs.module.setting.impl.DescriptionSetting;
import xyz.ravenbs.module.setting.impl.SliderSetting;
import xyz.ravenbs.utility.Utils;

public class LongJump extends Module {
    private SliderSetting factor;

    public LongJump() {
        super("LongJump", ModuleCategory.movement);
        this.registerSetting(factor = new SliderSetting("Factor", 3, 1, 5, 0.1));
    }

    @Override
    public void onUpdate() {
        if (mc.player != null && mc.player.isOnGround() && (mc.player.forwardSpeed != 0 || mc.player.sidewaysSpeed != 0)) {
            mc.player.jump();
            Utils.setSpeed(Utils.getHorizontalSpeed() * factor.getInput());
        }
    }
}
