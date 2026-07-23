package xyz.ravenbs.mixin.render;

import xyz.ravenbs.module.ModuleManager;
import xyz.ravenbs.module.impl.render.Potions;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.effect.StatusEffects;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.client.MinecraftClient;

@Mixin(GameRenderer.class)
public class MixinGameRenderer {
    @Inject(method = "tiltViewWhenHurt", at = @At("HEAD"), cancellable = true)
    private void onTiltViewWhenHurt(MatrixStack matrices, float tickDelta, CallbackInfo ci) {
        if (ModuleManager.noHurtCam != null && ModuleManager.noHurtCam.isEnabled()) {
            ci.cancel();
        }
    }
    
    // Remove nausea effect
    @Inject(method = "renderWorld", at = @At("HEAD"))
    private void onRenderWorld(float tickDelta, long limitTime, MatrixStack matrices, CallbackInfo ci) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player != null && ModuleManager.getModule(Potions.class) != null && 
            ModuleManager.getModule(Potions.class).isEnabled()) {
            
            // Remove nausea
            if (Potions.removeNausea != null && Potions.removeNausea.isToggled()) {
                mc.player.removeStatusEffect(StatusEffects.NAUSEA);
            }
            
            // Remove blindness
            if (Potions.removeBlindness != null && Potions.removeBlindness.isToggled()) {
                mc.player.removeStatusEffect(StatusEffects.BLINDNESS);
            }
        }
    }
}

