package com.structureessentials.mixin;

import com.cupboard.util.RegistryLookup;
import com.cupboard.util.ResourceLocation;
import com.structureessentials.Timings;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Predicate;

@Mixin(Structure.class)
public class StructureTimingMixin
{
    @Unique
    private long time = 0;

    @Unique
    private ResourceLocation id = null;

    @Inject(method = "generate", at = @At("HEAD"))
    private void beforeGenerate(
        final Holder<Structure> selected,
        final ResourceKey<Level> dimension,
        final RegistryAccess registryAccess,
        final ChunkGenerator chunkGenerator,
        final BiomeSource biomeSource,
        final RandomState randomState,
        final StructureTemplateManager structureTemplateManager,
        final long seed,
        final ChunkPos sourceChunkPos,
        final int references,
        final LevelHeightAccessor heightAccessor,
        final Predicate<Holder<Biome>> validBiome,
        final CallbackInfoReturnable<StructureStart> cir)
    {
        time = System.nanoTime();
    }

    @Inject(method = "generate", at = @At("RETURN"))
    private void afterGenerate(
        final Holder<Structure> selected,
        final ResourceKey<Level> dimension,
        final RegistryAccess registryAccess,
        final ChunkGenerator chunkGenerator,
        final BiomeSource biomeSource,
        final RandomState randomState,
        final StructureTemplateManager structureTemplateManager,
        final long seed,
        final ChunkPos sourceChunkPos,
        final int references,
        final LevelHeightAccessor heightAccessor,
        final Predicate<Holder<Biome>> validBiome,
        final CallbackInfoReturnable<StructureStart> cir)
    {
        final long time = (System.nanoTime() - this.time) / 10;

        if (id == null)
        {
            id = RegistryLookup.getID(registryAccess, Registries.STRUCTURE, this);
        }

        if (id != null)
        {
            Timings.structureTimings.put(id, Timings.structureTimings.getOrDefault(id, 0L) + time);
        }
    }
}
