package xyz.ravenbs.utility;

import net.minecraft.client.MinecraftClient;

public class RotationUtils {
    public static final MinecraftClient mc = MinecraftClient.getInstance();
    public static float renderPitch;
    public static float prevRenderPitch;
    public static float renderYaw;
    public static float prevRenderYaw;
    public static float[] serverRotations = new float[] { 0, 0 };
    
    public static void setRenderYaw(float yaw) {
        if (mc.player != null) {
            mc.player.headYaw = yaw;
            mc.player.bodyYaw = yaw;
        }
    }

    public static float[] getRotations(net.minecraft.entity.Entity entity) {
        if (entity == null || mc.player == null) return null;
        
        double x = entity.getX() - mc.player.getX();
        double y = (entity.getBoundingBox().minY + entity.getBoundingBox().maxY) / 2.0 - (mc.player.getY() + mc.player.getEyeHeight(mc.player.getPose()));
        double z = entity.getZ() - mc.player.getZ();

        double dist = Math.sqrt(x * x + z * z);
        float yaw = (float) (Math.atan2(z, x) * 180.0 / Math.PI) - 90.0F;
        float pitch = (float) (-(Math.atan2(y, dist) * 180.0 / Math.PI));

        return new float[]{
            mc.player.getYaw() + net.minecraft.util.math.MathHelper.wrapDegrees(yaw - mc.player.getYaw()),
            mc.player.getPitch() + net.minecraft.util.math.MathHelper.wrapDegrees(pitch - mc.player.getPitch())
        };
    }
    
    public static float[] getRotations(net.minecraft.util.math.Vec3d vec) {
        if (vec == null || mc.player == null) return null;
        
        double x = vec.x - mc.player.getX();
        double y = vec.y - (mc.player.getY() + mc.player.getEyeHeight(mc.player.getPose()));
        double z = vec.z - mc.player.getZ();
        
        double dist = Math.sqrt(x * x + z * z);
        float yaw = (float) (Math.atan2(z, x) * 180.0 / Math.PI) - 90.0F;
        float pitch = (float) (-(Math.atan2(y, dist) * 180.0 / Math.PI));
        
        return new float[]{
            mc.player.getYaw() + net.minecraft.util.math.MathHelper.wrapDegrees(yaw - mc.player.getYaw()),
            mc.player.getPitch() + net.minecraft.util.math.MathHelper.wrapDegrees(pitch - mc.player.getPitch())
        };
    }
    public static float[] smooth(float[] current, float[] target, float speed) {
        float yawDiff = net.minecraft.util.math.MathHelper.wrapDegrees(target[0] - current[0]);
        float pitchDiff = net.minecraft.util.math.MathHelper.wrapDegrees(target[1] - current[1]);
        
        float yawChange = Math.max(-speed, Math.min(speed, yawDiff));
        float pitchChange = Math.max(-speed, Math.min(speed, pitchDiff));
        
        return new float[] { current[0] + yawChange, current[1] + pitchChange };
    }
}
