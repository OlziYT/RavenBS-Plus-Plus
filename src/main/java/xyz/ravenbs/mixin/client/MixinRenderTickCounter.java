package xyz.ravenbs.mixin.client;

import xyz.ravenbs.module.ModuleManager;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(RenderTickCounter.class)
public class MixinRenderTickCounter {
    @Shadow public float lastFrameDuration;

    @Shadow private float tickTime;

    @Redirect(
            method = "beginRenderTick",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/client/render/RenderTickCounter;tickTime:F"
            )
    )
    private float ravenbs$scaleTickTime(RenderTickCounter instance) {
        float effectiveTickTime = this.tickTime;
        if (ModuleManager.timer != null && ModuleManager.timer.isEnabled()) {
            float speed = (float) xyz.ravenbs.module.impl.movement.Timer.speed.getInput();
            if (speed > 0.0f) {
                effectiveTickTime /= speed;
            }
        }
        return Math.max(0.001f, effectiveTickTime);
    }
}
