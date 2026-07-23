package xyz.ravenbs.module.impl.player;

import xyz.ravenbs.module.Module;
import xyz.ravenbs.module.ModuleCategory;
import xyz.ravenbs.module.setting.impl.ButtonSetting;
import xyz.ravenbs.module.setting.impl.DescriptionSetting;
import xyz.ravenbs.module.setting.impl.SliderSetting;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;

public class AutoPlace extends Module {
    private SliderSetting delay;
    private ButtonSetting mouseDown;
    private long lastPlace;

    public AutoPlace() {
        super("AutoPlace", ModuleCategory.player);
        this.registerSetting(new DescriptionSetting("Hold right click to place blocks"));
        this.registerSetting(mouseDown = new ButtonSetting("Require Mouse Down", true));
        this.registerSetting(delay = new SliderSetting("Delay ms", 20, 0, 500, 10));
    }

    @Override
    public void onUpdate() {
        if (mouseDown.isToggled() && !mc.options.useKey.isPressed()) return;

        if (System.currentTimeMillis() - lastPlace < delay.getInput()) return;
            
        HitResult hit = mc.crosshairTarget;
        
        if (hit != null && hit.getType() == HitResult.Type.BLOCK) {
            BlockHitResult blockHit = (BlockHitResult) hit;
            
            // Perform place
            if (mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, blockHit).isAccepted()) {
                    mc.player.swingHand(Hand.MAIN_HAND);
                    lastPlace = System.currentTimeMillis();
            }
        }
    }
}
