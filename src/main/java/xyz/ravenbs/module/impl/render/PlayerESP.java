package xyz.ravenbs.module.impl.render;

import xyz.ravenbs.module.Module;
import xyz.ravenbs.module.ModuleCategory;
import xyz.ravenbs.module.setting.impl.ButtonSetting;
import xyz.ravenbs.module.setting.impl.SliderSetting;
import xyz.ravenbs.utility.Utils;
import xyz.ravenbs.utility.RenderUtils;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public class PlayerESP extends Module {
    private SliderSetting red;
    private SliderSetting green;
    private SliderSetting blue;
    private ButtonSetting rainbow;
    private ButtonSetting box;
    private ButtonSetting teamColor;
    private ButtonSetting renderSelf;
    private ButtonSetting showInvis;

    public PlayerESP() {
        super("PlayerESP", ModuleCategory.render);
        this.registerSetting(box = new ButtonSetting("Box", true));
        this.registerSetting(red = new SliderSetting("Red", 0, 0, 255, 1));
        this.registerSetting(green = new SliderSetting("Green", 255, 0, 255, 1));
        this.registerSetting(blue = new SliderSetting("Blue", 0, 0, 255, 1));
        this.registerSetting(rainbow = new ButtonSetting("Rainbow", false));
        this.registerSetting(teamColor = new ButtonSetting("Team color", false));
        this.registerSetting(renderSelf = new ButtonSetting("Render self", false));
        this.registerSetting(showInvis = new ButtonSetting("Show invis", true));
    }

    @Override
    public void onRenderWorld(WorldRenderContext context) {
        if (mc.world == null || mc.player == null) return;
        if (!box.isToggled()) return;
        
        Vec3d camera = context.camera().getPos();
        
        for (PlayerEntity player : mc.world.getPlayers()) {
            if (player == mc.player && !renderSelf.isToggled()) continue;
            if (player == mc.player && mc.options.getPerspective().isFirstPerson()) continue;
            if (player.isDead()) continue;
            if (!showInvis.isToggled() && player.isInvisible()) continue;
            if (xyz.ravenbs.module.impl.world.AntiBot.isBot(player)) continue;
            
            // Get color
            float r, g, b;
            if (rainbow.isToggled()) {
                int chroma = Utils.getChroma(2, 0);
                r = ((chroma >> 16) & 0xFF) / 255f;
                g = ((chroma >> 8) & 0xFF) / 255f;
                b = (chroma & 0xFF) / 255f;
            } else if (teamColor.isToggled()) {
                // Get team color from player's display name
                String name = player.getDisplayName().getString();
                if (name.startsWith("§")) {
                    char colorCode = name.charAt(1);
                    int tc = getColorFromCode(colorCode);
                    r = ((tc >> 16) & 0xFF) / 255f;
                    g = ((tc >> 8) & 0xFF) / 255f;
                    b = (tc & 0xFF) / 255f;
                } else {
                    r = (float) red.getInput() / 255f;
                    g = (float) green.getInput() / 255f;
                    b = (float) blue.getInput() / 255f;
                }
            } else {
                r = (float) red.getInput() / 255f;
                g = (float) green.getInput() / 255f;
                b = (float) blue.getInput() / 255f;
            }
            
            // Interpolate position
            Vec3d prevPos = new Vec3d(player.prevX, player.prevY, player.prevZ);
            Vec3d curPos = new Vec3d(player.getX(), player.getY(), player.getZ());
            Vec3d interp = prevPos.lerp(curPos, context.tickDelta());
            
            // Calculate box
            Box bbox = player.getBoundingBox();
            double width = bbox.maxX - bbox.minX;
            double height = bbox.maxY - bbox.minY;
            
            Box renderBox = new Box(
                interp.x - width/2 - camera.x,
                interp.y - camera.y,
                interp.z - width/2 - camera.z,
                interp.x + width/2 - camera.x,
                interp.y + height - camera.y,
                interp.z + width/2 - camera.z
            );
            
            context.matrixStack().push();
            RenderUtils.drawBox(context.matrixStack(), renderBox, r, g, b, 1.0f);
            context.matrixStack().pop();
        }
    }

    private int getColorFromCode(char code) {
        switch (code) {
            case '0': return 0x000000;
            case '1': return 0x0000AA;
            case '2': return 0x00AA00;
            case '3': return 0x00AAAA;
            case '4': return 0xAA0000;
            case '5': return 0xAA00AA;
            case '6': return 0xFFAA00;
            case '7': return 0xAAAAAA;
            case '8': return 0x555555;
            case '9': return 0x5555FF;
            case 'a': return 0x55FF55;
            case 'b': return 0x55FFFF;
            case 'c': return 0xFF5555;
            case 'd': return 0xFF55FF;
            case 'e': return 0xFFFF55;
            case 'f': return 0xFFFFFF;
            default: return 0xFFFFFF;
        }
    }
}
