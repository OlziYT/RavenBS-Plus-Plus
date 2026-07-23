package xyz.ravenbs.mixin.render;

import xyz.ravenbs.module.ModuleManager;
import xyz.ravenbs.module.impl.render.ChestESP;
import xyz.ravenbs.utility.ChamsVertexConsumerProvider;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.ShulkerBoxBlockEntityRenderer;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ShulkerBoxBlockEntityRenderer.class)
public class MixinShulkerBoxBlockEntityRenderer {

    @ModifyVariable(method = "render", at = @At("HEAD"), argsOnly = true)
    private VertexConsumerProvider modifyVertexConsumerProvider(VertexConsumerProvider vertexConsumerProvider, ShulkerBoxBlockEntity entity) {
        if (ModuleManager.chestESP != null && ModuleManager.chestESP.isEnabled()) {
            int style = (int) ChestESP.style.getInput(); // 0=Box, 1=Chams, 2=Both
            if (style == 1 || style == 2) {
                if (entity.getWorld() != null) {
                    // Shulkers have specific textures too, often colored.
                    // We rely on dynamic extraction or pass null.
                    Identifier atlasId = new Identifier("minecraft", "textures/atlas/shulker_boxes.png");
                    return new ChamsVertexConsumerProvider(vertexConsumerProvider, atlasId, true, true);
                }
            }
        }
        return vertexConsumerProvider;
    }
}
