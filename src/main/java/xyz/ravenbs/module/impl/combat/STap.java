package xyz.ravenbs.module.impl.combat;

import xyz.ravenbs.event.PostMotionEvent;
import xyz.ravenbs.event.PreMotionEvent;
import xyz.ravenbs.module.Module;
import xyz.ravenbs.module.ModuleCategory;
import xyz.ravenbs.module.setting.impl.SliderSetting;
import net.minecraft.entity.Entity;

public class STap extends Module {
    public static SliderSetting range, chance, delay;
    
    public STap() {
        super("STap", ModuleCategory.combat);
        this.registerSetting(range = new SliderSetting("Range", 3.0, 3.0, 6.0, 0.1));
        this.registerSetting(chance = new SliderSetting("Chance", 100, 0, 100, 1));
        this.registerSetting(delay = new SliderSetting("Delay ms", 100, 50, 500, 10)); // how long to hold S
    }

    // Usually invoked on Attack, but for simplicity we'll check it in Update or reuse WTap logic pattern
    // Ideally we should hook into AttackEntityEvent or simply check attack flag
    
    private long lastAttackTime = 0;
    private boolean isHolding = false;
    private long startHoldTime = 0;

    @Override
    public void onUpdate() {
        if (isHolding) {
            if (System.currentTimeMillis() - startHoldTime > delay.getInput()) {
                isHolding = false;
                syncBackKeyState();
            }
        }
    }
    
    // Simplification: We need an event when player attacks.
    // Since we don't have AttackEvent easily wired in all modules yet (except via Mixin),
    // let's rely on detection via simple logic or assume AutoClicker/KillAura might trigger it?
    // Actually, let's implement a public 'onAttack' method in Module that MixinClientPlayerEntity calls?
    // Or simpler: Check mc.options.attackKey.isPressed() or LeftClickCounter.
    
    // Better: We'll implement onPreUpdate and check if we are swinging/attacking target.
    // Raven b+ uses "PostAttack" event essentially.
    
    @Override
    public void onPostMotion(PostMotionEvent e) {
        if (mc.player == null) return;
        
        // This is a naive implementation without a proper AttackEvent.
        // Usually WTap/STap works best with KillAura or manual clicks.
        // For now, let's just leave it basic or placeholder if we can't detect attack reliably.
    }
    
    // Public method to be called by KillAura or Mixin
    public void onAttack(Entity target) {
        if (mc.player == null || mc.options == null || target == null) {
            return;
        }

        if (Math.random() * 100 > chance.getInput()) return;
        
        if (mc.player.distanceTo(target) <= range.getInput()) {
             startSTap();
        }
    }
    
    private void startSTap() {
        isHolding = true;
        startHoldTime = System.currentTimeMillis();
        mc.options.backKey.setPressed(true);
    }

    @Override
    public void onDisable() {
        isHolding = false;
        syncBackKeyState();
    }

    private void syncBackKeyState() {
        if (mc.options == null || mc.getWindow() == null) {
            return;
        }

        int keyCode = ((xyz.ravenbs.mixin.client.MixinKeyBindingAccessor) mc.options.backKey).getBoundKey().getCode();
        boolean pressed = net.minecraft.client.util.InputUtil.isKeyPressed(mc.getWindow().getHandle(), keyCode);
        mc.options.backKey.setPressed(pressed);
    }
}
