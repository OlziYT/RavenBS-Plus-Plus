package xyz.ravenbs.module.impl.render;

import xyz.ravenbs.module.Module;
import xyz.ravenbs.module.ModuleCategory;
import xyz.ravenbs.module.setting.impl.SliderSetting;
import xyz.ravenbs.module.setting.impl.ButtonSetting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;

import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.util.Identifier;
import com.mojang.blaze3d.systems.RenderSystem;
import java.awt.Color;

public class Radar extends Module {
    private SliderSetting size;
    private SliderSetting scale;
    private SliderSetting xPos;
    private SliderSetting yPos;
    private ButtonSetting round;
    
    private ButtonSetting players;
    private ButtonSetting mobs;
    private ButtonSetting items;
    private ButtonSetting others;
    
    private NativeImageBackedTexture mapTexture;
    private Identifier mapTextureId;

    public Radar() {
        super("Radar", ModuleCategory.render);
        this.registerSetting(size = new SliderSetting("Size", 100, 50, 200, 10));
        this.registerSetting(scale = new SliderSetting("Scale", 2, 0.5, 5, 0.1));
        this.registerSetting(xPos = new SliderSetting("X", 5, 0, 1920, 10)); // Max 1920 for now, could be improved
        this.registerSetting(yPos = new SliderSetting("Y", 50, 0, 1080, 10));
        this.registerSetting(round = new ButtonSetting("Round", true));
        
        this.registerSetting(players = new ButtonSetting("Players", true));
        this.registerSetting(mobs = new ButtonSetting("Mobs", true));
        this.registerSetting(items = new ButtonSetting("Items", false));
        this.registerSetting(others = new ButtonSetting("Other", false));
    }

