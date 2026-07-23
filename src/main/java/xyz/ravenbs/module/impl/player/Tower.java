package xyz.ravenbs.module.impl.player;

import xyz.ravenbs.module.Module;
import xyz.ravenbs.module.ModuleCategory;
import xyz.ravenbs.module.ModuleManager;
import xyz.ravenbs.module.setting.impl.ButtonSetting;
import xyz.ravenbs.module.setting.impl.DescriptionSetting;
import xyz.ravenbs.module.setting.impl.SliderSetting;
import xyz.ravenbs.utility.Utils;
import net.minecraft.util.math.Vec3d;

public class Tower extends Module {
    private SliderSetting mode;
    private ButtonSetting disableWhileHurt;
    private ButtonSetting disableInLiquid;
    
    private String[] modes = new String[]{"Vanilla", "NCP", "Hypixel"};
    private int towerTicks = 0;
    private boolean towering = false;

    public Tower() {
        super("Tower", ModuleCategory.player);
        this.registerSetting(new DescriptionSetting("Works with Scaffold."));
        this.registerSetting(mode = new SliderSetting("Mode", 0, modes));
        this.registerSetting(disableWhileHurt = new ButtonSetting("Disable while hurt", false));
        this.registerSetting(disableInLiquid = new ButtonSetting("Disable in liquid", false));
        
        // This module is a helper, not enabled directly
        // canBeEnabled = false; // Not available in our Module class
    }

    @Override
    public void onUpdate() {
        if (!canTower()) {
            reset();
            return;
        }
        
        // Check if jump key is pressed
        if (!mc.options.jumpKey.isPressed()) {
            reset();
            return;
        }
        
        towering = true;
        towerTicks = mc.player.isOnGround() ? 0 : towerTicks + 1;
        
        switch ((int) mode.getInput()) {
            case 0: // Vanilla
                if (mc.player.isOnGround()) {
                    mc.player.jump();
                }
                break;
                
            case 1: // NCP
                if (mc.player.isOnGround()) {
                    mc.player.setVelocity(mc.player.getVelocity().x, 0.42, mc.player.getVelocity().z);
                } else {
                    double y = mc.player.getY() % 1;
                    if (y > 0.4 && y < 0.43) {
                        mc.player.setVelocity(mc.player.getVelocity().x, 0.33, mc.player.getVelocity().z);
                    } else if (y > 0.7) {
                        mc.player.setVelocity(mc.player.getVelocity().x, 1 - y, mc.player.getVelocity().z);
                    }
                }
                break;
                
            case 2: // Hypixel
                if (mc.player.isOnGround()) {
                    mc.player.setVelocity(mc.player.getVelocity().x, 0.42, mc.player.getVelocity().z);
                } else if (towerTicks <= 3) {
                    // Slow down horizontal motion
                    Vec3d vel = mc.player.getVelocity();
                    mc.player.setVelocity(vel.x * 0.7, vel.y, vel.z * 0.7);
                }
                break;
        }
    }

    public boolean canTower() {
        if (mc.player == null) return false;
        if (mc.currentScreen != null) return false;
        if (disableWhileHurt.isToggled() && mc.player.hurtTime > 9) return false;
        if (disableInLiquid.isToggled() && (mc.player.isSubmergedInWater() || mc.player.isInLava())) return false;
        
        // Only tower when scaffold is enabled and holding blocks
        if (ModuleManager.scaffold == null || !ModuleManager.scaffold.isEnabled()) return false;
        
        return true;
    }
    
    public boolean isTowering() {
        return towering && canTower();
    }

    private void reset() {
        towerTicks = 0;
        towering = false;
    }
}
