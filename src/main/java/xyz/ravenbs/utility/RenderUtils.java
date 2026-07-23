package xyz.ravenbs.utility;

import net.minecraft.client.gui.DrawContext;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;

import net.minecraft.client.MinecraftClient;
import java.awt.Color;

public class RenderUtils {

    public static void drawRect(DrawContext context, int left, int top, int right, int bottom, int color) {
        context.fill(left, top, right, bottom, color);
    }
    
    public static void drawRoundedRectangle(DrawContext context, int left, int top, int right, int bottom, int radius, int color) {
        context.fill(left, top, right, bottom, color);
    }

    public static void drawGradientRect(DrawContext context, int left, int top, int right, int bottom, int startColor, int endColor) {
        context.fillGradient(left, top, right, bottom, startColor, endColor);
    }
    
    public static int getChroma(long speed, long delay) {
        long time = System.currentTimeMillis() + delay;
        return Color.HSBtoRGB((time % (1000L * speed)) / (1000.0f * speed), 0.8f, 0.8f);
    }

    public static void drawBox(MatrixStack matrices, net.minecraft.util.math.Box box, float red, float green, float blue, float alpha) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest(); 
        
        buffer.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
        
        drawBoxLines(buffer, matrices, box, red, green, blue, alpha);
        
        tessellator.draw();
        
