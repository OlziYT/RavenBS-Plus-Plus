package xyz.ravenbs.module.impl.combat;

import xyz.ravenbs.module.Module;
import xyz.ravenbs.module.ModuleCategory;
import xyz.ravenbs.module.setting.impl.ButtonSetting;
import xyz.ravenbs.module.setting.impl.SliderSetting;
import xyz.ravenbs.utility.RotationUtils;
import xyz.ravenbs.utility.Utils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;

public class AimAssist extends Module {
    private SliderSetting speed;
    private SliderSetting fov;
    private SliderSetting distance;
    private ButtonSetting clickAim;
    private ButtonSetting weaponOnly;
    private ButtonSetting aimInvis;
    
    public AimAssist() {
        super("AimAssist", ModuleCategory.combat);
        this.registerSetting(speed = new SliderSetting("Speed", 45, 1, 100, 1));
        this.registerSetting(fov = new SliderSetting("FOV", 90, 15, 180, 5));
        this.registerSetting(distance = new SliderSetting("Distance", 4.5, 1, 8, 0.5));
        this.registerSetting(clickAim = new ButtonSetting("Click aim", true));
        this.registerSetting(weaponOnly = new ButtonSetting("Weapon only", false));
        this.registerSetting(aimInvis = new ButtonSetting("Aim invis", false));
    }

    @Override
    public void onUpdate() {
        if (mc.player == null || mc.world == null) return;
        if (clickAim.isToggled() && !mc.options.attackKey.isPressed()) return;
        if (weaponOnly.isToggled() && !Utils.isHoldingWeapon()) return;
        
        Entity target = getClosestTarget();
        if (target != null) {
            float[] rotations = RotationUtils.getRotations(target);
            if (rotations != null) {
                // Smoothly interpolate
                // Current
                float yaw = mc.player.getYaw();
                float pitch = mc.player.getPitch();
                
                // Target
                float targetYaw = rotations[0];
                float targetPitch = rotations[1];
                
                // Delta
                float yawDelta = MathHelper.wrapDegrees(targetYaw - yaw);
                float pitchDelta = MathHelper.wrapDegrees(targetPitch - pitch);
                
                // Clamp speed
                double speedVal = speed.getInput();
                // Simple factor
                double yawStep = yawDelta * (speedVal / 100.0);
                double pitchStep = pitchDelta * (speedVal / 100.0);
                
                mc.player.setYaw(yaw + (float)yawStep);
                mc.player.setPitch(pitch + (float)pitchStep);
            }
        }
    }
    
    private Entity getClosestTarget() {
        Entity closest = null;
        double minDistance = distance.getInput();
        
        for (Entity entity : mc.world.getEntities()) {
            if (entity instanceof PlayerEntity && entity != mc.player) {
                if (!aimInvis.isToggled() && entity.isInvisible()) continue;
                
                double d = mc.player.distanceTo(entity);
                if (d > minDistance) continue;
                
                if (!Utils.canPlayerBeSeen(entity)) continue;
                
                // FOV check
                float[] rots = RotationUtils.getRotations(entity);
                float yawDelta = Math.abs(MathHelper.wrapDegrees(rots[0] - mc.player.getYaw()));
                if (yawDelta > fov.getInput()) continue;
                
                if (d < minDistance) {
                    minDistance = d;
                    closest = entity;
                }
            }
        }
        return closest;
    }
}
