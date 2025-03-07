package com.structureessentials.config;

import com.cupboard.config.CupboardConfig;
import com.cupboard.config.ICommonConfig;
import com.google.gson.JsonObject;
import com.structureessentials.StructureEssentials;

public class CommonConfiguration implements ICommonConfig
{
    public static CupboardConfig<CommonConfiguration> config                    = new CupboardConfig<>(StructureEssentials.MODID, new CommonConfiguration());
    public        boolean                             structurePlacementLogging = false;
    public int     structureSearchTimeout     = 50;
    public boolean useFastStructureLookup     = true;
    public boolean warnMissingRegistryEntry   = true;
    public boolean disableLegacyRandomCrashes = true;
    public int     mapSearchRadius            = 40;
    public int     globalSearchRadius         = 70;
    public int     locateSearchRadius         = 110;
    public double  spacingSeparationModifier  = 1.0d;
    public int     minimumStructureDistance = 32;
    public boolean minimumStructureDistanceEnabled = false;
    public boolean minimumStructureDistanceLogging = false;
    public boolean forceUniqueSalt          = false;
    public boolean logDuplicatedSalt        = true;
    public boolean autoBiomeCompat  = true;
    public boolean autoBiomeCompatLogging  = false;
    public double  autoBiomeCompatStrictness  = 1.0d;

    public CommonConfiguration()
    {

    }

    public JsonObject serialize()
    {
        final JsonObject root = new JsonObject();

        final JsonObject entry = new JsonObject();
        entry.addProperty("desc:", "Enables debug logging of structure placement. Warning: This will spam the logs and is only recommended for debugging purposes. Default: false");
        entry.addProperty("structurePlacementLogging", structurePlacementLogging);
        root.add("structurePlacementLogging", entry);

        final JsonObject entry2 = new JsonObject();
        entry2.addProperty("desc:", "The maximum time (in seconds) a structure search is allowed to take. Default: 50");
        entry2.addProperty("structureSearchTimeout", structureSearchTimeout);
        root.add("structureSearchTimeout", entry2);

        final JsonObject entry3 = new JsonObject();
        entry3.addProperty("desc:", "Enables faster structure search. Default: true");
        entry3.addProperty("useFastStructureLookup", useFastStructureLookup);
        root.add("useFastStructureLookup", entry3);

        final JsonObject entry6 = new JsonObject();
        entry6.addProperty("desc:", "Specifies the maximum radius map items can search for structures. Lowering this value reduces the time structure searches stall the server but decreases the range in which structures are found. Vanilla: 50, Default: 40");
        entry6.addProperty("mapSearchRadius", mapSearchRadius);
        root.add("mapSearchRadius", entry6);

        final JsonObject entry8 = new JsonObject();
        entry8.addProperty("desc:", "Sets the search radius for the locate structure command. Vanilla: 100, Default: 110");
        entry8.addProperty("locateSearchRadius", locateSearchRadius);
        root.add("locateSearchRadius", entry8);

        final JsonObject entry7 = new JsonObject();
        entry7.addProperty("desc:", "Sets the global maximum structure search radius. The vanilla locate command uses 100. Lowering this value reduces the time structure searches stall the server but decreases the range in which structures are found. Default: 70");
        entry7.addProperty("globalSearchRadius", globalSearchRadius);
        root.add("globalSearchRadius", entry7);

        final JsonObject entry9 = new JsonObject();
        entry9.addProperty("desc:",
          "Adjusts the structure spacing (average spawn distance) and separation (minimum spawn distance). Increasing the value makes structures spawn farther apart, while decreasing it makes them spawn closer together. Vanilla Default: 1.0");
        entry9.addProperty("spacingSeparationModifier", spacingSeparationModifier);
        root.add("spacingSeparationModifier", entry9);

        final JsonObject entry14 = new JsonObject();
        entry14.addProperty("desc:",
            "Structure sets use a salt value to determine the randomness of their placement. Duplicated use of the same salt value can cause structures to spawn on the same place, this config enables logging duplicated salt values. Default: true");
        entry14.addProperty("logDuplicatedSalt", logDuplicatedSalt);
        root.add("logDuplicatedSalt", entry14);

        final JsonObject entry15 = new JsonObject();
        entry15.addProperty("desc:",
            "Set a minimum distance in blocks between structures generated which prevents structure overlaps(not 100% but close). Not recommended to use higher values, as that may strain the worldgen due to repeated structure retries and can prevent surfaces structures when there is some in a cave below. If you want structures more spaced out than this use the spacing/seperation modifier. Default: 32 blocks, range 16-512");
        entry15.addProperty("minimumStructureDistance", minimumStructureDistance);
        entry15.addProperty("enabled", minimumStructureDistanceEnabled);
        entry15.addProperty("logOverlaps", minimumStructureDistanceLogging);
        root.add("minimumStructureDistance", entry15);

        final JsonObject entry10 = new JsonObject();
        entry10.addProperty("desc:",
          "Automatically analyzes present biomes and adjust structure spawning to include fitting ones. Default: true");
        entry10.addProperty("autoBiomeCompat", autoBiomeCompat);
        root.add("autoBiomeCompat", entry10);

        final JsonObject entry11 = new JsonObject();
        entry11.addProperty("desc:",
          "Enables logging for the autoBiomeCompat feature. Default: false");
        entry11.addProperty("autoBiomeCompatLogging", autoBiomeCompatLogging);
        root.add("autoBiomeCompatLogging", entry11);

        final JsonObject entry12 = new JsonObject();
        entry12.addProperty("desc:",
          "Sets a modifier for how strict the autoBiomeCompat is, lower allows adding less similar biomes to be added. E.g. 0.5 decreases the similarity requirements by 50% . Default: 1.0");
        entry12.addProperty("autoBiomeCompatStrictness", autoBiomeCompatStrictness);
        root.add("autoBiomeCompatStrictness", entry12);

        final JsonObject entry4 = new JsonObject();
        entry4.addProperty("desc:",
          "Prevents crashes due to missing registry entries (e.g., changes in mod structure IDs) by converting them into log error messages instead. Default: true");
        entry4.addProperty("warnMissingRegistryEntry", warnMissingRegistryEntry);
        root.add("warnMissingRegistryEntry", entry4);

        final JsonObject entry5 = new JsonObject();
        entry5.addProperty("desc:", "Prevents crashes caused by multithreaded access to thread-specific random number generators. Default: true");
        entry5.addProperty("disableLegacyRandomCrashes", disableLegacyRandomCrashes);
        root.add("disableLegacyRandomCrashes", entry5);

        //TODO: feature idea: Structure set jsons generator to have distances inbetween structures? does also require disabling existing structure set though
        //TODO: generate datapack automatically from structure biome compat for manual adjustments
        return root;
    }

