package xyz.ravenbs.clickgui.components.impl;

import xyz.ravenbs.clickgui.components.Component;
import xyz.ravenbs.module.Module;
import xyz.ravenbs.module.ModuleCategory;
import xyz.ravenbs.module.ModuleManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class CategoryComponent {
    public ModuleCategory category;
    public int x, y, width, height;
    public boolean dragging = false;
    public boolean opened = true;
    
    private int dragX, dragY;
    private List<Component> modules = new ArrayList<>();
    public List<Component> getModules() { return modules; }
    
    public CategoryComponent(ModuleCategory category, int x, int y) {
        this.category = category;
        this.x = x;
        this.y = y;
        this.width = 90; // Reduced from 100
        this.height = 16; // Reduced from 18
        
        // Load modules for this category
        reloadModules();
    }
    
    public void reloadModules() {
        this.modules.clear();
        for (Module m : ModuleManager.modules) {
            if (m.getCategory() == this.category) {
                // Initialize with 0 offset, logic will handle rendering positions
                modules.add(new ModuleComponent(m, this, 0));
            }
        }
    }

    public boolean renderFrame = true;

    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (renderFrame && dragging) {
            this.x = mouseX - dragX;
            this.y = mouseY - dragY;
        }

        int currentY = y;

        if (renderFrame) {
            // Draw Title Bar
            context.fill(x, y, x + width, y + height, new Color(0, 0, 0, 180).getRGB());
            
            String catName = category.name();
            String key = "raven.category." + catName.toLowerCase();
            if (net.minecraft.client.resource.language.I18n.hasTranslation(key)) {
                catName = net.minecraft.client.resource.language.I18n.translate(key);
            }
            // Capitalize first letter if not translated (fallback)
            if (catName.equals(category.name())) {
                 catName = catName.substring(0, 1).toUpperCase() + catName.substring(1).toLowerCase();
            }
            
            context.drawText(MinecraftClient.getInstance().textRenderer, catName, x + 22, y + 5, -1, true);
            ItemStack stack = getIcon();
            if (stack != null) {
                context.drawItem(stack, x + 2, y + 1);
            }
            currentY += height;
        }

        // If opened or frameless (always visible in single window mode)
        if (opened || !renderFrame) {
            int runningOffset = 0;
            
            // Calculate total height for scrolling
            int totalHeight = 0;
            for (Component c : modules) {
                if (c instanceof ModuleComponent) {
                     totalHeight += ((ModuleComponent)c).getHeight();
                }
            }
            
            // Clamp scrollY
            // If frameless, the scroll height constraint might be different (parent window height).
            // For now, keep simple clamping logic or rely on visual clipping.
            if (scrollY > totalHeight - 50) scrollY = Math.max(0, totalHeight - 50); 
            if (scrollY < 0) scrollY = 0;

            // Background
            if (renderFrame) {
                context.fill(x, currentY, x + width, currentY + totalHeight + 4, new Color(0,0,0,160).getRGB());
            }

            // Render Modules
            for (Component c : modules) {
                if (c instanceof ModuleComponent) {
                    ((ModuleComponent)c).setOffset(runningOffset);
                    c.render(context, mouseX, mouseY, delta);
                    runningOffset += ((ModuleComponent)c).getHeight();
                }
            }
        }
    }
    
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Frame/Header Interactions
        if (renderFrame && isHoveringHeader(mouseX, mouseY)) {
            if (button == 0) {
                dragging = true;
                dragX = (int)mouseX - x;
                dragY = (int)mouseY - y;
                return true;
            } else if (button == 1) {
                opened = !opened;
                return true;
            }
        }
        
        // Content Interactions
        if (opened || !renderFrame) {
            int runningOffset = 0;
            for (Component c : modules) {
                if (c instanceof ModuleComponent) {
                   ((ModuleComponent)c).setOffset(runningOffset);
                   if (c.mouseClicked(mouseX, mouseY, button)) return true;
                   runningOffset += ((ModuleComponent)c).getHeight();
                }
            }
        }

        return false;
    }
    
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (renderFrame && button == 0) {
            dragging = false;
        }
        
        if (opened || !renderFrame) {
            for (Component c : modules) {
                if (c.mouseReleased(mouseX, mouseY, button)) return true;
            }
        }
        
        return false;
    }
    
    public int scrollY = 0;
    private Integer layoutX = null;
    private Integer layoutY = null;
    private Integer layoutWidth = null;
    private Integer layoutScrollY = null;

    public void setLayoutOverride(int x, int y, int width, int scrollY) {
        this.layoutX = x;
        this.layoutY = y;
        this.layoutWidth = width;
        this.layoutScrollY = scrollY;
    }

    public void clearLayoutOverride() {
        this.layoutX = null;
        this.layoutY = null;
        this.layoutWidth = null;
        this.layoutScrollY = null;
    }

    public int getCurrentX() {
        return layoutX != null ? layoutX : x;
    }

    public int getCurrentY() {
        return layoutY != null ? layoutY : y;
    }

    public int getCurrentWidth() {
        return layoutWidth != null ? layoutWidth : width;
    }

    public int getCurrentScrollY() {
        return layoutScrollY != null ? layoutScrollY : scrollY;
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        // Scroll works if hovering the whole column
        if (isHoveringColumn(mouseX, mouseY)) {
            scrollY -= amount * 15; // Scroll speed
            if (scrollY < 0) scrollY = 0;
            return true;
        }
        return false;
    }

    // Strict for Header (Clicks)
    private boolean isHoveringHeader(double mouseX, double mouseY) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    // Broad for Scrolling
    private boolean isHoveringColumn(double mouseX, double mouseY) {
         return mouseX >= x && mouseX <= x + width && mouseY >= y;
    }

    public ItemStack getIcon() {
        switch (category) {
            case combat: return new ItemStack(Items.DIAMOND_SWORD);
            case movement: return new ItemStack(Items.DIAMOND_BOOTS);
            case player: return new ItemStack(Items.GOLDEN_APPLE);
            case world: return new ItemStack(Items.MAP);
            case render: return new ItemStack(Items.ENDER_EYE);
            case minigames: return new ItemStack(Items.GOLD_INGOT);
            case fun: return new ItemStack(Items.SLIME_BALL);
            case other: return new ItemStack(Items.CLOCK);
            case client: return new ItemStack(Items.COMPASS);
        }
        return null;
    }
}
