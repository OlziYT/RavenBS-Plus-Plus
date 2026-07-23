package xyz.ravenbs.module.impl.combat;

import xyz.ravenbs.event.ReceivePacketEvent;
import xyz.ravenbs.module.Module;
import xyz.ravenbs.module.ModuleCategory;
import xyz.ravenbs.module.setting.impl.SliderSetting;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;

public class Velocity extends Module {
    private SliderSetting horizontal;
    private SliderSetting vertical;
    private SliderSetting chance;

    public Velocity() {
        super("Velocity", ModuleCategory.combat);
        this.registerSetting(horizontal = new SliderSetting("Horizontal", 0.0, 0.0, 100.0, 1.0));
        this.registerSetting(vertical = new SliderSetting("Vertical", 0.0, 0.0, 100.0, 1.0));
        this.registerSetting(chance = new SliderSetting("Chance", 100.0, 0.0, 100.0, 1.0));
    }

    @Override
    public void onReceivePacket(ReceivePacketEvent e) {
        if (Math.random() > chance.getInput() / 100.0) {
            return;
        }

        if (e.getPacket() instanceof EntityVelocityUpdateS2CPacket) {
            EntityVelocityUpdateS2CPacket packet = (EntityVelocityUpdateS2CPacket) e.getPacket();
            if (packet.getId() == mc.player.getId()) {
                if (horizontal.getInput() == 0 && vertical.getInput() == 0) {
                    e.setCancelled(true);
                } else {
                    xyz.ravenbs.mixin.accessor.AccessorEntityVelocityUpdateS2CPacket accessor = (xyz.ravenbs.mixin.accessor.AccessorEntityVelocityUpdateS2CPacket) packet;
                    accessor.setVelocityX((int)(accessor.getVelocityX() * horizontal.getInput() / 100.0));
                    accessor.setVelocityY((int)(accessor.getVelocityY() * vertical.getInput() / 100.0));
                    accessor.setVelocityZ((int)(accessor.getVelocityZ() * horizontal.getInput() / 100.0));
                }
            }
        } else if (e.getPacket() instanceof ExplosionS2CPacket) {
            ExplosionS2CPacket packet = (ExplosionS2CPacket) e.getPacket();
            if (horizontal.getInput() == 0 && vertical.getInput() == 0) {
                e.setCancelled(true);
            } else {
                xyz.ravenbs.mixin.accessor.AccessorExplosionS2CPacket accessor = (xyz.ravenbs.mixin.accessor.AccessorExplosionS2CPacket) packet;
                accessor.setPlayerVelocityX((float)(accessor.getPlayerVelocityX() * horizontal.getInput() / 100.0));
                accessor.setPlayerVelocityY((float)(accessor.getPlayerVelocityY() * vertical.getInput() / 100.0));
                accessor.setPlayerVelocityZ((float)(accessor.getPlayerVelocityZ() * horizontal.getInput() / 100.0));
            }
        }
    }
}
