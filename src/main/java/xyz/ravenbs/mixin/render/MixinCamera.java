package xyz.ravenbs.mixin.render;

import xyz.ravenbs.module.ModuleManager;
import xyz.ravenbs.module.impl.render.ExtendCamera;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Camera.class)
public class MixinCamera {
    @Inject(method = "clipToSpace", at = @At("HEAD"), cancellable = true)
    private void onClipToSpace(double desiredCameraDistance, CallbackInfoReturnable<Double> cir) {
        if (ModuleManager.noCameraClip != null && ModuleManager.noCameraClip.isEnabled()) {
            cir.setReturnValue(desiredCameraDistance);
        }
    }
    
    // ExtendCamera - modify third person distance
    @ModifyArg(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Camera;moveBy(DDD)V", ordinal = 0), index = 0)
    private double modifyThirdPersonDistance(double original) {
        if (ModuleManager.getModule(ExtendCamera.class) != null && 
            ModuleManager.getModule(ExtendCamera.class).isEnabled()) {
            return -ExtendCamera.getDistance();
        }
        return original;
    }
}

