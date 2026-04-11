package com.structureessentials.mixin;

import com.cupboard.util.RegistryLookup;
import com.cupboard.util.ResourceLocation;
import com.structureessentials.IGeneratorNearbyStructureHolder;
import com.structureessentials.StructureEssentials;
import com.structureessentials.config.CommonConfiguration;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.function.Predicate;

@Mixin(Structure.class)
public abstract class StructureStartMinDistMixin
{
    @Shadow
    @Final
    public Structure.StructureSettings settings;

    @Inject(method = "generate", at = @At(value = "RETURN"), cancellable = true)
    private void checkOtherStructuresNearby
        (
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
            final CallbackInfoReturnable<StructureStart> cir
        )
    {
        if (cir.getReturnValue() == StructureStart.INVALID_START || !CommonConfiguration.config.getCommonConfig().minimumStructureDistanceEnabled)
        {
            return;
        }

        if (!(chunkGenerator instanceof IGeneratorNearbyStructureHolder))
        {
            if (CommonConfiguration.config.getCommonConfig().minimumStructureDistanceLogging)
            {
                StructureEssentials.LOGGER.warn("Skipping structure minimum distance check, invalid generator: " + chunkGenerator);
            }
            return;
        }

        final IGeneratorNearbyStructureHolder nearbyStructureHolder = (IGeneratorNearbyStructureHolder) chunkGenerator;

        final int distance = CommonConfiguration.config.getCommonConfig().minimumStructureDistance;
        final int xzOffset = 3000000 * distance;
        final int yOffset = this.settings.step() == GenerationStep.Decoration.SURFACE_STRUCTURES ? 2000 : 500;

        final String name;
        ResourceLocation regID = RegistryLookup.getID(registryAccess, Registries.STRUCTURE, this);
        if (regID != null)
        {
            name = regID.toString();
        }
        else
        {
            name = "unknown:" + this;
        }

        for (final var piece : cir.getReturnValue().getPieces())
        {
            final BlockPos center = piece.getLocatorPosition();
            String nearby =
                nearbyStructureHolder.getNearby(BlockPos.asLong((center.getX() + xzOffset) / distance,
                    (center.getY() + yOffset) / distance,
                    (center.getZ() + xzOffset) / distance));

            if (nearby == null && (piece.getBoundingBox().getXSpan() > 8 || piece.getBoundingBox().getYSpan() > 8 || piece.getBoundingBox().getZSpan() > 8))
            {
                for (final BlockPos pos : getBoundingBoxCorners(piece.getBoundingBox()))
                {
                    nearby = nearbyStructureHolder.getNearby(BlockPos.asLong((pos.getX() + xzOffset) / distance,
                        (pos.getY() + yOffset) / distance,
                        (pos.getZ() + xzOffset) / distance));

                    if (nearby != null && !nearby.equals(name))
                    {
                        break;
                    }
                }
            }

            if (nearby != null && !nearby.equals(name))
            {
                if (CommonConfiguration.config.getCommonConfig().minimumStructureDistanceLogging)
                {
                    StructureEssentials.LOGGER.warn(
                        "Prevented structure overlap for: " + name + " at: " + center +
                            " existing structure: " + nearby);
                }
                cir.setReturnValue(StructureStart.INVALID_START);
                return;
            }
        }

        for (final var piece : cir.getReturnValue().getPieces())
        {
            final BlockPos center = piece.getLocatorPosition();
            nearbyStructureHolder.setNearby(BlockPos.asLong((center.getX() + xzOffset) / distance, (center.getY() + 2000) / distance, (center.getZ() + xzOffset) / distance),
                name);

            if (piece.getBoundingBox().getXSpan() > 8 || piece.getBoundingBox().getYSpan() > 8 || piece.getBoundingBox().getZSpan() > 8)
            {
                for (final BlockPos pos : getBoundingBoxCorners(piece.getBoundingBox()))
                {
                    nearbyStructureHolder.setNearby(BlockPos.asLong((pos.getX() + xzOffset) / distance, (pos.getY() + 2000) / distance, (pos.getZ() + xzOffset) / distance),
                        name);
                }
            }
        }
    }

    @Unique
    private static List<BlockPos> getBoundingBoxCorners(BoundingBox box)
    {
        return List.of(
            new BlockPos(box.minX(), box.minY(), box.minZ()),
            new BlockPos(box.minX(), box.minY(), box.maxZ()),
            new BlockPos(box.minX(), box.maxY(), box.minZ()),
            new BlockPos(box.minX(), box.maxY(), box.maxZ()),
            new BlockPos(box.maxX(), box.minY(), box.minZ()),
            new BlockPos(box.maxX(), box.minY(), box.maxZ()),
            new BlockPos(box.maxX(), box.maxY(), box.minZ()),
            new BlockPos(box.maxX(), box.maxY(), box.maxZ())
        );
    }
}
