package xyz.ravenbs.clickgui.components;

import net.minecraft.client.gui.DrawContext;

public class Component {
    public boolean visible = true;

    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return false;
    }

    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return false;
    }
    
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
         return false;
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return false;
    }

    public int getHeight() { return 0; }
    
    public void onGuiClosed() {
    }

    public void setOffset(int offset) {
        // Base implementation does nothing, override in subclasses if needed
    }
}
