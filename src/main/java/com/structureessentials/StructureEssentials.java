package com.structureessentials;

import com.structureessentials.config.Configuration;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Random;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(StructureEssentials.MODID)
public class StructureEssentials
{
    public static final String MODID = "structureessentials";
    public static final Logger LOGGER = LogManager.getLogger();
    public static Configuration config = new Configuration();
    public static Random rand = new Random();

    public StructureEssentials()
    {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
    }


    private void setup(final FMLCommonSetupEvent event)
    {
        config.load();
        LOGGER.info(MODID + " mod initialized");
    }
}
