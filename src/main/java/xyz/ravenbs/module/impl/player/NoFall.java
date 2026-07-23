package xyz.ravenbs.module.impl.player;

import xyz.ravenbs.module.Module;
import xyz.ravenbs.module.ModuleCategory;
import xyz.ravenbs.module.setting.impl.SliderSetting;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

public class NoFall extends Module {
    private SliderSetting mode;
    private String[] modes = new String[] { "Spoof", "No Ground" }; // "No Ground" is experimental, usually Spoof is standard

    public NoFall() {
        super("NoFall", ModuleCategory.player);
        this.registerSetting(mode = new SliderSetting("Mode", 0, modes));
    }

    @Override
    public void onPreMotion(xyz.ravenbs.event.PreMotionEvent e) {
        if (mc.player.fallDistance > 2.5) {
             e.setOnGround(true);
        }
    }
}
