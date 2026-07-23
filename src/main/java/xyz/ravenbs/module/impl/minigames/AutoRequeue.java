package xyz.ravenbs.module.impl.minigames;

import xyz.ravenbs.module.Module;
import xyz.ravenbs.module.ModuleCategory;
import xyz.ravenbs.module.setting.impl.DescriptionSetting;
import xyz.ravenbs.module.setting.impl.SliderSetting;
import xyz.ravenbs.event.ReceivePacketEvent;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;

public class AutoRequeue extends Module {
    private SliderSetting delay;
    
    public AutoRequeue() {
        super("AutoRequeue", ModuleCategory.minigames);
        this.registerSetting(delay = new SliderSetting("Delay sec", 0, 0, 5, 0.5));
    }

    @Override
    public void onReceivePacket(ReceivePacketEvent e) {
        if (e.getPacket() instanceof GameMessageS2CPacket) {
            GameMessageS2CPacket packet = (GameMessageS2CPacket) e.getPacket();
            String msg = packet.content().getString();
            
            // Common Hypixel Triggers
            if (msg.contains("You died!") || msg.contains("Game Over") || msg.contains("Victory!")) {
                new Thread(() -> {
                    try {
                        Thread.sleep((long)(delay.getInput() * 1000));
                        if (mc.player != null) {
                            mc.player.networkHandler.sendChatCommand("play bedwars_eight_two"); // Default to BW doubles
                        }
                    } catch (InterruptedException ex) {}
                }).start();
            }
        }
    }
}
