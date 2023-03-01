package com.structureessentials;

import com.structureessentials.command.Command;
import com.structureessentials.config.Configuration;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Random;

public class StructureEssentials implements ModInitializer
{
    public static final String MODID = "structureessentials";
    public static final Logger LOGGER = LogManager.getLogger();
    public static Configuration config = new Configuration();
    public static Random rand = new Random();

    public StructureEssentials()
    {

    }

    @Override
    public void onInitialize()
    {
        config.load();
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated, commandSelection) ->
        {
            dispatcher.register(new Command().build());
        });

        LOGGER.info(MODID + " mod initialized");
    }
}
