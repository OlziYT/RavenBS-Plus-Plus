package xyz.ravenbs.module;

import xyz.ravenbs.module.impl.client.GuiModule;
import xyz.ravenbs.module.impl.combat.AimAssist;
import xyz.ravenbs.module.impl.combat.AutoClicker;
import xyz.ravenbs.module.impl.combat.HitBox;
import xyz.ravenbs.module.impl.combat.KillAura;
import xyz.ravenbs.module.impl.combat.Reach;
import xyz.ravenbs.module.impl.combat.Velocity;
import xyz.ravenbs.module.impl.combat.WTap;
import xyz.ravenbs.module.impl.movement.AutoJump;
import xyz.ravenbs.module.impl.movement.Fly;
import xyz.ravenbs.module.impl.movement.InvMove;
import xyz.ravenbs.module.impl.movement.NoSlow;
import xyz.ravenbs.module.impl.minigames.BedAura;
import xyz.ravenbs.module.impl.movement.SafeWalk;
import xyz.ravenbs.module.impl.movement.Speed;
import xyz.ravenbs.module.impl.movement.Sprint;
import xyz.ravenbs.module.impl.world.Scaffold;
import xyz.ravenbs.module.impl.world.FastBreak;
import xyz.ravenbs.module.impl.other.AntiAFK;
import xyz.ravenbs.module.impl.player.AntiVoid;
import xyz.ravenbs.module.impl.player.AutoTool;
import xyz.ravenbs.module.impl.player.FastPlace;
import xyz.ravenbs.module.impl.player.NoFall;
import xyz.ravenbs.module.impl.render.ChestESP;
import xyz.ravenbs.module.impl.render.ESP;
import xyz.ravenbs.module.impl.render.FullBright;
import xyz.ravenbs.module.impl.render.HUD;
import xyz.ravenbs.module.impl.render.Tracers;
import xyz.ravenbs.module.impl.render.NameTags;
import xyz.ravenbs.module.impl.render.BridgeInfo;
import xyz.ravenbs.module.impl.movement.Timer;
import xyz.ravenbs.module.impl.minigames.SumoFence;
import xyz.ravenbs.module.impl.combat.AutoWeapon;
import xyz.ravenbs.event.PreMotionEvent;
import xyz.ravenbs.event.PostMotionEvent;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ModuleManager {
    public static List<Module> modules = new ArrayList<>();
    private static final Logger LOGGER = LoggerFactory.getLogger("ModuleManager");
    
    public static void onPreMotion(xyz.ravenbs.event.PreMotionEvent e) {
        for (Module m : modules) {
            if (m.isEnabled()) {
                try {
                    m.onPreMotion(e);
                } catch (Throwable t) {
                    logModuleError(m, "onPreMotion", t);
                }
            }
        }
    }

    public static void onPostMotion(xyz.ravenbs.event.PostMotionEvent e) {
        for (Module m : modules) {
            if (m.isEnabled()) {
                try {
                    m.onPostMotion(e);
                } catch (Throwable t) {
                    logModuleError(m, "onPostMotion", t);
                }
            }
        }
    }
    
    public static void onPreUpdate() {
        for (Module m : modules) {
            if (m.isEnabled()) {
                try {
                    m.onPreUpdate();
                } catch (Throwable t) {
                    logModuleError(m, "onPreUpdate", t);
                }
            }
        }
    }
    
    public static void onPostUpdate() {
        for (Module m : modules) {
            if (m.isEnabled()) {
                try {
                    m.onPostUpdate();
                } catch (Throwable t) {
                    logModuleError(m, "onPostUpdate", t);
                }
            }
        }
    }

    public static void onUpdate() {
        for (Module m : modules) {
            try {
                m.onKeyBind();
            } catch (Throwable t) {
                logModuleError(m, "onKeyBind", t);
            }
            if (m.isEnabled()) {
                try {
                    m.onUpdate();
                } catch (Throwable t) {
                    logModuleError(m, "onUpdate", t);
                }
            }
        }
    }
    
    public static void onReceivePacket(xyz.ravenbs.event.ReceivePacketEvent e) {
        for (Module m : modules) {
            if (m.isEnabled()) {
                try {
                    m.onReceivePacket(e);
                } catch (Throwable t) {
                    logModuleError(m, "onReceivePacket", t);
                }
            }
        }
    }
    
    public static void onSendPacket(xyz.ravenbs.event.SendPacketEvent e) {
        if (e.getPacket() instanceof net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket) {
            String msg = ((net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket) e.getPacket()).chatMessage();
            if (xyz.ravenbs.utility.CommandManager.onChat(msg)) {
                e.setCancelled(true);
                return;
            }
        }
        for (Module m : modules) {
            if (m.isEnabled()) {
                try {
                    m.onSendPacket(e);
                } catch (Throwable t) {
                    logModuleError(m, "onSendPacket", t);
                }
            }
        }
    }
    
    public static void onRenderWorld(net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext context) {
        for (Module m : modules) {
            if (m.isEnabled()) {
                try {
                    m.onRenderWorld(context);
                } catch (Throwable t) {
                    logModuleError(m, "onRenderWorld", t);
                }
            }
        }
    }

    public static void onRender(net.minecraft.client.gui.DrawContext context, float tickDelta) {
        for (Module m : modules) {
            if (m.isEnabled()) {
                try {
                    m.onRender(context, tickDelta);
                } catch (Throwable t) {
                    logModuleError(m, "onRender", t);
                }
            }
        }
    }

    public static void sort() {
        modules.sort(Comparator.comparing(Module::getName));
    }
    
    // Modules
    public static Sprint sprint;
    public static GuiModule gui;
    // Render
    public static FullBright fullBright;
    public static HUD hud;
    public static ESP esp;
    // Other
    public static AntiAFK antiAFK;
    public static AutoJump autoJump;
    
    // Combat
    public static KillAura killAura;
    public static Velocity velocity;
    public static Reach reach;
    public static AutoClicker autoClicker;
    public static AimAssist aimAssist;
    public static HitBox hitBox;
    public static WTap wTap;
    public static AutoWeapon autoWeapon;

    // Movement
    public static Speed speed;
    public static Fly fly;
    public static SafeWalk safeWalk;
    
    // Player
    public static NoFall noFall;
    public static FastPlace fastPlace;
    public static AntiVoid antiVoid;

    // World
    public static Scaffold scaffold;
    public static FastBreak fastBreak;
    
    // Minigames
    public static BedAura bedAura;
    public static xyz.ravenbs.module.impl.render.BedESP bedESP;
    public static xyz.ravenbs.module.impl.world.AntiBot antiBot;
    public static xyz.ravenbs.module.impl.other.NameHider nameHider; // Static reference for mixins
    public static ChestESP chestESP;
    public static SumoFence sumoFence;
    public static Tracers tracers;
    public static NameTags nameTags;
    public static BridgeInfo bridgeInfo;
    
    // Combat (Moved up)
    
    // Movement
    public static InvMove invMove;
    public static NoSlow noSlow;
    public static Timer timer;
    
    // Other/Player
    public static xyz.ravenbs.module.impl.player.AutoTool autoTool;
    public static xyz.ravenbs.module.impl.render.Chams chams;
    public static xyz.ravenbs.module.impl.render.NoCameraClip noCameraClip;
    public static xyz.ravenbs.module.impl.render.NoHurtCam noHurtCam;
    public static xyz.ravenbs.module.impl.render.Trajectories trajectories; // Maybe needed later?

    public void register() {
        // --- Combat ---
        addModule(killAura = new KillAura());
        addModule(autoClicker = new AutoClicker());
        addModule(aimAssist = new AimAssist());
        addModule(reach = new Reach());
        addModule(velocity = new Velocity());
        addModule(wTap = new WTap());
        addModule(new xyz.ravenbs.module.impl.combat.STap());
        addModule(hitBox = new HitBox());
        addModule(new xyz.ravenbs.module.impl.combat.BurstClicker());
        addModule(new xyz.ravenbs.module.impl.combat.ClickAssist());
        addModule(new xyz.ravenbs.module.impl.combat.Reduce());
        addModule(new xyz.ravenbs.module.impl.combat.RodAimbot());
        addModule(new xyz.ravenbs.module.impl.combat.TPAura());
        addModule(new xyz.ravenbs.module.impl.combat.JumpReset());
        addModule(autoWeapon = new AutoWeapon());

        // --- Movement ---
        addModule(speed = new Speed());
        addModule(new xyz.ravenbs.module.impl.movement.BHop());
        addModule(fly = new Fly());
        addModule(new xyz.ravenbs.module.impl.movement.Boost());
        addModule(sprint = new Sprint());
        addModule(new xyz.ravenbs.module.impl.movement.KeepSprint());
        addModule(new xyz.ravenbs.module.impl.movement.LongJump());
        addModule(safeWalk = new SafeWalk());
        addModule(autoJump = new AutoJump());
        addModule(invMove = new InvMove());
        addModule(noSlow = new NoSlow());
        addModule(timer = new Timer());
        addModule(new xyz.ravenbs.module.impl.movement.StopMotion());
        addModule(new xyz.ravenbs.module.impl.movement.VClip());
        addModule(new xyz.ravenbs.module.impl.movement.Teleport());

        // --- Player ---
        addModule(noFall = new NoFall());
        addModule(fastPlace = new FastPlace());
        addModule(new xyz.ravenbs.module.impl.player.AutoPlace());
        addModule(new xyz.ravenbs.module.impl.player.FastMine());
        addModule(autoTool = new AutoTool());
        addModule(new xyz.ravenbs.module.impl.player.InvManager());
        addModule(new xyz.ravenbs.module.impl.player.AutoSwap());
        addModule(new xyz.ravenbs.module.impl.player.WaterBucket());
        addModule(new xyz.ravenbs.module.impl.player.Blink());
        addModule(new xyz.ravenbs.module.impl.player.Freecam());
        addModule(new xyz.ravenbs.module.impl.player.NoRotate());
        addModule(antiVoid = new AntiVoid());
        addModule(new xyz.ravenbs.module.impl.player.AntiFireball());
        addModule(new xyz.ravenbs.module.impl.player.DelayRemover());

        // --- Render ---
        addModule(chams = new xyz.ravenbs.module.impl.render.Chams());
        addModule(esp = new ESP());
        addModule(new xyz.ravenbs.module.impl.render.PlayerESP());
        addModule(new xyz.ravenbs.module.impl.render.MobESP());
        addModule(chestESP = new ChestESP());
        addModule(bedESP = new xyz.ravenbs.module.impl.render.BedESP());
        addModule(new xyz.ravenbs.module.impl.render.ItemESP());
        addModule(tracers = new Tracers());
        addModule(nameTags = new NameTags());
        addModule(fullBright = new FullBright());
        addModule(hud = new HUD());
        addModule(bridgeInfo = new BridgeInfo());
        addModule(new xyz.ravenbs.module.impl.render.TargetHUD());
        addModule(new xyz.ravenbs.module.impl.render.Radar());
        addModule(trajectories = new xyz.ravenbs.module.impl.render.Trajectories());
        addModule(new xyz.ravenbs.module.impl.render.Indicators());
        addModule(new xyz.ravenbs.module.impl.render.Potions());
        addModule(new xyz.ravenbs.module.impl.render.Shaders());
        addModule(new xyz.ravenbs.module.impl.render.Xray());
        addModule(new xyz.ravenbs.module.impl.render.AntiShuffle());
        addModule(noCameraClip = new xyz.ravenbs.module.impl.render.NoCameraClip());
        addModule(noHurtCam = new xyz.ravenbs.module.impl.render.NoHurtCam());
        addModule(new xyz.ravenbs.module.impl.render.KeyStrokes());
        addModule(new xyz.ravenbs.module.impl.render.ExtendCamera());
        addModule(new xyz.ravenbs.module.impl.render.BreakProgress());

        // --- World ---
        addModule(scaffold = new Scaffold());
        addModule(fastBreak = new FastBreak());
        addModule(antiBot = new xyz.ravenbs.module.impl.world.AntiBot());
        addModule(new xyz.ravenbs.module.impl.world.Weather());

        // --- Minigames ---
        addModule(bedAura = new BedAura());
        addModule(new xyz.ravenbs.module.impl.minigames.BedWars());
        addModule(new xyz.ravenbs.module.impl.minigames.SkyWars());
        addModule(sumoFence = new SumoFence());
        addModule(new xyz.ravenbs.module.impl.minigames.MurderMystery());
        addModule(new xyz.ravenbs.module.impl.minigames.DuelsStats());
        addModule(new xyz.ravenbs.module.impl.minigames.SpeedBuilders());
        addModule(new xyz.ravenbs.module.impl.minigames.AutoRequeue());
        addModule(new xyz.ravenbs.module.impl.minigames.AutoWho());

        // --- Other / Fun ---
        addModule(new xyz.ravenbs.module.impl.other.Anticheat());
        addModule(new xyz.ravenbs.module.impl.other.ViewPackets());
        addModule(nameHider = new xyz.ravenbs.module.impl.other.NameHider());
        addModule(new xyz.ravenbs.module.impl.other.FakeLag());
        addModule(new xyz.ravenbs.module.impl.other.LatencyAlerts());
        addModule(new xyz.ravenbs.module.impl.other.FakeChat());
        addModule(new xyz.ravenbs.module.impl.other.ChatBypass());
        addModule(antiAFK = new AntiAFK());
        addModule(new xyz.ravenbs.module.impl.fun.Spin());
        addModule(new xyz.ravenbs.module.impl.fun.Derp());

        // --- Client ---
        addModule(gui = new GuiModule());
    }
    
    public void addModule(Module m) {
        modules.add(m);
    }
    
    private static void logModuleError(Module module, String phase, Throwable t) {
        LOGGER.error("Module {} threw during {}", module.getName(), phase, t);
    }
    
    public static Module getModule(String name) {
        for (Module m : modules) {
            if (m.getName().equalsIgnoreCase(name)) {
                return m;
            }
        }
        return null;
    }
    
    public static Module getModule(Class<? extends Module> clazz) {
        for (Module m : modules) {
            if (m.getClass() == clazz) {
                return m;
            }
        }
        return null;
    }
}
