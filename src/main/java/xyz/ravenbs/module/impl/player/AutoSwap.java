package xyz.ravenbs.module.impl.player;

import xyz.ravenbs.module.Module;
import xyz.ravenbs.module.ModuleCategory;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;

public class AutoSwap extends Module {
    public AutoSwap() {
        super("AutoSwap", ModuleCategory.player);
    }

    // Usually triggered on attack (Left click or Killaura).
    // Hook into attack or update loop?
    // If we are holding left click on an entity, swap to sword.
    
    @Override
    public void onUpdate() {
        if (mc.player == null || mc.options == null) {
            return;
        }

        if (mc.options.attackKey.isPressed() && mc.targetedEntity instanceof LivingEntity) {
            swapToWeapon();
        }
    }
    
    public void swapToWeapon() {
        int slot = -1;
        double bestDamage = getAttackDamage(mc.player.getMainHandStack());
        
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (!(stack.getItem() instanceof SwordItem) && !(stack.getItem() instanceof AxeItem)) {
                continue;
            }

            double damage = getAttackDamage(stack);
            if (damage > bestDamage) {
                bestDamage = damage;
                slot = i;
            }
        }
        
        if (slot != -1 && mc.player.getInventory().selectedSlot != slot) {
            mc.player.getInventory().selectedSlot = slot;
        }
    }

    private double getAttackDamage(ItemStack stack) {
        double damage = 0.0;
        for (EntityAttributeModifier modifier : stack.getAttributeModifiers(EquipmentSlot.MAINHAND).get(EntityAttributes.GENERIC_ATTACK_DAMAGE)) {
            damage += modifier.getValue();
        }
        return damage;
    }
}
