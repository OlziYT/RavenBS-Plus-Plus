package xyz.ravenbs.module.impl.render;

import xyz.ravenbs.module.Module;
import xyz.ravenbs.module.ModuleCategory;
import xyz.ravenbs.module.setting.impl.DescriptionSetting;
import xyz.ravenbs.module.setting.impl.SliderSetting;
import net.minecraft.util.Identifier;

import java.util.Locale;

public class Shaders extends Module {
    private SliderSetting shader;
    private String[] shaderNames = {"Blur", "Bits", "Antialias", "Creeper", "Desaturate", "Flip", "Invert", "Notch", "NTSC", "Outline", "Phosphor", "Sobel", "Spider", "Wobble"};
    private int appliedShaderIndex = -1;

    public Shaders() {
        super("Shaders", ModuleCategory.render);
        this.registerSetting(new DescriptionSetting("Applies post-processing shaders."));
        this.registerSetting(shader = new SliderSetting("Shader", 0, shaderNames));
    }

    @Override
    public void onEnable() {
        if (mc.gameRenderer == null) {
            this.disable();
            return;
        }
        
        applyShader();
    }

    @Override
    public void onUpdate() {
        int selected = getShaderIndex();
        if (selected != appliedShaderIndex) {
            applyShader();
        }
    }

    @Override
    public void onDisable() {
        if (mc.gameRenderer != null) {
            mc.gameRenderer.disablePostProcessor();
        }
        appliedShaderIndex = -1;
    }
    
    private void applyShader() {
        if (mc.gameRenderer == null) {
            return;
        }

        mc.gameRenderer.disablePostProcessor();

        try {
            String shaderPath = shaderNames[getShaderIndex()].toLowerCase(Locale.ROOT);
            Identifier shaderId = new Identifier("minecraft", "shaders/post/" + shaderPath + ".json");
            ((xyz.ravenbs.mixin.accessor.InvokerGameRenderer) mc.gameRenderer).invokeLoadPostProcessor(shaderId);
            appliedShaderIndex = getShaderIndex();
        } catch (Exception e) {
            xyz.ravenbs.RavenBSFabric.LOGGER.error("Failed to load shader {}", shaderNames[getShaderIndex()], e);
            appliedShaderIndex = -1;
            disable();
        }
    }
    
    public int getShaderIndex() {
        return (int) shader.getInput();
    }
}
