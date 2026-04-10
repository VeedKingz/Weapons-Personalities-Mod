package com.weaponpersonalities.systems;

import com.weaponpersonalities.config.ModConfig;
import com.weaponpersonalities.effects.EffectsSystem;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.math.Vec3d;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Central dispatcher that applies each weapon's unique combat mechanics.
 * Called from {@link com.weaponpersonalities.events.CombatEventHandler}.
 */
public final class WeaponBehaviorHandler {

    private WeaponBehaviorHandler() {}

    // Tracks whether the player was sprinting within the dagger sprint window
    private static final Map<UUID, Integer> sprintEndTick = new HashMap<>();

    // Tracks last-attack tick for spear wind-up
    private static final Map<UUID, Integer> spearWindUpStart = new HashMap<>();

    /**
     * Main entry point. Called when a player successfully damages a living entity.
     *
     * @param player   Attacking player
     * @param target   Entity being hit
     * @param stack    Held item
     * @param tick     Current world tick
     * @return         Extra damage to add on top of base damage (can be 0)
     */
    public static float handleAttack(EntityPlayer player,
                                     EntityLivingBase target,
                                     ItemStack stack,
                                     int tick) {
        WeaponType type = WeaponType.of(stack);
        float extraDamage = 0f;

        // Mastery bonus applies to every weapon type
        extraDamage += (float) ComboSystem.masteryDamageBonus(player, type);

        switch (type) {
            case SWORD:  extraDamage += handleSword(player, target, tick);  break;
            case AXE:    extraDamage += handleAxe(player, target);           break;
            case SPEAR:  extraDamage += handleSpear(player, target, tick);   break;
            case DAGGER: extraDamage += handleDagger(player, target, tick);  break;
            default:     break;
        }

        // Record hit for mastery
        int masteryLevel = ComboSystem.recordHit(player, type);
        if (masteryLevel > 0 && masteryLevel == ModConfig.masteryMaxLevel) {
            EffectsSystem.spawnMasteryParticles(target);
        }

        return extraDamage;
    }

    // ── Sword ──────────────────────────────────────────────────────────────
    private static float handleSword(EntityPlayer player,
                                     EntityLivingBase target,
                                     int tick) {
        int stacks = ComboSystem.onSwordHit(player, tick);
        float bonus = (float) ComboSystem.comboMultiplier(stacks);

        if (ComboSystem.isFinisher(stacks)) {
            // Finisher: extra knockback + particles
            Vec3d dir = player.getLookVec();
            target.addVelocity(
                dir.x * ModConfig.swordComboFinisherKnockback,
                0.3,
                dir.z * ModConfig.swordComboFinisherKnockback
            );
            target.velocityChanged = true;
            EffectsSystem.spawnComboFinisherParticles(target);
            EffectsSystem.playComboFinisherSound(player);
            ComboSystem.resetCombo(player.getUniqueID());
        } else {
            EffectsSystem.spawnCritParticles(target); // small hit spark
        }

        return bonus;
    }

    // ── Axe ────────────────────────────────────────────────────────────────
    private static float handleAxe(EntityPlayer player, EntityLivingBase target) {
        float bonus = 0f;

        // Shield-disable mechanic
        if (target instanceof EntityPlayer) {
            EntityPlayer targetPlayer = (EntityPlayer) target;
            if (targetPlayer.isActiveItemStackBlocking()) {
                // Force cooldown on the shield slot
                targetPlayer.getCooldownTracker().setCooldown(
                    targetPlayer.getActiveItemStack().getItem(),
                    ModConfig.axeShieldDisableDurationTicks
                );
                bonus += (float) ModConfig.axeShieldDisableDamageBonus;
            }
        }

        // Stagger chance
        if (Math.random() < ModConfig.axeStaggerChance) {
            target.addPotionEffect(new PotionEffect(
                MobEffects.SLOWNESS,
                ModConfig.axeStaggerDurationTicks,
                (int) ModConfig.axeStaggerSlowAmplifier
            ));
            EffectsSystem.spawnStaggerParticles(target);
        }

        EffectsSystem.playHeavyHitSound(player);
        return bonus;
    }

    // ── Spear ──────────────────────────────────────────────────────────────
    private static float handleSpear(EntityPlayer player,
                                     EntityLivingBase target,
                                     int tick) {
        float bonus = 0f;
        UUID id = player.getUniqueID();

        // Wind-up tracking: first attack registers start; subsequent checks delta
        if (!spearWindUpStart.containsKey(id)) {
            spearWindUpStart.put(id, tick);
            return 0f; // no bonus on the very first swing (wind-up required)
        }

        int windUpElapsed = tick - spearWindUpStart.get(id);
        spearWindUpStart.put(id, tick);

        // Range bonus: check if target is near max range
        double dist = player.getDistanceSqToEntity(target);
        double maxRangeSq = 16.0; // ~4 blocks = max spear range
        boolean atMaxRange = dist >= maxRangeSq * 0.7;

        if (atMaxRange && windUpElapsed >= ModConfig.spearWindUpTicks) {
            bonus += (float) ModConfig.spearRangeBonusDamage;
            EffectsSystem.spawnCritParticles(target);
        }

        return bonus;
    }

    // ── Dagger ─────────────────────────────────────────────────────────────
    private static float handleDagger(EntityPlayer player,
                                      EntityLivingBase target,
                                      int tick) {
        float bonus = 0f;
        UUID id = player.getUniqueID();

        // Backstab: player must be roughly behind the target
        if (isAttackingFromBehind(player, target)) {
            bonus += (float) (target.getHealth() * (ModConfig.daggerBackstabMultiplier - 1.0));
            EffectsSystem.spawnCritParticles(target);
            EffectsSystem.playFastHitSound(player);
        }

        // Sprint bonus: first hit after recent sprint
        Integer endTick = sprintEndTick.get(id);
        if (endTick != null && (tick - endTick) <= ModConfig.daggerSprintBonusWindow) {
            bonus += (float) ModConfig.daggerSprintBonusDamage;
            sprintEndTick.remove(id);
            EffectsSystem.spawnCritParticles(target);
        }

        EffectsSystem.playFastHitSound(player);
        return bonus;
    }

    /**
     * Called from the player tick event when the player stops sprinting.
     * Records the tick so the dagger sprint bonus can be applied.
     */
    public static void onPlayerStopSprint(EntityPlayer player, int tick) {
        sprintEndTick.put(player.getUniqueID(), tick);
    }

    // ── Geometry helper ────────────────────────────────────────────────────

    /**
     * Returns true when the attacking player is roughly behind the target.
     * "Behind" = the angle between the target's look vector and the
     * attacker's direction from the target is less than 60 degrees.
     */
    private static boolean isAttackingFromBehind(EntityPlayer attacker,
                                                  EntityLivingBase target) {
        Vec3d targetLook = target.getLookVec();
        Vec3d toAttacker = attacker.getPositionVector()
                                   .subtract(target.getPositionVector())
                                   .normalize();

        // Dot product: positive = same direction as target look = behind target
        double dot = targetLook.dotProduct(toAttacker);
        return dot > 0.5; // ~60° cone behind
    }
}
