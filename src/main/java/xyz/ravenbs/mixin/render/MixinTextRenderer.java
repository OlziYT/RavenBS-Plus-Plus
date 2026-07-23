package xyz.ravenbs.mixin.render;

import xyz.ravenbs.module.ModuleManager;
import xyz.ravenbs.module.impl.other.NameHider;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(TextRenderer.class)
public class MixinTextRenderer {
    // Modifier text argument in draw functions
    // This is tricky because there are many draw overloads.
    // In Fabric/Yarn, 'draw' methods behave differently.
    
    // Simplest: Modify 'text' variable in the main 'draw' method.
    // public int draw(String text, float x, float y, int color, boolean shadow, Matrix4f matrix, VertexConsumerProvider vertexConsumers, TextRenderer.TextLayerType layerType, int backgroundColor, int light)
    
    @ModifyVariable(method = "draw(Ljava/lang/String;FFIZLorg/joml/Matrix4f;Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/client/font/TextRenderer$TextLayerType;II)I", at = @At("HEAD"), argsOnly = true)
    private String modifyText(String text) {
        if (ModuleManager.nameHider != null && ModuleManager.nameHider.isEnabled() && text != null) {
            return NameHider.format(text);
        }
        return text;
    }
}
