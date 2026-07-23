package xyz.ravenbs.module.impl.player;

import xyz.ravenbs.module.Module;
import xyz.ravenbs.module.ModuleCategory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.FireballEntity;
import net.minecraft.util.Hand;

public class AntiFireball extends Module {
    public AntiFireball() {
        super("AntiFireball", ModuleCategory.player);
    }

    @Override
    public void onUpdate() {
        if (mc.world == null || mc.player == null) return;
        
        for (Entity e : mc.world.getEntities()) {
            if (e instanceof FireballEntity && mc.player.distanceTo(e) < 5) {
                mc.interactionManager.attackEntity(mc.player, e);
                mc.player.swingHand(Hand.MAIN_HAND);
                break; // Hit one per tick
            }
        }
    }
}
