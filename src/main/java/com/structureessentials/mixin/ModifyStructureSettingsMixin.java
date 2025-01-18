package com.structureessentials.mixin;

import com.structureessentials.IStructureModifier;
import net.minecraft.core.HolderSet;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.neoforged.neoforge.common.world.ModifiableStructureInfo;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ModifiableStructureInfo.class)
public class ModifyStructureSettingsMixin implements IStructureModifier
{
    @Shadow
    @Nullable
    private ModifiableStructureInfo.@Nullable StructureInfo modifiedStructureInfo;

    @Shadow
    @Final
    private ModifiableStructureInfo.StructureInfo originalStructureInfo;

    @Override
    public void setStructureBiomes(final HolderSet<Biome> newBiomes)
    {
        Structure.StructureSettings prev = modifiedStructureInfo == null ? originalStructureInfo.structureSettings() : modifiedStructureInfo.structureSettings();

        ModifiableStructureInfo.StructureInfo info =
            new ModifiableStructureInfo.StructureInfo(new Structure.StructureSettings(newBiomes, prev.spawnOverrides(), prev.step(), prev.terrainAdaptation()));
        modifiedStructureInfo = info;
    }
}
