package xyz.ravenbs.module.impl.movement;

import xyz.ravenbs.module.Module;
import xyz.ravenbs.module.ModuleCategory;
import xyz.ravenbs.module.setting.impl.SliderSetting;
import xyz.ravenbs.utility.Utils;

public class Fly extends Module {
    private SliderSetting speed;
    private boolean oldFlying;
    private boolean oldAllowFlying;
    private float oldFlySpeed;
    
    public Fly() {
        super("Fly", ModuleCategory.movement);
        this.registerSetting(speed = new SliderSetting("Speed", 2.0, 0.1, 5.0, 0.1));
    }

    @Override
    public void onEnable() {
        if (mc.player == null) {
            return;
        }

        oldFlying = mc.player.getAbilities().flying;
        oldAllowFlying = mc.player.getAbilities().allowFlying;
        oldFlySpeed = mc.player.getAbilities().getFlySpeed();
    }

    @Override
    public void onDisable() {
        if (mc.player == null) return;
        mc.player.getAbilities().flying = oldFlying;
        mc.player.getAbilities().allowFlying = oldAllowFlying;
        mc.player.getAbilities().setFlySpeed(oldFlySpeed);
        mc.player.setVelocity(0, mc.player.getVelocity().y, 0);
    }

    @Override
    public void onUpdate() {
        if (mc.player == null || mc.options == null) {
            return;
        }

        if (!Utils.isMoving()) {
            mc.player.setVelocity(0, 0, 0);
        } else {
            mc.player.getAbilities().flying = false;
            mc.player.getAbilities().allowFlying = oldAllowFlying;
            mc.player.getAbilities().setFlySpeed(oldFlySpeed);
             
            double y = 0;
            if (mc.options.jumpKey.isPressed()) {
                y = speed.getInput();
            } else if (mc.options.sneakKey.isPressed()) {
                y = -speed.getInput();
            }
             
            Utils.setSpeed(speed.getInput());
            mc.player.setVelocity(mc.player.getVelocity().x, y, mc.player.getVelocity().z);
            mc.player.fallDistance = 0;
        }
    }
}
