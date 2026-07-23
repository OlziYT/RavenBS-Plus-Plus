package xyz.ravenbs.module.impl.fun;

import xyz.ravenbs.module.Module;
import xyz.ravenbs.module.ModuleCategory;
import xyz.ravenbs.module.setting.impl.SliderSetting;
import xyz.ravenbs.module.setting.impl.ButtonSetting;
import java.util.Random;

public class Derp extends Module {
    private SliderSetting speed;
    private ButtonSetting headless;
    private Random random = new Random();

    public Derp() {
        super("Derp", ModuleCategory.fun);
        this.registerSetting(speed = new SliderSetting("Speed", 10, 1, 20, 1));
        this.registerSetting(headless = new ButtonSetting("Headless", false));
    }

    @Override
    public void onUpdate() {
        if (mc.player == null) return;
        
        if (random.nextInt(20) < speed.getInput()) {
            float yaw = random.nextFloat() * 360;
            float pitch = random.nextFloat() * 180 - 90;
            
            if (headless.isToggled()) {
                pitch = 180; // Looks broken
            }
            
            mc.player.setYaw(yaw);
            mc.player.setPitch(pitch);
            mc.player.setHeadYaw(yaw);
        }
    }
}
