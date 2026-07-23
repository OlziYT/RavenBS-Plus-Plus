package xyz.ravenbs.clickgui.components.impl;

import xyz.ravenbs.clickgui.components.Component;
import xyz.ravenbs.module.Module;
import xyz.ravenbs.utility.RenderUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

import java.awt.Color;

public class ModuleComponent extends Component {
    private final Module mod;
    private final CategoryComponent parent;
    private int offset;
    private boolean isHovered;
    
    private boolean isExpanded;
    private final java.util.List<Component> settings = new java.util.ArrayList<>();
    public java.util.List<Component> getSettings() { return settings; }
    public boolean hasSettings() { return !settings.isEmpty(); }
    
    public ModuleComponent(Module mod, CategoryComponent parent, int offset) {
        this.mod = mod;
        this.parent = parent;
        this.offset = offset;
        
        // Init settings
        int setOffset = 16; // Start after module button
        
        // Add Bind Component FIRST (or last? Let's do first for visibility)
        settings.add(new BindComponent(this, setOffset));
        setOffset += 16;

        if (mod.getSettings() != null) {
            for (xyz.ravenbs.module.setting.Setting s : mod.getSettings()) {
                if (!s.visible) {
                    continue;
                }

                if (s instanceof xyz.ravenbs.module.setting.impl.ButtonSetting) {
                    settings.add(new ButtonComponent((xyz.ravenbs.module.setting.impl.ButtonSetting) s, this, setOffset));
                    setOffset += 16;
                } else if (s instanceof xyz.ravenbs.module.setting.impl.SliderSetting) {
                    settings.add(new SliderComponent((xyz.ravenbs.module.setting.impl.SliderSetting) s, this, setOffset));
                    setOffset += 16;
                } else if (s instanceof xyz.ravenbs.module.setting.impl.DescriptionSetting) {
                    settings.add(new DescriptionComponent((xyz.ravenbs.module.setting.impl.DescriptionSetting) s, this, setOffset));
                    setOffset += 12;
                } else if (s instanceof xyz.ravenbs.module.setting.impl.ModeSetting) {
                    settings.add(new ModeComponent((xyz.ravenbs.module.setting.impl.ModeSetting) s, this, setOffset));
                    setOffset += 16;
                } else if (s instanceof xyz.ravenbs.module.setting.impl.StringSetting) {
                    settings.add(new StringComponent((xyz.ravenbs.module.setting.impl.StringSetting) s, this, setOffset));
                    setOffset += 16;
                } else if (s instanceof xyz.ravenbs.module.setting.impl.ColorSetting) {
                    settings.add(new ColorComponent((xyz.ravenbs.module.setting.impl.ColorSetting) s, this, setOffset));
                    // Note: setOffset will need to account for dynamic height later? 
                    // No, 'offset' passed to component is the STARTING Y relative to module.
                    // But if a previous component Expands, does setOffset update?
                    // ModuleComponent re-calculates Y during render for ITSELF, but passes constant offets to children?
                    // Wait, current logic:
                    // new Component(..., offset) -> stores offset.
                    // render(): y = base + this.offset.
                    // If a component ABOVE expands, `this.offset` is static, so it WON'T move down.
                    // THIS IS A BUG IN THE EXISTING GUI SYSTEM if variable heights are introduced!
                    // ModuleComponent currently only handles settings with FIXED height (16 or 12).
                    // Sub-components don't push each other?
                    // Let's check ModuleComponent logic.
                    setOffset += 16;
                }
            }
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        int x = parent.getCurrentX();
        int y = parent.getCurrentY() + parent.height + offset - parent.getCurrentScrollY();
        
        // Visibility Check
        // Visibility Check
        if (y < parent.getCurrentY() + parent.height) return; // Don't render above header
        if (y > MinecraftClient.getInstance().currentScreen.height) return; // Don't render below screen
        int width = parent.getCurrentWidth();
        int height = 14; // Compact height
        
        // Background
        // Disabling background for disabled modules (Transparent) to fix "Two Colors" issue
        int color = mod.isEnabled() ? new Color(24, 154, 255).getRGB() : new Color(0, 0, 0, 0).getRGB(); 
        
        // Darken if hovered
        // Darken if hovered
        isHovered = isHovering(mouseX, mouseY, x, y, width, height);
        if (isHovered) {
             // Hover effect - subtle lighten or darken?
             // User likes semi-transparent. Let's make it slightly lighter if hovered.
             if (!mod.isEnabled()) color = new Color(255, 255, 255, 20).getRGB();
             else color = new Color(40, 170, 255).getRGB();
        }
        
        // Fill rect
        context.fill(x, y, x + width, y + height, color); // Main bg
        // Outline? Maybe later.
        
        // Just text for now
        // Draw centered module name
        // Draw centered module name
        net.minecraft.client.font.TextRenderer tr = MinecraftClient.getInstance().textRenderer;
        String name = mod.getLocalizedName();
        int textWidth = tr.getWidth(name);
        
        // Truncate if too long (width - 10 for padding)
        if (textWidth > width - 10) {
            String suffix = "...";
            int suffixWidth = tr.getWidth(suffix);
            // Try to fit as much as possible
            while (textWidth + suffixWidth > width - 10 && name.length() > 0) {
                name = name.substring(0, name.length() - 1);
                textWidth = tr.getWidth(name);
            }
            name = name + suffix;
            textWidth = tr.getWidth(name);
        }
        
        context.drawText(tr, name, x + (width - textWidth) / 2, y + 4, isExpanded ? -1 : new Color(0,0,0).getRGB(), false);
        
        // Tooltip
        // Tooltip
        if (isHovered && mod.getDescription() != null) {
            ((xyz.ravenbs.clickgui.ClickGuiScreen) MinecraftClient.getInstance().currentScreen).setTooltip(net.minecraft.text.Text.of(mod.getDescription()));
        }
        
        // Settings
        if (isExpanded) {
            int currentYOffset = 16; // Start after module button
            
            for (Component c : settings) {
                // Update component's offset dynamically before render
                c.setOffset(currentYOffset);
                c.render(context, mouseX, mouseY, delta);
                currentYOffset += c.getHeight();
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int x = parent.getCurrentX();
        int y = parent.getCurrentY() + parent.height + offset - parent.getCurrentScrollY();
        
        // Logic fix: Don't return false early if header is hidden, because settings might still be visible below!
        // if (y < parent.y + parent.height) return false; 
        
        int width = parent.getCurrentWidth();
        int height = 14;
        
        // Only allow toggling the module if the header itself is actually visible
        if (y >= parent.getCurrentY() + parent.height) {
            if (isHovering(mouseX, mouseY, x, y, width, height)) {
                if (button == 0) {
                    mod.toggle();
                    return true;
                } else if (button == 1) {
                    isExpanded = !isExpanded;
                    return true;
                }
            }
        }
        
        if (isExpanded) {
            for (Component c : settings) {
                if (c.mouseClicked(mouseX, mouseY, button)) return true;
            }
        }
        return false;
    }
    
    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (isExpanded) {
            for (Component c : settings) {
                if (c.mouseReleased(mouseX, mouseY, button)) return true;
            }
        }
        return false;
    }
    
    public int getHeight() {
        if (isExpanded) {
            int h = 14;
            for (Component c : settings) {
                h += c.getHeight();
            }
            return h;
        }
        return 14;
    }
    
    public Module getMod() { return mod; }
    public String getName() { return mod.getName(); }
    public boolean isEnabled() { return mod.isEnabled(); }
    public String getDescription() { return mod.getDescription(); }
    
    // Getters for sub-components
    public void setOffset(int offset) { this.offset = offset; }
    public CategoryComponent getParent() { return parent; }
    public int getOffset() { return offset; }
    
    private boolean isHovering(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }
}
