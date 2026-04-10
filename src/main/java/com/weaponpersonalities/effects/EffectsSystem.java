package com.weaponpersonalities.effects;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;

import java.util.Random;

/**
 * Centralizes all particle and sound feedback for weapon personalities.
 * All methods are safe to call server-side; the vanilla particle/sound
 * network packets handle client propagation automatically.
 */
public final class EffectsSystem {

    private EffectsSystem() {}

    private static final Random RNG = new Random();

    // ── Particles ──────────────────────────────────────────────────────────

    /**
     * Small critical-hit spark — used by sword combos, dagger crits, spear
     * range bonus, and mastery unlocks.
     */
    public static void spawnCritParticles(EntityLivingBase target) {
        World world = target.world;
        double x = target.posX;
        double y = target.posY + target.height * 0.75;
        double z = target.posZ;

        for (int i = 0; i < 6; i++) {
            double vx = (RNG.nextDouble() - 0.5) * 0.5;
            double vy = RNG.nextDouble() * 0.4 + 0.1;
            double vz = (RNG.nextDouble() - 0.5) * 0.5;
            world.spawnParticle(EnumParticleTypes.CRIT, x, y, z, vx, vy, vz);
        }
    }

    /**
     * Large burst of magic crit particles on combo finisher.
     */
    public static void spawnComboFinisherParticles(EntityLivingBase target) {
        World world = target.world;
        double x = target.posX;
        double y = target.posY + target.height * 0.5;
        double z = target.posZ;

        for (int i = 0; i < 16; i++) {
            double vx = (RNG.nextDouble() - 0.5) * 1.0;
            double vy = RNG.nextDouble() * 0.6;
            double vz = (RNG.nextDouble() - 0.5) * 1.0;
            world.spawnParticle(EnumParticleTypes.CRIT_MAGIC, x, y, z, vx, vy, vz);
        }

        // Outer ring of smoke for dramatic effect
        for (int i = 0; i < 8; i++) {
            double angle = (Math.PI * 2.0 / 8) * i;
            double rx = Math.cos(angle) * 0.5;
            double rz = Math.sin(angle) * 0.5;
            world.spawnParticle(EnumParticleTypes.SMOKE_LARGE,
                x + rx, y, z + rz, 0, 0.05, 0);
        }
    }

    /**
     * Slow/heavy purple particle cloud for axe stagger.
     */
    public static void spawnStaggerParticles(EntityLivingBase target) {
        World world = target.world;
        double x = target.posX;
        double y = target.posY + target.height * 0.5;
        double z = target.posZ;

        for (int i = 0; i < 10; i++) {
            double vx = (RNG.nextDouble() - 0.5) * 0.3;
            double vy = RNG.nextDouble() * 0.2;
            double vz = (RNG.nextDouble() - 0.5) * 0.3;
            world.spawnParticle(EnumParticleTypes.SPELL_MOB, x, y, z, vx, vy, vz);
        }
    }

    /**
     * Gold/yellow sparkle for max mastery hits.
     */
    public static void spawnMasteryParticles(EntityLivingBase target) {
        World world = target.world;
        double x = target.posX;
        double y = target.posY + target.height * 0.5;
        double z = target.posZ;

        for (int i = 0; i < 20; i++) {
            double vx = (RNG.nextDouble() - 0.5) * 0.8;
            double vy = RNG.nextDouble() * 0.8 + 0.2;
            double vz = (RNG.nextDouble() - 0.5) * 0.8;
            world.spawnParticle(EnumParticleTypes.SPELL_WITCH, x, y, z, vx, vy, vz);
        }
    }

    // ── Sounds ─────────────────────────────────────────────────────────────

    /**
     * Deep impact sound for axe heavy hits.
     */
    public static void playHeavyHitSound(EntityPlayer player) {
        player.world.playSound(
            null,
            player.posX, player.posY, player.posZ,
            SoundEvents.ENTITY_PLAYER_ATTACK_STRONG,
            SoundCategory.PLAYERS,
            1.2f, 0.7f  // louder, lower pitch = heavy
        );
    }

    /**
     * Quick snappy sound for dagger fast hits.
     */
    public static void playFastHitSound(EntityPlayer player) {
        player.world.playSound(
            null,
            player.posX, player.posY, player.posZ,
            SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP,
            SoundCategory.PLAYERS,
            0.9f, 1.6f  // lighter, higher pitch = fast
        );
    }

    /**
     * Satisfying finisher sound played when a sword combo completes.
     */
    public static void playComboFinisherSound(EntityPlayer player) {
        player.world.playSound(
            null,
            player.posX, player.posY, player.posZ,
            SoundEvents.ENTITY_PLAYER_ATTACK_CRIT,
            SoundCategory.PLAYERS,
            1.0f, 0.9f
        );
        // Secondary "whoosh" for drama
        player.world.playSound(
            null,
            player.posX, player.posY, player.posZ,
            SoundEvents.ENTITY_PLAYER_ATTACK_KNOCKBACK,
            SoundCategory.PLAYERS,
            0.7f, 1.2f
        );
    }
}
