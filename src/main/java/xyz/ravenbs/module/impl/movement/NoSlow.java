package xyz.ravenbs.module.impl.movement;

import xyz.ravenbs.module.Module;
import xyz.ravenbs.module.ModuleCategory;

public class NoSlow extends Module {
    public NoSlow() {
        super("NoSlow", ModuleCategory.movement);
    }
    
    // Logic is handled in MixinClientPlayerEntity (movement input modification)
}
