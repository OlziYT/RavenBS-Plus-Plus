package xyz.ravenbs;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RavenBSFabric implements ModInitializer {
    public static final String MOD_ID = "ravenbs-fabric";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Hello from RavenBS-Fabric!");
    }
}
