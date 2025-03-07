package com.structureessentials.mixin;

import com.structureessentials.IGeneratorNearbyStructureHolder;
import net.minecraft.world.level.chunk.ChunkGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.concurrent.ConcurrentHashMap;

@Mixin(ChunkGenerator.class)
public class StructureMinDistanceDataHolder implements IGeneratorNearbyStructureHolder
{
    @Unique
    private ConcurrentHashMap<Long, String> nearbyStructures = new ConcurrentHashMap<>();

    @Override
    public String getNearby(final long pos)
    {
        return nearbyStructures.get(pos);
    }

    @Override
    public void setNearby(final long pos, final String name)
    {
        nearbyStructures.putIfAbsent(pos, name);
    }
}
