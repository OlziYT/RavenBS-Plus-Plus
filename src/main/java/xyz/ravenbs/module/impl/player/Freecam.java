package xyz.ravenbs.module.impl.player;

import xyz.ravenbs.event.SendPacketEvent;
import xyz.ravenbs.module.Module;
import xyz.ravenbs.module.ModuleCategory;
import xyz.ravenbs.module.setting.impl.ButtonSetting;
import xyz.ravenbs.module.setting.impl.SliderSetting;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

public class Freecam extends Module {
    private SliderSetting speed;
    private ButtonSetting showArm;
    
    private OtherClientPlayerEntity dummy;
    private double oldX, oldY, oldZ;
    private float oldYaw, oldPitch;
    private boolean oldFlying;
    private float oldFlySpeed;

    public Freecam() {
        super("Freecam", ModuleCategory.player);
        this.registerSetting(speed = new SliderSetting("Speed", 1, 0.1, 5, 0.1));
        this.registerSetting(showArm = new ButtonSetting("Show Arm", false));
    }

    @Override
    public void onEnable() {
        if (mc.player == null || mc.world == null) return;
        
        // Save position
        oldX = mc.player.getX();
        oldY = mc.player.getY();
        oldZ = mc.player.getZ();
        oldYaw = mc.player.getYaw();
        oldPitch = mc.player.getPitch();
        oldFlying = mc.player.getAbilities().flying;
        oldFlySpeed = mc.player.getAbilities().getFlySpeed();

        // Create dummy
        dummy = new OtherClientPlayerEntity(mc.world, mc.player.getGameProfile());
        dummy.copyPositionAndRotation(mc.player);
        dummy.setHeadYaw(mc.player.getHeadYaw());
        dummy.getInventory().clone(mc.player.getInventory());
        mc.world.addEntity(-100, dummy);
        
        // In 1.20, we don't switch RenderViewEntity usually for Freecam, 
        // we keep the player as the controller but change input logic to "fly" noclip.
        // Actually, classic Freecam:
        // 1. Leave a dummy at original pos.
        // 2. Player entity becomes "ghost" (noclip, fly).
        
        // But if we want the camera to detach from the body...
        // Fabric way: Use Camera Mixin or just move the player client-side only (cancel movement packets).
        
    }

    @Override
    public void onDisable() {
        if (mc.player != null) {
            mc.player.setPosition(oldX, oldY, oldZ);
            mc.player.setYaw(oldYaw);
            mc.player.setPitch(oldPitch);
            mc.player.setVelocity(0, 0, 0);
            mc.player.getAbilities().flying = oldFlying;
            mc.player.getAbilities().setFlySpeed(oldFlySpeed);
        }

        if (dummy != null && mc.world != null) {
            mc.world.removeEntity(dummy.getId(), Entity.RemovalReason.DISCARDED);
            dummy = null;
        }
    }
    
    @Override
    public void onUpdate() {
        if (mc.player == null) return;
        
        // Fly logic
        mc.player.getAbilities().flying = true;
        mc.player.getAbilities().setFlySpeed((float)(speed.getInput() / 20.0));
        mc.player.setOnGround(false);
        
        // Prevent packets from sending? (Blink logic)
    }

    @Override
    public void onSendPacket(SendPacketEvent e) {
        if (e.getPacket() instanceof PlayerMoveC2SPacket) {
            e.setCancelled(true); // Don't send movement to server
        }
    }
}
