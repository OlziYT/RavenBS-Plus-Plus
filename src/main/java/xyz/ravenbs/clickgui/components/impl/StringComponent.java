package xyz.ravenbs.clickgui.components.impl;

import xyz.ravenbs.clickgui.components.Component;
import xyz.ravenbs.module.setting.impl.StringSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;

import java.awt.Color;

public class StringComponent extends Component {
    private final StringSetting setting;
    private final ModuleComponent parent;
    private int offset;
    private boolean isFocused;

    public StringComponent(StringSetting setting, ModuleComponent parent, int offset) {
        this.setting = setting;
        this.parent = parent;
        this.offset = offset;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        int x = parent.getParent().getCurrentX();
        int y = parent.getParent().getCurrentY() + parent.getParent().height + parent.getOffset() + this.offset - parent.getParent().getCurrentScrollY();
        int width = parent.getParent().getCurrentWidth();
        int height = 16;
        
        boolean isHovered = isHovering(mouseX, mouseY, x, y, width, height);

        // Background
        context.fill(x, y, x + width, y + height, new Color(0, 0, 0, 140).getRGB());

        // Label
        String text = setting.getLocalizedName() + ": ";
        context.drawText(MinecraftClient.getInstance().textRenderer, text, x + 7, y + 4, Color.GRAY.getRGB(), true);

        // Value
        int valueX = x + 7 + MinecraftClient.getInstance().textRenderer.getWidth(text);
        String value = setting.getString() + (isFocused ? "_" : "");
        
        // Truncate if too long?
        // Truncate if too long
        int availableWidth = width - (valueX - x) - 5;
        if (MinecraftClient.getInstance().textRenderer.getWidth(value) > availableWidth) {
            String suffix = "...";
            int suffixWidth = MinecraftClient.getInstance().textRenderer.getWidth(suffix);
            
            // If focused, show the END of the string (so you can see what you type)
            if (isFocused) {
                 // Prepend ...
                 while (MinecraftClient.getInstance().textRenderer.getWidth(value) + suffixWidth > availableWidth && value.length() > 0) {
                      value = value.substring(1);
                 }
                 value = suffix + value;
            } else {
                 // Append ...
                 while (MinecraftClient.getInstance().textRenderer.getWidth(value) + suffixWidth > availableWidth && value.length() > 0) {
                      value = value.substring(0, value.length() - 1);
                 }
                 value = value + suffix;
            }
        }
        
        int color = isFocused ? new Color(24, 154, 255).getRGB() : (isHovered ? -1 : Color.LIGHT_GRAY.getRGB());
        context.drawText(MinecraftClient.getInstance().textRenderer, value, valueX, y + 4, color, true);
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int x = parent.getParent().getCurrentX();
        int y = parent.getParent().getCurrentY() + parent.getParent().height + parent.getOffset() + this.offset - parent.getParent().getCurrentScrollY();
        int width = parent.getParent().getCurrentWidth();
        int height = 16;

        if (isHovering(mouseX, mouseY, x, y, width, height) && button == 0) {
            isFocused = !isFocused;
            return true;
        } else if (isFocused) {
            isFocused = false;
        }
        return false;
    }

    public boolean charTyped(char chr, int modifiers) {
        if (!isFocused) return false;
        String s = setting.getString();
        // Allow alphanumeric and some symbols
        if (Character.isLetterOrDigit(chr) || "_-. ".indexOf(chr) >= 0) {
            setting.setString(s + chr);
            return true;
        }
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!isFocused) return false;
        
        if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
            String s = setting.getString();
            if (s.length() > 0) {
                setting.setString(s.substring(0, s.length() - 1));
            }
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_ESCAPE) {
            isFocused = false;
            return true;
        }
        return false;
    }

    public int getHeight() {
        return 16;
    }

    private boolean isHovering(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }
    
    public void setOffset(int offset) {
        this.offset = offset;
    }
}
