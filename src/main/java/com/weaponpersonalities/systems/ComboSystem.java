package com.weaponpersonalities.systems;

import com.weaponpersonalities.config.ModConfig;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Tracks combo stacks, timing, and mastery progress per player UUID.
 *
 * All state lives in memory (server-side). The mastery data that should
 * survive logout/death is persisted into the player's EntityData NBT.
 */
public final class ComboSystem {

    private ComboSystem() {}

    // ── In-memory combo state ──────────────────────────────────────────────
    private static final Map<UUID, Integer> comboStacks      = new HashMap<>();
    private static final Map<UUID, Long>    lastHitTick      = new HashMap<>();

    // ── Mastery NBT keys ───────────────────────────────────────────────────
    private static final String NBT_ROOT          = "WP_Mastery";
    private static final String NBT_SWORD_HITS    = "SwordHits";
    private static final String NBT_AXE_HITS      = "AxeHits";
    private static final String NBT_SPEAR_HITS    = "SpearHits";
    private static final String NBT_DAGGER_HITS   = "DaggerHits";

    // ── Sword Combo API ────────────────────────────────────────────────────

    /** Called on every successful sword hit. Returns the new stack count. */
    public static int onSwordHit(EntityPlayer player, long currentTick) {
        UUID id = player.getUniqueID();
        int stacks = getStacks(id, currentTick);
        stacks = Math.min(stacks + 1, ModConfig.swordComboMaxStacks);
        comboStacks.put(id, stacks);
        lastHitTick.put(id, currentTick);
        return stacks;
    }

    /** Returns the current combo stacks, resetting if the window expired. */
    public static int getStacks(UUID id, long currentTick) {
        if (!lastHitTick.containsKey(id)) return 0;
        long elapsed = currentTick - lastHitTick.get(id);
        if (elapsed > ModConfig.swordComboResetTicks) {
            comboStacks.put(id, 0);
            return 0;
        }
        return comboStacks.getOrDefault(id, 0);
    }

    /** Resets the combo (called on miss or long delay). */
    public static void resetCombo(UUID id) {
        comboStacks.put(id, 0);
        lastHitTick.remove(id);
    }

    /** Returns true if this hit is a combo finisher. */
    public static boolean isFinisher(int stacks) {
        return stacks >= ModConfig.swordComboMaxStacks;
    }

    /**
     * Calculates the extra damage multiplier from the current combo level.
     * E.g., 0.15 per stack → stack 2 = +0.30, stack 3 = +0.45 (finisher).
     */
    public static double comboMultiplier(int stacks) {
        return stacks * ModConfig.swordComboMultiplierPerStack;
    }

    // ── Mastery API ────────────────────────────────────────────────────────

    /** Increments hit count for the given weapon type and returns the current mastery level. */
    public static int recordHit(EntityPlayer player, WeaponType type) {
        if (!ModConfig.masteryEnabled || type == WeaponType.NONE) return 0;

        NBTTagCompound data = getOrCreateMasteryTag(player);
        String key = hitKey(type);
        int hits = data.getInteger(key) + 1;
        data.setInteger(key, hits);
        saveMasteryTag(player, data);

        return masteryLevel(hits);
    }

    /** Returns the mastery level (0–max) for the given weapon type. */
    public static int getMasteryLevel(EntityPlayer player, WeaponType type) {
        if (!ModConfig.masteryEnabled || type == WeaponType.NONE) return 0;
        NBTTagCompound data = getOrCreateMasteryTag(player);
        return masteryLevel(data.getInteger(hitKey(type)));
    }

    /** Bonus flat damage from mastery (level × config bonus). */
    public static double masteryDamageBonus(EntityPlayer player, WeaponType type) {
        return getMasteryLevel(player, type) * ModConfig.masteryDamageBonus;
    }

    // ── Helpers ────────────────────────────────────────────────────────────

    private static int masteryLevel(int hits) {
        int level = hits / Math.max(1, ModConfig.masteryHitsPerLevel);
        return Math.min(level, ModConfig.masteryMaxLevel);
    }

    private static String hitKey(WeaponType type) {
        switch (type) {
            case SWORD:  return NBT_SWORD_HITS;
            case AXE:    return NBT_AXE_HITS;
            case SPEAR:  return NBT_SPEAR_HITS;
            case DAGGER: return NBT_DAGGER_HITS;
            default:     return "UnknownHits";
        }
    }

    private static NBTTagCompound getOrCreateMasteryTag(EntityPlayer player) {
        NBTTagCompound entityData = player.getEntityData();
        if (!entityData.hasKey(NBT_ROOT)) {
            entityData.setTag(NBT_ROOT, new NBTTagCompound());
        }
        return entityData.getCompoundTag(NBT_ROOT);
    }

    private static void saveMasteryTag(EntityPlayer player, NBTTagCompound data) {
        player.getEntityData().setTag(NBT_ROOT, data);
    }
}
