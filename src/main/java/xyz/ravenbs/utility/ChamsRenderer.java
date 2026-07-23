package xyz.ravenbs.utility;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;

/**
 * Chams rendering utility.
 * 
 * Note: Full model rendering through walls doesn't work in modern Minecraft
 * without custom shaders, because MC uses a buffered rendering pipeline that
 * ignores legacy GL state changes like glDisable(GL_DEPTH_TEST).
 * 
 * The working solution is implemented via MixinEntityGlowing which makes
 * players glow (outline visible through walls) - the same effect as Spectral Arrow.
 */
public class ChamsRenderer {
    
    public static void renderChams(WorldRenderContext context) {
        // Chams is implemented via MixinEntityGlowing (isGlowing override)
        // This method is kept for potential future use with custom shaders
    }
}
