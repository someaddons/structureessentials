package com.structureessentials.mixin;

import com.structureessentials.StructureEssentials;
import com.structureessentials.config.CommonConfiguration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
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

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

@Mixin(Structure.class)
public class StructureStartMinDistMixin
{
    @Unique
    private static ConcurrentHashMap<Long, Structure> nearbyStructures = new ConcurrentHashMap<>();

    @Inject(method = "generate", at = @At(value = "RETURN"), cancellable = true)
    private void checkOtherStructuresNearby(
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
        if (cir.getReturnValue() == StructureStart.INVALID_START || !CommonConfiguration.config.getCommonConfig().minimumStructureDistanceEnabled)
        {
            return;
        }

        final int distance = CommonConfiguration.config.getCommonConfig().minimumStructureDistance;

        for (final var piece : cir.getReturnValue().getPieces())
        {
            final BlockPos center = piece.getLocatorPosition();
            final Structure nearby = nearbyStructures.get(BlockPos.asLong(center.getX() / distance, center.getY() / distance, center.getZ() / distance));
            if (nearby != null)
            {
                if (CommonConfiguration.config.getCommonConfig().minimumStructureDistanceLogging)
                {
                    StructureEssentials.LOGGER.warn(
                        "Prevented structure overlap for: " + registryAccess.registry(Registries.STRUCTURE).get().getKey((Structure) (Object) this) + " at: " + center +
                            " existing structure: " + registryAccess.registry(Registries.STRUCTURE).get().getKey(nearby));
                }
                cir.setReturnValue(StructureStart.INVALID_START);
                return;
            }
        }

        for (final var piece : cir.getReturnValue().getPieces())
        {
            final BlockPos center = piece.getLocatorPosition();
            nearbyStructures.put(BlockPos.asLong(center.getX() / distance, center.getY() / distance, center.getZ() / distance), (Structure) (Object) this);
        }
    }
}
