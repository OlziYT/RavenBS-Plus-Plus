package xyz.ravenbs.module.impl.movement;

import xyz.ravenbs.module.Module;
import xyz.ravenbs.module.ModuleCategory;
import xyz.ravenbs.module.setting.impl.ButtonSetting;
import net.minecraft.util.math.Vec3d;

public class StopMotion extends Module {
    private ButtonSetting stopX;
    private ButtonSetting stopY;
    private ButtonSetting stopZ;

    public StopMotion() {
        super("StopMotion", ModuleCategory.movement);
        this.registerSetting(stopX = new ButtonSetting("Stop X", true));
        this.registerSetting(stopY = new ButtonSetting("Stop Y", true));
        this.registerSetting(stopZ = new ButtonSetting("Stop Z", true));
    }

    @Override
    public void onEnable() {
        if (mc.player == null) {
            this.disable();
            return;
        }
        
        Vec3d vel = mc.player.getVelocity();
        double x = stopX.isToggled() ? 0 : vel.x;
        double y = stopY.isToggled() ? 0 : vel.y;
        double z = stopZ.isToggled() ? 0 : vel.z;
        
        mc.player.setVelocity(x, y, z);
        this.disable();
    }
}
