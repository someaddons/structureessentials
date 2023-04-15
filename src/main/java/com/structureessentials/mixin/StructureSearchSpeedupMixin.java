package com.structureessentials.mixin;

import com.mojang.datafixers.util.Pair;
import com.structureessentials.StructureEssentials;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;

@Mixin(ChunkGenerator.class)
public class StructureSearchSpeedupMixin
{
    @Inject(method = "getStructureGeneratingAt", at = @At("HEAD"), cancellable = true)
    private static void onFind(
      final Set<Holder<Structure>> holderSet,
      final LevelReader level,
      final StructureManager structureManager,
      final boolean load,
      final StructurePlacement placement,
      final ChunkPos pos, final CallbackInfoReturnable<Pair<BlockPos, Holder<Structure>>> cir)
    {
        if (holderSet.isEmpty() || !StructureEssentials.config.getCommonConfig().useFastStructureLookup)
        {
            return;
        }

        boolean found = false;
        for (int i = -128; i < 284; i+=16)
        {
            final Holder<Biome> biomeHolder = level.getBiome(pos.getWorldPosition().offset(0,i,0));

            for (final Holder<Structure> structureHolder : holderSet)
            {
                if (structureHolder.value().biomes().contains(biomeHolder))
                {
                    found = true;
                    break;
                }
            }

            if (found)
            {
                break;
            }
        }

        if (!found)
        {
            cir.setReturnValue(null);
        }
    }
}
