package xyz.ravenbs.utility;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import java.awt.Color;
import java.util.Random;

public class Utils {
    public static final Random random = new Random();
    public static MinecraftClient mc = MinecraftClient.getInstance();
    
    public static boolean isHoldingWeapon() {
        if (mc.player == null) return false;
        net.minecraft.item.ItemStack stack = mc.player.getMainHandStack();
        if (stack == null) return false;
        net.minecraft.item.Item item = stack.getItem();
        return item instanceof net.minecraft.item.SwordItem || item instanceof net.minecraft.item.AxeItem;
    }
    
    public static boolean isHoldingBlock() {
        if (mc.player == null) return false;
        net.minecraft.item.ItemStack stack = mc.player.getMainHandStack();
        if (stack == null) return false;
        return stack.getItem() instanceof net.minecraft.item.BlockItem;
    }

    public static void sendMessage(String message) {
        if (MinecraftClient.getInstance().player != null) {
            String cleanMsg = message.replace("&", "§"); 
            MinecraftClient.getInstance().player.sendMessage(Text.of(cleanMsg), false);
        }
    }
    
    public static int getChroma(long speed, long delay) {
        long time = System.currentTimeMillis() + delay;
        return java.awt.Color.HSBtoRGB((time % (1000L * speed)) / (1000.0f * speed), 0.8f, 0.8f);
    }
    
    public static int mergeAlpha(int rgb, int alpha) {
        return (rgb & 0x00FFFFFF) | (alpha << 24);
    }
    
    public static boolean isFriended(net.minecraft.entity.player.PlayerEntity player) {
        return xyz.ravenbs.utility.FriendManager.isFriend(player);
    }
    
    public static boolean isTeamMate(net.minecraft.entity.Entity entity) {
        if (entity instanceof net.minecraft.entity.player.PlayerEntity) {
            if (isFriended((net.minecraft.entity.player.PlayerEntity) entity)) return true;
        }
        if (mc.player.getScoreboardTeam() != null && entity.getScoreboardTeam() != null) {
            return mc.player.getScoreboardTeam().isEqual(entity.getScoreboardTeam());
        }
        return false;
    }
    
    public static boolean inFov(float fov, net.minecraft.entity.Entity entity) {
        if (mc.player == null || entity == null) {
            return false;
        }

        float[] rotations = RotationUtils.getRotations(entity);
        if (rotations == null) {
            return false;
        }

        float yawDiff = net.minecraft.util.math.MathHelper.wrapDegrees(rotations[0] - mc.player.getYaw());
        return Math.abs(yawDiff) <= (fov / 2.0f);
    }
    
    public static boolean canPlayerBeSeen(net.minecraft.entity.Entity entity) {
        return MinecraftClient.getInstance().player.canSee(entity);
    }
    
    public static boolean isMoving() {
        return MinecraftClient.getInstance().player.input.movementForward != 0 || MinecraftClient.getInstance().player.input.movementSideways != 0;
    }
    
    public static void setSpeed(double speed) {
        net.minecraft.client.network.ClientPlayerEntity player = MinecraftClient.getInstance().player;
        float yaw = player.getYaw();
        double forward = player.input.movementForward;
        double strafe = player.input.movementSideways;
        
        if (forward == 0 && strafe == 0) {
            player.setVelocity(0, player.getVelocity().y, 0);
        } else {
            if (forward != 0) {
                if (strafe > 0) {
                    yaw += (forward > 0 ? -45 : 45);
                } else if (strafe < 0) {
                    yaw += (forward > 0 ? 45 : -45);
                }
                strafe = 0;
                if (forward > 0) {
                    forward = 1;
                } else if (forward < 0) {
                    forward = -1;
                }
            }
            
            double rad = Math.toRadians(yaw);
            double sin = Math.sin(rad);
            double cos = Math.cos(rad);
            
            player.setVelocity(
                (forward * speed * -sin) + (strafe * speed * cos),
                player.getVelocity().y,
                (forward * speed * cos) - (strafe * speed * -sin)
            );
        }
    }
    
    public static double getHorizontalSpeed() {
        return Math.sqrt(MinecraftClient.getInstance().player.getVelocity().x * MinecraftClient.getInstance().player.getVelocity().x + MinecraftClient.getInstance().player.getVelocity().z * MinecraftClient.getInstance().player.getVelocity().z);
    }
    public static float interpolate(float current, float target, float speed) {
        return current + (target - current) * speed;
    }
    
    public static int getColorForHealth(float pct) {
        return java.awt.Color.HSBtoRGB(pct / 3.0f, 1.0f, 1.0f);
    }
    
    public static java.util.List<net.minecraft.block.entity.BlockEntity> getAllBlockEntities() {
        java.util.List<net.minecraft.block.entity.BlockEntity> list = new java.util.ArrayList<>();
        net.minecraft.client.world.ClientWorld world = MinecraftClient.getInstance().world;
        if (world == null) return list;
        
        // Iterate only loaded chunks within render distance
        int radius = MinecraftClient.getInstance().options.getClampedViewDistance();
        net.minecraft.util.math.ChunkPos center = MinecraftClient.getInstance().player.getChunkPos();
        
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                net.minecraft.world.chunk.WorldChunk chunk = world.getChunkManager().getChunk(center.x + x, center.z + z, net.minecraft.world.chunk.ChunkStatus.FULL, false);
                if (chunk != null) {
                    list.addAll(chunk.getBlockEntities().values());
                }
            }
        }
        return list;
    }
    public static int darken(int rgb, float factor) {
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;
        
        r = Math.min(255, Math.max(0, (int)(r * factor)));
        g = Math.min(255, Math.max(0, (int)(g * factor)));
        b = Math.min(255, Math.max(0, (int)(b * factor)));
        
        return (rgb & 0xFF000000) | (r << 16) | (g << 8) | b;
    }
}
