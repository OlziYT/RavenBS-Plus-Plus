package xyz.ravenbs.module.impl.other;

import xyz.ravenbs.module.Module;
import xyz.ravenbs.module.ModuleCategory;
import xyz.ravenbs.module.setting.impl.SliderSetting;
import xyz.ravenbs.utility.NotificationManager;
import xyz.ravenbs.utility.Notification;
import net.minecraft.client.network.PlayerListEntry;

public class LatencyAlerts extends Module {
    private SliderSetting threshold;
    private long lastAlert = 0;

    public LatencyAlerts() {
        super("LatencyAlerts", ModuleCategory.other);
        this.registerSetting(threshold = new SliderSetting("Threshold", 100, 50, 500, 10));
    }

    @Override
    public void onUpdate() {
        if (mc.player == null || mc.getNetworkHandler() == null) return;
        
        PlayerListEntry entry = mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());
        if (entry == null) return;
        
        int ping = entry.getLatency();
        
        if (ping > threshold.getInput() && System.currentTimeMillis() - lastAlert > 5000) {
            NotificationManager.show("Latency", "High ping: " + ping + "ms", Notification.Type.WARNING, 3000);
            lastAlert = System.currentTimeMillis();
        }
    }
}
