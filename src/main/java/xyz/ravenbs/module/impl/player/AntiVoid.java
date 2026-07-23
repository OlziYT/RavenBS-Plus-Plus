package xyz.ravenbs.module.impl.player;

import xyz.ravenbs.module.Module;
import xyz.ravenbs.module.ModuleCategory;
import xyz.ravenbs.module.setting.impl.SliderSetting;

public class AntiVoid extends Module {
    private SliderSetting distance;
    
    public AntiVoid() {
        super("AntiVoid", ModuleCategory.player);
        this.registerSetting(distance = new SliderSetting("Distance", 5, 3, 10, 1));
    }

    @Override
    public void onUpdate() {
        if (mc.player == null) return;
        
        if (mc.player.fallDistance > distance.getInput() && !mc.player.isSpectator() && !mc.player.getAbilities().allowFlying) {
            // Check if void is below
            if (mc.player.getY() < mc.world.getBottomY() + 10 || isVoidBelow()) {
                // Bounce/Flag back
                // Simple motion set to jump up
                mc.player.setVelocity(mc.player.getVelocity().x, 2.0, mc.player.getVelocity().z);
                mc.player.fallDistance = 0;
            }
        }
    }
    
    private boolean isVoidBelow() {
        // Simplified check: if Y is very low or scanning down
        return mc.player.getY() < 0;
    }
}
