package com.structureessentials.mixin;

import com.structureessentials.IStructureModifier;
import net.minecraft.core.HolderSet;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.structure.Structure;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Structure.class)
public class ModifyStructureSettingsMixin implements IStructureModifier
{
    @Shadow
    @Final
    @Mutable
    public Structure.StructureSettings settings;

    @Override
    public void setStructureBiomes(final HolderSet<Biome> newBiomes)
    {
        settings = new Structure.StructureSettings(newBiomes, settings.spawnOverrides(), settings.step(), settings.terrainAdaptation());
    }
}
