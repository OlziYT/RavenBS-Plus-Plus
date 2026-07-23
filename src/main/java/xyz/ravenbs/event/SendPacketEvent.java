package xyz.ravenbs.event;

import net.minecraft.network.packet.Packet;

public class SendPacketEvent {
    private Packet<?> packet;
    private boolean cancelled;

    public SendPacketEvent(Packet<?> packet) {
        this.packet = packet;
    }

    public Packet<?> getPacket() {
        return packet;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}
