package xyz.ravenbs.module.impl.render;

import xyz.ravenbs.module.Module;
import xyz.ravenbs.module.ModuleCategory;
import xyz.ravenbs.utility.RenderUtils;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.EnderChestBlockEntity;
import net.minecraft.util.math.Box;

import java.awt.Color;

public class ChestESP extends Module {
    public static xyz.ravenbs.module.setting.impl.ModeSetting style;

    public ChestESP() {
        super("ChestESP", ModuleCategory.render);
        this.registerSetting(style = new xyz.ravenbs.module.setting.impl.ModeSetting("Style", new String[]{"Box", "Chams", "Mix"}, 0));
    }

    @Override
    public void onRenderWorld(WorldRenderContext context) {
        if (mc.world == null) return;
        
        int mode = (int) style.getInput(); // 0=Box, 1=Chams, 2=Both
        if (mode == 1) return; // Chams only, skip box

        for (BlockEntity blockEntity : xyz.ravenbs.utility.Utils.getAllBlockEntities()) {
            Color color = null;

            if (blockEntity instanceof ChestBlockEntity) {
                // ChestBlockEntity covers both Chest and Trapped Chest usually, but we can differentiate if needed.
                // Trapped Chests extend ChestBlockEntity in some mappings, or are distinct.
                // In Fabric 1.20.1, TrappedChestBlockEntity extends ChestBlockEntity.
                if (blockEntity instanceof net.minecraft.block.entity.TrappedChestBlockEntity) {
                    color = Color.ORANGE; // Same color or maybe darker? Let's stick to Orange for now.
                } else {
                    color = Color.ORANGE;
                }
            } else if (blockEntity instanceof EnderChestBlockEntity) {
                color = Color.MAGENTA;
            } else if (blockEntity instanceof net.minecraft.block.entity.ShulkerBoxBlockEntity) {
                // Shulker color?
                 color = new Color(255, 0, 255); // Pink/Magenta for Shulkers default
            } else if (blockEntity instanceof net.minecraft.block.entity.BarrelBlockEntity) {
                 color = new Color(139, 69, 19); // Brown
            }

            if (color != null) {
                Box box = new Box(blockEntity.getPos());
                // Adjust box for Chests/EnderChests which are slightly smaller than full block? 
                // RenderUtils.drawBoxLines usually takes standard box. 
                // Chests are 0.0625 inset. But new Box(pos) gives 1x1x1.
                // Ideally we get the bounding box from block state, but generic 1x1x1 is often "good enough" or we can refine.
                // Let's stick to 1x1x1 for now as per previous code.
                
                RenderUtils.drawBoxLines(context, box, color);
            }
        }
    }
}
