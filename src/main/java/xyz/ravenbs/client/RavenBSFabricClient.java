package xyz.ravenbs.client;

import xyz.ravenbs.module.ModuleManager;
import xyz.ravenbs.utility.Utils;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;

import xyz.ravenbs.config.ConfigManager;

public class RavenBSFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        Utils.mc = MinecraftClient.getInstance();

        ModuleManager moduleManager = new ModuleManager();
        moduleManager.register();
        ConfigManager.bootstrap();
        xyz.ravenbs.utility.FriendManager.initialize();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player != null && client.world != null) {
                ModuleManager.onUpdate();
            }
        });

        HudRenderCallback.EVENT.register((drawContext, tickDelta) -> {
            ModuleManager.onRender(drawContext, tickDelta);
            if (ModuleManager.bridgeInfo != null && ModuleManager.bridgeInfo.isEnabled()) {
                 xyz.ravenbs.module.impl.render.BridgeInfo.onRender(drawContext);
            }
        });

        WorldRenderEvents.AFTER_ENTITIES.register(context -> {
            ModuleManager.onRenderWorld(context);

            // Chams: Second render pass with depth disabled
            xyz.ravenbs.utility.ChamsRenderer.renderChams(context);
        });

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            ConfigManager.saveActiveConfig();
            xyz.ravenbs.utility.FriendManager.save();
        }));

        // Update Checker
        try {
            xyz.ravenbs.RavenBSFabric.LOGGER.info("Starting Update Checker...");
            xyz.ravenbs.utility.UpdateChecker.checkForUpdates();
        } catch (Exception e) {
            xyz.ravenbs.RavenBSFabric.LOGGER.error("Failed to start Update Checker", e);
        }
        
        net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            xyz.ravenbs.utility.FriendManager.refreshOnlineIdentities();
            xyz.ravenbs.utility.UpdateChecker.onJoin();
        });

        xyz.ravenbs.RavenBSFabric.LOGGER.info("RavenBS-Fabric initialized");
    }
}
