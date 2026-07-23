package xyz.ravenbs.module.impl.minigames;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.text.Text;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.MathHelper;
import xyz.ravenbs.module.Module;
import xyz.ravenbs.module.ModuleCategory;
import xyz.ravenbs.module.impl.combat.KillAura;
import xyz.ravenbs.module.impl.other.NameHider;
import xyz.ravenbs.module.setting.impl.ButtonSetting;
import xyz.ravenbs.module.setting.impl.DescriptionSetting;
import xyz.ravenbs.module.setting.impl.ModeSetting;
import xyz.ravenbs.module.setting.impl.SliderSetting;
import xyz.ravenbs.utility.Notification;
import xyz.ravenbs.utility.NotificationManager;
import xyz.ravenbs.utility.Utils;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class DuelsStats extends Module {
    private static final String[] LAYOUTS = {"Horizontal", "Vertical"};
    private static final String[] TARGET_MODES = {"Auto", "Crosshair", "Aura", "Attacker", "Closest"};
    private static final String[] TARGET_MODE_KEYS = {"auto", "crosshair", "aura", "attacker", "closest"};

    private final ButtonSetting renderHud;
    private final ButtonSetting targetAlerts;
    private final ButtonSetting showHeldItem;
    private final ButtonSetting showWeaponInfo;
    private final ButtonSetting showEffects;
    private final ButtonSetting showFightState;
    private final ButtonSetting middleClickLock;
    private final ButtonSetting showLockState;
    private final ButtonSetting onlyInDuels;
    private final ButtonSetting compactMode;
    private final ButtonSetting editPosition;
    private final ModeSetting layoutMode;
    private final ModeSetting targetMode;
    private final SliderSetting hudScale;
    private final SliderSetting targetRange;
    private final SliderSetting targetPersistence;

    private PlayerEntity opponent;
    private UUID lockedTargetUuid;
    private long lastSeenMs;
    private float animatedHealth;
    private boolean wasMiddleClickDown;

    public int posX = 6;
    public int posY = 90;

    public DuelsStats() {
        super("DuelsStats", ModuleCategory.minigames);
        this.registerSetting(new DescriptionSetting("Shows live stats for the tracked duel opponent."));
        this.registerSetting(renderHud = new ButtonSetting("Render HUD", true));
        this.registerSetting(editPosition = new ButtonSetting("Edit position", false));
        this.registerSetting(layoutMode = new ModeSetting("Layout", LAYOUTS, 0));
        this.registerSetting(targetMode = new ModeSetting("Target mode", TARGET_MODES, 0));
        this.registerSetting(hudScale = new SliderSetting("Size", 1.0, 0.5, 2.0, 0.05));
        this.registerSetting(compactMode = new ButtonSetting("Compact mode", false));
        this.registerSetting(onlyInDuels = new ButtonSetting("Only in duels", false));
        this.registerSetting(targetAlerts = new ButtonSetting("Target alerts", true));
        this.registerSetting(showHeldItem = new ButtonSetting("Show held item", true));
        this.registerSetting(showWeaponInfo = new ButtonSetting("Weapon info", true));
        this.registerSetting(showEffects = new ButtonSetting("Effects", true));
        this.registerSetting(showFightState = new ButtonSetting("Fight state", true));
        this.registerSetting(middleClickLock = new ButtonSetting("Middle click lock", true));
        this.registerSetting(showLockState = new ButtonSetting("Show lock state", true));
        this.registerSetting(targetRange = new SliderSetting("Target range", 32, 8, 64, 1));
        this.registerSetting(targetPersistence = new SliderSetting("Target persist", 1.2, 0.0, 3.0, 0.1));

        layoutMode.visible = false;
        targetMode.visible = false;
        hudScale.visible = false;
        compactMode.visible = false;
    }

    @Override
    public void onDisable() {
        opponent = null;
        lockedTargetUuid = null;
        lastSeenMs = 0L;
        animatedHealth = 0.0f;
        wasMiddleClickDown = false;
    }

    @Override
    public void onUpdate() {
        if (editPosition.isToggled()) {
            mc.setScreen(new EditScreen());
            editPosition.setEnabled(false);
        }

        if (mc.player == null || mc.world == null) {
            clearTarget();
            return;
        }

        handleMiddleClickLock();

        if (onlyInDuels.isToggled() && !isLikelyDuel()) {
            clearTarget();
            return;
        }

        PlayerEntity nextOpponent = resolveOpponent();
        if (nextOpponent != null) {
            if (nextOpponent != opponent && targetAlerts.isToggled()) {
                NotificationManager.show(tr("raven.duelsstats.notify.title"), trf("raven.duelsstats.notify.tracking", formatPlayerName(nextOpponent)), Notification.Type.INFO, 1500);
            }
            opponent = nextOpponent;
            lastSeenMs = System.currentTimeMillis();
            animatedHealth = getHealthPct(opponent);
            return;
        }

        if (opponent != null) {
            long persistenceMs = (long) (targetPersistence.getInput() * 1000.0);
            if (!isAliveCandidate(opponent) || System.currentTimeMillis() - lastSeenMs > persistenceMs) {
                clearTarget();
            }
        }
    }

    @Override
    public void onRender(DrawContext context, float tickDelta) {
        if (!renderHud.isToggled() || mc.player == null || opponent == null || !isAliveCandidate(opponent)) {
            return;
        }

        animatedHealth = Utils.interpolate(animatedHealth, getHealthPct(opponent), 0.18f);

        context.getMatrices().push();
        context.getMatrices().translate(posX, posY, 0);
        context.getMatrices().scale(getScale(), getScale(), 1.0f);
        if (isVertical()) {
            renderVertical(context, opponent);
        } else {
            renderHorizontal(context, opponent);
        }
        context.getMatrices().pop();
    }

    private void renderHorizontal(DrawContext context, PlayerEntity target) {
        int width = getBaseHudWidth();
        String name = trimToWidth(formatPlayerName(target), width - 56);

        context.fill(0, 0, width, getBaseHudHeight(), new Color(0, 0, 0, 145).getRGB());
        context.fill(0, 0, width, 1, new Color(255, 255, 255, 45).getRGB());

        drawPlayerHead(context, target, 6, 6);
        context.drawText(mc.textRenderer, name, 28, 7, 0xFFFFFFFF, true);
        if (showLockState.isToggled() && isLockedTarget(target)) {
            context.drawText(mc.textRenderer, tr("raven.duelsstats.preview.locked"), width - 32, 7, 0xFF7CFC8A, true);
        }

        if (showHeldItem.isToggled()) {
            ItemStack heldStack = target.getMainHandStack();
            if (!heldStack.isEmpty()) {
                context.drawItem(heldStack, width - 22, 5);
            }
        }

        drawHealthBar(context, 28, 20, width - 34, 7, animatedHealth);
        context.drawText(mc.textRenderer, getHealthText(target), 28, 31, 0xFFFFFFFF, true);

        int infoY = 42;
        if (isCompact()) {
            context.drawText(mc.textRenderer, getCompactCoreLine(target), 6, infoY, 0xFFCED8E5, true);
            infoY += 10;
        } else {
            context.drawText(mc.textRenderer, trf("raven.duelsstats.preview.armor", target.getArmor()), 28, infoY, 0xFFB7C4D6, true);
            context.drawText(mc.textRenderer, trf("raven.duelsstats.preview.distance", getDistanceValue(target)), width - 52, infoY, 0xFFDDDDDD, true);
            infoY += 10;
            int ping = getLatency(target);
            context.drawText(mc.textRenderer, trf("raven.duelsstats.preview.ping", ping >= 0 ? ping + "ms" : "?"), 28, infoY, 0xFF9FE870, true);
            infoY += 10;
        }

        for (InfoRow row : buildExtraRows(target)) {
            context.drawText(mc.textRenderer, trimToWidth(row.text, width - 12), 6, infoY, row.color, true);
            infoY += 10;
        }
    }

    private void renderVertical(DrawContext context, PlayerEntity target) {
        int width = getBaseHudWidth();
        String name = trimToWidth(formatPlayerName(target), width - 16);

        context.fill(0, 0, width, getBaseHudHeight(), new Color(0, 0, 0, 145).getRGB());
        context.fill(0, 0, width, 1, new Color(255, 255, 255, 45).getRGB());

        drawPlayerHead(context, target, (width / 2) - 8, 6);
        context.drawCenteredTextWithShadow(mc.textRenderer, Text.of(name), width / 2, 28, 0xFFFFFFFF);
        if (showLockState.isToggled() && isLockedTarget(target)) {
            context.drawText(mc.textRenderer, tr("raven.duelsstats.preview.locked_short"), 8, 8, 0xFF7CFC8A, true);
        }

        if (showHeldItem.isToggled()) {
            ItemStack heldStack = target.getMainHandStack();
            if (!heldStack.isEmpty()) {
                context.drawItem(heldStack, width - 20, 6);
            }
        }

        drawHealthBar(context, 8, 42, width - 16, 7, animatedHealth);
        context.drawCenteredTextWithShadow(mc.textRenderer, Text.of(getHealthText(target)), width / 2, 53, 0xFFFFFFFF);

        int infoY = 66;
        if (isCompact()) {
            context.drawCenteredTextWithShadow(mc.textRenderer, Text.of(trimToWidth(trf("raven.duelsstats.preview.distance", getDistanceValue(target)), width - 16)), width / 2, infoY, 0xFFDDDDDD);
            infoY += 10;
            int ping = getLatency(target);
            String compactLine = trf("raven.duelsstats.preview.vertical_compact", target.getArmor(), ping >= 0 ? ping + "ms" : "?");
            context.drawCenteredTextWithShadow(mc.textRenderer, Text.of(trimToWidth(compactLine, width - 16)), width / 2, infoY, 0xFFCED8E5);
            infoY += 10;
        } else {
            context.drawCenteredTextWithShadow(mc.textRenderer, Text.of(trimToWidth(trf("raven.duelsstats.preview.distance", getDistanceValue(target)), width - 16)), width / 2, infoY, 0xFFDDDDDD);
            infoY += 10;
            context.drawCenteredTextWithShadow(mc.textRenderer, Text.of(trimToWidth(trf("raven.duelsstats.preview.armor", target.getArmor()), width - 16)), width / 2, infoY, 0xFFB7C4D6);
            infoY += 10;
            int ping = getLatency(target);
            context.drawCenteredTextWithShadow(mc.textRenderer, Text.of(trimToWidth(trf("raven.duelsstats.preview.ping", ping >= 0 ? ping + "ms" : "?"), width - 16)), width / 2, infoY, 0xFF9FE870);
            infoY += 10;
        }

        for (InfoRow row : buildExtraRows(target)) {
            context.drawCenteredTextWithShadow(mc.textRenderer, Text.of(trimToWidth(row.text, width - 16)), width / 2, infoY, row.color);
            infoY += 10;
        }
    }

    private List<InfoRow> buildExtraRows(PlayerEntity target) {
        List<InfoRow> rows = new ArrayList<>();
        if (showFightState.isToggled()) {
            rows.add(new InfoRow(getStatusText(target), getStatusColor(target)));
        }
        if (showWeaponInfo.isToggled()) {
            rows.add(new InfoRow(getWeaponText(target), 0xFFE8DAB2));
        }
        if (showEffects.isToggled()) {
            rows.add(new InfoRow(getEffectsSummary(target), 0xFF9CC9FF));
        }
        return rows;
    }

    private void drawHealthBar(DrawContext context, int x, int y, int width, int height, float healthPct) {
        context.fill(x, y, x + width, y + height, new Color(20, 20, 20, 190).getRGB());
        context.fill(x, y, x + (int) (width * MathHelper.clamp(healthPct, 0.0f, 1.0f)), y + height, Utils.getColorForHealth(healthPct));
    }

    private void drawPlayerHead(DrawContext context, PlayerEntity target, int x, int y) {
        if (mc.getNetworkHandler() == null) {
            return;
        }

        PlayerListEntry entry = mc.getNetworkHandler().getPlayerListEntry(target.getUuid());
        if (entry != null) {
            context.drawTexture(entry.getSkinTexture(), x, y, 16, 16, 8, 8, 8, 8, 64, 64);
        }
    }

    private void clearTarget() {
        opponent = null;
        lastSeenMs = 0L;
        animatedHealth = 0.0f;
    }

    private PlayerEntity resolveOpponent() {
        PlayerEntity lockedTarget = getLockedTarget();
        if (lockedTarget != null) {
            return lockedTarget;
        }

        return switch (targetMode.getInput()) {
            case 1 -> getCrosshairTarget();
            case 2 -> getKillAuraTarget();
            case 3 -> getAttackerTarget();
            case 4 -> getClosestTarget();
            default -> resolveSmartTarget();
        };
    }

    private PlayerEntity resolveSmartTarget() {
        PlayerEntity lockedTarget = getLockedTarget();
        if (lockedTarget != null) {
            return lockedTarget;
        }

        PlayerEntity crosshairTarget = getCrosshairTarget();
        if (isValidOpponent(crosshairTarget)) {
            return crosshairTarget;
        }

        PlayerEntity killAuraTarget = getKillAuraTarget();
        if (isValidOpponent(killAuraTarget)) {
            return killAuraTarget;
        }

        PlayerEntity attackerTarget = getAttackerTarget();
        if (isValidOpponent(attackerTarget)) {
            return attackerTarget;
        }

        if (isValidOpponent(opponent)) {
            return opponent;
        }

        return getClosestTarget();
    }

    private void handleMiddleClickLock() {
        if (!middleClickLock.isToggled() || mc.currentScreen != null) {
            wasMiddleClickDown = false;
            return;
        }

        boolean middleDown = org.lwjgl.glfw.GLFW.glfwGetMouseButton(mc.getWindow().getHandle(), org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_MIDDLE) == org.lwjgl.glfw.GLFW.GLFW_PRESS;
        if (!middleDown) {
            wasMiddleClickDown = false;
            return;
        }

        if (wasMiddleClickDown) {
            return;
        }
        wasMiddleClickDown = true;

        PlayerEntity hoveredTarget = getCrosshairTargetRaw();
        if (hoveredTarget != null && isAliveCandidate(hoveredTarget) && !Utils.isTeamMate(hoveredTarget)) {
            if (isLockedTarget(hoveredTarget)) {
                lockedTargetUuid = null;
                NotificationManager.show(tr("raven.duelsstats.notify.title"), tr("raven.duelsstats.notify.unlocked"), Notification.Type.INFO, 1200);
            } else {
                lockedTargetUuid = hoveredTarget.getUuid();
                opponent = hoveredTarget;
                lastSeenMs = System.currentTimeMillis();
                NotificationManager.show(tr("raven.duelsstats.notify.title"), trf("raven.duelsstats.notify.locked", formatPlayerName(hoveredTarget)), Notification.Type.INFO, 1500);
            }
            return;
        }

        if (lockedTargetUuid != null) {
            lockedTargetUuid = null;
            NotificationManager.show(tr("raven.duelsstats.notify.title"), tr("raven.duelsstats.notify.unlocked"), Notification.Type.INFO, 1200);
        }
    }

    private PlayerEntity getLockedTarget() {
        if (lockedTargetUuid == null || mc.world == null) {
            return null;
        }

        PlayerEntity player = mc.world.getPlayerByUuid(lockedTargetUuid);
        if (!isAliveCandidate(player)) {
            lockedTargetUuid = null;
            return null;
        }

        return player;
    }

    private PlayerEntity getCrosshairTarget() {
        PlayerEntity player = getCrosshairTargetRaw();
        if (isValidOpponent(player)) {
            return player;
        }
        return null;
    }

    private PlayerEntity getCrosshairTargetRaw() {
        if (mc.crosshairTarget instanceof EntityHitResult entityHitResult && entityHitResult.getEntity() instanceof PlayerEntity player) {
            return player;
        }
        return null;
    }

    private PlayerEntity getKillAuraTarget() {
        if (KillAura.target instanceof PlayerEntity player && isValidOpponent(player)) {
            return player;
        }
        return null;
    }

    private PlayerEntity getAttackerTarget() {
        if (mc.player.getAttacker() instanceof PlayerEntity player && isValidOpponent(player)) {
            return player;
        }
        return null;
    }

    private PlayerEntity getClosestTarget() {
        List<PlayerEntity> candidates = new ArrayList<>();
        for (PlayerEntity player : mc.world.getPlayers()) {
            if (isValidOpponent(player)) {
                candidates.add(player);
            }
        }

        if (candidates.isEmpty()) {
            return null;
        }

        candidates.sort(Comparator.comparingDouble(player -> mc.player.squaredDistanceTo(player)));
        return candidates.get(0);
    }

    private boolean isValidOpponent(PlayerEntity player) {
        if (!isAliveCandidate(player)) {
            return false;
        }

        if (mc.player.squaredDistanceTo(player) > targetRange.getInput() * targetRange.getInput()) {
            return false;
        }

        return !Utils.isTeamMate(player);
    }

    private boolean isAliveCandidate(PlayerEntity player) {
        return mc.player != null && player != null && player != mc.player && player.isAlive() && !player.isSpectator();
    }

    private boolean isLockedTarget(PlayerEntity player) {
        return lockedTargetUuid != null && player != null && lockedTargetUuid.equals(player.getUuid());
    }

    private boolean isLikelyDuel() {
        if (mc.world == null) {
            return false;
        }

        Scoreboard scoreboard = mc.world.getScoreboard();
        ScoreboardObjective objective = scoreboard.getObjectiveForSlot(1);
        if (objective == null) {
            return false;
        }

        String title = objective.getDisplayName().getString().toLowerCase(Locale.ROOT);
        String name = objective.getName().toLowerCase(Locale.ROOT);
        return title.contains("duel")
                || name.contains("duel")
                || title.contains("boxing")
                || title.contains("sumo")
                || title.contains("bridge")
                || title.contains("uhc")
                || title.contains("nodebuff")
                || title.contains("combo");
    }

    private float getHealthPct(PlayerEntity player) {
        float totalHealth = Math.max(1.0f, player.getMaxHealth() + player.getAbsorptionAmount());
        return MathHelper.clamp((player.getHealth() + player.getAbsorptionAmount()) / totalHealth, 0.0f, 1.0f);
    }

    private String getHealthText(PlayerEntity player) {
        float total = Math.max(0.0f, player.getHealth()) + Math.max(0.0f, player.getAbsorptionAmount());
        float max = Math.max(1.0f, player.getMaxHealth() + player.getAbsorptionAmount());
        return trf("raven.duelsstats.preview.hp", total, max);
    }

    private String formatDistance(PlayerEntity player) {
        return String.format(Locale.ROOT, "%.1f", getDistanceValue(player));
    }

    private double getDistanceValue(PlayerEntity player) {
        return mc.player.distanceTo(player);
    }

    private String formatPlayerName(PlayerEntity player) {
        return NameHider.format(player.getName().getString());
    }

    private String getCompactCoreLine(PlayerEntity player) {
        int ping = getLatency(player);
        return trf("raven.duelsstats.preview.core_compact", mc.player.distanceTo(player), player.getArmor(), ping >= 0 ? ping + "ms" : "?");
    }

    private String getTargetModeLabel() {
        int index = MathHelper.clamp(targetMode.getInput(), 0, TARGET_MODES.length - 1);
        return tr("raven.duelsstats.target." + TARGET_MODE_KEYS[index]);
    }

    private String getWeaponText(PlayerEntity player) {
        ItemStack stack = player.getMainHandStack();
        if (stack.isEmpty()) {
            return tr("raven.duelsstats.preview.weapon_none");
        }

        double damage = getAttackDamage(stack) + getWeaponEnchantBonus(stack);
        return trf("raven.duelsstats.preview.weapon", trimName(stack.getName().getString(), 10), damage);
    }

    private String getEffectsSummary(PlayerEntity player) {
        if (player.getStatusEffects().isEmpty()) {
            return tr("raven.duelsstats.preview.effects_none");
        }

        List<String> parts = new ArrayList<>();
        for (StatusEffectInstance effect : player.getStatusEffects()) {
            String id = Registries.STATUS_EFFECT.getId(effect.getEffectType()).getPath();
            if (effect.getDuration() <= 0) {
                continue;
            }

            parts.add(abbreviateEffect(id) + (effect.getAmplifier() + 1));
            if (parts.size() >= (isCompact() ? 2 : 3)) {
                break;
            }
        }

        return parts.isEmpty() ? tr("raven.duelsstats.preview.effects_none") : tr("raven.duelsstats.preview.effects_prefix") + String.join(" ", parts);
    }

    private String abbreviateEffect(String id) {
        if (id.contains("strength")) return "STR";
        if (id.contains("speed")) return "SPD";
        if (id.contains("regeneration")) return "REG";
        if (id.contains("fire_resistance")) return "FIR";
        if (id.contains("resistance")) return "RES";
        if (id.contains("jump_boost")) return "JMP";
        if (id.contains("absorption")) return "ABS";
        return id.length() <= 3 ? id.toUpperCase(Locale.ROOT) : id.substring(0, 3).toUpperCase(Locale.ROOT);
    }

    private String getStatusText(PlayerEntity target) {
        String hitState = target.hurtTime > 0
                ? trf("raven.duelsstats.preview.status_hurt", target.hurtTime)
                : tr("raven.duelsstats.preview.status_open");
        return hitState + " | " + getFightStateLabel(target);
    }

    private int getStatusColor(PlayerEntity target) {
        if (target.hurtTime > 0) {
            return 0xFFFFAA55;
        }

        return switch (getFightStateKey(target)) {
            case "adv" -> 0xFF7CFC8A;
            case "risk" -> 0xFFFF7A7A;
            default -> 0xFFE6D96A;
        };
    }

    private String getFightStateKey(PlayerEntity target) {
        double selfScore = getFightScore(mc.player);
        double targetScore = getFightScore(target);
        double delta = selfScore - targetScore;

        if (delta > 5.0) {
            return "adv";
        }
        if (delta < -5.0) {
            return "risk";
        }
        return "even";
    }

    private String getFightStateLabel(PlayerEntity target) {
        return tr("raven.duelsstats.state." + getFightStateKey(target));
    }

    private double getFightScore(PlayerEntity player) {
        double score = player.getHealth() + player.getAbsorptionAmount();
        score += player.getArmor() * 0.85;
        score += getWeaponThreat(player) * 0.9;
        score += getArmorEnchantScore(player) * 0.7;

        for (StatusEffectInstance effect : player.getStatusEffects()) {
            String id = Registries.STATUS_EFFECT.getId(effect.getEffectType()).getPath();
            if (id.contains("strength")) score += 3.5 + effect.getAmplifier();
            if (id.contains("speed")) score += 1.2 + (effect.getAmplifier() * 0.5);
            if (id.contains("regeneration")) score += 1.7 + (effect.getAmplifier() * 0.6);
            if (id.contains("resistance")) score += 2.2 + effect.getAmplifier();
            if (id.contains("absorption")) score += 2.0 + effect.getAmplifier();
        }

        return score;
    }

    private double getWeaponThreat(PlayerEntity player) {
        ItemStack stack = player.getMainHandStack();
        return getAttackDamage(stack) + getWeaponEnchantBonus(stack);
    }

    private double getArmorEnchantScore(PlayerEntity player) {
        double total = 0.0;
        for (ItemStack armorStack : player.getArmorItems()) {
            total += EnchantmentHelper.getLevel(Enchantments.PROTECTION, armorStack);
            total += EnchantmentHelper.getLevel(Enchantments.BLAST_PROTECTION, armorStack) * 0.5;
            total += EnchantmentHelper.getLevel(Enchantments.PROJECTILE_PROTECTION, armorStack) * 0.35;
            total += EnchantmentHelper.getLevel(Enchantments.UNBREAKING, armorStack) * 0.15;
        }
        return total;
    }

    private double getWeaponEnchantBonus(ItemStack stack) {
        if (stack.isEmpty()) {
            return 0.0;
        }

        double bonus = 0.0;
        bonus += EnchantmentHelper.getLevel(Enchantments.SHARPNESS, stack) * 1.0;
        bonus += EnchantmentHelper.getLevel(Enchantments.FIRE_ASPECT, stack) * 0.35;
        bonus += EnchantmentHelper.getLevel(Enchantments.KNOCKBACK, stack) * 0.2;
        return bonus;
    }

    private double getAttackDamage(ItemStack stack) {
        double damage = 1.0;
        for (EntityAttributeModifier modifier : stack.getAttributeModifiers(EquipmentSlot.MAINHAND).get(EntityAttributes.GENERIC_ATTACK_DAMAGE)) {
            damage += modifier.getValue();
        }
        return damage;
    }

    private String trimName(String name, int maxLength) {
        return name.length() > maxLength ? name.substring(0, maxLength) : name;
    }

    private String trimToWidth(String text, int maxWidth) {
        if (mc == null || mc.textRenderer == null || mc.textRenderer.getWidth(text) <= maxWidth) {
            return text;
        }

        String suffix = "...";
        int suffixWidth = mc.textRenderer.getWidth(suffix);
        if (suffixWidth >= maxWidth) {
            return suffix;
        }

        int end = text.length();
        while (end > 0 && mc.textRenderer.getWidth(text.substring(0, end)) + suffixWidth > maxWidth) {
            end--;
        }
        return text.substring(0, Math.max(0, end)) + suffix;
    }

    private String tr(String key) {
        return I18n.translate(key);
    }

    private String trf(String key, Object... args) {
        return I18n.translate(key, args);
    }

    private boolean isVertical() {
        return layoutMode.getInput() == 1;
    }

    private boolean isCompact() {
        return compactMode.isToggled();
    }

    private int getBaseHudWidth() {
        if (isVertical()) {
            return isCompact() ? 88 : 96;
        }
        return isCompact() ? 160 : 176;
    }

    private int getBaseHudHeight() {
        int height = isVertical() ? 64 : 52;
        if (isVertical()) {
            height += isCompact() ? 20 : 30;
        } else if (!isCompact()) {
            height += 20;
        }
        if (showFightState.isToggled()) height += 10;
        if (showWeaponInfo.isToggled()) height += 10;
        if (showEffects.isToggled()) height += 10;
        return height;
    }

    private float getScale() {
        return (float) hudScale.getInput();
    }

    private int getHudWidth() {
        return Math.max(1, Math.round(getBaseHudWidth() * getScale()));
    }

    private int getHudHeight() {
        return Math.max(1, Math.round(getBaseHudHeight() * getScale()));
    }

    private void clampHudPosition(int screenWidth, int screenHeight) {
        posX = MathHelper.clamp(posX, 0, Math.max(0, screenWidth - getHudWidth()));
        posY = MathHelper.clamp(posY, 0, Math.max(0, screenHeight - getHudHeight()));
    }

    private int getLatency(PlayerEntity player) {
        if (mc.getNetworkHandler() == null) {
            return -1;
        }

        PlayerListEntry entry = mc.getNetworkHandler().getPlayerListEntry(player.getUuid());
        return entry != null ? entry.getLatency() : -1;
    }

    private class EditScreen extends Screen {
        private boolean dragging;
        private boolean draggingSizeSlider;
        private int dragX;
        private int dragY;

        private EditScreen() {
            super(Text.of(tr("raven.duelsstats.editor.title")));
        }

        @Override
        public void render(DrawContext context, int mouseX, int mouseY, float delta) {
            this.renderBackground(context);

            int width = getHudWidth();
            int height = getHudHeight();
            int previewBorder = dragging ? 0xFF72C4FF : 0x99FFFFFF;
            context.fill(posX - 2, posY - 2, posX + width + 2, posY + height + 2, new Color(0, 0, 0, 70).getRGB());
            context.fill(posX, posY, posX + width, posY + height, new Color(0, 0, 0, 145).getRGB());
            context.drawBorder(posX, posY, width, height, previewBorder);

            context.getMatrices().push();
            context.getMatrices().translate(posX, posY, 0);
            context.getMatrices().scale(getScale(), getScale(), 1.0f);

            if (isVertical()) {
                context.drawCenteredTextWithShadow(mc.textRenderer, Text.of(tr("raven.duelsstats.preview.title")), getBaseHudWidth() / 2, 10, 0xFFFFFFFF);
                drawHealthBar(context, 8, 42, getBaseHudWidth() - 16, 7, 0.72f);
                context.drawCenteredTextWithShadow(mc.textRenderer, Text.of(trf("raven.duelsstats.preview.hp", 16.5f, 20.0f)), getBaseHudWidth() / 2, 53, 0xFFFFFFFF);
                int rowY = 66;
                if (isCompact()) {
                    context.drawText(mc.textRenderer, trf("raven.duelsstats.preview.core_compact", 3.6f, 12, "42ms"), 8, rowY, 0xFFCED8E5, true);
                    rowY += 10;
                } else {
                    context.drawText(mc.textRenderer, trf("raven.duelsstats.preview.distance", 3.6f), 8, rowY, 0xFFDDDDDD, true);
                    rowY += 10;
                    context.drawText(mc.textRenderer, trf("raven.duelsstats.preview.armor", 12), 8, rowY, 0xFFB7C4D6, true);
                    rowY += 10;
                    context.drawText(mc.textRenderer, trf("raven.duelsstats.preview.ping", "42ms"), 8, rowY, 0xFF9FE870, true);
                    rowY += 10;
                }
                if (showFightState.isToggled()) {
                    context.drawText(mc.textRenderer, tr("raven.duelsstats.preview.status_example"), 8, rowY, 0xFF7CFC8A, true);
                    rowY += 10;
                }
                if (showWeaponInfo.isToggled()) {
                    context.drawText(mc.textRenderer, trimName(trf("raven.duelsstats.preview.weapon", "Sword", 8.0f), 16), 8, rowY, 0xFFE8DAB2, true);
                    rowY += 10;
                }
                if (showEffects.isToggled()) {
                    context.drawText(mc.textRenderer, trimName(tr("raven.duelsstats.preview.effects_example"), 16), 8, rowY, 0xFF9CC9FF, true);
                }
            } else {
                context.drawText(mc.textRenderer, tr("raven.duelsstats.preview.title"), 28, 7, 0xFFFFFFFF, true);
                drawHealthBar(context, 28, 20, getBaseHudWidth() - 34, 7, 0.72f);
                context.drawText(mc.textRenderer, trf("raven.duelsstats.preview.hp", 16.5f, 20.0f), 28, 31, 0xFFFFFFFF, true);
                int rowY = 42;
                if (isCompact()) {
                    context.drawText(mc.textRenderer, trf("raven.duelsstats.preview.core_compact", 3.6f, 12, "42ms"), 6, rowY, 0xFFCED8E5, true);
                    rowY += 10;
                } else {
                    context.drawText(mc.textRenderer, trf("raven.duelsstats.preview.armor", 12), 28, rowY, 0xFFB7C4D6, true);
                    context.drawText(mc.textRenderer, trf("raven.duelsstats.preview.distance", 3.6f), getBaseHudWidth() - 52, rowY, 0xFFDDDDDD, true);
                    rowY += 10;
                    context.drawText(mc.textRenderer, trf("raven.duelsstats.preview.ping", "42ms"), 28, rowY, 0xFF9FE870, true);
                    rowY += 10;
                }
                if (showFightState.isToggled()) {
                    context.drawText(mc.textRenderer, tr("raven.duelsstats.preview.status_example"), 6, rowY, 0xFF7CFC8A, true);
                    rowY += 10;
                }
                if (showWeaponInfo.isToggled()) {
                    context.drawText(mc.textRenderer, trf("raven.duelsstats.preview.weapon", "Sword", 8.0f), 6, rowY, 0xFFE8DAB2, true);
                    rowY += 10;
                }
                if (showEffects.isToggled()) {
                    context.drawText(mc.textRenderer, tr("raven.duelsstats.preview.effects_example"), 6, rowY, 0xFF9CC9FF, true);
                }
            }

            context.getMatrices().pop();

            renderControlPanel(context, mouseX, mouseY);

            super.render(context, mouseX, mouseY, delta);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (button == 0 && handleControlClick(mouseX, mouseY)) {
                return true;
            }

            int width = getHudWidth();
            int height = getHudHeight();
            if (mouseX >= posX && mouseX <= posX + width && mouseY >= posY && mouseY <= posY + height) {
                dragging = true;
                dragX = (int) mouseX - posX;
                dragY = (int) mouseY - posY;
                return true;
            }
            return super.mouseClicked(mouseX, mouseY, button);
        }

        @Override
        public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
            int width = getHudWidth();
            int height = getHudHeight();
            if (mouseX >= posX && mouseX <= posX + width && mouseY >= posY && mouseY <= posY + height) {
                double nextScale = hudScale.getInput() + (amount > 0 ? 0.05 : -0.05);
                hudScale.setValue(nextScale);
                clampHudPosition(this.width, this.height);
                return true;
            }
            return super.mouseScrolled(mouseX, mouseY, amount);
        }

        @Override
        public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
            if (draggingSizeSlider) {
                updateScaleFromMouse(mouseX);
                return true;
            }
            if (dragging) {
                posX = (int) mouseX - dragX;
                posY = (int) mouseY - dragY;
                clampHudPosition(this.width, this.height);
                return true;
            }
            return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
        }

        @Override
        public boolean mouseReleased(double mouseX, double mouseY, int button) {
            dragging = false;
            draggingSizeSlider = false;
            return super.mouseReleased(mouseX, mouseY, button);
        }

        @Override
        public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            if (keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_T) {
                targetMode.cycle();
                return true;
            }
            return super.keyPressed(keyCode, scanCode, modifiers);
        }

        private void renderControlPanel(DrawContext context, int mouseX, int mouseY) {
            int panelX = getControlPanelX();
            int panelY = getControlPanelY();
            int panelWidth = getControlPanelWidth();
            int panelHeight = getControlPanelHeight();
            int rowWidth = panelWidth - 20;

            context.fill(panelX + 2, panelY + 2, panelX + panelWidth + 2, panelY + panelHeight + 2, new Color(0, 0, 0, 52).getRGB());
            context.fill(panelX, panelY, panelX + panelWidth, panelY + panelHeight, new Color(11, 15, 23, 214).getRGB());
            context.fill(panelX, panelY, panelX + panelWidth, panelY + 1, 0xCC57B9FF);
            context.drawBorder(panelX, panelY, panelWidth, panelHeight, 0x32FFFFFF);

            context.drawText(mc.textRenderer, tr("raven.duelsstats.editor.title"), panelX + 10, panelY + 7, 0xFFFFFFFF, true);
            context.drawText(mc.textRenderer, trimToWidth(tr("raven.duelsstats.editor.description"), panelWidth - 20), panelX + 10, panelY + 18, 0x93A6BF, false);

            int rowY = panelY + 29;
            drawControlRow(context, panelX + 10, rowY, rowWidth, 15, tr("raven.duelsstats.editor.layout"), tr(isVertical() ? "raven.duelsstats.editor.layout_vertical" : "raven.duelsstats.editor.layout_horizontal"), isHovering(mouseX, mouseY, panelX + 10, rowY, rowWidth, 15), 0xE657B9FF);
            drawControlRow(context, panelX + 10, rowY + 17, rowWidth, 15, tr("raven.duelsstats.editor.compact"), tr(compactMode.isToggled() ? "raven.duelsstats.editor.on" : "raven.duelsstats.editor.off"), isHovering(mouseX, mouseY, panelX + 10, rowY + 17, rowWidth, 15), compactMode.isToggled() ? 0xE66ADF8A : 0xCC73839A);
            drawControlRow(context, panelX + 10, rowY + 34, rowWidth, 15, tr("raven.duelsstats.editor.target"), getTargetModeLabel(), isHovering(mouseX, mouseY, panelX + 10, rowY + 34, rowWidth, 15), 0xE6E7B868);
            drawSizeSlider(context, panelX + 10, rowY + 52, rowWidth, 17, mouseX, mouseY);
        }

        private void drawControlRow(DrawContext context, int x, int y, int width, int height, String label, String value, boolean hovered, int accentColor) {
            int bg = hovered ? new Color(21, 28, 39, 228).getRGB() : new Color(15, 20, 29, 214).getRGB();
            int border = hovered ? 0x62FFFFFF : 0x22FFFFFF;
            context.fill(x, y, x + width, y + height, bg);
            context.fill(x, y, x + 3, y + height, accentColor);
            context.drawBorder(x, y, width, height, border);

            int innerWidth = width - 14;
            int labelMaxWidth = Math.max(28, innerWidth / 2);
            String fittedLabel = trimToWidth(label, labelMaxWidth);
            String fittedValue = trimToWidth(value, innerWidth - mc.textRenderer.getWidth(fittedLabel) - 8);

            context.drawText(mc.textRenderer, fittedLabel, x + 8, y + 3, 0x93A6BF, false);
            int valueWidth = mc.textRenderer.getWidth(fittedValue);
            context.drawText(mc.textRenderer, fittedValue, x + width - valueWidth - 7, y + 3, 0xFFFFFFFF, false);
        }

        private void drawSizeSlider(DrawContext context, int x, int y, int width, int height, int mouseX, int mouseY) {
            boolean hovered = isHovering(mouseX, mouseY, x, y, width, height);
            context.fill(x, y, x + width, y + height, hovered ? new Color(21, 28, 39, 228).getRGB() : new Color(15, 20, 29, 214).getRGB());
            context.fill(x, y, x + 3, y + height, 0xE6A98CFF);
            context.drawBorder(x, y, width, height, hovered || draggingSizeSlider ? 0x62FFFFFF : 0x22FFFFFF);

            context.drawText(mc.textRenderer, tr("raven.setting.size"), x + 6, y + 3, 0x93A6BF, false);
            String valueText = String.format(Locale.ROOT, "%.0f%%", hudScale.getInput() * 100.0);
            context.drawText(mc.textRenderer, valueText, x + width - mc.textRenderer.getWidth(valueText) - 6, y + 3, 0xFFFFFFFF, false);

            int sliderY = y + height - 3;
            int sliderX = x + 6;
            int sliderWidth = width - 12;
            context.fill(sliderX, sliderY, sliderX + sliderWidth, sliderY + 4, new Color(8, 10, 14, 220).getRGB());
            double pct = (hudScale.getInput() - hudScale.getMin()) / (hudScale.getMax() - hudScale.getMin());
            int filled = (int) Math.round(sliderWidth * pct);
            context.fill(sliderX, sliderY, sliderX + filled, sliderY + 4, 0xE6A98CFF);

            int knobX = sliderX + filled;
            context.fill(knobX - 2, sliderY - 2, knobX + 2, sliderY + 6, hovered || draggingSizeSlider ? 0xFFFFFFFF : 0xFFD8C9FF);
        }

        private boolean handleControlClick(double mouseX, double mouseY) {
            int panelX = getControlPanelX();
            int panelY = getControlPanelY();
            int panelWidth = getControlPanelWidth();
            int rowWidth = panelWidth - 20;
            int rowY = panelY + 30;

            if (isHovering(mouseX, mouseY, panelX + 10, rowY, rowWidth, 15)) {
                layoutMode.cycle();
                clampHudPosition(this.width, this.height);
                return true;
            }

            if (isHovering(mouseX, mouseY, panelX + 10, rowY + 17, rowWidth, 15)) {
                compactMode.toggle();
                clampHudPosition(this.width, this.height);
                return true;
            }

            if (isHovering(mouseX, mouseY, panelX + 10, rowY + 34, rowWidth, 15)) {
                targetMode.cycle();
                return true;
            }

            if (isHovering(mouseX, mouseY, panelX + 10, rowY + 52, rowWidth, 17)) {
                draggingSizeSlider = true;
                updateScaleFromMouse(mouseX);
                return true;
            }

            return false;
        }

        private void updateScaleFromMouse(double mouseX) {
            int sliderX = getControlPanelX() + 16;
            int sliderWidth = getControlPanelWidth() - 32;
            double pct = MathHelper.clamp((mouseX - sliderX) / sliderWidth, 0.0, 1.0);
            double value = hudScale.getMin() + pct * (hudScale.getMax() - hudScale.getMin());
            hudScale.setValue(value);
            clampHudPosition(this.width, this.height);
        }

        private int getControlPanelWidth() {
            return 180;
        }

        private int getControlPanelHeight() {
            return 100;
        }

        private int getControlPanelX() {
            int margin = 14;
            return MathHelper.clamp(margin, margin, Math.max(margin, this.width - getControlPanelWidth() - margin));
        }

        private int getControlPanelY() {
            int margin = 14;
            int centered = (this.height - getControlPanelHeight()) / 2;
            return MathHelper.clamp(centered, margin, Math.max(margin, this.height - getControlPanelHeight() - margin));
        }

        private boolean isHovering(double mouseX, double mouseY, int x, int y, int width, int height) {
            return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
        }
    }

    private record InfoRow(String text, int color) {}
}
