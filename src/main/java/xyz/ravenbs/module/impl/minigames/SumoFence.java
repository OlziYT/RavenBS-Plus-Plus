package xyz.ravenbs.module.impl.minigames;

import xyz.ravenbs.module.Module;
import xyz.ravenbs.module.ModuleCategory;
import xyz.ravenbs.module.setting.impl.SliderSetting;
import net.minecraft.block.Block;
import net.minecraft.block.FenceBlock;
import net.minecraft.util.math.BlockPos;

public class SumoFence extends Module {
    private SliderSetting height;
    
    public SumoFence() {
        super("SumoFence", ModuleCategory.minigames);
        this.registerSetting(height = new SliderSetting("Fence Height", 1.5, 1.0, 2.0, 0.1));
    }
    
    // Standard SumoFence makes fences huge collision boxes.
    // In Fabric 1.20 we can mixin into FenceBlock.getCollisionShape.
    
    // Since we are porting "easy" things, a Mixin into AbstractBlock or FenceBlock is needed.
}
