package xyz.ravenbs.utility;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;

public class ScaffoldUtils {
    private static MinecraftClient mc = MinecraftClient.getInstance();

    public static class BlockData {
        public BlockPos pos;
        public Direction face;
        
        public BlockData(BlockPos pos, Direction face) {
            this.pos = pos;
            this.face = face;
        }
    }

    public static BlockData getBlockData(BlockPos pos) {
        if (!isAirOrLiquid(pos.add(0, -1, 0))) 
            return new BlockData(pos.add(0, -1, 0), Direction.UP);
        if (!isAirOrLiquid(pos.add(-1, 0, 0))) 
            return new BlockData(pos.add(-1, 0, 0), Direction.EAST);
        if (!isAirOrLiquid(pos.add(1, 0, 0))) 
            return new BlockData(pos.add(1, 0, 0), Direction.WEST);
        if (!isAirOrLiquid(pos.add(0, 0, -1))) 
            return new BlockData(pos.add(0, 0, -1), Direction.SOUTH);
        if (!isAirOrLiquid(pos.add(0, 0, 1))) 
            return new BlockData(pos.add(0, 0, 1), Direction.NORTH);

        return null;
    }

    public static boolean isAirOrLiquid(BlockPos pos) {
        BlockState state = mc.world.getBlockState(pos);
        return state.isAir() || !state.getFluidState().isEmpty();
    }
    
    public static Vec3d getVectorForRotation(BlockData data) {
        BlockPos pos = data.pos;
        Direction face = data.face;
        
        double x = pos.getX() + 0.5 + face.getOffsetX() * 0.5;
        double y = pos.getY() + 0.5 + face.getOffsetY() * 0.5;
        double z = pos.getZ() + 0.5 + face.getOffsetZ() * 0.5;
        
        return new Vec3d(x, y, z);
    }
}
