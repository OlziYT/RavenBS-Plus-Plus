package xyz.ravenbs.module.impl.movement;

import xyz.ravenbs.module.Module;
import xyz.ravenbs.module.ModuleCategory;

public class AutoJump extends Module {
    public AutoJump() {
        super("AutoJump", ModuleCategory.movement);
    }

    @Override
    public void onUpdate() {
        if (mc.player != null && mc.player.isOnGround()) {
            mc.player.jump();
        }
    }
}
