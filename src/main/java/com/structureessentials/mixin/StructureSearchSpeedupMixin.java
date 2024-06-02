package com.structureessentials.mixin;

import com.mojang.datafixers.util.Pair;
import com.structureessentials.StructureEssentials;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
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

        int[] yLevels = Mth.outFromOrigin(65, level.getMinBuildHeight() + 1, level.getMaxBuildHeight(), 64).toArray();

        final BlockPos worldPos = pos.getWorldPosition();

        outer:
        for (int i = 0; i < 4; i++)
        {
            final int xQuart = QuartPos.fromBlock(worldPos.getX() + i * 4);
            final int zQuart = QuartPos.fromBlock(worldPos.getZ() + i * 4);

            for (int yBlock : yLevels)
            {
                final int yQuart = QuartPos.fromBlock(yBlock);
                final Holder<Biome> holder = ((ServerLevel) level).getChunkSource()
                  .getGenerator()
                  .getBiomeSource()
                  .getNoiseBiome(xQuart, yQuart, zQuart, ((ServerLevel) level).getChunkSource().randomState().sampler());

                for (final Holder<Structure> structureHolder : holderSet)
                {
                    if (structureHolder.value().biomes().contains(holder))
                    {
                        found = true;
                        break outer;
                    }
                }
            }
        }

        if (!found)
        {
            cir.setReturnValue(null);
        }
    }
}
