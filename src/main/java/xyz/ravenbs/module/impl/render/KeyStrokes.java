package xyz.ravenbs.module.impl.render;

import xyz.ravenbs.module.Module;
import xyz.ravenbs.module.ModuleCategory;
import xyz.ravenbs.module.setting.impl.ButtonSetting;
import xyz.ravenbs.module.setting.impl.SliderSetting;
import xyz.ravenbs.utility.RenderUtils;
import xyz.ravenbs.utility.Utils;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.KeyBinding;

import java.awt.Color;

public class KeyStrokes extends Module {
    private SliderSetting x;
    private SliderSetting y;
    private ButtonSetting showMouse;
    private ButtonSetting showSpace;
    private ButtonSetting rainbow;

    public KeyStrokes() {
        super("KeyStrokes", ModuleCategory.render);
        this.registerSetting(x = new SliderSetting("X", 10, 0, 1920, 1));
        this.registerSetting(y = new SliderSetting("Y", 10, 0, 1080, 1));
        this.registerSetting(showMouse = new ButtonSetting("Show Mouse", true));
        this.registerSetting(showSpace = new ButtonSetting("Show Space", false));
        this.registerSetting(rainbow = new ButtonSetting("Rainbow", false));
    }

    @Override
    public void onRender(DrawContext context, float tickDelta) {
        if (mc.currentScreen != null && !(mc.currentScreen instanceof xyz.ravenbs.clickgui.ClickGuiScreen)) return;

        int posX = (int) x.getInput();
        int posY = (int) y.getInput();
        int gap = 2;
        int size = 20; // Size of square keys (W, A, S, D)
        
        // Colors
        int pressedColor = new Color(255, 255, 255, 200).getRGB();
        int normalColor = new Color(0, 0, 0, 100).getRGB();
        int textColor = -1;
        if (rainbow.isToggled()) {
            textColor = Utils.getChroma(2, 0);
        }

        // Keys
        KeyBinding forward = mc.options.forwardKey;
        KeyBinding left = mc.options.leftKey;
        KeyBinding back = mc.options.backKey;
        KeyBinding right = mc.options.rightKey;
        KeyBinding jump = mc.options.jumpKey;
        KeyBinding attack = mc.options.attackKey;
        KeyBinding use = mc.options.useKey;

        // W
        drawKey(context, forward, "W", posX + size + gap, posY, size, size, normalColor, pressedColor, textColor);

        // A, S, D
        int row2Y = posY + size + gap;
        drawKey(context, left, "A", posX, row2Y, size, size, normalColor, pressedColor, textColor);
        drawKey(context, back, "S", posX + size + gap, row2Y, size, size, normalColor, pressedColor, textColor);
        drawKey(context, right, "D", posX + (size + gap) * 2, row2Y, size, size, normalColor, pressedColor, textColor);

        int nextY = row2Y + size + gap;

        // Mouse
        if (showMouse.isToggled()) {
            int mouseWidth = (size * 3 + gap * 2) / 2;
            int mouseHeight = 18;
            
            drawKey(context, attack, "LMB", posX, nextY, mouseWidth, mouseHeight, normalColor, pressedColor, textColor);
            drawKey(context, use, "RMB", posX + mouseWidth + gap, nextY, mouseWidth, mouseHeight, normalColor, pressedColor, textColor);
            
            nextY += mouseHeight + gap;
        }
        
        // Space
        if (showSpace.isToggled()) {
            int spaceWidth = size * 3 + gap * 2;
            drawKey(context, jump, "---", posX, nextY, spaceWidth, 15, normalColor, pressedColor, textColor);
        }
    }

    private void drawKey(DrawContext context, KeyBinding key, String name, int x, int y, int width, int height, int normalColor, int pressedColor, int textColor) {
        boolean pressed = key.isPressed();
        
        // Draw Background
        context.fill(x, y, x + width, y + height, pressed ? pressedColor : normalColor);
        
        // Draw Text
        int textX = x + (width - mc.textRenderer.getWidth(name)) / 2;
        int textY = y + (height - mc.textRenderer.fontHeight) / 2;
        
        int color = pressed ? new Color(0, 0, 0, 255).getRGB() : textColor;
        context.drawText(mc.textRenderer, name, textX, textY, color, true);
    }
}
