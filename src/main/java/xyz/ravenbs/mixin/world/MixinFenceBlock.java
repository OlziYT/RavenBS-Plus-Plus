package xyz.ravenbs.mixin.world;

import xyz.ravenbs.module.ModuleManager;
import net.minecraft.block.BlockState;
import net.minecraft.block.FenceBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FenceBlock.class)
public class MixinFenceBlock {
    // @Inject(method = "getCollisionShape", at = @At("HEAD"), cancellable = true)
    public void onGetCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context, CallbackInfoReturnable<VoxelShape> cir) {
        if (ModuleManager.sumoFence != null && ModuleManager.sumoFence.isEnabled()) {
            cir.setReturnValue(VoxelShapes.cuboid(0, 0, 0, 1, 1.5, 1)); // Taller collision
        }
    }
}
