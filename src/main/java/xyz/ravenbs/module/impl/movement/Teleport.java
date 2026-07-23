package xyz.ravenbs.module.impl.movement;

import xyz.ravenbs.module.Module;
import xyz.ravenbs.module.ModuleCategory;
import xyz.ravenbs.module.setting.impl.ButtonSetting;
import xyz.ravenbs.module.setting.impl.DescriptionSetting;
import xyz.ravenbs.module.setting.impl.SliderSetting;
import net.minecraft.util.math.Vec3d;

public class Teleport extends Module {
    private SliderSetting distance;
    private ButtonSetting groundOnly;

    public Teleport() {
        super("Teleport", ModuleCategory.movement);
        this.registerSetting(new DescriptionSetting("Teleports forward on enable."));
        this.registerSetting(distance = new SliderSetting("Distance", 5.0, 1.0, 20.0, 0.5));
        this.registerSetting(groundOnly = new ButtonSetting("Ground only", true));
    }

    @Override
    public void onEnable() {
        if (mc.player == null) {
            this.disable();
            return;
        }
        
        if (groundOnly.isToggled() && !mc.player.isOnGround()) {
            this.disable();
            return;
        }
        
        // Calculate forward direction
        float yaw = mc.player.getYaw();
        double radians = Math.toRadians(yaw);
        double dx = -Math.sin(radians) * distance.getInput();
        double dz = Math.cos(radians) * distance.getInput();
        
        Vec3d currentPos = mc.player.getPos();
        mc.player.setPosition(currentPos.x + dx, currentPos.y, currentPos.z + dz);
        
        this.disable();
    }
}
