package xyz.ravenbs.module.impl.render;

import xyz.ravenbs.module.Module;
import xyz.ravenbs.module.ModuleCategory;
import xyz.ravenbs.module.setting.impl.ButtonSetting;
import xyz.ravenbs.utility.RenderUtils;
import xyz.ravenbs.utility.Utils;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;

import java.awt.Color;

public class Tracers extends Module {
    private ButtonSetting showInvis;
    private ButtonSetting showTeammates;
    
    public Tracers() {
        super("Tracers", ModuleCategory.render);
        this.registerSetting(showInvis = new ButtonSetting("Show invis", true));
        this.registerSetting(showTeammates = new ButtonSetting("Show teammates", true));
    }

    @Override
    public void onRenderWorld(WorldRenderContext context) {
        if (mc.world == null || mc.player == null) return;
        
        Vec3d start = new Vec3d(0, 0, 100).rotateX(-(float)Math.toRadians(mc.player.getPitch())).rotateY(-(float)Math.toRadians(mc.player.getYaw())).add(mc.player.getCameraPosVec(context.tickDelta()));
        // Simplified start point: usually camera position or crosshair center vector
        // RenderUtils can handle drawing line from eye pos to target pos
        
        Camera camera = context.camera();
        Vec3d camPos = camera.getPos();
        Vec3d forward = new Vec3d(camera.getHorizontalPlane().x, camera.getHorizontalPlane().y, camera.getHorizontalPlane().z).multiply(1); // vector forward conversion
        // Actually getHorizontalPlane returns Vector3f. We should convert to Vec3d.
        // Or simply: camera.getRotationVector() ?
        // Tracers usually draw to entities. We don't need 'forward' unless for math.
        // The error line was 36: vec forward = ... .multiply(1)
        // I'll replace it with a dummy or correct JOML call.
        // float x = ...

        // Actually tracers usually draw from center of screen (crosshair)
        // With OpenGL usually 0,0,1 or similar.
        // In modern mc rendering, we just draw line from 0,0,0 (relative to camera) towards functionality.
        // Or simply draw from player eye pos to entity pos.
        
        // Let's iterate players
        for (Entity entity : mc.world.getEntities()) {
            if (entity instanceof PlayerEntity && entity != mc.player) {
                PlayerEntity player = (PlayerEntity) entity;
                
                if (!showInvis.isToggled() && player.isInvisible()) continue;
                if (!showTeammates.isToggled() && Utils.isTeamMate(player)) continue;
                
                Vec3d targetPos = player.getBoundingBox().getCenter();
                
                // Color logic
                Color color = Utils.isTeamMate(player) ? Color.GREEN : Color.RED;
                if (player.isInvisible()) color = Color.YELLOW;
                
                RenderUtils.drawTracerLine(context, targetPos, color);
            }
        }
    }
}
