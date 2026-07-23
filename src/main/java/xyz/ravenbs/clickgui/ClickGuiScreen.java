package xyz.ravenbs.clickgui;

import xyz.ravenbs.clickgui.components.Component;
import xyz.ravenbs.clickgui.components.impl.CategoryComponent;
import xyz.ravenbs.clickgui.components.impl.ProfilesCategoryComponent;
import xyz.ravenbs.clickgui.components.impl.ModuleComponent;

import xyz.ravenbs.clickgui.components.impl.StringComponent; // Needed? I used full qualified name in code above but better to import

import xyz.ravenbs.module.ModuleCategory;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class ClickGuiScreen extends Screen {
    private static final int SETTINGS_PANEL_WIDTH = 140;

    private static final class SettingsLayout {
        final int x;
        final int y;
        final int totalHeight;
        final int contentStartY;

        SettingsLayout(int x, int y, int totalHeight, int contentStartY) {
            this.x = x;
            this.y = y;
            this.totalHeight = totalHeight;
            this.contentStartY = contentStartY;
        }
    }

    // Cascading State
    private static final List<CategoryComponent> categories = new ArrayList<>();
    
    // Sticky state (to allow moving mouse between panels without instant close)
    private CategoryComponent stickyCategory = null;
    private ModuleComponent stickyModule = null;
    
    // Scroll state for each panel
    private int modulesScrollY = 0;
    private int settingsScrollY = 0;
    private static final int MAX_VISIBLE_HEIGHT = 300; // Max height before scrolling kicks in

    public ClickGuiScreen() {
        super(Text.of("ClickGui"));
    }
    
    @Override
    protected void init() {
        // Init categories if empty
        if (categories.isEmpty()) {
             for (ModuleCategory cat : ModuleCategory.values()) {
                if (cat == ModuleCategory.profiles) {
                    categories.add(new ProfilesCategoryComponent(0, 0));
                } else {
                    categories.add(new CategoryComponent(cat, 0, 0)); 
                }
            }
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Update Sticky State based on Hover Paths
        updateCascadingState(mouseX, mouseY);
        
        // Render Root Column (Categories)
        int catX = 10;
        int catY = 20;
        int catW = 100;
        int catH = 20;
        
        int currentY = catY;
        for (CategoryComponent cat : categories) {
             boolean isHovered = (mouseX >= catX && mouseX < catX + catW && mouseY >= currentY && mouseY < currentY + catH);
             boolean isActive = (cat == stickyCategory);
             
             // Background
             int color = new Color(0, 0, 0, 160).getRGB();
             if (isActive) color = new Color(24, 154, 255, 180).getRGB(); // Selected
             else if (isHovered) color = new Color(50, 50, 50, 160).getRGB();
             
             context.fill(catX, currentY, catX + catW, currentY + catH, color);
             
             // Icon
             int textX = catX + 5;
             if (cat.getIcon() != null) {
                 context.drawItem(cat.getIcon(), catX + 4, currentY + 2);
                 textX += 18; // Shift text
             }
             
             // Text
             String catName = cat.category.name();
             String key = "raven.category." + catName.toLowerCase();
             if (net.minecraft.client.resource.language.I18n.hasTranslation(key)) {
                 catName = net.minecraft.client.resource.language.I18n.translate(key);
             } else {
                 catName = catName.substring(0, 1).toUpperCase() + catName.substring(1).toLowerCase();
             }
             
             context.drawText(net.minecraft.client.MinecraftClient.getInstance().textRenderer, catName, textX, currentY + 6, -1, true);
             
             // Arrow indicator if hovered/active
             if (isActive || isHovered) {
                 context.drawText(net.minecraft.client.MinecraftClient.getInstance().textRenderer, ">", catX + catW - 10, currentY + 6, -1, true);
             }
             
             // Store geometry for interaction
             cat.x = catX; cat.y = currentY; cat.width = catW; cat.height = catH;
             
             currentY += catH + 1; // 1px gap
        }
        
        // Render Level 2: Modules (if Category Active)
        if (stickyCategory != null) {
            renderModules(context, mouseX, mouseY, stickyCategory, delta);
        }
        
        super.render(context, mouseX, mouseY, delta);
    }
    
    private void renderModules(DrawContext context, int mouseX, int mouseY, CategoryComponent cat, float delta) {
        int modW = (cat.category == ModuleCategory.profiles) ? 140 : 110;
        int modX = cat.x + cat.width; 
        
        List<Component> modules = cat.getModules();
        int totalH = modules.size() * 16;
        
        int modY = getModulePanelY(cat, totalH);
        
        // Calculate available height based on SCREEN SIZE
        int availableH = this.height - modY - 10; // 10px padding from bottom
        if (availableH < 50) availableH = 50; // Minimum height
        
        // Check if we need scrolling
        boolean needsScroll = totalH > availableH;
        int visibleH = needsScroll ? availableH : totalH;
        
        // Clamp scroll
        int maxScroll = Math.max(0, totalH - visibleH);
        if (modulesScrollY > maxScroll) modulesScrollY = maxScroll;
        if (modulesScrollY < 0) modulesScrollY = 0;
        
        // Background
        context.fill(modX, modY, modX + modW, modY + visibleH, new Color(0,0,0,140).getRGB());
        
        // Scrollbar ...
        if (needsScroll && maxScroll > 0) {
            int scrollbarH = Math.max(20, (int)((float)visibleH / totalH * visibleH));
            int scrollbarY = modY + (int)((float)modulesScrollY / maxScroll * (visibleH - scrollbarH));
            context.fill(modX + modW - 3, scrollbarY, modX + modW, scrollbarY + scrollbarH, new Color(255,255,255,120).getRGB());
        }
        
        int scaleFactor = (int) net.minecraft.client.MinecraftClient.getInstance().getWindow().getScaleFactor();
        int windowHeight = net.minecraft.client.MinecraftClient.getInstance().getWindow().getFramebufferHeight();
        int scissorX = modX * scaleFactor;
        int scissorY = windowHeight - (modY + visibleH) * scaleFactor;
        int scissorW = modW * scaleFactor;
        int scissorH = visibleH * scaleFactor;
        
        if (needsScroll) {
            com.mojang.blaze3d.systems.RenderSystem.enableScissor(scissorX, scissorY, scissorW, scissorH);
        }
        
        int currentY = modY - modulesScrollY;
        
        cat.setLayoutOverride(modX, modY, modW, modulesScrollY);
        
        for (Component c : modules) {
            if (c instanceof ModuleComponent) {
                ModuleComponent mod = (ModuleComponent) c;
                
                // Skip if completely outside visible area (optimization)
                if (needsScroll && (currentY + 16 < modY || currentY > modY + visibleH)) {
                    currentY += 16;
                    continue;
                }
                
                boolean isHovered = (mouseX >= modX && mouseX < modX + modW && mouseY >= currentY && mouseY < currentY + 16);
                if (needsScroll) {
                    isHovered = isHovered && mouseY >= modY && mouseY < modY + visibleH;
                }
                boolean isActive = (mod == stickyModule);
                
                int bgColor = mod.isEnabled() ? new Color(24, 154, 255, 120).getRGB() : new Color(0,0,0,0).getRGB();
                if (isActive) bgColor = new Color(24, 154, 255, 200).getRGB();
                else if (isHovered) bgColor = new Color(255, 255, 255, 40).getRGB();
                
                context.fill(modX, currentY, modX + modW - (needsScroll ? 4 : 0), currentY + 16, bgColor);
                context.drawText(net.minecraft.client.MinecraftClient.getInstance().textRenderer, mod.getName(), modX + 5, currentY + 4, mod.isEnabled() ? -1 : Color.LIGHT_GRAY.getRGB(), true);
                
                if (mod.hasSettings()) {
                     context.drawText(net.minecraft.client.MinecraftClient.getInstance().textRenderer, "+", modX + modW - (needsScroll ? 17 : 13), currentY + 4, Color.GRAY.getRGB(), true);
                } else if (mod.getDescription() != null && !mod.getDescription().isEmpty()) {
                    context.drawText(net.minecraft.client.MinecraftClient.getInstance().textRenderer, "?", modX + modW - (needsScroll ? 17 : 13), currentY + 4, Color.GRAY.getRGB(), true);
                }
                
                if (isActive) {
                    if (needsScroll) com.mojang.blaze3d.systems.RenderSystem.disableScissor();
                    renderSettings(context, mouseX, mouseY, mod, modX + modW, Math.max(currentY, modY), delta);
                    if (needsScroll) com.mojang.blaze3d.systems.RenderSystem.enableScissor(scissorX, scissorY, scissorW, scissorH);
                }
                
                currentY += 16;
            }
        }
        
        cat.clearLayoutOverride();
        
        if (needsScroll) {
            com.mojang.blaze3d.systems.RenderSystem.disableScissor();
        }
    }
    
    private void renderSettings(DrawContext context, int mouseX, int mouseY, ModuleComponent mod, int x, int y, float delta) {
        List<Component> settings = mod.getSettings();
        int setW = SETTINGS_PANEL_WIDTH; // Wider for text
        
        // Wrap Description
        List<String> descLines = new ArrayList<>();
        int descH = 0;
        if (mod.getDescription() != null && !mod.getDescription().isEmpty()) {
            String fullDesc = "§7" + mod.getDescription();
            // Simple word wrap
            int wrapWidth = setW - 10;
            
            // Re-do wrapping cleanly:
            net.minecraft.client.font.TextRenderer tr = net.minecraft.client.MinecraftClient.getInstance().textRenderer;
            for (net.minecraft.text.OrderedText ot : tr.wrapLines(Text.of(fullDesc), wrapWidth)) {
                StringBuilder sb = new StringBuilder();
                ot.accept((index, style, codePoint) -> {
                    sb.append((char)codePoint);
                    return true;
                });
                descLines.add(sb.toString());
            }

            descH = (descLines.size() * 10) + 6; // Padding
        }

        // Calculate height
        int totalH = descH;
        for(Component s : settings) totalH += s.getHeight();
        if (totalH == 0) return; // Nothing to show?
        
        // Clamp
        if (y + totalH > this.height) y = this.height - totalH - 5;
        if (y < 5) y = 5;
        
        // Background
        context.fill(x, y, x + setW, y + totalH, new Color(0,0,0,200).getRGB());
        
        int currentY = y;
        
        // Render Description
        if (!descLines.isEmpty()) {
            for (String line : descLines) {
                 context.drawText(net.minecraft.client.MinecraftClient.getInstance().textRenderer, line, x + 4, currentY + 4, -1, true);
                 currentY += 10;
            }
            currentY += 6; // Padding from desc to items
        }

        CategoryComponent cat = mod.getParent();
        cat.setLayoutOverride(x, currentY - cat.height - mod.getOffset() - 16, setW, 0);
        
        for (Component s : settings) {
             s.render(context, mouseX, mouseY, delta);
             currentY += s.getHeight();
        }

        cat.clearLayoutOverride();
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
         // Route based on Sticky State
         if (stickyModule != null && stickyCategory != null) {
                SettingsLayout settingsLayout = getSettingsLayout(stickyCategory, stickyModule);
                if (settingsLayout != null
                        && mouseX >= settingsLayout.x
                        && mouseX <= settingsLayout.x + SETTINGS_PANEL_WIDTH
                        && mouseY >= settingsLayout.y
                        && mouseY <= settingsLayout.y + settingsLayout.totalHeight) {
                    CategoryComponent cat = stickyModule.getParent();
                    cat.setLayoutOverride(
                            settingsLayout.x,
                            settingsLayout.contentStartY - cat.height - stickyModule.getOffset() - 16,
                            SETTINGS_PANEL_WIDTH,
                            0
                    );

                    boolean handled = false;
                    for(Component s : stickyModule.getSettings()) {
                        if (s.mouseClicked(mouseX, mouseY, button)) {
                            handled = true;
                            break;
                        }
                    }

                    cat.clearLayoutOverride();

                    if (handled) return true;
                }
         }
         
         if (stickyCategory != null) {
              // Check Modules
               int modX = stickyCategory.x + stickyCategory.width;
               int modW = (stickyCategory.category == ModuleCategory.profiles) ? 140 : 110;
               int totalH = stickyCategory.getModules().size() * 16;
               int modY = getModulePanelY(stickyCategory, totalH);
               
               // Calculate available height (same as renderModules)
               int availableH = this.height - modY - 10;
               if (availableH < 50) availableH = 50;
               boolean needsScroll = totalH > availableH;
               int visibleH = needsScroll ? availableH : totalH;
               
               if (mouseX >= modX && mouseX <= modX + modW && mouseY >= modY && mouseY <= modY + visibleH) {
                    // Account for scroll offset when calculating index
                    int relY = (int)(mouseY - modY + modulesScrollY);
                    int idx = relY / 16; 
                    List<Component> modules = stickyCategory.getModules();
                   if (idx >= 0 && idx < modules.size()) {
                       Component c = modules.get(idx);
                       if (c instanceof ModuleComponent) {
                           ((ModuleComponent)c).getMod().toggle();
                           return true;
                       }
                   }
               }
         }
         return super.mouseClicked(mouseX, mouseY, button);
    }
    
    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
         if (stickyModule != null) {
             SettingsLayout settingsLayout = stickyCategory != null ? getSettingsLayout(stickyCategory, stickyModule) : null;
             CategoryComponent cat = stickyModule.getParent();
             if (settingsLayout != null) {
                 cat.setLayoutOverride(
                         settingsLayout.x,
                         settingsLayout.contentStartY - cat.height - stickyModule.getOffset() - 16,
                         SETTINGS_PANEL_WIDTH,
                         0
                 );
             }
             try {
                 for(Component s : stickyModule.getSettings()) {
                     if (s.mouseReleased(mouseX, mouseY, button)) return true;
                 }
             } finally {
                 if (settingsLayout != null) {
                     cat.clearLayoutOverride();
                 }
             }
         }
         return super.mouseReleased(mouseX, mouseY, button);
    }
    
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        // Check if hovering modules panel
        if (stickyCategory != null) {
            int modX = stickyCategory.x + stickyCategory.width;
            int modW = (stickyCategory.category == ModuleCategory.profiles) ? 140 : 110;
            int totalH = stickyCategory.getModules().size() * 16;
            int modY = getModulePanelY(stickyCategory, totalH);
            
            // Calculate available height based on screen size (same as renderModules)
            int availableH = this.height - modY - 10;
            if (availableH < 50) availableH = 50;
            
            boolean needsScroll = totalH > availableH;
            int visibleH = needsScroll ? availableH : totalH;
            
            if (needsScroll && mouseX >= modX && mouseX < modX + modW && mouseY >= modY && mouseY < modY + visibleH) {
                modulesScrollY -= (int)(amount * 16);
                int maxScroll = Math.max(0, totalH - visibleH);
                if (modulesScrollY > maxScroll) modulesScrollY = maxScroll;
                if (modulesScrollY < 0) modulesScrollY = 0;
                return true;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, amount);
    }

    private void updateCascadingState(int mouseX, int mouseY) {
         // 1. Check Root
         int catX = 10; 
         int catY = 20; 
         int catW = 100;
         int catH = 20;
         
         boolean inRoot = (mouseX >= catX && mouseX < catX + catW);
         if (inRoot) {
             for (CategoryComponent cat : categories) {
                  if (mouseY >= cat.y && mouseY < cat.y + cat.height) {
                      if (stickyCategory != cat) {
                          modulesScrollY = 0;
                          settingsScrollY = 0;
                      }
                      stickyCategory = cat;
                      stickyModule = null; 
                      return;
                  }
             }
         }
         
          // 2. Check Modules
           if (stickyCategory != null) {
                int modX = stickyCategory.x + stickyCategory.width;
                int modW = (stickyCategory.category == ModuleCategory.profiles) ? 140 : 110;
                int totalH = stickyCategory.getModules().size() * 16;
               int modY = getModulePanelY(stickyCategory, totalH);
               
               // Calculate available height (same as renderModules)
               int availableH = this.height - modY - 10;
               if (availableH < 50) availableH = 50;
               boolean needsScroll = totalH > availableH;
               int visibleH = needsScroll ? availableH : totalH;
               
               if (mouseX >= modX && mouseX < modX + modW && mouseY >= modY && mouseY < modY + visibleH) {
                   // Account for scroll offset when calculating index
                   int relY = (int)(mouseY - modY + modulesScrollY);
                   int idx = relY / 16; 
                   List<Component> modules = stickyCategory.getModules();
                   if (idx >= 0 && idx < modules.size()) {
                       Component c = modules.get(idx);
                       if (c instanceof ModuleComponent) {
                            stickyModule = (ModuleComponent) c;
                       }
                   }
                   return;
               }
              
             // 3. Check Settings
              if (stickyModule != null) {
                   // Ensure it has something to show
                   boolean hasContent = stickyModule.hasSettings() || (stickyModule.getDescription() != null && !stickyModule.getDescription().isEmpty());
                   if (hasContent) {
                      SettingsLayout settingsLayout = getSettingsLayout(stickyCategory, stickyModule);
                      if (settingsLayout != null
                              && mouseX >= settingsLayout.x
                              && mouseX < settingsLayout.x + SETTINGS_PANEL_WIDTH
                              && mouseY >= settingsLayout.y
                              && mouseY < settingsLayout.y + settingsLayout.totalHeight) {
                           return; // Keep open
                       }
                   }
              }
         }
         
         // If outside...
         if (stickyCategory != null) {
              // Add buffer? current logic detects "Not in Root, Not in Mod, Not in Settings".
              // If we are moving smoothly, we might skip a pixel?
              // By removing gaps, we minimize this. detect strict adjacency.
              stickyCategory = null;
              stickyModule = null;
         }
    }

    // Tooltip State
    private Text currentTooltip = null;
    public void setTooltip(Text tooltip) {
        this.currentTooltip = tooltip;
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (stickyModule != null) {
            for (Component s : stickyModule.getSettings()) {
                if (s instanceof xyz.ravenbs.clickgui.components.impl.StringComponent) {
                    if (((xyz.ravenbs.clickgui.components.impl.StringComponent) s).charTyped(chr, modifiers)) return true;
                }
            }
        }
        return super.charTyped(chr, modifiers);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (stickyModule != null) {
            for (Component s : stickyModule.getSettings()) {
                if (s.keyPressed(keyCode, scanCode, modifiers)) return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean shouldPause() { return false; }

    private SettingsLayout getSettingsLayout(CategoryComponent category, ModuleComponent module) {
        boolean hasContent = module.hasSettings() || (module.getDescription() != null && !module.getDescription().isEmpty());
        if (!hasContent) {
            return null;
        }

        int modX = category.x + category.width;
        int modW = (category.category == ModuleCategory.profiles) ? 140 : 110;
        int totalH = category.getModules().size() * 16;
        int modY = getModulePanelY(category, totalH);
        if (modY < 5) {
            modY = 5;
        }

        int modItemY = modY + category.getModules().indexOf(module) * 16 - modulesScrollY;
        int setX = modX + modW;
        int setY = Math.max(modItemY, modY);

        int descH = 0;
        if (module.getDescription() != null && !module.getDescription().isEmpty()) {
            net.minecraft.client.font.TextRenderer tr = net.minecraft.client.MinecraftClient.getInstance().textRenderer;
            int lines = tr.wrapLines(Text.of("§7" + module.getDescription()), SETTINGS_PANEL_WIDTH - 10).size();
            descH = (lines * 10) + 6;
        }

        int settingsHeight = descH;
        for(Component s : module.getSettings()) settingsHeight += s.getHeight();
        if (settingsHeight == 0) {
            return null;
        }

        if (setY + settingsHeight > this.height) setY = this.height - settingsHeight - 5;
        if (setY < 5) setY = 5;

        return new SettingsLayout(setX, setY, settingsHeight, setY + descH);
    }

    private int getModulePanelY(CategoryComponent cat, int totalH) {
        int modY = cat.y;
        int availableBelow = this.height - modY - 10;
        
        // If it fits below, stay there (but ensure at least some minimal space if scrolling needed)
        // Actually, if it needs scroll, we should maximize space if current space is too small.
        
        // Logic:
        // 1. If totalH fits below cat.y -> return cat.y
        if (totalH <= availableBelow) return modY;
        
        // 2. If it fits on screen (totalH < screen - 20) -> shift up to fit
        if (totalH <= this.height - 20) {
            return this.height - totalH - 10;
        }
        
        // 3. If it is HUGE (needs scroll)
        // If the space below is tiny (< 150px) and we have more space by moving up, move up.
        if (availableBelow < 150) {
             // Use full height (start at 10)
             return 10;
        }
        
        return modY;
    }
}
