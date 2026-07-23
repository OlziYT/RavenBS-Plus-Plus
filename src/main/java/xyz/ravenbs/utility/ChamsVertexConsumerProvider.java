package xyz.ravenbs.utility;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;

public class ChamsVertexConsumerProvider implements VertexConsumerProvider {
    private final VertexConsumerProvider parent;
    private final Identifier texture;
    private final boolean ignoreDepth;
    private final boolean isSolid;

    public ChamsVertexConsumerProvider(VertexConsumerProvider parent, Identifier texture, boolean ignoreDepth, boolean isSolid) {
        this.parent = parent;
        this.texture = texture;
        this.ignoreDepth = ignoreDepth;
        this.isSolid = isSolid;
    }

    @Override
    public VertexConsumer getBuffer(RenderLayer layer) {
        if (ignoreDepth) {
            // Only apply Chams to layers that use the specific entity vertex format.
            // Text rendering (nametags) and other UI elements use different formats and will crash
            // if we try to provide overlay/normal data to them.
            if (layer.getVertexFormat() != VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL) {
                return parent.getBuffer(layer);
            }

            // Attempt to extract the texture
            String layerStr = layer.toString();
            Identifier resolvedTexture = this.texture;
            try {
                java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("texture=Texture\\[id=([^,]+)");
                java.util.regex.Matcher matcher = pattern.matcher(layerStr);
                if (matcher.find()) {
                    resolvedTexture = new Identifier(matcher.group(1));
                }
            } catch (Exception ignored) {}

            if (resolvedTexture != null) {
                // Return a Dual Consumer that renders BOTH the normal layer (for correct depth/sorting)
                // AND the Chams layer (for visibility through walls).
                VertexConsumer normalBuffer = parent.getBuffer(layer);
                RenderLayer chamsLayer = isSolid ? 
                    ChamsRenderLayer.getChamsLayerSolid(resolvedTexture) : 
                    ChamsRenderLayer.getChamsLayerEntity(resolvedTexture);
                
                VertexConsumer chamsBuffer = parent.getBuffer(chamsLayer);
                return new DualVertexConsumer(normalBuffer, chamsBuffer);
            }
        }
        return parent.getBuffer(layer);
    }

    // Inner class to forward vertices to two consumers
    private static class DualVertexConsumer implements VertexConsumer {
        private final VertexConsumer first;
        private final VertexConsumer second;

        public DualVertexConsumer(VertexConsumer first, VertexConsumer second) {
            this.first = first;
            this.second = second;
        }

        @Override public VertexConsumer vertex(double x, double y, double z) { first.vertex(x, y, z); second.vertex(x, y, z); return this; }
        @Override public VertexConsumer color(int red, int green, int blue, int alpha) { first.color(red, green, blue, alpha); second.color(red, green, blue, alpha); return this; }
        @Override public VertexConsumer texture(float u, float v) { first.texture(u, v); second.texture(u, v); return this; }
        @Override public VertexConsumer overlay(int u, int v) { first.overlay(u, v); second.overlay(u, v); return this; }
        @Override public VertexConsumer light(int u, int v) { first.light(u, v); second.light(u, v); return this; }
        @Override public VertexConsumer normal(float x, float y, float z) { first.normal(x, y, z); second.normal(x, y, z); return this; }
        @Override public void next() { first.next(); second.next(); }
        @Override public void fixedColor(int red, int green, int blue, int alpha) { first.fixedColor(red, green, blue, alpha); second.fixedColor(red, green, blue, alpha); }
        @Override public void unfixColor() { first.unfixColor(); second.unfixColor(); }
    }
}

