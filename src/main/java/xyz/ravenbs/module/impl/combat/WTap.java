package xyz.ravenbs.module.impl.combat;

import xyz.ravenbs.event.PostUpdateEvent;
import xyz.ravenbs.module.Module;
import xyz.ravenbs.module.ModuleCategory;
import xyz.ravenbs.module.setting.impl.SliderSetting;

public class WTap extends Module {
    private SliderSetting range;
    private SliderSetting chance;
    
    public WTap() {
        super("WTap", ModuleCategory.combat);
        this.registerSetting(range = new SliderSetting("Range", 3.0, 1.0, 6.0, 0.1));
        this.registerSetting(chance = new SliderSetting("Chance", 100, 0, 100, 1));
    }

    @Override
    public void onPostUpdate() {
        if (mc.player == null) return;
        
        if (mc.player.isSprinting() && mc.player.handSwingProgress > 0 && mc.player.handSwingProgress < 5) {
            // Reset sprint to deal more knockback
            if (Math.random() * 100 < chance.getInput()) {
                 mc.player.setSprinting(false);
                 // We will re-enable it next tick via Sprint module or manual hold
                 // But to ensure the server registers the stop, we check
            }
        }
    }
}
