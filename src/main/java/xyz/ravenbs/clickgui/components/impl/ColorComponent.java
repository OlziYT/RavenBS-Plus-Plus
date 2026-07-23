package xyz.ravenbs.clickgui.components.impl;

import xyz.ravenbs.clickgui.components.Component;
import xyz.ravenbs.module.setting.impl.ColorSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class ColorComponent extends Component {
    private final ColorSetting setting;
    private final ModuleComponent parent;
    private int offset;
    private boolean isHovered;
    private boolean isExpanded;
    
    private final List<Component> subComponents = new ArrayList<>();

    public ColorComponent(ColorSetting setting, ModuleComponent parent, int offset) {
        this.setting = setting;
        this.parent = parent;
        this.offset = offset;
        
        // Init SubComponents
        int subOffset = 16; // Start below header
        subComponents.add(new SliderComponent(setting.getRed(), parent, offset + subOffset));
        subOffset += 16;
        subComponents.add(new SliderComponent(setting.getGreen(), parent, offset + subOffset));
        subOffset += 16;
        subComponents.add(new SliderComponent(setting.getBlue(), parent, offset + subOffset));
        subOffset += 16;
        subComponents.add(new ButtonComponent(setting.getRainbow(), parent, offset + subOffset));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        int x = parent.getParent().getCurrentX();
        int y = parent.getParent().getCurrentY() + parent.getParent().height + parent.getOffset() + this.offset - parent.getParent().getCurrentScrollY();
        
        // Visibility Check
        if (y < parent.getParent().getCurrentY() + parent.getParent().height) return;

        int width = parent.getParent().getCurrentWidth();
        int height = 16;
        
        isHovered = isHovering(mouseX, mouseY, x, y, width, height);

        // Render Header
        context.fill(x, y, x + width, y + height, new Color(0, 0, 0, 140).getRGB());
        
        String text = setting.getLocalizedName();
        context.drawText(MinecraftClient.getInstance().textRenderer, text, x + 7, y + 4, isHovered ? -1 : Color.GRAY.getRGB(), true);
        
        // Color Preview Box
        int color = setting.getRGB();
        int previewX = x + width - 20;
        int previewY = y + 2;
        context.fill(previewX, previewY, previewX + 16, previewY + 12, color);
        // Border
        context.drawBorder(previewX, previewY, 16, 12, new Color(0,0,0).getRGB());
        
        // Render Arrow?
        String arrow = isExpanded ? "-" : "+";
        context.drawText(MinecraftClient.getInstance().textRenderer, arrow, x + width - 30, y + 4, Color.GRAY.getRGB(), true);

        // Render SubComponents if expanded
        if (isExpanded) {
            for (Component c : subComponents) {
                c.render(context, mouseX, mouseY, delta);
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int x = parent.getParent().getCurrentX();
        int y = parent.getParent().getCurrentY() + parent.getParent().height + parent.getOffset() + this.offset - parent.getParent().getCurrentScrollY();
        int width = parent.getParent().getCurrentWidth();
        int height = 16;
        
        if (isHovering(mouseX, mouseY, x, y, width, height)) {
            if (button == 1) { // Right click to expand/collapse
                isExpanded = !isExpanded;
                return true;
            }
        }
        
        if (isExpanded) {
            for (Component c : subComponents) {
                if (c.mouseClicked(mouseX, mouseY, button)) return true;
            }
        }
        
        return false;
    }
    
    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
         if (isExpanded) {
            for (Component c : subComponents) {
                if (c.mouseReleased(mouseX, mouseY, button)) return true;
            }
        }
        return false;
    }

    public int getHeight() {
        int h = 16;
        if (isExpanded) {
            for (Component c : subComponents) {
                h += c.getHeight();
            }
        }
        return h;
    }
    
    private boolean isHovering(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }
    public void setOffset(int offset) {
        this.offset = offset;
        int subOffset = offset + 16; // Start below header
        for (Component c : subComponents) {
            c.setOffset(subOffset);
            subOffset += c.getHeight();
        }
    }
}
