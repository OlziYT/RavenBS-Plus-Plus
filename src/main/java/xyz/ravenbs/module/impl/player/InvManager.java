package xyz.ravenbs.module.impl.player;

import xyz.ravenbs.module.Module;
import xyz.ravenbs.module.ModuleCategory;
import xyz.ravenbs.module.setting.impl.SliderSetting;
import xyz.ravenbs.utility.Utils;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolItem;
import net.minecraft.screen.slot.SlotActionType;

import java.util.Arrays;
import java.util.List;

public class InvManager extends Module {
    private SliderSetting delay;
    private long lastClean;
    
    private static final List<Item> TRASH = Arrays.asList(
            Items.ROTTEN_FLESH, Items.FEATHER, Items.STRING, Items.WHEAT_SEEDS, 
            Items.WHEAT_SEEDS, Items.BONE, Items.GLASS_BOTTLE
    );

    public InvManager() {
        super("InvManager", ModuleCategory.player);
        this.registerSetting(delay = new SliderSetting("Delay ms", 100, 0, 500, 10));
    }

    @Override
    public void onUpdate() {
        if (mc.player == null || mc.currentScreen != null) return; // Only when in game, no GUI open
        
        if (System.currentTimeMillis() - lastClean < delay.getInput()) return;

        // Auto Armor
        equipBestArmor();
        
        // Auto Clean
        cleanInventory();
        
        lastClean = System.currentTimeMillis();
    }
    
    private void equipBestArmor() {
        // Simplified Logic for 1.20
        // Slots 5-8 are armor (Helmet 5, Chest 6, Leg 7, Boots 8) in Container ID 0 (PlayerInventory 9-44 usually + Armor)
        // Actually Armor slots are 5,6,7,8.
        
        int[] armorSlots = new int[]{5, 6, 7, 8};
        
        for (int i = 0; i < 4; i++) {
            int armorSlot = armorSlots[i];
            ItemStack currentArmor = mc.player.getInventory().getArmorStack(3 - i); // Helmet is index 3 in list? No, getArmorStack checks specific.
            // Indices: 0: boots, 1: leggings, 2: chestplate, 3: helmet
            
            int bestSlot = -1;
            int maxProt = getArmorProtection(currentArmor);
            
            // Search inventory (9 to 44)
            for (int slot = 9; slot < 45; slot++) {
                ItemStack stack = mc.player.getInventory().getStack(slot); // Logic mapping might differ for slot ID vs Inventory ID
                if (stack.getItem() instanceof ArmorItem) {
                    ArmorItem armor = (ArmorItem) stack.getItem();
                    // Check type
                    if (armor.getSlotType().getEntitySlotId() == i) { // Not quite right API
                         // Manual check
                         // If slot=5 (Helm), we need HEAD. 
                         // Fabric/Yarn: getSlotType() -> EquipmentSlot
                    }
                }
            }
        }
    }
    
    private void cleanInventory() {
        for (int i = 9; i < 45; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.isEmpty()) continue;
            
            if (isTrash(stack)) {
                // Drop
                mc.interactionManager.clickSlot(mc.player.playerScreenHandler.syncId, i, 1, SlotActionType.THROW, mc.player);
                return; // One per tick
            }
        }
    }
    
    private boolean isTrash(ItemStack stack) {
        if (TRASH.contains(stack.getItem())) return true;
        // Check for worse tools?
        return false;
    }
    
    private int getArmorProtection(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return -1;
        if (!(stack.getItem() instanceof ArmorItem)) return -1;
        return ((ArmorItem) stack.getItem()).getProtection();
    }
}
