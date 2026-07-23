package xyz.ravenbs.mixin.client;

import xyz.ravenbs.module.ModuleManager;
import xyz.ravenbs.module.impl.combat.Reach;
import xyz.ravenbs.module.impl.player.FastMine;
import xyz.ravenbs.module.impl.player.DelayRemover;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerInteractionManager.class)
public class MixinClientPlayerInteractionManager {
    @Shadow private float currentBreakingProgress;
    @Shadow private int blockBreakingCooldown;
    
    @Inject(method = "updateBlockBreakingProgress", at = @At("HEAD"))
    public void onUpdateBlockBreakingProgress(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        // FastBreak
        if (ModuleManager.fastBreak != null && ModuleManager.fastBreak.isEnabled()) {
            this.currentBreakingProgress += (float) (xyz.ravenbs.module.impl.world.FastBreak.speed.getInput() - 1.0);
        }
        
        // FastMine (additional speed multiplier)
        FastMine fastMine = (FastMine) ModuleManager.getModule(FastMine.class);
        if (fastMine != null && fastMine.isActiveNow()) {
            // Apply multiplier
            this.currentBreakingProgress += (float) ((fastMine.getMultiplier() - 1.0) * 0.1);
            
            // Apply delay reduction
            if (this.blockBreakingCooldown > fastMine.getDelay()) {
                this.blockBreakingCooldown = fastMine.getDelay();
            }
        }
        
        // DelayRemover - remove hit delay
        if (ModuleManager.getModule(DelayRemover.class) != null && 
            ModuleManager.getModule(DelayRemover.class).isEnabled() &&
            DelayRemover.hitDelay != null && DelayRemover.hitDelay.isToggled()) {
            this.blockBreakingCooldown = 0;
        }
    }

    @Inject(method = "getReachDistance", at = @At("RETURN"), cancellable = true)
    private void onGetReachDistance(CallbackInfoReturnable<Float> cir) {
        Reach reach = (Reach) ModuleManager.getModule(Reach.class);
        if (reach == null || !reach.isEnabled()) {
            return;
        }

        if (reach.isWeaponOnly() && !xyz.ravenbs.utility.Utils.isHoldingWeapon()) {
            return;
        }

        cir.setReturnValue((float) Reach.getReach());
    }
}