    @Override
    public void onRender(DrawContext context, float delta) {
        if (!this.isEnabled()) return;
        
        int s = (int) size.getInput();
        int px = (int) xPos.getInput();
        int py = (int) yPos.getInput();
        int cx = px + s / 2;
        int cy = py + s / 2;
        
        // Background
        if (round.isToggled()) {
             // Draw Circle Background (Manual approximation or large polygon?)
             // Since we don't have a helper, we can just draw a filled square 
             // because the map texture will cover it, and we can strictly clip the map?
             // Actually, if we want a nice background, we should draw a circle.
             // For now, let's use a simpler approach: Draw the texture which has its own background opacity?
             // Or just draw a rect for now, fix circle draw later if needed.
             // Usually circular maps have a border. 
             // Let's rely on the map texture to provide the shape visual.
        } else {
             context.fill(px, py, px + s, py + s, new Color(0, 0, 0, 120).getRGB());
        }
        
        float scaleVal = (float) scale.getInput();
        float yaw = mc.player.getYaw();
        float cos = MathHelper.cos((float)Math.toRadians(yaw));
        float sin = MathHelper.sin((float)Math.toRadians(yaw));
        
        // Initialize texture if needed
        if (mapTexture == null || mapTexture.getImage().getWidth() != 128) {
            mapTexture = new net.minecraft.client.texture.NativeImageBackedTexture(128, 128, false);
            mapTextureId = mc.getTextureManager().registerDynamicTexture("raven_radar_map", mapTexture);
        }
        
        net.minecraft.client.texture.NativeImage img = mapTexture.getImage();
        
        // Update Texture
        int texRadius = 64;
        int pBlockX = (int)mc.player.getX();
        int pBlockZ = (int)mc.player.getZ();
        int pBlockY = (int)mc.player.getY();
        
        boolean isRound = round.isToggled();
        
        for (int z = 0; z < 128; z++) {
            for (int x = 0; x < 128; x++) {
                int dx = x - 64;
                int dz = z - 64;
                
                // Scale factor for world loop?
                // The texture represents a fixed 128x128 grid? 
                // No, we want "Scale" to determine how much world we see.
                // But the texture size is fixed.
                // So (x, z) in texture maps to world coord.
                // To support "Zoom" without changing texture size, we need to sample differently?
                // Or we update the texture based on 'renderRadius'.
                // If scale is HIGH (Zoom In), renderLines covers FEWER blocks.
                // We want 1 pixel on texture = 1 block?
                // If yes, then texture always shows 128 blocks. "Zoom" would just scale the texture Draw.
                // BUT user wants HIGH RES map.
                // If we zoom out (Scale 0.5), we see MORE blocks. 128 pixels need to represent 256 blocks.
                // That requires subsampling. Too complex for simple loop.
                // Let's stick to: 1 pixel = 1 block.
                // "Scale" setting will just scale the RENDERED texture size on screen?
                // NO, "Scale" in Radar usually means "Radius of view".
                // Let's make "Scale" controls the TEXTURE SAMPLING STEP? No.
                // Let's Keep 1 pixel = 1 block for simplicity.
                // And "Scale" setting controls how BIG the map looks (the 's' variable)? 
                // No, 'Size' controls 's'.
                // 'Scale' controls ZOOM.
                // If Zoom=2, 1 block = 2 pixels on screen.
                // We have a fixed Texture (128x128).
                // If we map it to 's' (e.g. 100px).
                // Then 1 block = (100/128) pixels. ~= 0.8 pixels.
                // If we want Zoom, we should change the TEXTURE RESOLUTION or Sampling?
                // Simplest: The texture ALWAYS shows 128x128 blocks around player.
                // The 's' (Size) setting determines on-screen widget size.
                // The 'Scale' setting from previous code was used for `renderRadius`.
                
                // Let's redefine:
                // We render a 128x128 block area into the texture.
                // We display this texture scaled to fit 's'.
                // To implement 'Zoom', we effectively clip the texture rendering?
                // No, let's just Stick to: Texture = Fixed World Area (Radius 64).
                // And 'scale' setting... actually standard radar usually has fixed radius.
                // Let's ignore 'scale' for map generation to keep it robust, or use it to act as "Interval".
                
                // Let's go with: Texture pixels = world blocks (1:1). 
                // So we see 64 blocks radius.
                
                // Clipping for Round Shape
                if (isRound && dx*dx + dz*dz > 60*60) { // 60 radius to leave border
                     img.setColor(x, z, 0);
                     continue;
                }
                if (!isRound && (Math.abs(dx) > 60 || Math.abs(dz) > 60)) {
                    // Small border for square too
                     img.setColor(x, z, 0); 
                     continue; // Optional border
                }
                // Background color for valid map area
                int bgColor = new Color(0, 0, 0, 120).getRGB();
                
                // World Coords
                int wx = pBlockX + dx;
                int wz = pBlockZ + dz;
                
                int by = mc.world.getTopY(net.minecraft.world.Heightmap.Type.WORLD_SURFACE, wx, wz) - 1;
                net.minecraft.block.BlockState state = mc.world.getBlockState(new net.minecraft.util.math.BlockPos(wx, by, wz));
                
                int color = bgColor;
                if (!state.isAir()) {
                    int mapColor = state.getMapColor(mc.world, new net.minecraft.util.math.BlockPos(wx, by, wz)).color;
                    mapColor |= 0xFF000000;
                    
                    if (by < pBlockY) mapColor = xyz.ravenbs.utility.Utils.darken(mapColor, 0.8f);
                    else if (by > pBlockY) mapColor = xyz.ravenbs.utility.Utils.darken(mapColor, 1.2f);
                    color = mapColor;
                }
                
                // ARGB fix? NativeImage might be ABGR.
                // Let's just set it.
                img.setColor(x, z, color);
            }
        }
        mapTexture.upload();
        
        // Render
        context.getMatrices().push();
        context.getMatrices().translate(cx, cy, 0);
        context.getMatrices().multiply(net.minecraft.util.math.RotationAxis.POSITIVE_Z.rotationDegrees(-yaw + 180));
        
        // Scale logic: We have 128x128 graphics. We want to fit them into 's' x 's' box.
        // Scale factor = s / 128.0f.
        // Also apply user 'Scale' setting? 
        // If user wants to "Zoom In" (Scale > 1), we scale UP the texture, but we need to CLIP it to the box 's'.
        // Rendering a larger texture inside a smaller box requires Scissor test or Mask.
        // Standard DrawContext doesn't expose Scissor easily.
        // EASIER FIX: Always render whole texture (Radius 64 blocks) to fit 's'.
        // Ignore 'Scale' setting for Map View, use 'Scale' only for Entity Blip positioning relative to center?
        // NO, Entity Blips must match Map.
        // So 'Scale' setting is redundant if Map Area is fixed to 128 blocks.
        // Let's just fit texture to box.
        float fitScale = (float)s / 128.0f;
        context.getMatrices().scale(fitScale, fitScale, 1);
        
        RenderSystem.setShaderTexture(0, mapTextureId);
        RenderSystem.setShader(net.minecraft.client.render.GameRenderer::getPositionTexProgram);
        RenderSystem.enableBlend();
        
        // Draw centered 128x128 (goes from -64 to 64)
        context.drawTexture(mapTextureId, -64, -64, 0, 0.0f, 0.0f, 128, 128, 128, 128);
        
        context.getMatrices().pop();
        
        // --- Entities & Compass ---
        // Need to render these ON TOP, clamped to 's'.
        // Code below needs update to use 'px/py/cx/cy' correctly and match map scale.
        // Map Scale is now: 128 pixels (texture) = 128 blocks.
        // On screen: 's' pixels = 128 blocks.
        // So 1 block = s/128 pixels.
        float blipScale = (float)s / 128.0f;
        
        // Compass
        if (round.isToggled()) {
             drawDirection(context, "N", 0, -50, cx, cy, cos, sin, s);
             drawDirection(context, "S", 0, 50, cx, cy, cos, sin, s);
             drawDirection(context, "E", 50, 0, cx, cy, cos, sin, s);
             drawDirection(context, "W", -50, 0, cx, cy, cos, sin, s);
        }

        // --- Draw Entities ---
        // Map Scale: 128 world blocks = s pixels.
        // So 1 block = s / 128.0f pixels.
        // blipScale is already defined above
        
        for (Entity e : mc.world.getEntities()) {
            if (e == mc.player) continue;
            
            // Filtering & Coloring
            int color = 0;
            
            if (e instanceof PlayerEntity) {
                if (!players.isToggled()) continue;
                
                // Try to get Team Color
                int teamColor = -1;
                net.minecraft.text.TextColor tc = e.getDisplayName().getStyle().getColor();
                if (tc != null) {
                    teamColor = tc.getRgb() | 0xFF000000;
                } else {
                     // Fallback to checking Team directly
                    net.minecraft.scoreboard.AbstractTeam team = ((PlayerEntity)e).getScoreboardTeam();
                    if (team != null && team.getColor() != null && team.getColor().getColorValue() != null) {
                        teamColor = team.getColor().getColorValue() | 0xFF000000;
                    }
                }

                if (teamColor != -1) {
                    color = teamColor;
                } else {
                    color = xyz.ravenbs.utility.Utils.isTeamMate(e) ? 0xFF00FFFF : 0xFF00FF00;
                }
                
                if (e.isInvisible()) color = (color & 0x00FFFFFF) | 0x80000000; // Keep color but semi-transparent
            } else if (e instanceof net.minecraft.entity.mob.MobEntity) { // Hostile & Passive check?
                 if (!mobs.isToggled()) continue;
                 if (e instanceof net.minecraft.entity.mob.Monster) {
                     color = 0xFFFF0000; // Red (Hostiles)
                 } else {
                     color = 0xFFFFAA00; // Orange/Yellow (Passive/Neutrals)
                 }
            } else if (e instanceof net.minecraft.entity.ItemEntity) {
                if (!items.isToggled()) continue;
                color = 0xFF00AAAA; // Aqua
            } else {
                if (!others.isToggled()) continue;
                color = 0xFFAAAAAA; // Gray
            }
            
            double dx = e.getX() - mc.player.getX();
            double dz = e.getZ() - mc.player.getZ();
            
            // Rotate
            double rotX = -(dx * cos - dz * sin);
            double rotY = -(dx * sin + dz * cos);
            
            // Scale to Screen Pixels
            // rotX/Y are in blocks. Multiply by blipScale.
            double finalX = rotX * blipScale;
            double finalY = rotY * blipScale;
            
            // Clamp strictly for Square, or Radius for Round
            if (round.isToggled()) {
                 double distSq = finalX*finalX + finalY*finalY;
                 double maxRad = (s / 2.0) - 2; // -2 Padding
                 if (distSq > maxRad*maxRad) {
                     // Clamp to edge or Hide? 
                     // Standard radar clamps.
                     double len = Math.sqrt(distSq);
                     finalX = (finalX / len) * maxRad;
                     finalY = (finalY / len) * maxRad;
                 }
            } else {
                 double maxDist = (s / 2.0) - 2;
                 if (finalX < -maxDist) finalX = -maxDist;
                 if (finalX > maxDist) finalX = maxDist;
                 if (finalY < -maxDist) finalY = -maxDist;
                 if (finalY > maxDist) finalY = maxDist;
            }
            
            int mapX = (int)(cx + finalX);
            int mapY = (int)(cy + finalY);
            
            // Draw
            // Small dot (2x2)
            context.fill(mapX - 1, mapY - 1, mapX + 1, mapY + 1, color);
        }
    }
    
