package xyz.ravenbs.mixin.client;

import com.mojang.authlib.GameProfile;
import xyz.ravenbs.event.PostMotionEvent;
import xyz.ravenbs.event.PreMotionEvent;
import xyz.ravenbs.module.ModuleManager;
import xyz.ravenbs.utility.RotationUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public abstract class MixinClientPlayerEntity extends AbstractClientPlayerEntity {

    @Shadow @Final public ClientPlayNetworkHandler networkHandler;
    @Shadow protected abstract void sendMovementPackets();
    @Shadow private boolean autoJumpEnabled;
    @Shadow public abstract boolean isSneaking();
    @Shadow public abstract boolean isUsingItem();

    public MixinClientPlayerEntity(ClientWorld world, GameProfile profile) {
        super(world, profile);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTickPre(CallbackInfo ci) {
        ModuleManager.onPreUpdate();
    }

    @Inject(method = "tick", at = @At("RETURN"))
    private void onTickPost(CallbackInfo ci) {
        ModuleManager.onPostUpdate();
    }

    // Redirecting the call to sendMovementPackets to inject our logic
    // This assumes sendMovementPackets is called in tick().
    // Getting getting precise control over packet sending is tricky with just @Inject.
    // We will use a Redirect or just Overwrite if necessary, but Redirect/Inject is safer.
    
    private float storedYaw;
    private float storedPitch;
    private boolean storedOnGround;

    @Inject(method = "sendMovementPackets", at = @At("HEAD"), cancellable = true)
    private void onSendMovementPackets(CallbackInfo ci) {
        // Prevent recursive calls if we triggered this ourselves (unlikely here but good practice)
        
        // Store original visuals
        storedYaw = this.getYaw();
        storedPitch = this.getPitch();

        // Create PreMotionEvent
        PreMotionEvent event = new PreMotionEvent(
            this.getX(),
            this.getBoundingBox().minY,
            this.getZ(),
            storedYaw,
            storedPitch,
            this.isOnGround(),
            this.isSprinting(),
            this.isSneaking()
        );

        // Fire event
        ModuleManager.onPreMotion(event);

        if (event.isCancelled()) {
            ci.cancel();
            return;
        }

        // Apply Silent Rotations: Set player yaw/pitch to event values
        this.setYaw(event.getYaw());
        this.setPitch(event.getPitch());
        
        // Apply ground state (Fixes NoFall)
        boolean originalOnGround = this.isOnGround();
        this.setOnGround(event.isOnGround());
        this.storedOnGround = originalOnGround;

        // Update RotationUtils
        RotationUtils.serverRotations[0] = event.getYaw();
        RotationUtils.serverRotations[1] = event.getPitch();
    }

    @Inject(method = "sendMovementPackets", at = @At("RETURN"))
    private void onSendMovementPacketsPost(CallbackInfo ci) {
        // Restore visuals
        this.setYaw(storedYaw);
        this.setPitch(storedPitch);
        
        // Restore ground state
        this.setOnGround(storedOnGround);
        
        ModuleManager.onPostMotion(new PostMotionEvent());
    }
    
    @Redirect(method = "tickMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;isUsingItem()Z"))
    private boolean redirectIsUsingItem(ClientPlayerEntity instance) {
        if (ModuleManager.noSlow != null && ModuleManager.noSlow.isEnabled()) {
            return false; // Pretend we are not using item so we don't slow down
        }
        return instance.isUsingItem();
    }

    @Override
    public boolean clipAtLedge() {
        // SafeWalk Implementation
        // In 1.20, ClientPlayerEntity overrides this method (which likely comes from PlayerEntity).
        // By overriding it here in the Mixin (which merges into the class), we can control the return value.
        // Returning true prevents falling off edges (like sneaking).
        
        try {
            boolean safe = false;
            if (ModuleManager.safeWalk != null && ModuleManager.safeWalk.isEnabled()) {
                safe = true;
            }
            if (!safe && ModuleManager.scaffold != null && ModuleManager.scaffold.isEnabled() && ModuleManager.scaffold.getSafeWalk()) {
                safe = true;
            }
            
            if (safe) return true;
        } catch (Exception ignored) {}
        
        return super.clipAtLedge();
    }
}
