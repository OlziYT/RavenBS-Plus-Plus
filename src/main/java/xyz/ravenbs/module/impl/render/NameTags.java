package xyz.ravenbs.module.impl.render;

import xyz.ravenbs.module.Module;
import xyz.ravenbs.module.ModuleCategory;
import xyz.ravenbs.module.setting.impl.ButtonSetting;
import xyz.ravenbs.module.setting.impl.SliderSetting;
import xyz.ravenbs.utility.Utils;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

import java.awt.Color;

public class NameTags extends Module {
    private SliderSetting scale;
    private ButtonSetting showHealth;
    private ButtonSetting showArmor; // simplified placeholder
    
    public NameTags() {
        super("NameTags", ModuleCategory.render);
        this.registerSetting(scale = new SliderSetting("Scale", 1.0, 0.5, 3.0, 0.1));
        this.registerSetting(showHealth = new ButtonSetting("Show Health", true));
        this.registerSetting(showArmor = new ButtonSetting("Show Armor", false)); 
    }

    @Override
    public void onRenderWorld(WorldRenderContext context) {
         // NameTags in Fabric 1.20 are best handled by mixing into EntityRenderer or using WorldRender events 
         // and manually drawing text billboards.
         // Since we want to hide vanilla nametags, we often need a Mixin to cancel vanilla rendering.
         
         // For now, let's implement the drawer here.
         // Note: Vanilla renders nametags in the translucent pass.
         
         if (mc.world == null || mc.player == null) return;
         
         for (Entity entity : mc.world.getEntities()) {
             if (entity instanceof PlayerEntity && entity != mc.player) {
                 renderNameTag((PlayerEntity) entity, context);
             }
         }
    }
    
    private void renderNameTag(PlayerEntity player, WorldRenderContext context) {
        Camera camera = context.camera();
        Vec3d camPos = camera.getPos();
        Vec3d targetPos = player.getPos().add(0, player.getHeight() + 0.5, 0);
        
        // Check visibility
        if (camera.getPos().distanceTo(targetPos) > 100) return;
        
        MatrixStack matrices = context.matrixStack();
        matrices.push();
        
        double x = targetPos.x - camPos.x;
        double y = targetPos.y - camPos.y;
        double z = targetPos.z - camPos.z;
        
        matrices.translate(x, y, z);
        matrices.multiply(camera.getRotation());
        matrices.scale(-0.025f * (float)scale.getInput(), -0.025f * (float)scale.getInput(), 0.025f * (float)scale.getInput());
        
        TextRenderer textRenderer = mc.textRenderer;
        String text = player.getName().getString();
        
        if (showHealth.isToggled()) {
            text += " " + (int)player.getHealth(); // simplified health
        }
        
        float width = textRenderer.getWidth(text) / 2f;
        
        // Background
        // RenderUtils.drawRect(...); // Optional
        
        // Text with shadow
        textRenderer.draw(text, -width, 0, -1, true, matrices.peek().getPositionMatrix(), context.consumers(), TextRenderer.TextLayerType.NORMAL, 0, 15728880);
        
        matrices.pop();
    }
}
