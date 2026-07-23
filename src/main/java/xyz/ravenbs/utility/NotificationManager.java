package xyz.ravenbs.utility;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class NotificationManager {
    private static final List<Notification> notifications = new ArrayList<>();
    
    public static void show(String title, String message, Notification.Type type, long duration) {
        notifications.add(new Notification(title, message, type, duration));
    }
    
    public static void show(String title, String message, Notification.Type type) {
        show(title, message, type, 2000);
    }
    
    public static void render(DrawContext context) {
        if (notifications.isEmpty()) return;
        
        MinecraftClient mc = MinecraftClient.getInstance();
        int width = mc.getWindow().getScaledWidth();
        int height = mc.getWindow().getScaledHeight();
        
        int yOffset = height - 50;
        
        Iterator<Notification> it = notifications.iterator();
        while (it.hasNext()) {
            Notification n = it.next();
            if (n.isExpired()) {
                it.remove();
                continue;
            }
            
            renderNotification(context, n, width, yOffset);
            yOffset -= 35; // Space between notifications
        }
    }
    
    private static void renderNotification(DrawContext context, Notification n, int screenWidth, int y) {
        int w = 120;
        int h = 30;
        int x = screenWidth - w - 10;
        
        // Animation
        double progress = n.getProgress();
        double animation;
        if (progress < 0.1) {
            animation = progress / 0.1; // Fade in
        } else if (progress > 0.9) {
            animation = (1 - progress) / 0.1; // Fade out
        } else {
            animation = 1;
        }
        
        // Slide animation?
        int drawX = x + (int)((1 - animation) * (w + 20));
        
        // Background
        int color = 0xAA000000; // Black transparent
        context.fill(drawX, y, drawX + w, y + h, color);
        
        // Type color strip
        int typeColor = 0xFFFFFFFF;
        switch (n.getType()) {
            case INFO: typeColor = 0xFF55FF55; break; // Green
            case WARNING: typeColor = 0xFFFFFF55; break; // Yellow
            case ERROR: typeColor = 0xFFFF5555; break; // Red
            case SUCCESS: typeColor = 0xFF55FFFF; break; // Aqua
        }
        context.fill(drawX, y, drawX + 2, y + h, typeColor);
        
        // Text
        context.drawText(MinecraftClient.getInstance().textRenderer, n.getTitle(), drawX + 6, y + 4, typeColor, false);
        context.drawText(MinecraftClient.getInstance().textRenderer, n.getMessage(), drawX + 6, y + 16, 0xFFDDDDDD, false);
    }
}
