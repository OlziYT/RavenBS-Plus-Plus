package xyz.ravenbs.module.impl.render;

import xyz.ravenbs.module.Module;
import xyz.ravenbs.module.ModuleCategory;
import xyz.ravenbs.module.setting.impl.ButtonSetting;
import xyz.ravenbs.module.setting.impl.SliderSetting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.thrown.EggEntity;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.entity.projectile.thrown.SnowballEntity;
import net.minecraft.util.math.Vec3d;

public class Indicators extends Module {
    private ButtonSetting renderArrows;
    private ButtonSetting renderPearls;
    private ButtonSetting renderEggs;
    private ButtonSetting renderSnowballs;
    private SliderSetting radius;
    private ButtonSetting renderDistance;

    public Indicators() {
        super("Indicators", ModuleCategory.render);
        this.registerSetting(renderArrows = new ButtonSetting("Arrows", true));
        this.registerSetting(renderPearls = new ButtonSetting("Ender pearls", true));
        this.registerSetting(renderEggs = new ButtonSetting("Eggs", false));
        this.registerSetting(renderSnowballs = new ButtonSetting("Snowballs", false));
        this.registerSetting(radius = new SliderSetting("Circle radius", 50, 30, 200, 5));
        this.registerSetting(renderDistance = new ButtonSetting("Render distance", true));
    }

    @Override
    public void onRender(DrawContext context, float tickDelta) {
        if (mc.player == null || mc.world == null) return;
        
        int screenWidth = mc.getWindow().getScaledWidth();
        int screenHeight = mc.getWindow().getScaledHeight();
        int centerX = screenWidth / 2;
        int centerY = screenHeight / 2;
        
        for (Entity entity : mc.world.getEntities()) {
            if (!shouldRender(entity)) continue;
            
            // Calculate direction to entity
            Vec3d entityPos = entity.getPos();
            Vec3d selfPos = mc.player.getPos();
            
            double dx = entityPos.x - selfPos.x;
            double dz = entityPos.z - selfPos.z;
            
            // Calculate angle
            double angle = Math.atan2(dz, dx);
            double playerYaw = Math.toRadians(mc.player.getYaw()); // Math angle is South(90). Yaw 0 is South.
            // standard screen: Right(0).
            // We want South(90 map) to be Up(-90 screen).
            // shift = -180?
            // Let's use the proven +180 offset from analysis
            playerYaw = Math.toRadians(mc.player.getYaw() + 180);
            double relativeAngle = angle - playerYaw;
            
            // Position on circle
            int r = (int) radius.getInput();
            int indicatorX = centerX + (int) (Math.cos(relativeAngle) * r);
            int indicatorY = centerY + (int) (Math.sin(relativeAngle) * r);
            
            // Get color based on entity type
            int color = getColorForEntity(entity);
            
            // Draw indicator
            context.fill(indicatorX - 4, indicatorY - 4, indicatorX + 4, indicatorY + 4, color);
            
            // Draw distance
            if (renderDistance.isToggled()) {
                int dist = (int) mc.player.distanceTo(entity);
                String text = dist + "m";
                context.drawText(mc.textRenderer, text, indicatorX - mc.textRenderer.getWidth(text) / 2, indicatorY + 6, 0xFFFFFFFF, true);
            }
        }
    }
    
    private boolean shouldRender(Entity entity) {
        if (entity instanceof ArrowEntity && renderArrows.isToggled()) {
            ArrowEntity arrow = (ArrowEntity) entity;
            // In Fabric 1.20, check if arrow is in ground via velocity
            return arrow.getVelocity().lengthSquared() > 0.01;
        }
        if (entity instanceof EnderPearlEntity && renderPearls.isToggled()) return true;
        if (entity instanceof EggEntity && renderEggs.isToggled()) return true;
        if (entity instanceof SnowballEntity && renderSnowballs.isToggled()) return true;
        return false;
    }
    
    private int getColorForEntity(Entity entity) {
        if (entity instanceof ArrowEntity) return 0xFFFFFFFF;
        if (entity instanceof EnderPearlEntity) return 0xFFD200FF;
        if (entity instanceof EggEntity) return 0xFFFFEE9A;
        if (entity instanceof SnowballEntity) return 0xFFFFFFFF;
        return 0xFFFFFFFF;
    }
}
