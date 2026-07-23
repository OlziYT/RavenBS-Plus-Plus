package xyz.ravenbs.module.impl.combat;

import xyz.ravenbs.module.Module;
import xyz.ravenbs.module.ModuleCategory;
import xyz.ravenbs.module.setting.impl.SliderSetting;
import xyz.ravenbs.utility.Utils;

public class Reach extends Module {
    public static SliderSetting min;
    public static SliderSetting max;
    public static SliderSetting weaponOnly;
    private static double cachedReach = 3.0;

    public Reach() {
        super("Reach", ModuleCategory.combat);
        this.registerSetting(min = new SliderSetting("Min", 3.1, 3.0, 6.0, 0.05));
        this.registerSetting(max = new SliderSetting("Max", 3.3, 3.0, 6.0, 0.05));
        this.registerSetting(weaponOnly = new SliderSetting("Weapon only", 0, 0, 1, 1)); // Boolean as slider for now or we can use Button
    }

    @Override
    public void onEnable() {
        rollReach();
    }

    @Override
    public void onUpdate() {
        rollReach();
    }

    @Override
    public void onDisable() {
        cachedReach = 3.0;
    }

    public static double getReach() {
        return cachedReach;
    }

    public boolean isWeaponOnly() {
        return weaponOnly.getInput() >= 1.0;
    }

    private void rollReach() {
        if (min.getInput() >= max.getInput()) {
            cachedReach = min.getInput();
            return;
        }
        cachedReach = min.getInput() + Utils.random.nextDouble() * (max.getInput() - min.getInput());
    }
}
