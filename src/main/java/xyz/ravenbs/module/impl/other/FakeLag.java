package xyz.ravenbs.module.impl.other;

import xyz.ravenbs.event.SendPacketEvent;
import xyz.ravenbs.module.Module;
import xyz.ravenbs.module.ModuleCategory;
import xyz.ravenbs.module.setting.impl.SliderSetting;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.KeepAliveC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class FakeLag extends Module {
    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger("RavenBS/FakeLag");
    private SliderSetting delay;
    public static final Queue<Packet<?>> packetQueue = new ConcurrentLinkedQueue<>();
    private long lastSend;

    public FakeLag() {
        super("FakeLag", ModuleCategory.other);
        this.registerSetting(delay = new SliderSetting("Delay ms", 200, 50, 5000, 50));
    }

    private boolean ignoring = false;

    @Override
    public void onDisable() {
        sendQueue();
    }
    
    @Override
    public void onUpdate() {
        if (System.currentTimeMillis() - lastSend > delay.getInput()) {
            sendQueue();
            lastSend = System.currentTimeMillis();
        }
    }
    
    @Override
    public void onSendPacket(SendPacketEvent e) {
        if (ignoring) return;
        
        Packet<?> p = e.getPacket();
        // Don't delay KeepAlive if possible to avoid timeout, OR delay it to simulate lag properly?
        // Usually FakeLag chokes movement packets.
        if (p instanceof PlayerMoveC2SPacket || p instanceof KeepAliveC2SPacket) {
            e.setCancelled(true);
            packetQueue.add(p);
        }
    }
    
    private void sendQueue() {
        if (mc.getNetworkHandler() == null) {
            packetQueue.clear();
            return;
        }
        
        ignoring = true;
        try {
            while (!packetQueue.isEmpty()) {
                Packet<?> packet = packetQueue.poll();
                if (packet != null) {
                    mc.getNetworkHandler().getConnection().send(packet, null);
                }
            }
        } catch (Exception e) {
            LOGGER.error("Failed to flush fake lag queue", e);
        }
        ignoring = false;
    }
}
