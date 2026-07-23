package xyz.ravenbs.utility;

import net.minecraft.client.render.*;
import net.minecraft.util.Identifier;

/**
 * Custom RenderLayer for Chams effect (render through walls)
 */
public class ChamsRenderLayer extends RenderLayer {
    
    public ChamsRenderLayer(String name, VertexFormat vertexFormat, VertexFormat.DrawMode drawMode, int expectedBufferSize, boolean hasCrumbling, boolean translucent, Runnable startAction, Runnable endAction) {
        super(name, vertexFormat, drawMode, expectedBufferSize, hasCrumbling, translucent, startAction, endAction);
    }

    private static final RenderLayer CHAMS_LAYER = RenderLayer.of(
        "chams",
        VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL,
        VertexFormat.DrawMode.QUADS,
        1536,
        true,
        false,
        RenderLayer.MultiPhaseParameters.builder()
            .program(RenderPhase.ENTITY_TRANSLUCENT_PROGRAM)
            .texture(RenderPhase.NO_TEXTURE)
            .transparency(RenderPhase.TRANSLUCENT_TRANSPARENCY)
            .cull(RenderPhase.DISABLE_CULLING)
            .lightmap(RenderPhase.ENABLE_LIGHTMAP)
            .overlay(RenderPhase.ENABLE_OVERLAY_COLOR)
            .depthTest(RenderPhase.ALWAYS_DEPTH_TEST) // This makes it render through walls!
            .writeMaskState(RenderPhase.COLOR_MASK)
            .build(false)
    );

    // For solid blocks like Beds: Use Depth Range Hack to fix "wood on top" while keeping it visible
    public static RenderLayer getChamsLayerSolid(Identifier texture) {
        return RenderLayer.of(
            "chams_solid", 
            VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL,
            VertexFormat.DrawMode.QUADS,
            1536,
            true,
            false,
            RenderLayer.MultiPhaseParameters.builder()
                .program(RenderPhase.ENTITY_SOLID_PROGRAM) // Solid for beds
                .texture(new RenderPhase.Texture(texture, false, false))
                .transparency(RenderPhase.NO_TRANSPARENCY)
                .cull(RenderPhase.DISABLE_CULLING)
                .lightmap(RenderPhase.ENABLE_LIGHTMAP)
                .overlay(RenderPhase.ENABLE_OVERLAY_COLOR)
                .depthTest(RenderPhase.LEQUAL_DEPTH_TEST) // Correct sorting
                .layering(POLYGON_OFFSET_LAYERING) // Force to front
                .writeMaskState(RenderPhase.ALL_MASK) 
                .build(false)
        );
    }

    // For entities like Players: Use Classic Chams (Always Depth Test) to avoid Z-fighting inner layers
    public static RenderLayer getChamsLayerEntity(Identifier texture) {
        return RenderLayer.of(
            "chams_entity", 
            VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL,
            VertexFormat.DrawMode.QUADS,
            1536,
            true,
            false,
            RenderLayer.MultiPhaseParameters.builder()
                .program(RenderPhase.ENTITY_TRANSLUCENT_PROGRAM) // Translucent for players
                .texture(new RenderPhase.Texture(texture, false, false))
                .transparency(RenderPhase.TRANSLUCENT_TRANSPARENCY)
                .cull(RenderPhase.ENABLE_CULLING)
                .lightmap(RenderPhase.ENABLE_LIGHTMAP)
                .overlay(RenderPhase.ENABLE_OVERLAY_COLOR)
                .depthTest(RenderPhase.ALWAYS_DEPTH_TEST) // Classic "See through walls"
                .writeMaskState(RenderPhase.COLOR_MASK) // Don't write depth to avoid fighting with normal pass
                .build(false)
        );
    }

    private static final RenderLayer.Layering POLYGON_OFFSET_LAYERING = new RenderLayer.Layering("polygon_offset_layering", () -> {
        // Range 0.0 - 0.01 forces the bed to render 'on top' of the world but keeps internal sorting relatively correct
        org.lwjgl.opengl.GL11.glDepthRange(0.0, 0.01); 
    }, () -> {
        org.lwjgl.opengl.GL11.glDepthRange(0.0, 1.0);
    });
}
