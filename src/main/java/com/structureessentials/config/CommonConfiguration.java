package com.structureessentials.config;

import com.google.gson.JsonObject;
import com.structureessentials.StructureEssentials;

public class CommonConfiguration
{
    public boolean structurePlacementLogging = false;
    public int structureSearchTimeout = 50;
    public boolean useFastStructureLookup = true;
    public boolean warnMissingRegistryEntry = true;

    public CommonConfiguration()
    {

    }

    public JsonObject serialize()
    {
        final JsonObject root = new JsonObject();

        final JsonObject entry = new JsonObject();
        entry.addProperty("desc:", "Enables debug logging of structure placement, does spam logs, only recommenced for debugging. Default: false");
        entry.addProperty("structurePlacementLogging", structurePlacementLogging);
        root.add("structurePlacementLogging", entry);

        final JsonObject entry2 = new JsonObject();
        entry2.addProperty("desc:", "Maximum time in seconds a structure search is allowed to take: default 50");
        entry2.addProperty("structureSearchTimeout", structureSearchTimeout);
        root.add("structureSearchTimeout", entry2);

        final JsonObject entry3 = new JsonObject();
        entry3.addProperty("desc:", "Whether the fast structure search is used, default: true");
        entry3.addProperty("useFastStructureLookup", useFastStructureLookup);
        root.add("useFastStructureLookup", entry3);

        final JsonObject entry4 = new JsonObject();
        entry4.addProperty("desc:", "Prevents crashes for missing registry entries(e.g. a mod update structure ids) and turns them into a log error message instead, default: true");
        entry4.addProperty("warnMissingRegistryEntry", warnMissingRegistryEntry);
        root.add("warnMissingRegistryEntry", entry4);

        return root;
    }

    public void deserialize(JsonObject data)
    {
        if (data == null)
        {
            StructureEssentials.LOGGER.error("Config file was empty!");
            return;
        }

        structurePlacementLogging = data.get("structurePlacementLogging").getAsJsonObject().get("structurePlacementLogging").getAsBoolean();
        structureSearchTimeout = data.get("structureSearchTimeout").getAsJsonObject().get("structureSearchTimeout").getAsInt();
        useFastStructureLookup = data.get("useFastStructureLookup").getAsJsonObject().get("useFastStructureLookup").getAsBoolean();
    }
}
