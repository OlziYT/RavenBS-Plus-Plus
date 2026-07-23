package xyz.ravenbs.module.impl.render;

import xyz.ravenbs.module.Module;
import xyz.ravenbs.module.ModuleCategory;
import xyz.ravenbs.module.setting.impl.ButtonSetting;
import xyz.ravenbs.module.setting.impl.ModeSetting;
import xyz.ravenbs.utility.RenderUtils;
import net.minecraft.block.BedBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BedBlockEntity;
import net.minecraft.client.util.math.MatrixStack;

public class BedESP extends Module {

    public static ModeSetting style;

    public BedESP() {
        super("BedESP", ModuleCategory.render);
        this.registerSetting(style = new ModeSetting("Style", new String[]{"Box", "Chams", "Mix"}, 0));
    }

    @Override
    public void onRenderWorld(net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext context) {
        if (mc.world == null) return;

        int mode = (int) style.getInput(); // 0=Box, 1=Chams, 2=Both
        if (mode == 1) return; // Chams only, skip box

        for (BlockEntity be : xyz.ravenbs.utility.Utils.getAllBlockEntities()) {
            if (be instanceof BedBlockEntity) {
                RenderUtils.drawBlockBox(context.matrixStack(), be.getPos(), java.awt.Color.RED);
            }
        }
    }
}
