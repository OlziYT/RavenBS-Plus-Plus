package xyz.ravenbs.module.impl.movement;

import xyz.ravenbs.module.Module;
import xyz.ravenbs.module.ModuleCategory;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.util.InputUtil;

public class InvMove extends Module {
    public InvMove() {
        super("InvMove", ModuleCategory.movement);
    }

    @Override
    public void onUpdate() {
        if (mc.currentScreen instanceof HandledScreen) {
             handleKey(mc.options.forwardKey);
             handleKey(mc.options.backKey);
             handleKey(mc.options.leftKey);
             handleKey(mc.options.rightKey);
             handleKey(mc.options.jumpKey);
             handleKey(mc.options.sprintKey);
             
             // Rotation logic usually goes here (allowing looking around in inventory)
             // Requires detecting arrow keys or mouse delta
        }
    }

    @Override
    public void onDisable() {
        if (mc.options == null || mc.getWindow() == null) {
            return;
        }

        syncKey(mc.options.forwardKey);
        syncKey(mc.options.backKey);
        syncKey(mc.options.leftKey);
        syncKey(mc.options.rightKey);
        syncKey(mc.options.jumpKey);
        syncKey(mc.options.sprintKey);
    }
    
    private void handleKey(net.minecraft.client.option.KeyBinding key) {
        syncKey(key);
    }

    private void syncKey(net.minecraft.client.option.KeyBinding key) {
        boolean pressed = InputUtil.isKeyPressed(
                mc.getWindow().getHandle(),
                ((xyz.ravenbs.mixin.client.MixinKeyBindingAccessor) key).getBoundKey().getCode()
        );
        key.setPressed(pressed);
    }
}
