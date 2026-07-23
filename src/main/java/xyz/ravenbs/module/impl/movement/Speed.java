package xyz.ravenbs.module.impl.movement;

import xyz.ravenbs.event.PreMotionEvent;
import xyz.ravenbs.module.Module;
import xyz.ravenbs.module.ModuleCategory;
import xyz.ravenbs.module.setting.impl.SliderSetting;
import xyz.ravenbs.utility.Utils;

public class Speed extends Module {
    private SliderSetting mode;
    private SliderSetting speed;
    
    private String[] modes = new String[] { "Vanilla", "Strafe" };

    public Speed() {
        super("Speed", ModuleCategory.movement);
        this.registerSetting(mode = new SliderSetting("Mode", 0, modes));
        this.registerSetting(speed = new SliderSetting("Speed", 1.0, 1.0, 5.0, 0.1));
    }

    @Override
    public void onUpdate() {
        if (!Utils.isMoving()) return;

        if (mode.getInput() == 0) { // Vanilla
             // Generic speed boost logic
             // In Fabric 1.20, we can just modify velocity if on ground or in air
             // But strictly speaking we should probably do this in onPreMotion for consisteny
        }
    }
    
    @Override
    public void onPreMotion(PreMotionEvent e) {
        if (!Utils.isMoving()) return;
        
        if (mode.getInput() == 0) { // Vanilla
            if (mc.player.isOnGround()) {
                 mc.player.jump();
            }
            // Simple strafe applied to logic
            Utils.setSpeed(speed.getInput() / 5.0); // Rough conversion
        } else if (mode.getInput() == 1) { // Strafe
            if (mc.player.isOnGround()) {
                mc.player.jump();
            }
            Utils.setSpeed(Utils.getHorizontalSpeed() + (speed.getInput() / 20.0));
        }
    }
}
