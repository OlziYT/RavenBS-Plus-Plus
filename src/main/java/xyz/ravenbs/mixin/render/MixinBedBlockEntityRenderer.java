package xyz.ravenbs.mixin.render;

import xyz.ravenbs.module.ModuleManager;
import xyz.ravenbs.module.impl.render.BedESP;
import xyz.ravenbs.utility.ChamsVertexConsumerProvider;
import net.minecraft.block.entity.BedBlockEntity;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BedBlockEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(BedBlockEntityRenderer.class)
public class MixinBedBlockEntityRenderer {

    @ModifyVariable(method = "render", at = @At("HEAD"), argsOnly = true)
    private VertexConsumerProvider modifyVertexConsumerProvider(VertexConsumerProvider vertexConsumerProvider, BedBlockEntity entity) {
        if (ModuleManager.bedESP != null && ModuleManager.bedESP.isEnabled()) {
            int style = (int) BedESP.style.getInput(); // 0=Box, 1=Chams, 2=Both
            if (style == 1 || style == 2) {
                // Fix: Don't render Chams for items in hand/inventory (world is null)
                if (entity.getWorld() != null) {
                    // Fix: Use the Bed Atlas texture, as bed models use atlas UVs.
                    Identifier atlasId = new Identifier("minecraft", "textures/atlas/beds.png");
                    return new ChamsVertexConsumerProvider(vertexConsumerProvider, atlasId, true, true);
                }
            }
        }
        return vertexConsumerProvider;
    }
}
