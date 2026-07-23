package xyz.ravenbs.module.impl.other;

import xyz.ravenbs.module.Module;
import xyz.ravenbs.module.ModuleCategory;
import xyz.ravenbs.module.setting.impl.ButtonSetting;
import xyz.ravenbs.module.setting.impl.DescriptionSetting;
import xyz.ravenbs.module.setting.impl.SliderSetting;
import xyz.ravenbs.utility.Utils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.text.Text;

import java.util.HashMap;
import java.util.UUID;

public class Anticheat extends Module {
    private SliderSetting interval;
    private ButtonSetting ignoreTeammates;
    private ButtonSetting shouldPing;
    private ButtonSetting scaffold;
    private ButtonSetting noFall;
    private ButtonSetting noSlow;

    private HashMap<UUID, Long> flags = new HashMap<>();
    private long lastAlert;

    public Anticheat() {
        super("Anticheat", ModuleCategory.other);
        this.registerSetting(new DescriptionSetting("Tries to detect cheaters."));
        this.registerSetting(interval = new SliderSetting("Flag interval", 20.0, 0.0, 60.0, 1.0));
        this.registerSetting(ignoreTeammates = new ButtonSetting("Ignore teammates", false));
        this.registerSetting(shouldPing = new ButtonSetting("Should ping", true));
        this.registerSetting(new DescriptionSetting("Detected cheats"));
        this.registerSetting(scaffold = new ButtonSetting("Scaffold", true));
        this.registerSetting(noFall = new ButtonSetting("NoFall", true));
        this.registerSetting(noSlow = new ButtonSetting("NoSlow", true));
    }

    @Override
    public void onUpdate() {
        if (mc.world == null || mc.player == null) return;
        if (mc.isIntegratedServerRunning()) return;
        
        for (PlayerEntity player : mc.world.getPlayers()) {
            if (player == mc.player) continue;
            if (player.isDead()) continue;
            if (xyz.ravenbs.module.impl.world.AntiBot.isBot(player)) continue;
            if (ignoreTeammates.isToggled() && Utils.isTeamMate(player)) continue;
            
            performCheck(player);
        }
    }

    private void performCheck(PlayerEntity player) {
        // Basic scaffold check
        if (scaffold.isToggled()) {
            if (player.getPitch() >= 70.0f && 
                player.getMainHandStack().getItem() instanceof BlockItem &&
                player.handSwinging &&
                !player.isOnGround()) {
                
                alert(player, "Scaffold");
            }
        }
        
        // Basic NoFall check - if player is falling but not taking damage
        // This requires tracking fall distance which is complex
        
        // Basic NoSlow check - if player is using item but moving at full speed
        // This requires tracking speed which is complex
    }

    private void alert(PlayerEntity player, String cheat) {
        long now = System.currentTimeMillis();
        
        // Check interval
        Long lastFlag = flags.get(player.getUuid());
        if (lastFlag != null && now - lastFlag < interval.getInput() * 1000) {
            return;
        }
        
        flags.put(player.getUuid(), now);
        
        // Send alert
        String msg = "§7[§dR§7]§r " + player.getName().getString() + " §7detected for §d" + cheat;
        mc.player.sendMessage(Text.of(msg), false);
        
        // Ping sound
        if (shouldPing.isToggled() && now - lastAlert >= 1500) {
            mc.player.playSound(net.minecraft.sound.SoundEvents.BLOCK_NOTE_BLOCK_PLING.value(), 1.0f, 1.0f);
            lastAlert = now;
        }
    }

    @Override
    public void onDisable() {
        flags.clear();
        lastAlert = 0;
    }
}