    public void deserialize(JsonObject data)
    {
        structurePlacementLogging = data.get("structurePlacementLogging").getAsJsonObject().get("structurePlacementLogging").getAsBoolean();
        structureSearchTimeout = data.get("structureSearchTimeout").getAsJsonObject().get("structureSearchTimeout").getAsInt();
        useFastStructureLookup = data.get("useFastStructureLookup").getAsJsonObject().get("useFastStructureLookup").getAsBoolean();
        warnMissingRegistryEntry = data.get("warnMissingRegistryEntry").getAsJsonObject().get("warnMissingRegistryEntry").getAsBoolean();
        disableLegacyRandomCrashes = data.get("disableLegacyRandomCrashes").getAsJsonObject().get("disableLegacyRandomCrashes").getAsBoolean();
        mapSearchRadius = data.get("mapSearchRadius").getAsJsonObject().get("mapSearchRadius").getAsInt();
        globalSearchRadius = data.get("globalSearchRadius").getAsJsonObject().get("globalSearchRadius").getAsInt();
        locateSearchRadius = data.get("locateSearchRadius").getAsJsonObject().get("locateSearchRadius").getAsInt();
        spacingSeparationModifier = data.get("spacingSeparationModifier").getAsJsonObject().get("spacingSeparationModifier").getAsDouble();
        autoBiomeCompat = data.get("autoBiomeCompat").getAsJsonObject().get("autoBiomeCompat").getAsBoolean();
        autoBiomeCompatLogging = data.get("autoBiomeCompatLogging").getAsJsonObject().get("autoBiomeCompatLogging").getAsBoolean();
        autoBiomeCompatStrictness = data.get("autoBiomeCompatStrictness").getAsJsonObject().get("autoBiomeCompatStrictness").getAsDouble();
        minimumStructureDistance = Math.min(512, Math.max(16, data.get("minimumStructureDistance").getAsJsonObject().get("minimumStructureDistance").getAsInt()));
        minimumStructureDistanceEnabled = data.get("minimumStructureDistance").getAsJsonObject().get("enabled").getAsBoolean();
        minimumStructureDistanceLogging = data.get("minimumStructureDistance").getAsJsonObject().get("logOverlaps").getAsBoolean();
        logDuplicatedSalt = data.get("logDuplicatedSalt").getAsJsonObject().get("logDuplicatedSalt").getAsBoolean();
    }
}
