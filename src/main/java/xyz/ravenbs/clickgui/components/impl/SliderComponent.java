package xyz.ravenbs.clickgui.components.impl;

import xyz.ravenbs.clickgui.components.Component;
import xyz.ravenbs.module.setting.impl.SliderSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

import java.awt.Color;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class SliderComponent extends Component {
    private final SliderSetting setting;
    private final ModuleComponent parent;
    private int offset;
    private boolean isHovered;
    private boolean dragging = false;

    public SliderComponent(SliderSetting setting, ModuleComponent parent, int offset) {
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
        
        isHovered = isHovering(mouseX, mouseY, x, y, width, height);

        // Logic
        if (dragging) {
            double diff = Math.min(width, Math.max(0, mouseX - x));
            double min = setting.getMin();
            double max = setting.getMax();
            
            if (diff == 0) {
                setting.setValue(min);
            } else {
                double value = round(((diff / width) * (max - min) + min), 2);
                setting.setValue(value);
            }
        }
        
        // Rendering
        // Background
        context.fill(x, y, x + width, y + height, new Color(0, 0, 0, 140).getRGB());
        
        // Slider Fill
        double min = setting.getMin();
        double max = setting.getMax();
        double curr = setting.getInput();
        int sliderWidth = (int) ((curr - min) / (max - min) * width);
        context.fill(x, y, x + sliderWidth, y + height, new Color(24, 154, 255, 150).getRGB());

        // Text
        net.minecraft.client.font.TextRenderer tr = MinecraftClient.getInstance().textRenderer;
        String name = setting.getLocalizedName();
        String val = ": " + setting.getInput();
        String display = name + val;
        
        if (tr.getWidth(display) > width - 10) {
            // Truncate name, keep value
            int valWidth = tr.getWidth(val);
            int availableForName = width - 10 - valWidth;
            String suffix = "...";
            int suffixWidth = tr.getWidth(suffix);
            
            if (availableForName > suffixWidth) {
                 while (tr.getWidth(name) + suffixWidth > availableForName && name.length() > 0) {
                     name = name.substring(0, name.length() - 1);
                 }
                 name = name + suffix;
            } else {
                 // Extreme case, just show value or ...
                 name = ""; 
            }
            display = name + val;
        }

        context.drawText(tr, display, x + 7, y + 4, -1, true);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int x = parent.getParent().getCurrentX();
        int y = parent.getParent().getCurrentY() + parent.getParent().height + parent.getOffset() + this.offset - parent.getParent().getCurrentScrollY();
        int width = parent.getParent().getCurrentWidth();
        int height = 16;
        
        if (isHovering(mouseX, mouseY, x, y, width, height) && button == 0) {
            dragging = true;
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        dragging = false;
        return false;
    }

    public int getHeight() {
        return 16;
    }

    private boolean isHovering(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }
    
    private double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();
        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
    public void setOffset(int offset) {
        this.offset = offset;
    }
}
