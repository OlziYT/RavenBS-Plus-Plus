package xyz.ravenbs.mixin.client;

import xyz.ravenbs.module.ModuleManager;
import xyz.ravenbs.module.impl.combat.STap;
import xyz.ravenbs.module.impl.player.DelayRemover;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftClient.class)
public class MixinMinecraftClient {
    @Shadow
    private int itemUseCooldown;
    
    @Shadow
    public ClientPlayerEntity player;

    @Shadow
    public HitResult crosshairTarget;
    
    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        // FastPlace
        if (ModuleManager.fastPlace != null && ModuleManager.fastPlace.isEnabled()) {
            if (this.player != null && this.player.getMainHandStack().getItem() instanceof net.minecraft.item.BlockItem) {
                this.itemUseCooldown = (int) ModuleManager.fastPlace.getDelay();
            }
        }
        
        // DelayRemover - click delay
        if (ModuleManager.getModule(DelayRemover.class) != null && 
            ModuleManager.getModule(DelayRemover.class).isEnabled()) {
            
            if (DelayRemover.clickDelay != null && DelayRemover.clickDelay.isToggled()) {
                this.itemUseCooldown = 0;
            }
        }
    }
    
    // Remove attack cooldown (1.9+ attack cooldown)
    @Inject(method = "doAttack", at = @At("HEAD"))
    private void onDoAttackHead(CallbackInfoReturnable<Boolean> cir) {
        if (player != null && ModuleManager.getModule(DelayRemover.class) != null && 
            ModuleManager.getModule(DelayRemover.class).isEnabled() &&
            DelayRemover.hitDelay != null && DelayRemover.hitDelay.isToggled()) {
            // Reset attack cooldown
            player.resetLastAttackedTicks();
        }
    }

    @Inject(method = "doAttack", at = @At("RETURN"))
    private void onDoAttackReturn(CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValueZ() || !(crosshairTarget instanceof EntityHitResult entityHitResult)) {
            return;
        }

        STap sTap = (STap) ModuleManager.getModule(STap.class);
        if (sTap != null && sTap.isEnabled()) {
            sTap.onAttack(entityHitResult.getEntity());
        }
    }
}

