package xyz.ravenbs.module.impl.player;

import xyz.ravenbs.event.ReceivePacketEvent;
import xyz.ravenbs.module.Module;
import xyz.ravenbs.module.ModuleCategory;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.network.packet.s2c.play.PositionFlag;

public class NoRotate extends Module {
    public NoRotate() {
        super("NoRotate", ModuleCategory.player);
    }

    @Override
    public void onReceivePacket(ReceivePacketEvent e) {
        if (mc.player == null) {
            return;
        }

        if (e.getPacket() instanceof PlayerPositionLookS2CPacket packet) {
            xyz.ravenbs.mixin.accessor.AccessorPlayerPositionLookS2CPacket accessor =
                    (xyz.ravenbs.mixin.accessor.AccessorPlayerPositionLookS2CPacket) packet;

            float yaw = packet.getFlags().contains(PositionFlag.X_ROT) ? 0.0f : mc.player.getYaw();
            float pitch = packet.getFlags().contains(PositionFlag.Y_ROT) ? 0.0f : mc.player.getPitch();

            accessor.setYaw(yaw);
            accessor.setPitch(pitch);
        }
    }
}
