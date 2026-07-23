package xyz.ravenbs.module.impl.combat;

import xyz.ravenbs.module.Module;
import xyz.ravenbs.module.ModuleCategory;
import xyz.ravenbs.module.setting.impl.ButtonSetting;
import xyz.ravenbs.module.setting.impl.SliderSetting;
import xyz.ravenbs.utility.RotationUtils;
import xyz.ravenbs.utility.Utils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;

public class TPAura extends Module {
    private SliderSetting range;
    private SliderSetting tpRange;
    private ButtonSetting ignoreTeammates;

    public TPAura() {
        super("TPAura", ModuleCategory.combat);
        this.registerSetting(range = new SliderSetting("Attack Range", 6.0, 3.0, 20.0, 0.5));
        this.registerSetting(tpRange = new SliderSetting("TP Range", 3.0, 1.0, 5.0, 0.1));
        this.registerSetting(ignoreTeammates = new ButtonSetting("Ignore teammates", true));
    }

    @Override
    public void onUpdate() {
        if (mc.player == null || mc.world == null) return;
        if (mc.player.getAttackCooldownProgress(0.5f) < 1.0f) return;
        
        PlayerEntity target = getTarget();
        if (target == null) return;
        
        // Teleport behind target
        double yaw = Math.toRadians(target.getYaw());
        double tpDist = tpRange.getInput();
        double newX = target.getX() + Math.sin(yaw) * tpDist;
        double newZ = target.getZ() - Math.cos(yaw) * tpDist;
        
        // Teleport
        mc.player.setPosition(newX, target.getY(), newZ);
        
        // Attack
        mc.interactionManager.attackEntity(mc.player, target);
        mc.player.swingHand(Hand.MAIN_HAND);
        
        // Teleport back? Or stay?
    }

    private PlayerEntity getTarget() {
        PlayerEntity closest = null;
        double closestDist = range.getInput() * range.getInput();
        
        for (PlayerEntity player : mc.world.getPlayers()) {
            if (player == mc.player) continue;
            if (player.isDead()) continue;
            if (player.isSpectator()) continue;
            if (xyz.ravenbs.utility.FriendManager.isFriend(player)) continue;
            if (ignoreTeammates.isToggled() && Utils.isTeamMate(player)) continue;
            
            double dist = mc.player.squaredDistanceTo(player);
            if (dist < closestDist) {
                closestDist = dist;
                closest = player;
            }
        }
        
        return closest;
    }
}
