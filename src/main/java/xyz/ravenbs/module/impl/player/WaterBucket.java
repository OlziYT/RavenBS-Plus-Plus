package xyz.ravenbs.module.impl.player;

import xyz.ravenbs.event.PreMotionEvent;
import xyz.ravenbs.module.Module;
import xyz.ravenbs.module.ModuleCategory;
import xyz.ravenbs.module.setting.impl.ButtonSetting;
import xyz.ravenbs.module.setting.impl.SliderSetting;
import xyz.ravenbs.utility.RotationUtils;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;

public class WaterBucket extends Module {
    private ButtonSetting silentAim;
    private ButtonSetting switchToItem;
    private SliderSetting fallDistance;

    public WaterBucket() {
        super("WaterBucket", ModuleCategory.player);
        this.registerSetting(silentAim = new ButtonSetting("Silent aim", true));
        this.registerSetting(switchToItem = new ButtonSetting("Switch to item", true));
        this.registerSetting(fallDistance = new SliderSetting("Fall distance", 3.0, 1.0, 10.0, 0.5));
    }

    @Override
    public void onPreMotion(PreMotionEvent e) {
        if (!inPosition()) return;
        
        // Check if we have water bucket
        int slot = findWaterBucket();
        if (slot == -1) return;
        
        // Switch to water bucket
        if (switchToItem.isToggled() && mc.player.getInventory().selectedSlot != slot) {
            mc.player.getInventory().selectedSlot = slot;
        }
        
        // Check if holding water bucket
        if (mc.player.getMainHandStack().getItem() != Items.WATER_BUCKET) return;
        
        // Aim down
        if (silentAim.isToggled()) {
            e.setPitch(90);
        } else {
            mc.player.setPitch(90);
        }
        
        // Use water bucket
        if (mc.crosshairTarget != null && mc.crosshairTarget.getType() == HitResult.Type.BLOCK) {
            mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
        }
    }

    private boolean inPosition() {
        if (mc.player == null) return false;
        return !mc.player.getAbilities().flying &&
               !mc.player.isCreative() &&
               !mc.player.isOnGround() &&
               mc.player.getVelocity().y < -0.5 &&
               !mc.player.isTouchingWater() &&
               mc.player.fallDistance >= fallDistance.getInput();
    }

    private int findWaterBucket() {
        PlayerInventory inv = mc.player.getInventory();
        for (int i = 0; i < 9; i++) {
            if (inv.getStack(i).getItem() == Items.WATER_BUCKET) {
                return i;
            }
        }
        return -1;
    }
}
