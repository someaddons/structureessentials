package com.structureessentials.mixin;

import com.cupboard.util.RegistryLookup;
import com.cupboard.util.ResourceLocation;
import com.structureessentials.StructureEssentials;
import com.structureessentials.Timings;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.placement.FeaturePlacer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(FeaturePlacer.class)
public abstract class PlacedFeatureErrorMixin
{
    @Redirect(method = "place(Lnet/minecraft/world/level/levelgen/placement/PlacedFeature;Lnet/minecraft/util/RandomSource;Lnet/minecraft/core/BlockPos;Z)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/levelgen/feature/Feature;place(Lnet/minecraft/world/level/WorldGenLevel;Lnet/minecraft/world/level/chunk/ChunkGenerator;Lnet/minecraft/util/RandomSource;Lnet/minecraft/core/BlockPos;)Z"))
    private boolean onPlace(
        final Feature instance,
        final WorldGenLevel worldGenLevel,
        final ChunkGenerator chunkGenerator,
        final RandomSource randomSource,
        final BlockPos blockPos)
    {
        final ResourceLocation key = RegistryLookup.getID(worldGenLevel, Registries.FEATURE, instance);
        try
        {
            long prev = System.nanoTime();
            boolean result = instance.place(worldGenLevel, chunkGenerator, randomSource, blockPos);
            prev = (System.nanoTime() - prev) / 10;
            if (key != null)
            {
                Timings.featureTimings.put(key, Timings.featureTimings.getOrDefault(key, 0L) + prev);
            }
            return result;
        }
        catch (Exception e)
        {
            StructureEssentials.LOGGER.warn("Feature: " + key + " errored during placement at " + blockPos);
            return false;
        }
    }
}
