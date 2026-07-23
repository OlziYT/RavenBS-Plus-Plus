package xyz.ravenbs.module.impl.combat;

import xyz.ravenbs.event.PreMotionEvent;
import xyz.ravenbs.module.Module;
import xyz.ravenbs.module.ModuleCategory;
import xyz.ravenbs.module.setting.impl.ButtonSetting;
import xyz.ravenbs.module.setting.impl.SliderSetting;
import xyz.ravenbs.utility.RotationUtils;
import xyz.ravenbs.utility.Utils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FishingRodItem;
import net.minecraft.util.Hand;

public class RodAimbot extends Module {
    private SliderSetting fov;
    private SliderSetting distance;
    private ButtonSetting aimInvis;
    private ButtonSetting ignoreTeammates;
    private boolean rotate = false;
    private boolean rightClick = false;
    private PlayerEntity target;

    public RodAimbot() {
        super("RodAimbot", ModuleCategory.combat);
        this.registerSetting(fov = new SliderSetting("FOV", 180, 30, 360, 4));
        this.registerSetting(distance = new SliderSetting("Distance", 6, 3, 30, 0.5));
        this.registerSetting(aimInvis = new ButtonSetting("Aim invis", false));
        this.registerSetting(ignoreTeammates = new ButtonSetting("Ignore teammates", false));
    }

    @Override
    public void onDisable() {
        rotate = false;
        rightClick = false;
        target = null;
    }

    @Override
    public void onUpdate() {
        if (mc.player == null || mc.currentScreen != null) return;
        
        // Check if holding rod and right clicking
        if (mc.player.getMainHandStack().getItem() instanceof FishingRodItem) {
            if (mc.options.useKey.isPressed() && mc.player.fishHook == null) {
                target = getTarget();
                if (target != null) {
                    rightClick = true;
                    rotate = true;
                }
            }
        }
    }

    @Override
    public void onPreMotion(PreMotionEvent e) {
        if (!rotate || target == null) return;
        if (!(mc.player.getMainHandStack().getItem() instanceof FishingRodItem)) return;
        
        float[] rotations = RotationUtils.getRotations(target);
        if (rotations != null) {
            e.setYaw(rotations[0]);
            e.setPitch(rotations[1]);
        }
        
        if (rightClick) {
            mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
            rightClick = false;
        }
        
        if (!rightClick) {
            rotate = false;
        }
    }

    private PlayerEntity getTarget() {
        PlayerEntity closest = null;
        double closestDist = distance.getInput() * distance.getInput();
        
        for (PlayerEntity player : mc.world.getPlayers()) {
            if (player == mc.player) continue;
            if (player.isDead()) continue;
            if (!aimInvis.isToggled() && player.isInvisible()) continue;
            if (xyz.ravenbs.utility.FriendManager.isFriend(player)) continue;
            if (ignoreTeammates.isToggled() && Utils.isTeamMate(player)) continue;
            if (xyz.ravenbs.module.impl.world.AntiBot.isBot(player)) continue;
            
            double dist = mc.player.squaredDistanceTo(player);
            if (dist < closestDist) {
                closestDist = dist;
                closest = player;
            }
        }
        
        return closest;
    }
}
