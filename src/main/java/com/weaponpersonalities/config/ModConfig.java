package com.weaponpersonalities.config;

import net.minecraftforge.common.config.Configuration;

import java.io.File;

/**
 * Config system – all values are adjustable in config/weaponpersonalities.cfg.
 */
public final class ModConfig {

    private ModConfig() {}

    // ── Sword ──────────────────────────────────────────────────────────────
    public static double swordComboMultiplierPerStack = 0.15;
    public static int    swordComboMaxStacks           = 3;
    public static int    swordComboResetTicks          = 40;  // 2 seconds
    public static double swordComboFinisherKnockback   = 1.5;

    // ── Axe ────────────────────────────────────────────────────────────────
    public static double axeShieldDisableDamageBonus  = 5.0;
    public static int    axeShieldDisableDurationTicks = 100; // 5 seconds
    public static double axeStaggerSlowAmplifier      = 1.0;  // Slowness II
    public static int    axeStaggerDurationTicks       = 40;
    public static double axeStaggerChance              = 0.35;

    // ── Spear ──────────────────────────────────────────────────────────────
    public static double spearRangeBonusDamage         = 3.0;
    public static double spearWindUpReductionFactor    = 0.6;
    public static int    spearWindUpTicks              = 10;

    // ── Dagger ─────────────────────────────────────────────────────────────
    public static double daggerBackstabMultiplier      = 2.0;
    public static double daggerSprintBonusDamage       = 2.5;
    public static int    daggerSprintBonusWindow       = 20;  // 1 second after sprint

    // ── Mastery ────────────────────────────────────────────────────────────
    public static boolean masteryEnabled               = true;
    public static int     masteryHitsPerLevel          = 100;
    public static int     masteryMaxLevel              = 5;
    public static double  masteryDamageBonus           = 0.05; // 5% per level

    public static void init(File configFile) {
        Configuration cfg = new Configuration(configFile);
        cfg.load();

        // Sword
        swordComboMultiplierPerStack = cfg.getDouble(
            "comboMultiplierPerStack", "sword", swordComboMultiplierPerStack,
            0.0, 1.0,
            "Damage multiplier added per combo stack (e.g. 0.15 = +15% per stack)."
        );
        swordComboMaxStacks = cfg.getInt(
            "comboMaxStacks", "sword", swordComboMaxStacks, 1, 10,
            "Maximum number of combo stacks before the finisher triggers."
        );
        swordComboResetTicks = cfg.getInt(
            "comboResetTicks", "sword", swordComboResetTicks, 10, 200,
            "Ticks of no hit before the combo resets (20 ticks = 1 second)."
        );
        swordComboFinisherKnockback = cfg.getDouble(
            "comboFinisherKnockback", "sword", swordComboFinisherKnockback,
            0.0, 5.0,
            "Extra knockback strength on the combo finisher hit."
        );

        // Axe
        axeShieldDisableDamageBonus = cfg.getDouble(
            "shieldDisableDamageBonus", "axe", axeShieldDisableDamageBonus,
            0.0, 20.0,
            "Extra damage dealt when breaking through a shield."
        );
        axeShieldDisableDurationTicks = cfg.getInt(
            "shieldDisableDurationTicks", "axe", axeShieldDisableDurationTicks,
            20, 400,
            "How long (ticks) the shield is disabled after being hit by an axe."
        );
        axeStaggerSlowAmplifier = cfg.getDouble(
            "staggerSlowAmplifier", "axe", axeStaggerSlowAmplifier,
            0.0, 4.0,
            "Slowness effect amplifier applied on stagger (0 = Slowness I, 1 = Slowness II …)."
        );
        axeStaggerDurationTicks = cfg.getInt(
            "staggerDurationTicks", "axe", axeStaggerDurationTicks,
            5, 200,
            "Duration of the stagger slowness effect in ticks."
        );
        axeStaggerChance = cfg.getDouble(
            "staggerChance", "axe", axeStaggerChance,
            0.0, 1.0,
            "Probability (0.0–1.0) that an axe hit triggers a stagger."
        );

        // Spear
        spearRangeBonusDamage = cfg.getDouble(
            "rangeBonusDamage", "spear", spearRangeBonusDamage,
            0.0, 20.0,
            "Bonus damage when hitting from maximum spear range."
        );
        spearWindUpTicks = cfg.getInt(
            "windUpTicks", "spear", spearWindUpTicks,
            1, 60,
            "Ticks the player must hold attack before a full-power spear strike."
        );

        // Dagger
        daggerBackstabMultiplier = cfg.getDouble(
            "backstabMultiplier", "dagger", daggerBackstabMultiplier,
            1.0, 5.0,
            "Damage multiplier for hitting an enemy from behind."
        );
        daggerSprintBonusDamage = cfg.getDouble(
            "sprintBonusDamage", "dagger", daggerSprintBonusDamage,
            0.0, 10.0,
            "Flat bonus damage on the first hit after sprinting."
        );
        daggerSprintBonusWindow = cfg.getInt(
            "sprintBonusWindowTicks", "dagger", daggerSprintBonusWindow,
            5, 100,
            "Ticks after stopping a sprint that the sprint bonus is still valid."
        );

        // Mastery
        masteryEnabled = cfg.getBoolean(
            "enabled", "mastery", masteryEnabled,
            "Enable the weapon mastery system."
        );
        masteryHitsPerLevel = cfg.getInt(
            "hitsPerLevel", "mastery", masteryHitsPerLevel,
            10, 10000,
            "Number of hits required to advance one mastery level."
        );
        masteryMaxLevel = cfg.getInt(
            "maxLevel", "mastery", masteryMaxLevel,
            1, 20,
            "Maximum mastery level a weapon can reach."
        );
        masteryDamageBonus = cfg.getDouble(
            "damageBonus", "mastery", masteryDamageBonus,
            0.0, 0.5,
            "Flat damage bonus granted per mastery level."
        );

        if (cfg.hasChanged()) {
            cfg.save();
        }
    }
}
