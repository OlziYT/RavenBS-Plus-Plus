package xyz.ravenbs.module.impl.minigames;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.block.BlockState;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.util.math.BlockPos;
import xyz.ravenbs.module.Module;
import xyz.ravenbs.module.ModuleCategory;
import xyz.ravenbs.module.setting.impl.ButtonSetting;
import xyz.ravenbs.module.setting.impl.DescriptionSetting;
import xyz.ravenbs.module.setting.impl.SliderSetting;
import xyz.ravenbs.utility.Notification;
import xyz.ravenbs.utility.NotificationManager;
import xyz.ravenbs.utility.RenderUtils;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SpeedBuilders extends Module {
    private final ButtonSetting paletteHud;
    private final ButtonSetting heldBlockEsp;
    private final ButtonSetting paletteAlerts;
    private final SliderSetting scanRadius;
    private final SliderSetting maxEntries;

    private final List<PaletteEntry> paletteEntries = new ArrayList<>();
    private final List<BlockPos> heldBlockMatches = new ArrayList<>();
    private String lastPaletteSignature = "";
    private int scanDelay;

    public SpeedBuilders() {
        super("SpeedBuilders", ModuleCategory.minigames);
        this.registerSetting(new DescriptionSetting("Shows nearby build palette and highlights blocks matching the held block."));
        this.registerSetting(paletteHud = new ButtonSetting("Palette HUD", true));
        this.registerSetting(heldBlockEsp = new ButtonSetting("Held block ESP", true));
        this.registerSetting(paletteAlerts = new ButtonSetting("Palette alerts", true));
        this.registerSetting(scanRadius = new SliderSetting("Scan radius", 6, 3, 10, 1));
        this.registerSetting(maxEntries = new SliderSetting("Max entries", 6, 3, 10, 1));
    }

    @Override
    public void onDisable() {
        paletteEntries.clear();
        heldBlockMatches.clear();
        lastPaletteSignature = "";
        scanDelay = 0;
    }

    @Override
    public void onUpdate() {
        if (mc.player == null || mc.world == null) {
            paletteEntries.clear();
            heldBlockMatches.clear();
            lastPaletteSignature = "";
            return;
        }

        if (++scanDelay < 5) {
            return;
        }
        scanDelay = 0;

        scanPalette();
    }

    @Override
    public void onRender(DrawContext context, float tickDelta) {
        if (!paletteHud.isToggled() || paletteEntries.isEmpty()) {
            return;
        }

        int x = mc.getWindow().getScaledWidth() - 118;
        int y = mc.getWindow().getScaledHeight() / 2 - 70;
        int width = 112;
        int height = 18 + (paletteEntries.size() * 18);

        context.fill(x, y, x + width, y + height, new Color(0, 0, 0, 140).getRGB());
        context.drawText(mc.textRenderer, "Palette", x + 6, y + 6, 0xFFFFFFFF, true);

        int entryY = y + 18;
        for (PaletteEntry entry : paletteEntries) {
            context.drawItem(entry.iconStack, x + 6, entryY);
            context.drawText(mc.textRenderer, entry.label, x + 26, entryY + 4, 0xFFE6E6E6, true);
            context.drawText(mc.textRenderer, String.valueOf(entry.count), x + width - 18, entryY + 4, 0xFF9FE870, true);
            entryY += 18;
        }
    }

    @Override
    public void onRenderWorld(WorldRenderContext context) {
        if (!heldBlockEsp.isToggled() || heldBlockMatches.isEmpty() || !(mc.player.getMainHandStack().getItem() instanceof BlockItem)) {
            return;
        }

        for (BlockPos pos : heldBlockMatches) {
            RenderUtils.drawBlockBox(context.matrixStack(), pos, new Color(90, 200, 255, 180));
        }
    }

    private void scanPalette() {
        Set<Item> inventoryBlocks = collectInventoryBlocks();
        if (inventoryBlocks.isEmpty()) {
            paletteEntries.clear();
            heldBlockMatches.clear();
            lastPaletteSignature = "";
            return;
        }

        Item heldBlockItem = getHeldBlockItem();
        Map<Item, Integer> blockCounts = new HashMap<>();
        List<BlockPos> matches = new ArrayList<>();

        BlockPos center = mc.player.getBlockPos();
        int radius = (int) scanRadius.getInput();

        for (int x = -radius; x <= radius; x++) {
            for (int y = -1; y <= 4; y++) {
                for (int z = -radius; z <= radius; z++) {
                    BlockPos pos = center.add(x, y, z);
                    BlockState state = mc.world.getBlockState(pos);
                    if (state.isAir() || !state.getFluidState().isEmpty()) {
                        continue;
                    }

                    Item blockItem = state.getBlock().asItem();
                    if (blockItem == Items.AIR || !inventoryBlocks.contains(blockItem)) {
                        continue;
                    }

                    blockCounts.merge(blockItem, 1, Integer::sum);
                    if (heldBlockItem != null && blockItem == heldBlockItem && matches.size() < 96) {
                        matches.add(pos.toImmutable());
                    }
                }
            }
        }

        paletteEntries.clear();
        blockCounts.entrySet().stream()
                .sorted(Map.Entry.<Item, Integer>comparingByValue(Comparator.reverseOrder()))
                .limit((int) maxEntries.getInput())
                .forEach(entry -> paletteEntries.add(new PaletteEntry(new ItemStack(entry.getKey()), trimLabel(entry.getKey().getName().getString()), entry.getValue())));

        heldBlockMatches.clear();
        heldBlockMatches.addAll(matches);

        String paletteSignature = buildPaletteSignature(blockCounts.keySet());
        if (!paletteSignature.isEmpty() && !paletteSignature.equals(lastPaletteSignature) && paletteAlerts.isToggled()) {
            String message = lastPaletteSignature.isEmpty() ? "Palette detected" : "Palette changed";
            NotificationManager.show("SpeedBuilders", message, Notification.Type.INFO, 1500);
        }
        lastPaletteSignature = paletteSignature;
    }

    private Set<Item> collectInventoryBlocks() {
        Set<Item> blocks = new HashSet<>();
        for (int slot = 0; slot < mc.player.getInventory().size(); slot++) {
            ItemStack stack = mc.player.getInventory().getStack(slot);
            if (stack.getItem() instanceof BlockItem) {
                blocks.add(stack.getItem());
            }
        }
        return blocks;
    }

    private Item getHeldBlockItem() {
        ItemStack heldStack = mc.player.getMainHandStack();
        if (heldStack.getItem() instanceof BlockItem) {
            return heldStack.getItem();
        }
        return null;
    }

    private String buildPaletteSignature(Set<Item> items) {
        List<String> itemIds = new ArrayList<>();
        for (Item item : items) {
            itemIds.add(Registries.ITEM.getId(item).toString());
        }
        itemIds.sort(String::compareTo);
        return String.join("|", itemIds);
    }

    private String trimLabel(String label) {
        return label.length() > 10 ? label.substring(0, 10) : label;
    }

    private static class PaletteEntry {
        private final ItemStack iconStack;
        private final String label;
        private final int count;

        private PaletteEntry(ItemStack iconStack, String label, int count) {
            this.iconStack = iconStack;
            this.label = label;
            this.count = count;
        }
    }
}
