package xyz.ravenbs.module.impl.render;

import xyz.ravenbs.module.Module;
import xyz.ravenbs.module.ModuleCategory;
import xyz.ravenbs.module.setting.impl.ButtonSetting;
import xyz.ravenbs.module.setting.impl.SliderSetting;
import xyz.ravenbs.utility.Utils;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;

public class Arrows extends Module {
    private SliderSetting radius;
    private ButtonSetting hideTeammates;
    private ButtonSetting renderDistance;

    public Arrows() {
        super("Arrows", ModuleCategory.render);
        this.registerSetting(radius = new SliderSetting("Circle radius", 50, 30, 200, 5));
        this.registerSetting(hideTeammates = new ButtonSetting("Hide teammates", true));
        this.registerSetting(renderDistance = new ButtonSetting("Render distance", true));
    }

    @Override
    public void onRender(DrawContext context, float tickDelta) {
        if (mc.player == null || mc.world == null) return;
        
        int screenWidth = mc.getWindow().getScaledWidth();
        int screenHeight = mc.getWindow().getScaledHeight();
        int centerX = screenWidth / 2;
        int centerY = screenHeight / 2;
        
        for (PlayerEntity player : mc.world.getPlayers()) {
            if (player == mc.player) continue;
            if (player.isDead()) continue;
            if (hideTeammates.isToggled() && Utils.isTeamMate(player)) continue;
            if (xyz.ravenbs.module.impl.world.AntiBot.isBot(player)) continue;
            
            // Calculate direction to player
            Vec3d playerPos = player.getPos();
            Vec3d selfPos = mc.player.getPos();
            
            double dx = playerPos.x - selfPos.x;
            double dz = playerPos.z - selfPos.z;
            
            // Calculate angle relative to player's look direction
            double angle = Math.atan2(dz, dx);
            double playerYaw = Math.toRadians(mc.player.getYaw() + 90);
            double relativeAngle = angle - playerYaw;
            
            // Calculate position on circle
            int r = (int) radius.getInput();
            int arrowX = centerX + (int) (Math.cos(relativeAngle) * r);
            int arrowY = centerY + (int) (Math.sin(relativeAngle) * r);
            
            // Draw arrow (simple triangle)
            int color = xyz.ravenbs.utility.FriendManager.isFriend(player)
                ? 0xFF00FF00 : 0xFFFF0000;
            
            context.fill(arrowX - 3, arrowY - 3, arrowX + 3, arrowY + 3, color);
            
            // Draw distance
            if (renderDistance.isToggled()) {
                int dist = (int) mc.player.distanceTo(player);
                String text = dist + "m";
                context.drawText(mc.textRenderer, text, arrowX - mc.textRenderer.getWidth(text) / 2, arrowY + 5, 0xFFFFFFFF, true);
            }
        }
    }
}
