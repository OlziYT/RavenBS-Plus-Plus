package xyz.ravenbs.module.impl.render;

import xyz.ravenbs.module.Module;
import xyz.ravenbs.module.ModuleCategory;
import xyz.ravenbs.utility.RenderUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.client.util.math.MatrixStack;

public class MobESP extends Module {
    public MobESP() {
        super("MobESP", ModuleCategory.render);
    }

    @Override
    public void onRenderWorld(net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext context) {
        for (Entity e : mc.world.getEntities()) {
            if (e instanceof MobEntity) {
                RenderUtils.drawEntityBox(context.matrixStack(), e, java.awt.Color.ORANGE);
            }
        }
    }
}
