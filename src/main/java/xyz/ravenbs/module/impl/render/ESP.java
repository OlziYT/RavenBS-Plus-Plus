package xyz.ravenbs.module.impl.render;

import xyz.ravenbs.module.Module;
import xyz.ravenbs.module.ModuleCategory;
import xyz.ravenbs.module.setting.impl.ButtonSetting;
import xyz.ravenbs.module.setting.impl.SliderSetting;
import xyz.ravenbs.utility.RenderUtils;
import xyz.ravenbs.utility.Utils;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

import java.awt.Color;

public class ESP extends Module {
    private ButtonSetting players;
    private ButtonSetting mobs;
    private ButtonSetting box;
    private xyz.ravenbs.module.setting.impl.ColorSetting color;
    
    public ESP() {
        super("ESP", ModuleCategory.render);
        this.registerSetting(players = new ButtonSetting("Players", true));
        this.registerSetting(mobs = new ButtonSetting("Mobs", false));
        this.registerSetting(box = new ButtonSetting("Box", true));
        this.registerSetting(color = new xyz.ravenbs.module.setting.impl.ColorSetting("Color", new Color(0, 255, 0)));
    }

    @Override
    public void onRenderWorld(WorldRenderContext context) {
        if (!box.isToggled()) return;
        
        for (Entity entity : mc.world.getEntities()) {
            if (entity == mc.player && mc.options.getPerspective().isFirstPerson()) continue;
            
            boolean render = false;
            
            if (entity instanceof PlayerEntity) {
                if (players.isToggled()) render = true;
            } else if (mobs.isToggled()) {
                render = true; // Simplified mob check
            }
            
            if (render) {
                Vec3d prevPos = new Vec3d(entity.prevX, entity.prevY, entity.prevZ);
                Vec3d curPos = new Vec3d(entity.getX(), entity.getY(), entity.getZ());
                
                Vec3d interp = prevPos.lerp(curPos, context.tickDelta());
                
                Vec3d camera = context.camera().getPos();
                
                // Relative position
                double x = interp.x - camera.x;
                double y = interp.y - camera.y;
                double z = interp.z - camera.z;
                
                Box bbox = entity.getBoundingBox();
                // Move box to 0,0,0 and then translate by x,y,z
                double width = bbox.maxX - bbox.minX;
                double height = bbox.maxY - bbox.minY;
                double depth = bbox.maxZ - bbox.minZ;
                
                // Center offset
                Box renderBox = new Box(x - width/2, y, z - depth/2, x + width/2, y + height, z + depth/2);

                context.matrixStack().push();
                // If we calculated positions correctly relative to camera, no need to translate further if using raw coords in drawBox
                // But RenderUtils.drawBox uses raw coords from the box and identity matrix or whatever is on stack.
                // context.matrixStack() should be used.
                
                int c = color.getRGB();
                float r = ((c >> 16) & 0xFF) / 255f;
                float g = ((c >> 8) & 0xFF) / 255f;
                float b = (c & 0xFF) / 255f;
                
                RenderUtils.drawBox(context.matrixStack(), renderBox, r, g, b, 1.0f);
                context.matrixStack().pop();
            }
        }
    }
}
