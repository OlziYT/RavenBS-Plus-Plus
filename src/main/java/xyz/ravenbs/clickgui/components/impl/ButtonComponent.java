package xyz.ravenbs.clickgui.components.impl;

import xyz.ravenbs.clickgui.components.Component;
import xyz.ravenbs.module.setting.impl.ButtonSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

import java.awt.Color;

public class ButtonComponent extends Component {
    private final ButtonSetting setting;
    private final ModuleComponent parent;
    private int offset;
    private boolean isHovered;

    public ButtonComponent(ButtonSetting setting, ModuleComponent parent, int offset) {
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

        // Save Y for clicks
        // (Not strictly creating a field for Y to keep simple, assuming consistent render)

        // Hover
        isHovered = isHovering(mouseX, mouseY, x, y, width, height);
        
        // Render
        // Background
        context.fill(x, y, x + width, y + height, new Color(0, 0, 0, 140).getRGB());
        
        // Text
        net.minecraft.client.font.TextRenderer tr = MinecraftClient.getInstance().textRenderer;
        String name = setting.getLocalizedName();
        int textWidth = tr.getWidth(name);
        
        // Truncate
        if (textWidth > width - 20) { // -20 to account for checkbox room
             String suffix = "...";
             int suffixWidth = tr.getWidth(suffix);
             while (textWidth + suffixWidth > width - 20 && name.length() > 0) {
                 name = name.substring(0, name.length() - 1);
                 textWidth = tr.getWidth(name);
             }
             name = name + suffix;
        }
        
        context.drawText(tr, name, x + 7, y + 4, isHovered ? -1 : Color.GRAY.getRGB(), true);
        
        // Checkbox/State
        if (setting.isToggled()) {
            context.drawText(MinecraftClient.getInstance().textRenderer, "V", x + width - 12, y + 4, new Color(24, 154, 255).getRGB(), true);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int x = parent.getParent().getCurrentX();
        int y = parent.getParent().getCurrentY() + parent.getParent().height + parent.getOffset() + this.offset - parent.getParent().getCurrentScrollY();
        int width = parent.getParent().getCurrentWidth();
        int height = 16;

        if (isHovering(mouseX, mouseY, x, y, width, height) && button == 0) {
            setting.toggle();
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
