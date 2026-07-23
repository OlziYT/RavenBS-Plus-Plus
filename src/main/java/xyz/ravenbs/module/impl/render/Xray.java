package xyz.ravenbs.module.impl.render;

import xyz.ravenbs.module.Module;
import xyz.ravenbs.module.ModuleCategory;
import xyz.ravenbs.module.setting.impl.ButtonSetting;
import xyz.ravenbs.module.setting.impl.SliderSetting;
import xyz.ravenbs.utility.RenderUtils;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class Xray extends Module {
    private SliderSetting range;
    private SliderSetting rate;
    private ButtonSetting iron;
    private ButtonSetting gold;
    private ButtonSetting diamond;
    private ButtonSetting emerald;
    private ButtonSetting lapis;
    private ButtonSetting redstone;
    private ButtonSetting coal;
    private ButtonSetting copper;
    private Set<BlockPos> blocks = ConcurrentHashMap.newKeySet();
    private long lastCheck = 0;

    public Xray() {
        super("Xray", ModuleCategory.render);
        this.registerSetting(range = new SliderSetting("Range", 20, 5, 50, 1));
        this.registerSetting(rate = new SliderSetting("Rate", 0.5, 0.1, 3.0, 0.1));
        this.registerSetting(coal = new ButtonSetting("Coal", true));
        this.registerSetting(copper = new ButtonSetting("Copper", true));
        this.registerSetting(diamond = new ButtonSetting("Diamond", true));
        this.registerSetting(emerald = new ButtonSetting("Emerald", true));
        this.registerSetting(gold = new ButtonSetting("Gold", true));
        this.registerSetting(iron = new ButtonSetting("Iron", true));
        this.registerSetting(lapis = new ButtonSetting("Lapis", true));
        this.registerSetting(redstone = new ButtonSetting("Redstone", true));
    }

    @Override
    public void onDisable() {
        this.blocks.clear();
    }

    @Override
    public void onUpdate() {
        if (mc.world == null || mc.player == null) return;
        
        if (System.currentTimeMillis() - lastCheck < rate.getInput() * 1000) {
            return;
        }
        lastCheck = System.currentTimeMillis();
        
        // Scan in separate thread to avoid lag
        new Thread(() -> {
            int r = (int) range.getInput();
            BlockPos playerPos = mc.player.getBlockPos();
            
            for (int x = -r; x <= r; x++) {
                for (int y = -r; y <= r; y++) {
                    for (int z = -r; z <= r; z++) {
                        BlockPos pos = playerPos.add(x, y, z);
                        if (blocks.contains(pos)) continue;
                        
                        Block block = mc.world.getBlockState(pos).getBlock();
                        if (canShow(block)) {
                            blocks.add(pos);
                        }
                    }
                }
            }
            
            // Remove old/broken blocks
            blocks.removeIf(pos -> !canShow(mc.world.getBlockState(pos).getBlock()));
        }).start();
    }

    @Override
    public void onRenderWorld(WorldRenderContext context) {
        if (blocks.isEmpty()) return;
        
        Vec3d camera = context.camera().getPos();
        
        for (BlockPos pos : blocks) {
            Block block = mc.world.getBlockState(pos).getBlock();
            if (!canShow(block)) continue;
            
            int[] rgb = getColor(block);
            float r = rgb[0] / 255f;
            float g = rgb[1] / 255f;
            float b = rgb[2] / 255f;
            
            Box box = new Box(
                pos.getX() - camera.x,
                pos.getY() - camera.y,
                pos.getZ() - camera.z,
                pos.getX() + 1 - camera.x,
                pos.getY() + 1 - camera.y,
                pos.getZ() + 1 - camera.z
            );
            
            context.matrixStack().push();
            RenderUtils.drawBox(context.matrixStack(), box, r, g, b, 1.0f);
            context.matrixStack().pop();
        }
    }

    private int[] getColor(Block block) {
        if (block == Blocks.IRON_ORE || block == Blocks.DEEPSLATE_IRON_ORE) return new int[]{255, 255, 255};
        if (block == Blocks.GOLD_ORE || block == Blocks.DEEPSLATE_GOLD_ORE) return new int[]{255, 215, 0};
        if (block == Blocks.DIAMOND_ORE || block == Blocks.DEEPSLATE_DIAMOND_ORE) return new int[]{0, 220, 255};
        if (block == Blocks.EMERALD_ORE || block == Blocks.DEEPSLATE_EMERALD_ORE) return new int[]{0, 255, 0};
        if (block == Blocks.LAPIS_ORE || block == Blocks.DEEPSLATE_LAPIS_ORE) return new int[]{0, 50, 255};
        if (block == Blocks.REDSTONE_ORE || block == Blocks.DEEPSLATE_REDSTONE_ORE) return new int[]{255, 0, 0};
        if (block == Blocks.COAL_ORE || block == Blocks.DEEPSLATE_COAL_ORE) return new int[]{50, 50, 50};
        if (block == Blocks.COPPER_ORE || block == Blocks.DEEPSLATE_COPPER_ORE) return new int[]{200, 100, 50};
        return new int[]{0, 0, 0};
    }

    private boolean canShow(Block block) {
        return (iron.isToggled() && (block == Blocks.IRON_ORE || block == Blocks.DEEPSLATE_IRON_ORE)) ||
               (gold.isToggled() && (block == Blocks.GOLD_ORE || block == Blocks.DEEPSLATE_GOLD_ORE)) ||
               (diamond.isToggled() && (block == Blocks.DIAMOND_ORE || block == Blocks.DEEPSLATE_DIAMOND_ORE)) ||
               (emerald.isToggled() && (block == Blocks.EMERALD_ORE || block == Blocks.DEEPSLATE_EMERALD_ORE)) ||
               (lapis.isToggled() && (block == Blocks.LAPIS_ORE || block == Blocks.DEEPSLATE_LAPIS_ORE)) ||
               (redstone.isToggled() && (block == Blocks.REDSTONE_ORE || block == Blocks.DEEPSLATE_REDSTONE_ORE)) ||
               (coal.isToggled() && (block == Blocks.COAL_ORE || block == Blocks.DEEPSLATE_COAL_ORE)) ||
               (copper.isToggled() && (block == Blocks.COPPER_ORE || block == Blocks.DEEPSLATE_COPPER_ORE));
    }
}
