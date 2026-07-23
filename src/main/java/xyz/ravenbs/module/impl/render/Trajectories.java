package xyz.ravenbs.module.impl.render;

import xyz.ravenbs.module.Module;
import xyz.ravenbs.module.ModuleCategory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.*;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.RaycastContext;
import net.minecraft.util.math.Box;
import xyz.ravenbs.utility.RenderUtils;

import java.awt.Color;

public class Trajectories extends Module {
    public Trajectories() {
        super("Trajectories", ModuleCategory.render);
    }

    @Override
    public void onRenderWorld(net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext context) {
        net.minecraft.client.util.math.MatrixStack matrices = context.matrixStack();
        float tickDelta = context.tickDelta();
        if (mc.player == null) return;
        
        ItemStack stack = mc.player.getMainHandStack();
        if (stack.getItem() instanceof BowItem || stack.getItem() == Items.SNOWBALL || stack.getItem() == Items.ENDER_PEARL || stack.getItem() == Items.EGG) {
            // Calculate Path
            double gravity = 0.05; // approx for arrow
            double velocity = 0.0;
            
            if (stack.getItem() instanceof BowItem) {
                float pull = (float)(stack.getMaxUseTime() - mc.player.getItemUseTimeLeft()) / 20.0F;
                pull = (pull * pull + pull * 2.0F) / 3.0F;
                if (pull > 1.0F) pull = 1.0F;
                if (pull <= 0.1F) return; // Not pulled enough
                velocity = pull * 3.0F;
            } else {
                velocity = 1.5; // snowball
                gravity = 0.03;
            }
            
            Vec3d pos = mc.player.getCameraPosVec(tickDelta);
            float yaw = mc.player.getYaw();
            float pitch = mc.player.getPitch();
            
            Vec3d motion = Vec3d.fromPolar(pitch, yaw).multiply(velocity);
            
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.getBuffer();
            
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableDepthTest();
            RenderSystem.setShader(GameRenderer::getPositionColorProgram);
            RenderSystem.lineWidth(2.0f);
            
            buffer.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
            Matrix4f matrix = matrices.peek().getPositionMatrix();
            // Need to transform to world coords relative to camera
            // Let's simplified draw
            
            // Loop sim
            Vec3d cur = pos; // Start at eyes
            // Need to subtract camera pos for rendering
             Vec3d cam = mc.gameRenderer.getCamera().getPos();
            
            for (int i = 0; i < 100; i++) {
                Vec3d next = cur.add(motion);
                
                // Collision check
                HitResult hit = mc.world.raycast(new RaycastContext(
                    cur, next,
                    RaycastContext.ShapeType.COLLIDER,
                    RaycastContext.FluidHandling.NONE,
                    mc.player
                ));
                
                if (hit.getType() != HitResult.Type.MISS) {
                    next = hit.getPos();
                }
                
                // Draw Line
                buffer.vertex(matrix, (float)(cur.x - cam.x), (float)(cur.y - cam.y), (float)(cur.z - cam.z)).color(0, 255, 0, 255).next();
                buffer.vertex(matrix, (float)(next.x - cam.x), (float)(next.y - cam.y), (float)(next.z - cam.z)).color(0, 255, 0, 255).next();
                
                if (hit.getType() != HitResult.Type.MISS) {
                    // Draw box at impact
                    double size = 0.5;
                    RenderUtils.drawBoxLines(buffer, matrices, new Box(next.x - size/2, next.y, next.z - size/2, next.x + size/2, next.y + size, next.z + size/2).offset(-cam.x, -cam.y, -cam.z), 1f, 0f, 0f, 0.5f);
                    break;
                }
                
                cur = next;
                motion = motion.multiply(0.99); // Drag
                motion = motion.subtract(0, gravity, 0); // Gravity
            }
            
            tessellator.draw();
            RenderSystem.enableDepthTest();
        }
    }
}
