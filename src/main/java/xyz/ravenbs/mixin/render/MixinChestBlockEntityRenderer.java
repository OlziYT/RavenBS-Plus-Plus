package xyz.ravenbs.mixin.render;

import xyz.ravenbs.module.ModuleManager;
import xyz.ravenbs.module.impl.render.ChestESP;
import xyz.ravenbs.utility.ChamsVertexConsumerProvider;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.ChestBlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ChestBlockEntityRenderer.class)
public class MixinChestBlockEntityRenderer<T extends BlockEntity> {

    @ModifyVariable(method = "render", at = @At("HEAD"), argsOnly = true)
    private VertexConsumerProvider modifyVertexConsumerProvider(VertexConsumerProvider vertexConsumerProvider, T entity) {
        if (ModuleManager.chestESP != null && ModuleManager.chestESP.isEnabled()) {
            int style = (int) ChestESP.style.getInput();
            if (style == 1 || style == 2) {
                 if (entity.getWorld() != null) {
                    // Use Chest Atlas
                    Identifier atlasId = new Identifier("minecraft", "textures/atlas/chest.png");
                    return new ChamsVertexConsumerProvider(vertexConsumerProvider, atlasId, true, true);
                }
            }
        }
        return vertexConsumerProvider;
    }
}
