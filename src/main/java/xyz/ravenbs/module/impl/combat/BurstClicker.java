package xyz.ravenbs.module.impl.combat;

import xyz.ravenbs.module.Module;
import xyz.ravenbs.module.ModuleCategory;
import xyz.ravenbs.module.setting.impl.SliderSetting;
import xyz.ravenbs.utility.Utils;
import net.minecraft.util.hit.BlockHitResult;

public class BurstClicker extends Module {
    private SliderSetting clicks, delay;
    private long lastBurst;

    public BurstClicker() {
        super("BurstClicker", ModuleCategory.combat);
        this.registerSetting(clicks = new SliderSetting("Clicks", 5, 1, 20, 1));
        this.registerSetting(delay = new SliderSetting("Delay sec", 1, 0.5, 5, 0.5));
    }

    @Override
    public void onUpdate() {
        if (mc.options.attackKey.isPressed()) {
            if (System.currentTimeMillis() - lastBurst > delay.getInput() * 1000) {
                // Perform burst
                // Very naive: just click X times in one tick? Or spread?
                // Usually BurstClicker dumps clicks.
                for (int i = 0; i < clicks.getInput(); i++) {
                    disableSafety();
                    // Fix arguments for attackBlock
                    if (mc.crosshairTarget instanceof BlockHitResult) {
                        BlockHitResult mousePos = (BlockHitResult) mc.crosshairTarget;
                        mc.interactionManager.attackBlock(mousePos.getBlockPos(), mousePos.getSide());
                    }
                    // Attack Entity?
                    if (mc.targetedEntity != null) {
                        mc.interactionManager.attackEntity(mc.player, mc.targetedEntity);
                    } else {
                        mc.player.swingHand(net.minecraft.util.Hand.MAIN_HAND);
                    }
                }
                lastBurst = System.currentTimeMillis();
            }
        }
    }
    
    private void disableSafety() {
        // Reset attack cooldown if needed
    }
}
