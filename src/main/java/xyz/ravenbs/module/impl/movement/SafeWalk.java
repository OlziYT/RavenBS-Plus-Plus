package xyz.ravenbs.module.impl.movement;

import xyz.ravenbs.module.Module;
import xyz.ravenbs.module.ModuleCategory;

public class SafeWalk extends Module {
    public SafeWalk() {
        super("SafeWalk", ModuleCategory.movement);
    }
    
    // Logic is handled in MixinEntity
}
