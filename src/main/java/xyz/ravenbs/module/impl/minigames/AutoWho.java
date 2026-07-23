package xyz.ravenbs.module.impl.minigames;

import xyz.ravenbs.module.Module;
import xyz.ravenbs.module.ModuleCategory;

public class AutoWho extends Module {
    public AutoWho() {
        super("AutoWho", ModuleCategory.minigames);
    }

    @Override
    public void onEnable() {
        if (mc.player != null) {
            mc.player.networkHandler.sendChatCommand("who");
        }
        this.disable();
    }
}
