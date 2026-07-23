package xyz.ravenbs.module.impl.combat;

import xyz.ravenbs.module.Module;
import xyz.ravenbs.module.ModuleCategory;
import xyz.ravenbs.module.setting.impl.SliderSetting;

public class JumpReset extends Module {
    private SliderSetting chance;

    public JumpReset() {
        super("JumpReset", ModuleCategory.combat);
        this.registerSetting(chance = new SliderSetting("Chance", 100, 0, 100, 1));
    }

    // Needs to hook into when player takes KB.
    // We can use the Velocity module logic pattern or generic update check if Velocity > X.
    // Or better: MixinEntity -> setVelocity.
    
    // For now, simple update check if we just took damage (hurtTime == maxHurtTime)
    
    @Override
    public void onUpdate() {
        if (mc.player.hurtTime == mc.player.maxHurtTime && mc.player.hurtTime > 0) {
            if (Math.random() * 100 <= chance.getInput()) {
                if (mc.player.isOnGround()) {
                    mc.player.jump();
                }
            }
        }
    }
}
