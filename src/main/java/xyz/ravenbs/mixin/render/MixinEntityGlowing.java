package xyz.ravenbs.mixin.render;

import xyz.ravenbs.module.ModuleManager;
import xyz.ravenbs.module.impl.render.Chams;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Makes all players glow (outline visible through walls) when Chams is enabled.
 * This is the ONLY reliable way to see entities through walls in modern Minecraft
 * without writing custom shaders.
 */
@Mixin(Entity.class)
public abstract class MixinEntityGlowing {

    @Inject(method = "isGlowing", at = @At("HEAD"), cancellable = true)
    private void isGlowing(CallbackInfoReturnable<Boolean> cir) {
        Entity self = (Entity) (Object) this;
        
        if (ModuleManager.chams != null && ModuleManager.chams.isEnabled()) {
            if (self instanceof PlayerEntity) {
                if (self != net.minecraft.client.MinecraftClient.getInstance().player) {
                    if (Chams.glowing != null && Chams.glowing.isToggled()) {
                        cir.setReturnValue(true);
                    }
                }
            }
        }
    }
}
