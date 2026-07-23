package xyz.ravenbs.module.impl.render;

import xyz.ravenbs.module.Module;
import xyz.ravenbs.module.ModuleCategory;
import xyz.ravenbs.utility.RenderUtils;
import java.awt.Color;

public class BreakProgress extends Module {
    public BreakProgress() {
        super("BreakProgress", ModuleCategory.render);
    }
    
    // Requires mixin to RenderGlobal or access to world.getBlockBreakingProgress()
    // Fabric API implies mixin for this.
    // Simplifying: If we assume we don't have mixin for break progress list map, we can't implement.
    // RavenBS usually highlights the block being broken.
    
    @Override
    public void onRenderWorld(net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext context) {
        if (mc.interactionManager == null || !mc.interactionManager.isBreakingBlock()) {
            return;
        }

        xyz.ravenbs.mixin.accessor.AccessorClientPlayerInteractionManager accessor =
                (xyz.ravenbs.mixin.accessor.AccessorClientPlayerInteractionManager) mc.interactionManager;
        net.minecraft.util.math.BlockPos pos = accessor.getCurrentBreakingPos();
        if (pos == null) {
            return;
        }

        float progress = Math.max(0.0f, Math.min(1.0f, accessor.getCurrentBreakingProgress()));
        int alpha = 80 + (int) (progress * 140.0f);
        Color color = new Color(255, 64 + (int) (progress * 160.0f), 64, alpha);
        RenderUtils.drawBlockBox(context.matrixStack(), pos, color);
    }
}