        RenderSystem.disableBlend();
    }
    
    // Internal helper made public for batching
    public static void drawBoxLines(BufferBuilder buffer, MatrixStack matrices, net.minecraft.util.math.Box box, float r, float g, float b, float a) {
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        float x1 = (float)box.minX;
        float y1 = (float)box.minY;
        float z1 = (float)box.minZ;
        float x2 = (float)box.maxX;
        float y2 = (float)box.maxY;
        float z2 = (float)box.maxZ;
        
        // Bottom
        buffer.vertex(matrix, x1, y1, z1).color(r, g, b, a).next();
        buffer.vertex(matrix, x2, y1, z1).color(r, g, b, a).next();
        buffer.vertex(matrix, x2, y1, z1).color(r, g, b, a).next();
        buffer.vertex(matrix, x2, y1, z2).color(r, g, b, a).next();
        buffer.vertex(matrix, x2, y1, z2).color(r, g, b, a).next();
        buffer.vertex(matrix, x1, y1, z2).color(r, g, b, a).next();
        buffer.vertex(matrix, x1, y1, z2).color(r, g, b, a).next();
        buffer.vertex(matrix, x1, y1, z1).color(r, g, b, a).next();
        
        // Top
        buffer.vertex(matrix, x1, y2, z1).color(r, g, b, a).next();
        buffer.vertex(matrix, x2, y2, z1).color(r, g, b, a).next();
        buffer.vertex(matrix, x2, y2, z1).color(r, g, b, a).next();
        buffer.vertex(matrix, x2, y2, z2).color(r, g, b, a).next();
        buffer.vertex(matrix, x2, y2, z2).color(r, g, b, a).next();
        buffer.vertex(matrix, x1, y2, z2).color(r, g, b, a).next();
        buffer.vertex(matrix, x1, y2, z2).color(r, g, b, a).next();
        buffer.vertex(matrix, x1, y2, z1).color(r, g, b, a).next();
        
        // Sides
        buffer.vertex(matrix, x1, y1, z1).color(r, g, b, a).next();
        buffer.vertex(matrix, x1, y2, z1).color(r, g, b, a).next();
        buffer.vertex(matrix, x2, y1, z1).color(r, g, b, a).next();
        buffer.vertex(matrix, x2, y2, z1).color(r, g, b, a).next();
        buffer.vertex(matrix, x2, y1, z2).color(r, g, b, a).next();
        buffer.vertex(matrix, x2, y2, z2).color(r, g, b, a).next();
        buffer.vertex(matrix, x1, y1, z2).color(r, g, b, a).next();
        buffer.vertex(matrix, x1, y2, z2).color(r, g, b, a).next();
    }
    
    // New Helper for ChestESP
    public static void drawBoxLines(WorldRenderContext context, net.minecraft.util.math.Box box, Color color) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.lineWidth(2.0f);
        
        buffer.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
        
        Camera camera = context.camera();
        Vec3d camPos = camera.getPos();
        
        // matrixStack is typically camera-relative or absolutely positioned depending on event.
        // In WorldRenderContext, valid MatrixStack is usually provided.
        // We need to translate box relative to camera if matrix stack is identity/origin-based?
        // Usually context.matrixStack() is already set up for camera relative rendering in WorldRenderEvents.
        
        // Let's assume standard behavior:
        // However, if we draw absolute coords with camera-relative matrix, we are offset double.
        // Box is absolute. Camera is absolute. 
        // We usually subtract camPos from Box coords.
        
        MatrixStack matrices = context.matrixStack();
        matrices.push();
        matrices.translate(-camPos.x, -camPos.y, -camPos.z);
        
        float r = color.getRed() / 255f;
        float g = color.getGreen() / 255f;
        float b = color.getBlue() / 255f;
        float a = color.getAlpha() / 255f;
        
        drawBoxLines(buffer, matrices, box, r, g, b, a);
        
        matrices.pop();
        
        tessellator.draw();
        RenderSystem.disableBlend();
    }
    
    // New Helper for Tracers
    public static void drawTracerLine(WorldRenderContext context, Vec3d target, Color color) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.lineWidth(2.0f);
        
        buffer.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
        
        Camera camera = context.camera();
        Vec3d camPos = camera.getPos();
        
        MatrixStack matrices = context.matrixStack();
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        
        org.joml.Vector3f horiz = camera.getHorizontalPlane();
        Vec3d start = new Vec3d(horiz.x, horiz.y, horiz.z).multiply(1.0); // Forward vector relative to 0,0,0
        // Wait, tracers usually go from center of screen (0, 0, distance) relative to camera
        
        // Simple Tracer: Line from camera eyes (0,0,0 relative) to target (relative)
        
        float r = color.getRed() / 255f;
        float g = color.getGreen() / 255f;
        float b = color.getBlue() / 255f;
        float a = color.getAlpha() / 255f;
        
        // Start (Eyes/Camera 0,0,0) - Actually we want to draw from look vector
        Vec3d vecForward = new Vec3d(0, 0, 1).rotateX(-(float)Math.toRadians(camera.getPitch())).rotateY(-(float)Math.toRadians(camera.getYaw()));
        // Just Use 0,0,0 (Camera position)
        
        buffer.vertex(matrix, 0, 0, 0).color(r, g, b, a).next();
        
        // End
        Vec3d end = target.subtract(camPos);
        buffer.vertex(matrix, (float)end.x, (float)end.y, (float)end.z).color(r, g, b, a).next();
        
        tessellator.draw();
        RenderSystem.disableBlend();
    }


    public static void drawBlockBox(MatrixStack matrices, net.minecraft.util.math.BlockPos pos, Color color) {
        // Calculate relative pos
        Camera camera = MinecraftClient.getInstance().gameRenderer.getCamera();
        Vec3d camPos = camera.getPos();
        
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.lineWidth(2.0f);
        
        buffer.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
        
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        
        double x = pos.getX() - camPos.x;
        double y = pos.getY() - camPos.y;
        double z = pos.getZ() - camPos.z;
        
        net.minecraft.util.math.Box box = new net.minecraft.util.math.Box(x, y, z, x + 1, y + 1, z + 1);
        
        drawBoxLines(buffer, matrices, box, color.getRed()/255f, color.getGreen()/255f, color.getBlue()/255f, color.getAlpha()/255f);
        
        tessellator.draw();
        RenderSystem.disableBlend();
    }
    
    public static void drawEntityBox(MatrixStack matrices, net.minecraft.entity.Entity entity, Color color) {
        Camera camera = MinecraftClient.getInstance().gameRenderer.getCamera();
        Vec3d camPos = camera.getPos();
        
        double x = net.minecraft.util.math.MathHelper.lerp(MinecraftClient.getInstance().getTickDelta(), entity.prevX, entity.getX()) - camPos.x;
        double y = net.minecraft.util.math.MathHelper.lerp(MinecraftClient.getInstance().getTickDelta(), entity.prevY, entity.getY()) - camPos.y;
        double z = net.minecraft.util.math.MathHelper.lerp(MinecraftClient.getInstance().getTickDelta(), entity.prevZ, entity.getZ()) - camPos.z;
        
        net.minecraft.util.math.Box box = entity.getBoundingBox().offset(-entity.getX() + x, -entity.getY() + y, -entity.getZ() + z);
        
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        
        buffer.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        
        drawBoxLines(buffer, matrices, box, color.getRed()/255f, color.getGreen()/255f, color.getBlue()/255f, color.getAlpha()/255f);
        
        tessellator.draw();
        RenderSystem.disableBlend();
    }
}
