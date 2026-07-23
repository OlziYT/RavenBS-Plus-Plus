package xyz.ravenbs.module.impl.render;

import xyz.ravenbs.module.Module;
import xyz.ravenbs.module.ModuleCategory;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.gui.DrawContext;

public class BridgeInfo extends Module {
    public BridgeInfo() {
        super("BridgeInfo", ModuleCategory.render);
    }
    
    // Logic mostly handled in HUD for simplicity, or we can hook here.
    // Let's hook into HUD rendering or generic render event.
    // For simplicity, we'll expose a static string that HUD can pick up, or render directly.
    
    // Ideally BridgeInfo renders "Blocks To Block: X"
    
    // Actually, let's implement a simple direct renderer.
    
    private void render(DrawContext context, float tickDelta) {
        if (!isEnabled() || mc.player == null) return;
        
        String text = "Blocks: " + getBlocksToBlock();
        context.drawText(mc.textRenderer, text, 10, 100, -1, true);
    }
    
    private String getBlocksToBlock() {
        if (mc.player == null) return "?";
        double y = mc.player.getY();
        
        // Raycast down to find ground
        // Simplified: just check distance to nearest block below
        for (int i = 0; i < 50; i++) {
             if (!mc.world.isAir(mc.player.getBlockPos().down(i))) {
                 return String.valueOf(i - 1);
             }
        }
        return ">50";
    }
    
    // Use the HUD callback
    public static void onRender(DrawContext context) {
         if (xyz.ravenbs.module.ModuleManager.bridgeInfo != null && xyz.ravenbs.module.ModuleManager.bridgeInfo.isEnabled()) {
             ((BridgeInfo)xyz.ravenbs.module.ModuleManager.bridgeInfo).render(context, 1.0f);
         }
    }
}
