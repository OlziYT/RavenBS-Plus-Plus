package xyz.ravenbs.mixin.accessor;

import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ExplosionS2CPacket.class)
public interface AccessorExplosionS2CPacket {
    @Accessor("playerVelocityX")
    void setPlayerVelocityX(float playerVelocityX);

    @Accessor("playerVelocityY")
    void setPlayerVelocityY(float playerVelocityY);

    @Accessor("playerVelocityZ")
    void setPlayerVelocityZ(float playerVelocityZ);

    @Accessor("playerVelocityX")
    float getPlayerVelocityX();

    @Accessor("playerVelocityY")
    float getPlayerVelocityY();

    @Accessor("playerVelocityZ")
    float getPlayerVelocityZ();
}
