package xyz.ravenbs.mixin.accessor;

import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MinecraftClient.class)
public interface IMixinMinecraftClient {
    @Accessor("itemUseCooldown")
    void setItemUseCooldown(int itemUseCooldown);

    @Accessor("itemUseCooldown")
    int getItemUseCooldown(); // Why duplicate? Removing duplicate.
    
    @Accessor("attackCooldown")
    void setAttackCooldown(int attackCooldown);

    @org.spongepowered.asm.mixin.gen.Invoker("doAttack")
    boolean invokeDoAttack();
}