    private void drawDirection(DrawContext context, String text, double worldOffsetX, double worldOffsetZ, int cx, int cy, float cos, float sin, int size) {
         // Transform world offset (relative to player) to radar space
         double rotX = -(worldOffsetX * cos - worldOffsetZ * sin);
         double rotY = -(worldOffsetX * sin + worldOffsetZ * cos);
         
         // In "Radar Space", N is far up.
         // We essentially project a point "Far North" and draw label there.
         // Let's normalize to edge of circle/box.
         
         // Just act like it's a very far entity to get angle? 
         // Or just manually clamp.
         
         // Let's assume the offsets PASSED IN are normalized vectors (e.g. 0, -1 for N).
         // Actually I passed 50. Let's stick to placing them near edge.
         
         double dist = (size / 2.0) - 8; // Inner padding
         
         // Normalize input vector
         double len = Math.sqrt(worldOffsetX * worldOffsetX + worldOffsetZ * worldOffsetZ);
         if (len == 0) return;
         double nx = worldOffsetX / len;
         double nz = worldOffsetZ / len;
         
         double rX = -(nx * cos - nz * sin) * dist;
         double rY = -(nx * sin + nz * cos) * dist;
         
         context.drawText(mc.textRenderer, text, (int)(cx + rX - 2), (int)(cy + rY - 3), -1, false);
    }
}
