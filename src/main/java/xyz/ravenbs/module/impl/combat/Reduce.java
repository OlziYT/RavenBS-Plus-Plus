package xyz.ravenbs.module.impl.combat;

import xyz.ravenbs.module.Module;
import xyz.ravenbs.module.ModuleCategory;
import xyz.ravenbs.module.setting.impl.DescriptionSetting;
import xyz.ravenbs.module.setting.impl.SliderSetting;
import net.minecraft.entity.Entity;

public class Reduce extends Module {
    private static SliderSetting chance;
    private static SliderSetting reduction;
    public static boolean enabled = false;

    public Reduce() {
        super("Reduce", ModuleCategory.combat);
        this.registerSetting(new DescriptionSetting("Overrides KeepSprint."));
        this.registerSetting(reduction = new SliderSetting("Attack reduction %", 60.0, 60.0, 100.0, 0.5));
        this.registerSetting(chance = new SliderSetting("Chance", 100.0, 0.0, 100.0, 1.0));
    }

    @Override
    public void onEnable() {
        enabled = true;
    }
    
    @Override
    public void onDisable() {
        enabled = false;
    }

    public static void reduce() {
        if (!enabled || mc.player == null) return;
        if (chance.getInput() == 0) return;
        
        if (chance.getInput() != 100.0 && Math.random() >= chance.getInput() / 100.0) {
            mc.player.setVelocity(
                mc.player.getVelocity().x * 0.6,
                mc.player.getVelocity().y,
                mc.player.getVelocity().z * 0.6
            );
            return;
        }
        
        double n = (100.0 - reduction.getInput()) / 100.0;
        mc.player.setVelocity(
            mc.player.getVelocity().x * n,
            mc.player.getVelocity().y,
            mc.player.getVelocity().z * n
        );
    }
}
