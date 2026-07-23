package xyz.ravenbs.module.impl.player;

import xyz.ravenbs.event.SendPacketEvent;
import xyz.ravenbs.module.Module;
import xyz.ravenbs.module.ModuleCategory;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Blink extends Module {
    private final Queue<Packet<?>> packets = new ConcurrentLinkedQueue<>();
    private boolean flushing;
    
    public Blink() {
        super("Blink", ModuleCategory.player);
    }
    
    @Override
    public void onEnable() {
        flushing = false;
        packets.clear();
    }

    @Override
    public void onDisable() {
        if (mc.getNetworkHandler() == null) {
            packets.clear();
            return;
        }

        flushing = true;
        try {
            while (!packets.isEmpty()) {
                Packet<?> packet = packets.poll();
                if (packet != null) {
                    mc.getNetworkHandler().getConnection().send(packet, null);
                }
            }
        } finally {
            flushing = false;
        }
    }

    @Override
    public void onSendPacket(SendPacketEvent e) {
        if (flushing) {
            return;
        }

        if (e.getPacket() instanceof PlayerMoveC2SPacket) {
            e.setCancelled(true);
            packets.add(e.getPacket());
        }
    }
}
