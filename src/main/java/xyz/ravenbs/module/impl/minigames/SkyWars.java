package xyz.ravenbs.module.impl.minigames;

import xyz.ravenbs.module.Module;
import xyz.ravenbs.module.ModuleCategory;
import xyz.ravenbs.module.setting.impl.ButtonSetting;
import xyz.ravenbs.module.setting.impl.DescriptionSetting;
import net.minecraft.client.gui.DrawContext;
import java.awt.Color;

public class SkyWars extends Module {
    private ButtonSetting refillAlert;
    private ButtonSetting autoWarp;
    
    public SkyWars() {
        super("SkyWars", ModuleCategory.minigames);
        this.registerSetting(new DescriptionSetting("SkyWars Utilities"));
        this.registerSetting(refillAlert = new ButtonSetting("Refill Alert", true));
        this.registerSetting(autoWarp = new ButtonSetting("Auto Warp", false));
    }

    @Override
    public void onReceivePacket(xyz.ravenbs.event.ReceivePacketEvent e) {
        if (e.getPacket() instanceof net.minecraft.network.packet.s2c.play.GameMessageS2CPacket) {
            net.minecraft.network.packet.s2c.play.GameMessageS2CPacket packet = (net.minecraft.network.packet.s2c.play.GameMessageS2CPacket) e.getPacket();
            String msg = packet.content().getString();
            
            if (refillAlert.isToggled() && msg.contains("Chests have been refilled")) {
                 xyz.ravenbs.utility.NotificationManager.show("SkyWars", "Chests Refilled!", xyz.ravenbs.utility.Notification.Type.INFO, 3000);
                 if (mc.player != null) mc.player.playSound(net.minecraft.sound.SoundEvents.BLOCK_NOTE_BLOCK_PLING.value(), 1.0f, 1.0f);
            }
            
            if (autoWarp.isToggled()) {
                if (msg.contains("You died!") || msg.contains("You won!")) {
                     new Thread(() -> {
                        try {
                            Thread.sleep(2000);
                            if (mc.player != null) mc.player.networkHandler.sendChatCommand("play_skywars_insane"); 
                        } catch (InterruptedException ex) {}
                    }).start();
                }
            }
        }
    }
}
