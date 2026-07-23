package xyz.ravenbs.module.impl.other;

import xyz.ravenbs.module.Module;
import xyz.ravenbs.module.ModuleCategory;
import xyz.ravenbs.module.setting.impl.SliderSetting;
import xyz.ravenbs.utility.Utils;
import net.minecraft.util.math.BlockPos;

public class AntiAFK extends Module {
    private SliderSetting delay;
    private long lastTime;

    public AntiAFK() {
        super("AntiAFK", ModuleCategory.other);
        this.registerSetting(delay = new SliderSetting("Delay (sec)", 5, 1, 60, 1));
    }

    @Override
    public void onUpdate() {
        if (mc.player == null) return;
        
        if (System.currentTimeMillis() - lastTime > delay.getInput() * 1000) {
            if (mc.player.isOnGround()) {
                mc.player.jump();
            }
            lastTime = System.currentTimeMillis();
        }
    }
}
