package xyz.ravenbs.module.impl.combat;

import xyz.ravenbs.event.PreMotionEvent;
import xyz.ravenbs.module.Module;
import xyz.ravenbs.module.ModuleCategory;
import xyz.ravenbs.module.setting.impl.ButtonSetting;
import xyz.ravenbs.module.setting.impl.SliderSetting;
import xyz.ravenbs.utility.RotationUtils;
import xyz.ravenbs.utility.Utils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class KillAura extends Module {
    private SliderSetting aps;
    private SliderSetting attackRange;
    private SliderSetting rotationMode;
    private ButtonSetting targetInvis;
    private ButtonSetting ignoreTeammates;
    private ButtonSetting weaponOnly; // Not imp yet
    
    private String[] rotationModes = new String[] { "Silent", "Lock view", "None" };

    public static LivingEntity target;
    private long lastAttackTime;
    
    public KillAura() {
        super("KillAura", ModuleCategory.combat);
        this.registerSetting(aps = new SliderSetting("Check Cooldown", 1.0, 0.0, 1.0, 1.0)); // 0 = Spam, 1 = Cooldown
        this.registerSetting(attackRange = new SliderSetting("Range", 3.0, 3.0, 6.0, 0.1));
        this.registerSetting(rotationMode = new SliderSetting("Rotation mode", 0, rotationModes));
        this.registerSetting(targetInvis = new ButtonSetting("Target invis", true));
        this.registerSetting(ignoreTeammates = new ButtonSetting("Ignore teammates", true));
        this.registerSetting(weaponOnly = new ButtonSetting("Weapon only", false));
    }

    @Override
    public void onDisable() {
        target = null;
    }

    @Override
    public void onPreMotion(PreMotionEvent e) {
        if (weaponOnly.isToggled() && !Utils.isHoldingWeapon()) {
            target = null;
            return;
        }

        setTarget();
        
        if (target != null) {
            float[] rotations = RotationUtils.getRotations(target);
            if (rotations != null) {
                // Apply rotations
                int mode = (int) rotationMode.getInput();
                if (mode == 0) { // Silent
                    e.setYaw(rotations[0]);
                    e.setPitch(rotations[1]);
                    // Note: Actual silent rotation requires mixin to use these values for packet
                } else if (mode == 1) { // Lock View
                    mc.player.setYaw(rotations[0]);
                    mc.player.setPitch(rotations[1]);
                }
                
                // Attack
                if (shouldAttack()) {
                    attack(target);
                }
            }
        }
    }

    private void setTarget() {
        target = null;
        List<LivingEntity> targets = new ArrayList<>();
        
        for (Entity entity : mc.world.getEntities()) {
            if (entity instanceof PlayerEntity && entity != mc.player) {
                PlayerEntity player = (PlayerEntity) entity;
                
                // Checks
                if (player.isDead() || player.isSpectator()) continue;
                if (xyz.ravenbs.module.impl.world.AntiBot.isBot(player)) continue;
                if (player.isCreative()) continue;
                if (!targetInvis.isToggled() && player.isInvisible()) continue;
                if (ignoreTeammates.isToggled() && Utils.isTeamMate(player)) continue;
                if (xyz.ravenbs.utility.FriendManager.isFriend(player)) continue;
                if (mc.player.distanceTo(player) > attackRange.getInput()) continue;
                
                targets.add(player);
            }
        }
        
        // Sort by distance
        targets.sort(Comparator.comparingDouble(e -> mc.player.distanceTo(e)));
        
        if (!targets.isEmpty()) {
            target = targets.get(0);
        }
    }

    private boolean shouldAttack() {
        if (weaponOnly.isToggled() && !Utils.isHoldingWeapon()) {
            return false;
        }

        if (aps.getInput() == 1.0) {
            return mc.player.getAttackCooldownProgress(0.5f) >= 1.0f;
        } else {
             // Spam mode logic (simplified)
             // long delay = (long) (1000.0 / aps.getInput());
             // return System.currentTimeMillis() - lastAttackTime >= delay;
             return mc.player.getAttackCooldownProgress(0.5f) >= 1.0f; // Force cooldown for now 1.20
        }
    }

    private void attack(LivingEntity entity) {
        mc.interactionManager.attackEntity(mc.player, entity);
        mc.player.swingHand(Hand.MAIN_HAND);
        STap sTap = (STap) xyz.ravenbs.module.ModuleManager.getModule(STap.class);
        if (sTap != null && sTap.isEnabled()) {
            sTap.onAttack(entity);
        }
        lastAttackTime = System.currentTimeMillis();
    }
}
