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

public final class WeaponBehaviorHandler {

    private WeaponBehaviorHandler() {}

    private static final Map<UUID, Integer> sprintEndTick   = new HashMap<>();
    private static final Map<UUID, Integer> spearWindUpStart = new HashMap<>();

    public static float handleAttack(EntityPlayer player,
                                     EntityLivingBase target,
                                     ItemStack stack,
                                     int tick) {
        WeaponType type = WeaponType.of(stack);
        float extraDamage = 0f;

        extraDamage += (float) ComboSystem.masteryDamageBonus(player, type);

        switch (type) {
            case SWORD:  extraDamage += handleSword(player, target, tick);  break;
            case AXE:    extraDamage += handleAxe(player, target);           break;
            case SPEAR:  extraDamage += handleSpear(player, target, tick);   break;
            case DAGGER: extraDamage += handleDagger(player, target, tick);  break;
            default:     break;
        }

        int masteryLevel = ComboSystem.recordHit(player, type);
        if (masteryLevel > 0 && masteryLevel == ModConfig.masteryMaxLevel) {
            EffectsSystem.spawnMasteryParticles(target);
        }

        return extraDamage;
    }

    private static float handleSword(EntityPlayer player,
                                     EntityLivingBase target,
                                     int tick) {
        int stacks = ComboSystem.onSwordHit(player, tick);
        float bonus = (float) ComboSystem.comboMultiplier(stacks);

        if (ComboSystem.isFinisher(stacks)) {
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
            EffectsSystem.spawnCritParticles(target);
        }

        return bonus;
    }

    private static float handleAxe(EntityPlayer player, EntityLivingBase target) {
        float bonus = 0f;

        if (target instanceof EntityPlayer) {
            EntityPlayer targetPlayer = (EntityPlayer) target;
            if (targetPlayer.isActiveItemStackBlocking()) {
                targetPlayer.getCooldownTracker().setCooldown(
                    targetPlayer.getActiveItemStack().getItem(),
                    ModConfig.axeShieldDisableDurationTicks
                );
                bonus += (float) ModConfig.axeShieldDisableDamageBonus;
            }
        }

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

    private static float handleSpear(EntityPlayer player,
                                     EntityLivingBase target,
                                     int tick) {
        float bonus = 0f;
        UUID id = player.getUniqueID();

        if (!spearWindUpStart.containsKey(id)) {
            spearWindUpStart.put(id, tick);
            return 0f;
        }

        int windUpElapsed = tick - spearWindUpStart.get(id);
        spearWindUpStart.put(id, tick);

        double distSq    = player.getDistanceSq(target);
        double maxRangeSq = 16.0;
        boolean atMaxRange = distSq >= maxRangeSq * 0.7;

        if (atMaxRange && windUpElapsed >= ModConfig.spearWindUpTicks) {
            bonus += (float) ModConfig.spearRangeBonusDamage;
            EffectsSystem.spawnCritParticles(target);
        }

        return bonus;
    }

    private static float handleDagger(EntityPlayer player,
                                      EntityLivingBase target,
                                      int tick) {
        float bonus = 0f;
        UUID id = player.getUniqueID();

        if (isAttackingFromBehind(player, target)) {
            bonus += (float) (target.getHealth() * (ModConfig.daggerBackstabMultiplier - 1.0));
            EffectsSystem.spawnCritParticles(target);
            EffectsSystem.playFastHitSound(player);
        }

        Integer endTick = sprintEndTick.get(id);
        if (endTick != null && (tick - endTick) <= ModConfig.daggerSprintBonusWindow) {
            bonus += (float) ModConfig.daggerSprintBonusDamage;
            sprintEndTick.remove(id);
            EffectsSystem.spawnCritParticles(target);
        }

        EffectsSystem.playFastHitSound(player);
        return bonus;
    }

    public static void onPlayerStopSprint(EntityPlayer player, int tick) {
        sprintEndTick.put(player.getUniqueID(), tick);
    }

    private static boolean isAttackingFromBehind(EntityPlayer attacker,
                                                  EntityLivingBase target) {
        Vec3d targetLook  = target.getLookVec();
        Vec3d toAttacker  = attacker.getPositionVector()
                                    .subtract(target.getPositionVector())
                                    .normalize();
        double dot = targetLook.dotProduct(toAttacker);
        return dot > 0.5;
    }
}
