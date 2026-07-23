package xyz.ravenbs.module.impl.other;

import xyz.ravenbs.module.Module;
import xyz.ravenbs.module.ModuleCategory;
import xyz.ravenbs.module.setting.impl.DescriptionSetting;
import xyz.ravenbs.module.setting.impl.StringSetting;
import xyz.ravenbs.module.setting.impl.ButtonSetting;
import net.minecraft.text.Text;

public class FakeChat extends Module {
    private StringSetting message;
    private ButtonSetting sendOnEnable;

    public FakeChat() {
        super("FakeChat", ModuleCategory.other);
        this.registerSetting(new DescriptionSetting("Sends fake chat messages (client-side)."));
        this.registerSetting(message = new StringSetting("Message", "Test message"));
        this.registerSetting(sendOnEnable = new ButtonSetting("Send on enable", true));
    }

    @Override
    public void onEnable() {
        if (sendOnEnable.isToggled() && mc.player != null) {
            mc.player.sendMessage(Text.of(message.getString()), false);
            this.disable();
        }
    }
}
