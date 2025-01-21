package com.structureessentials;

import net.minecraft.core.HolderSet;
import net.minecraft.world.level.biome.Biome;

public interface IStructureModifier
{
    void setStructureBiomes(HolderSet<Biome> newBiomes);
}
