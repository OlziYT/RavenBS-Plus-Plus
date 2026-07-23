package xyz.ravenbs.module.impl.render;

import com.mojang.blaze3d.systems.RenderSystem;
import xyz.ravenbs.module.Module;
import xyz.ravenbs.module.ModuleCategory;
import xyz.ravenbs.module.impl.combat.KillAura;
import xyz.ravenbs.module.setting.impl.ButtonSetting;
import xyz.ravenbs.module.setting.impl.DescriptionSetting;
import xyz.ravenbs.module.setting.impl.SliderSetting;
import xyz.ravenbs.utility.RenderUtils;
import xyz.ravenbs.utility.Utils;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

import java.awt.*;

public class TargetHUD extends Module {
    private SliderSetting mode;
    private ButtonSetting showStatus;
    private ButtonSetting healthColor;
    
    private LivingEntity target;
    private double lastHealth;
    private float lastHealthBar;
    private long lastAliveMS;
    
    public int posX = 70;
    public int posY = 30;
    
    private String[] modes = new String[]{"Modern", "Simple"};

    private ButtonSetting editPosition;

    public TargetHUD() {
        super("TargetHUD", ModuleCategory.render);
        this.registerSetting(new DescriptionSetting("Only works with KillAura."));
        this.registerSetting(mode = new SliderSetting("Mode", 0, modes));
        this.registerSetting(editPosition = new ButtonSetting("Edit position", false));
        this.registerSetting(showStatus = new ButtonSetting("Show Win/Loss", true));
        this.registerSetting(healthColor = new ButtonSetting("Health Color", true));
    }

    @Override
    public void onUpdate() {
        if (editPosition.isToggled()) {
            mc.setScreen(new EditScreen());
            editPosition.setEnabled(false);
        }
    }

    public void render(DrawContext context, float delta) {
        if (!this.isEnabled()) return;
        
        // Logic to determine target
        if (KillAura.target instanceof LivingEntity) {
            target = (LivingEntity) KillAura.target;
            lastAliveMS = System.currentTimeMillis();
        } else if (target != null) {
            if (System.currentTimeMillis() - lastAliveMS > 1000 || target.isDead() || (target instanceof PlayerEntity && !((PlayerEntity)target).isAlive())) {
                target = null;
                return;
            }
        } else {
            return;
        }

        if (target == null) return;

        // Render Logic
        String name = target.getName().getString();
        float health = target.getHealth();
        float maxHealth = target.getMaxHealth();
        float healthPct = MathHelper.clamp(health / maxHealth, 0, 1);
        
        int width = 120;
        int height = 50;
        int x = posX;
        int y = posY;
        
        // Background
        context.fill(x, y, x + width, y + height, new Color(0, 0, 0, 120).getRGB());
        
        // Face
        if (target instanceof PlayerEntity) {
            try {
                net.minecraft.client.network.PlayerListEntry entry = mc.getNetworkHandler().getPlayerListEntry(target.getUuid());
                if (entry != null) {
                    RenderSystem.setShaderTexture(0, entry.getSkinTexture());
                    context.drawTexture(entry.getSkinTexture(), x + 5, y + 5, 32, 32, 8, 8, 8, 8, 64, 64);
                }
            } catch (Exception ignored) {}
        }
        
        // Name
        context.drawText(mc.textRenderer, name, x + 40, y + 8, -1, true);
        
        // Health Bar
        float barWidth = width - 42;
        float barHeight = 8;
        float barX = x + 40;
        float barY = y + 25;
        
        // Animation
        if (lastHealthBar != healthPct) {
            lastHealthBar = Utils.interpolate(lastHealthBar, healthPct, 0.1f);
        }
        
        // Bar Background
        context.fill((int)barX, (int)barY, (int)(barX + barWidth), (int)(barY + barHeight), new Color(0, 0, 0, 100).getRGB());
        
        // Bar Foreground
        int color = healthColor.isToggled() ? Utils.getColorForHealth(healthPct) : new Color(0, 200, 0).getRGB();
        context.fill((int)barX, (int)barY, (int)(barX + (barWidth * lastHealthBar)), (int)(barY + barHeight), color);
        
        // HP Text
        String hpStr = String.format("%.1f HP", health);
        context.drawText(mc.textRenderer, hpStr, x + 40, y + 36, -1, true);
        
        if (showStatus.isToggled() && mc.player != null) {
            String win = (healthPct < (mc.player.getHealth()/mc.player.getMaxHealth())) ? "§aW" : "§cL";
            context.drawText(mc.textRenderer, win, x + width - 15, y + 36, -1, true);
        }
    }

    class EditScreen extends Screen {
        private boolean dragging = false;
        private int dragX, dragY;

        protected EditScreen() {
            super(Text.of("TargetHUD Editor"));
        }

        @Override
        public void render(DrawContext context, int mouseX, int mouseY, float delta) {
            this.renderBackground(context);
            
            // Draw fake target HUD
            int width = 120;
            int height = 50;
            int x = posX;
            int y = posY;
            
            context.fill(x, y, x + width, y + height, new Color(0, 0, 0, 120).getRGB());
            context.drawText(mc.textRenderer, "Target Name", x + 40, y + 8, -1, true);
            context.fill(x + 40, y + 25, x + 110, y + 33, new Color(0, 255, 0).getRGB());
            context.drawBorder(x, y, width, height, new Color(255, 255, 255).getRGB());
            
            context.drawCenteredTextWithShadow(mc.textRenderer, "Drag to move. ESC to save.", this.width / 2, this.height / 2, -1);
            
            super.render(context, mouseX, mouseY, delta);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (mouseX >= posX && mouseX <= posX + 120 && mouseY >= posY && mouseY <= posY + 50) {
                dragging = true;
                dragX = (int)mouseX - posX;
                dragY = (int)mouseY - posY;
                return true;
            }
            return super.mouseClicked(mouseX, mouseY, button);
        }

        @Override
        public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
            if (dragging) {
                posX = (int)mouseX - dragX;
                posY = (int)mouseY - dragY;
            }
            return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
        }

        @Override
        public boolean mouseReleased(double mouseX, double mouseY, int button) {
            dragging = false;
            return super.mouseReleased(mouseX, mouseY, button);
        }
    }
}
