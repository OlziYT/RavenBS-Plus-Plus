package xyz.ravenbs.module.impl.minigames;

import xyz.ravenbs.module.Module;
import xyz.ravenbs.module.ModuleCategory;
import xyz.ravenbs.module.setting.impl.SliderSetting;
import xyz.ravenbs.utility.Utils;
import net.minecraft.block.BedBlock;
import net.minecraft.block.BlockState;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class BedAura extends Module {
    private SliderSetting range;
    
    public BedAura() {
        super("BedAura", ModuleCategory.minigames);
        this.registerSetting(range = new SliderSetting("Range", 4.5, 1, 6, 0.5));
    }

    @Override
    public void onUpdate() {
        if (mc.world == null || mc.player == null) return;
        
        float r = (float) range.getInput();
        BlockPos playerPos = mc.player.getBlockPos();
        
        for (int x = (int) -r; x <= r; x++) {
            for (int y = (int) -r; y <= r; y++) {
                for (int z = (int) -r; z <= r; z++) {
                    BlockPos pos = playerPos.add(x, y, z);
                    if (mc.player.squaredDistanceTo(pos.toCenterPos()) > r * r) continue;
                    
                    BlockState state = mc.world.getBlockState(pos);
                    if (state.getBlock() instanceof BedBlock) {
                        // Found bed
                        // In 1.20, we should swing and break
                         mc.player.swingHand(Hand.MAIN_HAND);
                         mc.interactionManager.updateBlockBreakingProgress(pos, Direction.UP);
                         
                         // We can only break one block at a time effectively usually
                         return;
                    }
                }
            }
        }
    }
}
