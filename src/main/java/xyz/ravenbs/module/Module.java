package xyz.ravenbs.module;

import xyz.ravenbs.module.setting.Setting;
import xyz.ravenbs.module.setting.impl.ButtonSetting;
import xyz.ravenbs.module.setting.impl.SliderSetting;
import xyz.ravenbs.utility.Utils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;

import java.util.ArrayList;
import java.util.List;

public abstract class Module {
    protected static MinecraftClient mc = MinecraftClient.getInstance();
    
    protected String name;
    protected ModuleCategory category;
    protected int keycode;
    protected boolean enabled;
    private boolean isToggled = false;
    
    private ArrayList<Setting> settings = new ArrayList<>();
    
    public Module(String name, ModuleCategory category, int keycode) {
        this.name = name;
        this.category = category;
        this.keycode = keycode;
    }

    public Module(String name, ModuleCategory category) {
        this(name, category, 0);
    }
    
    public String getName() {
        return name;
    }
    
    public String getLocalizedName() {
        if (net.minecraft.client.resource.language.I18n.hasTranslation("raven.module." + name.toLowerCase() + ".name")) {
            return net.minecraft.client.resource.language.I18n.translate("raven.module." + name.toLowerCase() + ".name");
        }
        return name;
    }
    
    public String getDescription() {
        if (net.minecraft.client.resource.language.I18n.hasTranslation("raven.module." + name.toLowerCase() + ".desc")) {
            return net.minecraft.client.resource.language.I18n.translate("raven.module." + name.toLowerCase() + ".desc");
        }
        return null;
    }
    
    public ModuleCategory getCategory() {
        return category;
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        setEnabled(enabled, false);
    }

    public void setEnabled(boolean enabled, boolean notify) {
        if (this.enabled == enabled) {
            return;
        }

        this.enabled = enabled;
        if (enabled) {
            onEnable();
        } else {
            onDisable();
        }

        if (notify) {
            if (enabled) {
                xyz.ravenbs.utility.NotificationManager.show("Module", getName() + " Enabled", xyz.ravenbs.utility.Notification.Type.INFO);
            } else {
                xyz.ravenbs.utility.NotificationManager.show("Module", getName() + " Disabled", xyz.ravenbs.utility.Notification.Type.INFO);
            }
        }
    }
    
    public void toggle() {
        setEnabled(!enabled, true);
    }
    
    public void onEnable() {
        // Override
    }
    
    public void onPreMotion(xyz.ravenbs.event.PreMotionEvent e) {}
    public void onPostMotion(xyz.ravenbs.event.PostMotionEvent e) {}
    public void onPreUpdate() {}
    public void onPostUpdate() {}
    public void onReceivePacket(xyz.ravenbs.event.ReceivePacketEvent e) {}
    public void onSendPacket(xyz.ravenbs.event.SendPacketEvent e) {}
    public void onRenderWorld(net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext context) {}
    
    public void disable() {
        setEnabled(false);
    }

    public void enable() {
        setEnabled(true);
    }

    public void onDisable() {
        // Override
    }
    
    public void onRender(net.minecraft.client.gui.DrawContext context, float tickDelta) {}
    
    public void onUpdate() {
        // Frame/Tick update
    }
    
    public void onKeyBind() {
        if (this.keycode != 0) {
           boolean isDown;
           if (this.keycode < 0) {
               // Mouse
               int button = -100 - this.keycode;
               isDown = org.lwjgl.glfw.GLFW.glfwGetMouseButton(mc.getWindow().getHandle(), button) == org.lwjgl.glfw.GLFW.GLFW_PRESS;
           } else {
               // Keyboard
               isDown = InputUtil.isKeyPressed(mc.getWindow().getHandle(), this.keycode);
           }
           
           if (isDown && !this.isToggled) {
               this.toggle();
               this.isToggled = true;
           } else if (!isDown) {
               this.isToggled = false;
           }
        }
    }
    
    public int getKeycode() {
        return keycode;
    }
    
    public void setBind(int keycode) {
        this.keycode = keycode;
    }
    
    public void registerSetting(Setting setting) {
        this.settings.add(setting);
    }
    
    public List<Setting> getSettings() {
        return settings;
    }
}
