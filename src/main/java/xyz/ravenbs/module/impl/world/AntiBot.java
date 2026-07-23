package xyz.ravenbs.module.impl.world;

import xyz.ravenbs.module.Module;
import xyz.ravenbs.module.ModuleCategory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

public class AntiBot extends Module {
    public AntiBot() {
        super("AntiBot", ModuleCategory.world);
    }
    
    public static boolean isBot(Entity e) {
        if (!(e instanceof PlayerEntity)) return false;
        if (xyz.ravenbs.module.ModuleManager.antiBot == null || !xyz.ravenbs.module.ModuleManager.antiBot.isEnabled()) return false;
        
        // Simple check: Is in TabList?
        // Note: Hypixel Watchdog bots often ARE in tab list but invisible or have weird latency.
        // For standard "AntiBot", checking tab list presence is reliable for basic NPC removal.
        if (mc.getNetworkHandler() == null) return false;
        
        // If not in player list -> Bot
        boolean inTab = mc.getNetworkHandler().getPlayerListEntry(e.getUuid()) != null;
        return !inTab;
    }
}
