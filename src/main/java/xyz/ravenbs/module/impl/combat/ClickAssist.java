package xyz.ravenbs.module.impl.combat;

import xyz.ravenbs.module.Module;
import xyz.ravenbs.module.ModuleCategory;
import xyz.ravenbs.module.setting.impl.ButtonSetting;
import xyz.ravenbs.module.setting.impl.DescriptionSetting;
import xyz.ravenbs.module.setting.impl.SliderSetting;
import xyz.ravenbs.utility.Utils;
import net.minecraft.item.BlockItem;
import org.lwjgl.glfw.GLFW;

import java.awt.*;

public class ClickAssist extends Module {
    private SliderSetting chanceLeft;
    private SliderSetting chanceRight;
    private ButtonSetting rightClick;
    private ButtonSetting blocksOnly;
    private ButtonSetting weaponOnly;
    private ButtonSetting leftClick;
    private ButtonSetting disableInCreative;
    private Robot bot;
    private boolean ignNL = false;
    private boolean ignNR = false;

    public ClickAssist() {
        super("ClickAssist", ModuleCategory.combat);
        this.registerSetting(new DescriptionSetting("Boost your CPS."));
        this.registerSetting(disableInCreative = new ButtonSetting("Disable in creative", true));
        this.registerSetting(leftClick = new ButtonSetting("Left click", true));
        this.registerSetting(chanceLeft = new SliderSetting("Chance left", 80.0, 0.0, 100.0, 1.0));
        this.registerSetting(weaponOnly = new ButtonSetting("Weapon only", true));
        this.registerSetting(rightClick = new ButtonSetting("Right click", false));
        this.registerSetting(chanceRight = new SliderSetting("Chance right", 80.0, 0.0, 100.0, 1.0));
        this.registerSetting(blocksOnly = new ButtonSetting("Blocks only", true));
    }

    @Override
    public void onEnable() {
        try {
            this.bot = new Robot();
        } catch (AWTException e) {
            this.disable();
        }
    }

    @Override
    public void onDisable() {
        this.ignNL = false;
        this.ignNR = false;
        this.bot = null;
    }

    @Override
    public void onUpdate() {
        if (mc.player == null || mc.currentScreen != null) return;
        if (disableInCreative.isToggled() && mc.player.isCreative()) return;
        if (bot == null) return;
        
        // Left click boost
        if (leftClick.isToggled() && GLFW.glfwGetMouseButton(mc.getWindow().getHandle(), GLFW.GLFW_MOUSE_BUTTON_1) == GLFW.GLFW_PRESS) {
            if (weaponOnly.isToggled() && !Utils.isHoldingWeapon()) return;
            
            if (Math.random() * 100 < chanceLeft.getInput()) {
                if (!ignNL) {
                    bot.mouseRelease(16);
                    bot.mousePress(16);
                    ignNL = true;
                } else {
                    ignNL = false;
                }
            }
        }
        
        // Right click boost
        if (rightClick.isToggled() && GLFW.glfwGetMouseButton(mc.getWindow().getHandle(), GLFW.GLFW_MOUSE_BUTTON_2) == GLFW.GLFW_PRESS) {
            if (blocksOnly.isToggled() && !(mc.player.getMainHandStack().getItem() instanceof BlockItem)) return;
            
            if (Math.random() * 100 < chanceRight.getInput()) {
                if (!ignNR) {
                    bot.mouseRelease(4);
                    bot.mousePress(4);
                    ignNR = true;
                } else {
                    ignNR = false;
                }
            }
        }
    }
}
