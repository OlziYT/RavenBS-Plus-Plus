package xyz.ravenbs.clickgui.components.impl;

import xyz.ravenbs.clickgui.components.Component;
import xyz.ravenbs.module.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

import java.awt.Color;

public class BindComponent extends Component {
    private final ModuleComponent parent;
    private int offset;
    private boolean isBinding;

    public BindComponent(ModuleComponent parent, int offset) {
        this.parent = parent;
        this.offset = offset;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        int x = parent.getParent().getCurrentX();
        int y = parent.getParent().getCurrentY() + parent.getParent().height + parent.getOffset() + this.offset - parent.getParent().getCurrentScrollY();
        
        // Visibility Check (copied from others)
        if (y < parent.getParent().getCurrentY() + parent.getParent().height) return; 

        int width = parent.getParent().getCurrentWidth();
        int height = 16;
        
        boolean isHovered = isHovering(mouseX, mouseY, x, y, width, height);

        // Background
        context.fill(x, y, x + width, y + height, new Color(0, 0, 0, 140).getRGB());

        // Text
        String keyName;
        if (isBinding) {
            keyName = "Listening...";
        } else {
            int key = parent.getMod().getKeycode();
            if (key == 0) {
                keyName = "None";
            } else if (key < 0) {
                // Mouse Button
                int button = -100 - key;
                keyName = InputUtil.Type.MOUSE.createFromCode(button).getLocalizedText().getString().toUpperCase();
            } else {
                keyName = InputUtil.fromKeyCode(key, 0).getLocalizedText().getString().toUpperCase();
                // Fallback for unknown
                if (keyName.isEmpty()) keyName = "#" + key; 
            }
        }
        
        String text = net.minecraft.client.resource.language.I18n.translate("raven.gui.bind") + ": " + keyName;
        context.drawText(MinecraftClient.getInstance().textRenderer, text, x + 7, y + 4, isBinding ? new Color(24, 154, 255).getRGB() : (isHovered ? -1 : Color.GRAY.getRGB()), true);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int x = parent.getParent().getCurrentX();
        int y = parent.getParent().getCurrentY() + parent.getParent().height + parent.getOffset() + this.offset - parent.getParent().getCurrentScrollY();
        int width = parent.getParent().getCurrentWidth();
        int height = 16;
        
        // If binding, accept any click as bind (except 0/Left if we want to allow clicking out?)
        // Usually clicking raw allows binding mouse buttons.
        if (isBinding) {
            // Bind mouse button
            // Convention: -100 - button (0 -> -100, 1 -> -101)
            int key = -100 - button;
            parent.getMod().setBind(key);
            isBinding = false;
            return true;
        }
        
        if (isHovering(mouseX, mouseY, x, y, width, height) && button == 0) {
            isBinding = !isBinding;
            return true;
        }
        return false;
    }
    
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (isBinding) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                parent.getMod().setBind(0);
            } else if (keyCode == GLFW.GLFW_KEY_DELETE) {
                parent.getMod().setBind(0);
            } else {
                parent.getMod().setBind(keyCode);
            }
            isBinding = false;
            return true;
        }
        return false;
    }

    public int getHeight() {
        return 16;
    }
    
    // We don't interact with charTyped or mouseReleased mostly
    
    private boolean isHovering(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }
    public void setOffset(int offset) {
        this.offset = offset;
    }
}
