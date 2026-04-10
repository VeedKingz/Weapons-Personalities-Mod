package com.weaponpersonalities;

import com.weaponpersonalities.config.ModConfig;
import com.weaponpersonalities.events.CombatEventHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(
    modid = WeaponPersonalitiesMod.MOD_ID,
    name = WeaponPersonalitiesMod.MOD_NAME,
    version = WeaponPersonalitiesMod.MOD_VERSION,
    acceptedMinecraftVersions = "[1.12,1.13)"
)
public class WeaponPersonalitiesMod {

    public static final String MOD_ID      = "weaponpersonalities";
    public static final String MOD_NAME    = "Weapon Personalities";
    public static final String MOD_VERSION = "1.0.0";

    public static final Logger LOGGER = LogManager.getLogger(MOD_NAME);

    @Mod.Instance(MOD_ID)
    public static WeaponPersonalitiesMod instance;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        ModConfig.init(event.getSuggestedConfigurationFile());
        LOGGER.info("{} pre-initialization complete.", MOD_NAME);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(new CombatEventHandler());
        LOGGER.info("{} initialization complete.", MOD_NAME);
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        LOGGER.info("{} post-initialization complete.", MOD_NAME);
    }
}
