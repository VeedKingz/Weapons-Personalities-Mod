package com.weaponpersonalities.systems;

import net.minecraft.item.Item;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.nbt.NBTTagCompound;

/**
 * Defines every weapon personality the mod tracks.
 * Detection is tag-based so modded swords/axes/daggers/spears work
 * automatically when they extend the vanilla base classes or are tagged.
 */
public enum WeaponType {

    SWORD,
    AXE,
    SPEAR,
    DAGGER,
    NONE;

    // ── NBT keys used to tag custom spears & daggers ───────────────────────
    public static final String NBT_WEAPON_TYPE = "WP_WeaponType";

    /**
     * Resolves the weapon type for the given item stack.
     * Priority: explicit NBT tag > vanilla item class.
     */
    public static WeaponType of(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return NONE;

        // Check explicit tag first (for spears and daggers)
        if (stack.hasTagCompound()) {
            NBTTagCompound tag = stack.getTagCompound();
            if (tag != null && tag.hasKey(NBT_WEAPON_TYPE)) {
                String typeName = tag.getString(NBT_WEAPON_TYPE);
                try {
                    return WeaponType.valueOf(typeName.toUpperCase());
                } catch (IllegalArgumentException ignored) {}
            }
        }

        Item item = stack.getItem();

        if (item instanceof ItemSword) return SWORD;
        if (item instanceof ItemAxe)   return AXE;

        // Fallback: check item registry name for "spear" / "dagger" / "knife"
        String name = item.getRegistryName() != null
                ? item.getRegistryName().getResourcePath().toLowerCase()
                : "";

        if (name.contains("spear") || name.contains("lance") || name.contains("pike")) return SPEAR;
        if (name.contains("dagger") || name.contains("knife") || name.contains("stiletto")) return DAGGER;

        return NONE;
    }

    /**
     * Tags an ItemStack as a specific weapon type (for modded items that
     * don't extend vanilla classes).
     */
    public static void tag(ItemStack stack, WeaponType type) {
        if (stack.isEmpty()) return;
        stack.getOrCreateSubCompound(NBT_WEAPON_TYPE);
        NBTTagCompound compound = new NBTTagCompound();
        compound.setString(NBT_WEAPON_TYPE, type.name());
        stack.setTagCompound(compound);
    }
}
