package xyz.ravenbs.mixin.render;

import net.fabricmc.loader.api.FabricLoader;
import xyz.ravenbs.module.ModuleManager;
import xyz.ravenbs.module.impl.render.Chams;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(LivingEntityRenderer.class)
public abstract class MixinLivingEntityRenderer<T extends LivingEntity, M extends EntityModel<T>> {
    private static final boolean SODIUM_LOADED = FabricLoader.getInstance().isModLoaded("sodium");

    // Note: Polygon offset approach doesn't work in modern MC due to buffered rendering.
    // Chams through-wall effect is now implemented via MixinEntityGlowing (isGlowing override)
    
    @SuppressWarnings("unchecked")
    @ModifyVariable(method = "render", at = @At("HEAD"), argsOnly = true)
    private VertexConsumerProvider modifyVertexConsumerProvider(VertexConsumerProvider vertexConsumerProvider, T livingEntity) {
        if (SODIUM_LOADED) {
            return vertexConsumerProvider;
        }

        if (ModuleManager.chams != null && ModuleManager.chams.isEnabled() && livingEntity instanceof PlayerEntity) {
            if (livingEntity != net.minecraft.client.MinecraftClient.getInstance().player) {
                if (Chams.ignoreDepth != null && Chams.ignoreDepth.isToggled()) {
                    return new xyz.ravenbs.utility.ChamsVertexConsumerProvider(vertexConsumerProvider, ((net.minecraft.client.render.entity.EntityRenderer<T>) (Object) this).getTexture(livingEntity), true, false);
                }
            }
        }
        return vertexConsumerProvider;
    }
}
