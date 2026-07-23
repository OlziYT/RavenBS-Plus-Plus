package xyz.ravenbs.module.impl.world;

import xyz.ravenbs.module.Module;
import xyz.ravenbs.module.ModuleCategory;
import xyz.ravenbs.module.setting.impl.ButtonSetting;
import xyz.ravenbs.module.setting.impl.SliderSetting;

public class Weather extends Module {
    public SliderSetting time;
    public ButtonSetting rain;

    public Weather() {
        super("Weather", ModuleCategory.world);
        this.registerSetting(time = new SliderSetting("Time", 12000, 0, 24000, 1000));
        this.registerSetting(rain = new ButtonSetting("Disable Rain", true));
    }

    @Override
    public void onUpdate() {
        if (mc.world == null) return;
        
        // Client-side time change (visual only)
        // mc.world.setTimeOfDay((long) time.getInput()); // This requires mixin
        
        // Rain disable handled by mixin
    }
    
    public long getTime() {
        return (long) time.getInput();
    }
    
    public boolean shouldDisableRain() {
        return rain.isToggled();
    }
}
