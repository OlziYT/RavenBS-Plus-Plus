package xyz.ravenbs.module.impl.client;

import xyz.ravenbs.clickgui.ClickGuiScreen;
import xyz.ravenbs.module.Module;
import xyz.ravenbs.module.ModuleCategory;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class GuiModule extends Module {
    public static ClickGuiScreen clickGui;
    
    public GuiModule() {
        super("Gui", ModuleCategory.client, GLFW.GLFW_KEY_RIGHT_SHIFT);
    }
    
    @Override
    public void onEnable() {
        if (clickGui == null) {
            clickGui = new ClickGuiScreen();
        }
        mc.setScreen(clickGui);
        this.setEnabled(false); // Disable immediately so we can open it again later logic
    }
}
