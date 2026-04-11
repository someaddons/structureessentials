package com.structureessentials.mixin;

import com.cupboard.util.RegistryLookup;
import com.structureessentials.Timings;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(StructureStart.class)
public abstract class StructureStartTimingMixin
{
    @Shadow
    public abstract Structure getStructure();

    @Unique
    private long time = 0;

    @Unique
    private ResourceLocation id = null;

    @Inject(method = "placeInChunk", at = @At("HEAD"))
    private void beforeGenerate(
        final WorldGenLevel p_226851_,
        final StructureManager p_226852_,
        final ChunkGenerator p_226853_,
        final RandomSource p_226854_,
        final BoundingBox p_226855_,
        final ChunkPos p_226856_,
        final CallbackInfo ci)
    {
        time = System.nanoTime();
    }

    @Inject(method = "placeInChunk", at = @At("RETURN"))
    private void afterGenerate(
        final WorldGenLevel worldGenLevel,
        final StructureManager p_226852_,
        final ChunkGenerator p_226853_,
        final RandomSource p_226854_,
        final BoundingBox p_226855_,
        final ChunkPos p_226856_,
        final CallbackInfo ci)
    {
        final long time = (System.nanoTime() - this.time) / 10;

        if (id == null)
        {
            id = RegistryLookup.getID(worldGenLevel, Registries.STRUCTURE, this.getStructure());
        }

        if (id != null)
        {
            Timings.structureTimings.put(id, Timings.structureTimings.getOrDefault(id, 0L) + time);
        }
    }
}
