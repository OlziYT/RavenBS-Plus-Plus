package xyz.ravenbs.module.impl.other;

import xyz.ravenbs.module.Module;
import xyz.ravenbs.module.ModuleCategory;
import xyz.ravenbs.event.SendPacketEvent;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;

public class ChatBypass extends Module {
    public ChatBypass() {
        super("ChatBypass", ModuleCategory.other);
    }

    @Override
    public void onSendPacket(SendPacketEvent e) {
        if (e.getPacket() instanceof ChatMessageC2SPacket) {
            ChatMessageC2SPacket packet = (ChatMessageC2SPacket) e.getPacket();
            String msg = packet.chatMessage();
            if (msg.startsWith("/")) return;
            
            // Bypass logic: insert invisible chars?
            // StringBuilder sb = new StringBuilder();
            // ...
            // e.setPacket(new ChatMessageC2SPacket(sb.toString(), ...));
        }
    }
}
