package xyz.ravenbs.mixin.movement;

import xyz.ravenbs.module.ModuleManager;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class MixinEntity {

    // In 1.20, movement logic often calls adjustMovementForCollisions. 
    // And inside there, it checks for sneaking to avoid falling.
    // We can redirect the "isSneaking" check or similar.
    // But a more robust way often used in fabric hacks is to inject into `adjustMovementForCollisions`
    // and if it's the player, wrap the operation as if sneaking.
    
    // However, finding the exact injection point for "sneak to avoid fall" in `adjustMovementForCollisions` can be tricky without seeing code.
    // Usually it calls `this.onGround && this.isSneaking() && ...`
    
    // Let's try to inject into `isSneaking` (or `isDescending` / `clipAtLedge` method if it exists).
    // In Yarn, `clipAtLedge` might be the method.
    
    // If we can't find `clipAtLedge`, we can try `adjustMovementForCollisions`.
    // Let's use a broader approach: Redirecting `isSneaking` inside `adjustMovementForCollisions` is risky because it affects other things.
    
    // Simple approach: Mixin into `adjustMovementForCollisions` (which takes Vec3d movement)
    // and if SafeWalk is on and entity is player and onGround, we modify the behavior.
    
    // Actually, Mojang code often has `if (this.onGround && this.isSneaking() && this instanceof PlayerEntity)` block for edge clipping.
    // We can Redirect `isSneaking` specifically in that method to return true.
    
    // SafeWalk Implementation removed from here due to mixin failures.
    // Logic moved to MixinClientPlayerEntity#clipAtLedge
    
    @Inject(method = "getTargetingMargin", at = @At("HEAD"), cancellable = true)
    private void onGetTargetingMargin(CallbackInfoReturnable<Float> cir) {
        if (ModuleManager.hitBox != null && ModuleManager.hitBox.isEnabled()) {
            cir.setReturnValue((float) xyz.ravenbs.module.impl.combat.HitBox.expand.getInput());
        }
    }
}
