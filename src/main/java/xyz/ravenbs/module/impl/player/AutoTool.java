package xyz.ravenbs.module.impl.player;

import xyz.ravenbs.module.Module;
import xyz.ravenbs.module.ModuleCategory;
import xyz.ravenbs.utility.Utils;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;

public class AutoTool extends Module {
    public AutoTool() {
        super("AutoTool", ModuleCategory.player);
    }

    @Override
    public void onUpdate() {
        if (mc.currentScreen != null || mc.player == null || mc.world == null) return;
        
        if (!mc.options.attackKey.isPressed()) return;
        if (mc.crosshairTarget == null || mc.crosshairTarget.getType() != HitResult.Type.BLOCK) return;
        
        BlockHitResult blockHit = (BlockHitResult) mc.crosshairTarget;
        BlockState state = mc.world.getBlockState(blockHit.getBlockPos());
        
        float bestSpeed = 1.0f;
        int bestSlot = -1;
        
        ItemStack held = mc.player.getMainHandStack();
        float heldSpeed = held.getMiningSpeedMultiplier(state);
        
        // Find best tool in hotbar
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            float speed = stack.getMiningSpeedMultiplier(state);
            
            if (speed > bestSpeed) {
                bestSpeed = speed;
                bestSlot = i;
            }
        }
        
        if (bestSlot != -1 && bestSpeed > heldSpeed) {
            mc.player.getInventory().selectedSlot = bestSlot;
        }
    }
}
