package xyz.ravenbs.mixin.network;

import io.netty.channel.ChannelHandlerContext;
import xyz.ravenbs.event.ReceivePacketEvent;
import xyz.ravenbs.event.SendPacketEvent;
import xyz.ravenbs.module.ModuleManager;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.PacketCallbacks;
import net.minecraft.network.packet.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientConnection.class)
public class MixinClientConnection {

    @Inject(method = "channelRead0", at = @At("HEAD"), cancellable = true)
    private void onChannelRead(ChannelHandlerContext channelHandlerContext, Packet<?> packet, CallbackInfo ci) {
        ReceivePacketEvent event = new ReceivePacketEvent(packet);
        ModuleManager.onReceivePacket(event);

        if (event.isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(method = "send(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/PacketCallbacks;)V", at = @At("HEAD"), cancellable = true)
    private void onSend(Packet<?> packet, PacketCallbacks callbacks, CallbackInfo ci) {
        SendPacketEvent event = new SendPacketEvent(packet);
        ModuleManager.onSendPacket(event);

        if (event.isCancelled()) {
            ci.cancel();
        }
    }
}
