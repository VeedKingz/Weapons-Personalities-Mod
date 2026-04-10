package com.weaponpersonalities.events;

import com.weaponpersonalities.systems.ComboSystem;
import com.weaponpersonalities.systems.WeaponBehaviorHandler;
import com.weaponpersonalities.systems.WeaponType;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class CombatEventHandler {

    private final Map<UUID, Boolean> wasSprinting = new HashMap<>();

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onLivingHurt(LivingHurtEvent event) {
        if (!(event.getSource().getTrueSource() instanceof EntityPlayer)) return;

        EntityPlayer player = (EntityPlayer) event.getSource().getTrueSource();
        EntityLivingBase target = event.getEntityLiving();
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

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onLivingAttack(LivingAttackEvent event) {
        if (!(event.getSource().getTrueSource() instanceof EntityPlayer)) return;

        EntityPlayer player = (EntityPlayer) event.getSource().getTrueSource();
        WeaponType type = WeaponType.of(player.getHeldItemMainhand());

        if (type == WeaponType.SWORD && event.isCanceled()) {
            ComboSystem.resetCombo(player.getUniqueID());
        }
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        EntityPlayer player = event.player;
        UUID id = player.getUniqueID();

        boolean sprinting = player.isSprinting();
        boolean prev = wasSprinting.getOrDefault(id, false);
        if (prev && !sprinting) {
            int tick = (int) player.world.getTotalWorldTime();
            WeaponBehaviorHandler.onPlayerStopSprint(player, tick);
        }
        wasSprinting.put(id, sprinting);

        int tick = (int) player.world.getTotalWorldTime();
        ComboSystem.getStacks(id, tick);
    }

    @SubscribeEvent
    public void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        UUID id = event.player.getUniqueID();
        ComboSystem.resetCombo(id);
        wasSprinting.remove(id);
    }
}
