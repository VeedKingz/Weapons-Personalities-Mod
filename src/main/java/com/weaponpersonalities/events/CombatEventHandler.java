package com.weaponpersonalities.events;

import com.weaponpersonalities.systems.ComboSystem;
import com.weaponpersonalities.systems.WeaponBehaviorHandler;
import com.weaponpersonalities.systems.WeaponType;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Hooks into Forge combat events and routes them to weapon behavior handlers.
 *
 * Event priority is set to HIGH so our damage additions land before other mods
 * that may cancel or modify them at NORMAL priority.
 */
public final class CombatEventHandler {

    /** Cached sprint state per player to detect sprint-stop transitions. */
    private final Map<UUID, Boolean> wasSprinting = new HashMap<>();

    // ── LivingHurtEvent ────────────────────────────────────────────────────

    /**
     * Fired after damage is confirmed but before it is applied.
     * We add extra damage here so it interacts correctly with armour
     * and resistance calculations that happen afterward.
     */
    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onLivingHurt(LivingHurtEvent event) {
        // Only process player melee attacks
        if (!(event.getSource().getTrueSource() instanceof EntityPlayer)) return;

        EntityPlayer player = (EntityPlayer) event.getSource().getTrueSource();
        EntityLivingBase target = event.getEntityLiving();

        // Ignore damage the player deals to themselves
        if (player == target) return;

        ItemStack heldItem = player.getHeldItemMainhand();
        WeaponType type = WeaponType.of(heldItem);
        if (type == WeaponType.NONE) return;

        int tick = (int) player.world.getTotalWorldTime();
        float extra = WeaponBehaviorHandler.handleAttack(player, target, heldItem, tick);
        if (extra > 0f) {
            event.setAmount(event.getAmount() + extra);
        }
    }

    // ── LivingAttackEvent (miss / block detection) ─────────────────────────

    /**
     * Fired when damage is about to be attempted. If the attack is blocked
     * or cancelled, reset the sword combo to discourage spam-clicking.
     */
    @SubscribeEvent(priority = EventPriority.LOW)
    public void onLivingAttack(LivingAttackEvent event) {
        if (!(event.getSource().getTrueSource() instanceof EntityPlayer)) return;

        EntityPlayer player = (EntityPlayer) event.getSource().getTrueSource();
        WeaponType type = WeaponType.of(player.getHeldItemMainhand());

        if (type == WeaponType.SWORD && event.isCanceled()) {
            ComboSystem.resetCombo(player.getUniqueID());
        }
    }

    // ── Player Tick (sprint tracking, combo timeout) ───────────────────────

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        EntityPlayer player = event.player;
        UUID id = player.getUniqueID();

        // Sprint-stop detection for dagger sprint bonus
        boolean sprinting = player.isSprinting();
        boolean prev = wasSprinting.getOrDefault(id, false);
        if (prev && !sprinting) {
            int tick = (int) player.world.getTotalWorldTime();
            WeaponBehaviorHandler.onPlayerStopSprint(player, tick);
        }
        wasSprinting.put(id, sprinting);

        // Combo timeout: if the player hasn't hit anything in the reset window,
        // the next getStacks() call will auto-reset, but we log visually by
        // eagerly clearing expired state here.
        int tick = (int) player.world.getTotalWorldTime();
        ComboSystem.getStacks(id, tick); // side-effect: resets if expired
    }

    // ── Player logout cleanup ──────────────────────────────────────────────

    @SubscribeEvent
    public void onPlayerLogout(PlayerLoggedOutEvent event) {
        UUID id = event.player.getUniqueID();
        ComboSystem.resetCombo(id);
        wasSprinting.remove(id);
    }
}
