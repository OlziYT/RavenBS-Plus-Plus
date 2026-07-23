package xyz.ravenbs.clickgui.components.impl;

import xyz.ravenbs.clickgui.components.Component;
import xyz.ravenbs.module.setting.impl.ModeSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

import java.awt.Color;

public class ModeComponent extends Component {
    private final ModeSetting setting;
    private final ModuleComponent parent;
    private int offset;
    private boolean isHovered;

    public ModeComponent(ModeSetting setting, ModuleComponent parent, int offset) {
        this.setting = setting;
        this.parent = parent;
        this.offset = offset;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        CategoryComponent category = parent.getParent();
        int x = category.getCurrentX();
        int y = category.getCurrentY() + category.height + parent.getOffset() + this.offset - category.getCurrentScrollY();
        int width = category.getCurrentWidth();
        int height = 16;
        
        // Skip rendering if out of bounds (optimization)
        if (y < category.getCurrentY() + category.height) return;
        if (y > MinecraftClient.getInstance().currentScreen.height) return;
        
        isHovered = isHovering(mouseX, mouseY, x, y, width, height);
        
        // Background
        context.fill(x, y, x + width, y + height, new Color(0, 0, 0, 160).getRGB());
        
        // Text
        net.minecraft.client.font.TextRenderer tr = MinecraftClient.getInstance().textRenderer;
        String name = setting.getLocalizedName();
        String mod = ": " + setting.getOptions()[setting.getInput()];
        String text = name + mod;
        
        if (tr.getWidth(text) > width - 10) {
             // Truncate name, keep mode
             int modWidth = tr.getWidth(mod);
             int availableForName = width - 10 - modWidth;
             String suffix = "...";
             int suffixWidth = tr.getWidth(suffix);
             
             if (availableForName > suffixWidth) {
                  while (tr.getWidth(name) + suffixWidth > availableForName && name.length() > 0) {
                      name = name.substring(0, name.length() - 1);
                  }
                  name = name + suffix;
             }
             text = name + mod;
        }
        
        context.drawText(tr, text, x + 7, y + 4, isHovered ? -1 : Color.GRAY.getRGB(), true);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        CategoryComponent category = parent.getParent();
        int x = category.getCurrentX();
        int y = category.getCurrentY() + category.height + parent.getOffset() + this.offset - category.getCurrentScrollY();
        int width = category.getCurrentWidth();
        int height = 16;
        
        // Ensure we don't click if scrolled out of view (behind header)
        if (y < category.getCurrentY() + category.height) return false;

        boolean hovered = isHovering(mouseX, mouseY, x, y, width, height);
        
        if (hovered && button == 0) {
            setting.cycle();
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
