package xyz.ravenbs.module.impl.minigames;

import xyz.ravenbs.module.Module;
import xyz.ravenbs.module.ModuleCategory;
import xyz.ravenbs.utility.RenderUtils;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import xyz.ravenbs.utility.Utils;
import xyz.ravenbs.utility.RenderUtils; // Also likely missing

public class MurderMystery extends Module {
    private List<PlayerEntity> murderers = new ArrayList<>();
    private List<PlayerEntity> detectives = new ArrayList<>();

    public MurderMystery() {
        super("MurderMystery", ModuleCategory.minigames);
    }
    
    @Override
    public void onEnable() {
        murderers.clear();
        detectives.clear();
    }

    @Override
    public void onUpdate() {
        if (mc.world == null) return;
        
        for (PlayerEntity p : mc.world.getPlayers()) {
            if (p == mc.player) continue;
            
            // Check held item
            if (p.getMainHandStack().getItem() == Items.IRON_SWORD) {
                if (!murderers.contains(p)) {
                    murderers.add(p);
                    Utils.sendMessage("§cMurderer detected: " + p.getName().getString());
                }
            } else if (p.getMainHandStack().getItem() == Items.BOW) {
                if (!detectives.contains(p)) {
                    detectives.add(p);
                    Utils.sendMessage("§bDetective detected: " + p.getName().getString());
                }
            }
        }
    }
    
    @Override
    public void onRenderWorld(net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext context) {
        for (PlayerEntity p : murderers) {
            RenderUtils.drawEntityBox(context.matrixStack(), p, Color.RED);
        }
        for (PlayerEntity p : detectives) {
            RenderUtils.drawEntityBox(context.matrixStack(), p, Color.BLUE);
        }
    }
}
