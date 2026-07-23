package xyz.ravenbs.module.impl.other;

import xyz.ravenbs.event.ReceivePacketEvent;
import xyz.ravenbs.event.SendPacketEvent;
import xyz.ravenbs.module.Module;
import xyz.ravenbs.module.ModuleCategory;
import xyz.ravenbs.module.setting.impl.ButtonSetting;
import net.minecraft.network.packet.Packet;
import net.minecraft.text.Text;

public class ViewPackets extends Module {
    private ButtonSetting sent;
    private ButtonSetting received;
    private ButtonSetting ignoreMovement;
    private long tick = 0;

    public ViewPackets() {
        super("ViewPackets", ModuleCategory.other);
        this.registerSetting(sent = new ButtonSetting("Sent", true));
        this.registerSetting(received = new ButtonSetting("Received", false));
        this.registerSetting(ignoreMovement = new ButtonSetting("Ignore movement", true));
    }

    @Override
    public void onDisable() {
        tick = 0;
    }

    @Override
    public void onUpdate() {
        tick++;
    }

    @Override
    public void onSendPacket(SendPacketEvent e) {
        if (!sent.isToggled()) return;
        
        Packet<?> packet = e.getPacket();
        String name = packet.getClass().getSimpleName();
        
        if (ignoreMovement.isToggled() && name.contains("Player")) return;
        
        String msg = "§7[§dR§7]§r §aSent§7: §d" + name + " §7(t:§b" + tick + "§7)";
        mc.player.sendMessage(Text.of(msg), false);
    }

    @Override
    public void onReceivePacket(ReceivePacketEvent e) {
        if (!received.isToggled()) return;
        
        Packet<?> packet = e.getPacket();
        String name = packet.getClass().getSimpleName();
        
        String msg = "§7[§dR§7]§r §cReceived§7: §d" + name + " §7(t:§b" + tick + "§7)";
        mc.player.sendMessage(Text.of(msg), false);
    }
}
