package xyz.ravenbs.module.impl.render;

import xyz.ravenbs.module.Module;
import xyz.ravenbs.module.ModuleCategory;
import xyz.ravenbs.module.ModuleManager;
import xyz.ravenbs.module.setting.impl.ButtonSetting;
import xyz.ravenbs.module.setting.impl.SliderSetting;
import xyz.ravenbs.utility.RenderUtils;
import xyz.ravenbs.utility.Theme;
import xyz.ravenbs.utility.Utils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

import java.awt.Color;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class HUD extends Module {
    public static xyz.ravenbs.module.setting.impl.ColorSetting color;
    public static ButtonSetting showInfo;
    public static ButtonSetting showWatermark;
    public static ButtonSetting showArrayList;
    public static ButtonSetting alignRight;
    public static ButtonSetting lowercase;
    
    public static int posX = 5;
    public static int posY = 70;
    
    public HUD() {
        super("HUD", ModuleCategory.render);
        this.registerSetting(color = new xyz.ravenbs.module.setting.impl.ColorSetting("Theme", new Color(0, 255, 0)));
        // Enable Rainbow by default to match "current palette"
        color.getRainbow().setEnabled(true);
        this.registerSetting(showArrayList = new ButtonSetting("Show Array List", true));
        this.registerSetting(showWatermark = new ButtonSetting("Show Watermark", true));
        this.registerSetting(showInfo = new ButtonSetting("Show module info", true));
        this.registerSetting(alignRight = new ButtonSetting("Align right", false));
        this.registerSetting(lowercase = new ButtonSetting("Lowercase", false));
        this.setEnabled(true); // Enable by default
    }

    public void onRender(DrawContext context) { // Adapter for single arg call if needed
        onRender(context, 1.0f);
    }

    public void onRender(DrawContext context, float tickDelta) {
        if (!this.isEnabled() || mc.options.debugEnabled) return;

        // Render Watermark
        if (showWatermark.isToggled()) {
            renderWatermark(context);
        }

        // Render ArrayList
        if (showArrayList.isToggled()) {
            renderArrayList(context);
        }
    }

    private void renderWatermark(DrawContext context) {
        String text = lowercase.isToggled() ? "raven bS++" : "Raven BS++";
        
        int c;
        if (color.getRainbow().isToggled()) {
             c = Utils.getChroma(2, 0);
        } else {
             c = color.getRGB();
        }
        
        context.drawText(mc.textRenderer, text, 5, 5, c, true);
    }

    private void renderArrayList(DrawContext context) {
        List<Module> sortedModules = ModuleManager.modules.stream()
            .filter(m -> !m.getName().equals("HUD"))
            .filter(m -> !m.getName().equals("Gui"))
            .filter(m -> {
                boolean enabled = m.isEnabled();
                boolean configEnabled = xyz.ravenbs.config.ConfigManager.isModuleEnabledInConfig(m.getName());
                // Show if currently enabled OR if it WAS enabled in config (so we can show removal)
                return enabled || configEnabled;
            })
            .sorted(Comparator.comparingInt(m -> {
                String name = getDisplayName(m);
                return -mc.textRenderer.getWidth(name);
            }))
            .collect(Collectors.toList());

        int y = posY;
        double delay = 0;

        for (Module module : sortedModules) {
            String name = getDisplayName(module);
            
            int c;
            if (color.getRainbow().isToggled()) {
                c = Utils.getChroma(2, (long) delay);
            } else {
                c = color.getRGB();
            }

            int width = mc.textRenderer.getWidth(name);
            int x = alignRight.isToggled() ? context.getScaledWindowWidth() - width - 5 : posX;

            context.drawText(mc.textRenderer, name, x, y, c, true);
            
            y += mc.textRenderer.fontHeight + 2;
            delay -= 200; // Delay for rainbow gradient
        }
    }

    private String getDisplayName(Module module) {
        String name = getModuleName(module);
        boolean enabled = module.isEnabled();
        boolean configEnabled = xyz.ravenbs.config.ConfigManager.isModuleEnabledInConfig(module.getName());
        
        if (enabled && !configEnabled) {
            return "+" + name;
        } else if (!enabled && configEnabled) {
            return "-" + name;
        }
        return name;
    }

    private String getModuleName(Module module) {
        String name = module.getName();
        if (lowercase.isToggled()) {
            name = name.toLowerCase();
        }
        return name;
    }
}
