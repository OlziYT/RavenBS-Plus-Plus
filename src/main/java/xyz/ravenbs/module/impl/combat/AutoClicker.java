package xyz.ravenbs.module.impl.combat;

import xyz.ravenbs.module.Module;
import xyz.ravenbs.module.ModuleCategory;
import xyz.ravenbs.module.setting.impl.ButtonSetting;
import xyz.ravenbs.module.setting.impl.SliderSetting;
import xyz.ravenbs.utility.Utils;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

import java.util.Random;

public class AutoClicker extends Module {
    private SliderSetting minCPS;
    private SliderSetting maxCPS;
    private SliderSetting jitter;
    private ButtonSetting weaponOnly;
    private ButtonSetting breakBlocks; // prevent clicking while breaking
    
    private ButtonSetting alwaysOn;
    
    private final Random rand = new Random();
    private long nextClickTime;
    private boolean leftMouseDown;

    public AutoClicker() {
        super("AutoClicker", ModuleCategory.combat);
        this.registerSetting(minCPS = new SliderSetting("Min CPS", 9, 1, 42, 0.5));
        this.registerSetting(maxCPS = new SliderSetting("Max CPS", 12, 1, 42, 0.5));
        this.registerSetting(jitter = new SliderSetting("Jitter", 0, 0, 3, 0.1));
        this.registerSetting(alwaysOn = new ButtonSetting("Always on via bind", false));
        this.registerSetting(weaponOnly = new ButtonSetting("Weapon only", false));
        this.registerSetting(breakBlocks = new ButtonSetting("Break blocks", false));
    }

    @Override
    public void onEnable() {
        nextClickTime = 0L;
    }

    @Override
    public void onDisable() {
        nextClickTime = 0L;
    }
    
    @Override
    public void onUpdate() {
        if (mc.getWindow() != null) {
            leftMouseDown = InputUtil.isKeyPressed(mc.getWindow().getHandle(), GLFW.GLFW_MOUSE_BUTTON_1);
        } else {
            leftMouseDown = false;
        }

        if (mc.currentScreen != null || mc.player == null || mc.interactionManager == null) {
            nextClickTime = 0L;
            return;
        }

        boolean shouldClick = alwaysOn.isToggled() || leftMouseDown;
        if (weaponOnly.isToggled() && !Utils.isHoldingWeapon()) {
            shouldClick = false;
        }
        if (!breakBlocks.isToggled() && mc.interactionManager.isBreakingBlock()) {
            shouldClick = false;
        }

        if (!shouldClick) {
            nextClickTime = 0L;
            return;
        }

        long now = System.currentTimeMillis();
        if (nextClickTime == 0L) {
            nextClickTime = now;
        }
        if (now < nextClickTime) {
            return;
        }

        xyz.ravenbs.mixin.accessor.IMixinMinecraftClient accessor = (xyz.ravenbs.mixin.accessor.IMixinMinecraftClient) mc;
        accessor.setAttackCooldown(0);
        accessor.invokeDoAttack();

        if (jitter.getInput() > 0) {
            float yaw = mc.player.getYaw();
            float pitch = mc.player.getPitch();
            float yawRandom = (float) ((rand.nextDouble() - 0.5) * jitter.getInput());
            float pitchRandom = (float) ((rand.nextDouble() - 0.5) * jitter.getInput());
            mc.player.setYaw(yaw + yawRandom);
            mc.player.setPitch(pitch + pitchRandom);
        }

        nextClickTime = now + computeClickDelay();
    }

    private long computeClickDelay() {
        double min = minCPS.getInput();
        double max = maxCPS.getInput();
        if (min > max) {
            double swap = min;
            min = max;
            max = swap;
        }

        double cps = min + (rand.nextDouble() * (max - min));
        return Math.max(1L, (long) (1000.0 / cps));
    }
}
