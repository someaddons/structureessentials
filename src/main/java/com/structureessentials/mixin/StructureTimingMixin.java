package com.structureessentials.mixin;

import com.structureessentials.Timings;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
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
        final RegistryAccess p_226597_,
        final ChunkGenerator p_226598_,
        final BiomeSource p_226599_,
        final RandomState p_226600_,
        final StructureTemplateManager p_226601_,
        final long p_226602_,
        final ChunkPos p_226603_,
        final int p_226604_,
        final LevelHeightAccessor p_226605_,
        final Predicate<Holder<Biome>> p_226606_,
        final CallbackInfoReturnable<StructureStart> cir)
    {
        time = System.nanoTime();
    }

    @Inject(method = "generate", at = @At("RETURN"))
    private void afterGenerate(
        final RegistryAccess registryAccess,
        final ChunkGenerator p_226598_,
        final BiomeSource p_226599_,
        final RandomState p_226600_,
        final StructureTemplateManager p_226601_,
        final long p_226602_,
        final ChunkPos p_226603_,
        final int p_226604_,
        final LevelHeightAccessor p_226605_,
        final Predicate<Holder<Biome>> p_226606_,
        final CallbackInfoReturnable<StructureStart> cir)
    {
        final long time = (System.nanoTime() - this.time) / 10;

        if (id == null)
        {
            id = registryAccess.registry(Registries.STRUCTURE).get().getKey((Structure) (Object) this);
        }

        if (id != null)
        {
            Timings.structureTimings.put(id, Timings.structureTimings.getOrDefault(id, 0L) + time);
        }
    }
}
