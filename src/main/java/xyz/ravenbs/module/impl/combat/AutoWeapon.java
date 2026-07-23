package xyz.ravenbs.module.impl.combat;

import xyz.ravenbs.module.Module;
import xyz.ravenbs.module.ModuleCategory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;

public class AutoWeapon extends Module {
    public AutoWeapon() {
        super("AutoWeapon", ModuleCategory.combat);
    }
    
    // We hook into attack event.
    // Simplest way: check left click mouse button or attack packet.
    
    @Override
    public void onUpdate() {
        if (mc.options.attackKey.isPressed() && mc.targetedEntity instanceof LivingEntity) {
            // Swap to best weapon
            int bestSlot = -1;
            float bestDamage = -1;
            
            for (int i = 0; i < 9; i++) {
                ItemStack stack = mc.player.getInventory().getStack(i);
                if (stack.isEmpty()) continue;
                
                float damage = 0;
                if (stack.getItem() instanceof SwordItem) {
                    damage = ((SwordItem)stack.getItem()).getAttackDamage() + 1; // +1 base
                } else if (stack.getItem() instanceof AxeItem) {
                    damage = ((AxeItem)stack.getItem()).getAttackDamage() + 1;
                }
                
                // Add enchantments helper if needed, but for simplicity just base damage
                
                if (damage > bestDamage) {
                    bestDamage = damage;
                    bestSlot = i;
                }
            }
            
            if (bestSlot != -1) {
                mc.player.getInventory().selectedSlot = bestSlot;
            }
        }
    }
}
