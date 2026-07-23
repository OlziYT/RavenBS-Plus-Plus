package xyz.ravenbs.module.impl.minigames;

import xyz.ravenbs.module.Module;
import xyz.ravenbs.module.ModuleCategory;
import xyz.ravenbs.module.setting.impl.ButtonSetting;
import xyz.ravenbs.module.setting.impl.DescriptionSetting;
import net.minecraft.item.Items;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

import java.awt.Color;

public class BedWars extends Module {
    private ButtonSetting resourcesHud;
    private ButtonSetting alerts;
    private ButtonSetting autoRequeue;
    
    public BedWars() {
        super("BedWars", ModuleCategory.minigames);
        this.registerSetting(new DescriptionSetting("BedWars Utilities"));
        this.registerSetting(resourcesHud = new ButtonSetting("Resources HUD", true));
        this.registerSetting(alerts = new ButtonSetting("Chat Alerts", true));
        this.registerSetting(autoRequeue = new ButtonSetting("Auto Requeue", false));
    }

    @Override
    public void onRender(DrawContext context, float tickDelta) {
        if (!resourcesHud.isToggled() || mc.player == null) return;
        
        // Simple counts
        int iron = 0;
        int gold = 0;
        int diamond = 0;
        int emerald = 0;
        
        for (int i = 0; i < mc.player.getInventory().size(); i++) {
            net.minecraft.item.ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.getItem() == Items.IRON_INGOT) iron += stack.getCount();
            else if (stack.getItem() == Items.GOLD_INGOT) gold += stack.getCount();
            else if (stack.getItem() == Items.DIAMOND) diamond += stack.getCount();
            else if (stack.getItem() == Items.EMERALD) emerald += stack.getCount();
        }
        
        if (iron == 0 && gold == 0 && diamond == 0 && emerald == 0) return;
        
        int x = 5;
        int y = mc.getWindow().getScaledHeight() / 2; // Center-ish
        
        // Draw
        int offset = 0;
        if (iron > 0) {
            context.drawItem(new net.minecraft.item.ItemStack(Items.IRON_INGOT), x, y + offset);
            context.drawText(mc.textRenderer, String.valueOf(iron), x + 18, y + offset + 4, -1, true);
            offset += 18;
        }
        if (gold > 0) {
            context.drawItem(new net.minecraft.item.ItemStack(Items.GOLD_INGOT), x, y + offset);
            context.drawText(mc.textRenderer, String.valueOf(gold), x + 18, y + offset + 4, -1, true);
            offset += 18;
        }
        if (diamond > 0) {
            context.drawItem(new net.minecraft.item.ItemStack(Items.DIAMOND), x, y + offset);
            context.drawText(mc.textRenderer, String.valueOf(diamond), x + 18, y + offset + 4, -1, true);
            offset += 18;
        }
        if (emerald > 0) {
            context.drawItem(new net.minecraft.item.ItemStack(Items.EMERALD), x, y + offset);
            context.drawText(mc.textRenderer, String.valueOf(emerald), x + 18, y + offset + 4, -1, true);
            offset += 18;
        }
    }

    // Chat parsing handled in generic event listener or packet hook elsewhere?
    // ModuleManager has onSendPacket, but usually we intercept ReceivePacket for chat.
    // Let's implement onReceivePacket in base Module or mixin.
    // Assuming Module has onReceivePacket from previous steps.
    
    @Override
    public void onReceivePacket(xyz.ravenbs.event.ReceivePacketEvent e) {
        if (e.getPacket() instanceof net.minecraft.network.packet.s2c.play.GameMessageS2CPacket) {
            net.minecraft.network.packet.s2c.play.GameMessageS2CPacket packet = (net.minecraft.network.packet.s2c.play.GameMessageS2CPacket) e.getPacket();
            String msg = packet.content().getString();
            
            if (autoRequeue.isToggled()) {
                if (msg.contains("Victory!") || msg.contains("Game Over!")) {
                    new Thread(() -> {
                        try {
                            Thread.sleep(3000);
                            if (mc.player != null) mc.player.networkHandler.sendChatCommand("play_bedwars_four_four"); // Example logic
                        } catch (InterruptedException ex) {}
                    }).start();
                }
            }
            
            if (alerts.isToggled()) {
                 if (msg.contains("Bed is destroyed")) { // Hypixel specific
                     xyz.ravenbs.utility.NotificationManager.show("BedWars", "Bed Destroyed!", xyz.ravenbs.utility.Notification.Type.WARNING, 2000);
                 }
            }
        }
    }
}
