package xyz.ravenbs.mixin.accessor;

import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EntityVelocityUpdateS2CPacket.class)
public interface AccessorEntityVelocityUpdateS2CPacket {
    @Accessor("velocityX")
    void setVelocityX(int velocityX);

    @Accessor("velocityY")
    void setVelocityY(int velocityY);

    @Accessor("velocityZ")
    void setVelocityZ(int velocityZ);
    
    @Accessor("velocityX")
    int getVelocityX();

    @Accessor("velocityY")
    int getVelocityY();

    @Accessor("velocityZ")
    int getVelocityZ();
}
