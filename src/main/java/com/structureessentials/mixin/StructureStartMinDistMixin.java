package com.structureessentials.mixin;

import com.structureessentials.IGeneratorNearbyStructureHolder;
import com.structureessentials.StructureEssentials;
import com.structureessentials.config.CommonConfiguration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
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
            final RegistryAccess registryAccess,
            final ChunkGenerator generator,
            final BiomeSource biomeSource,
            final RandomState p_226600_,
            final StructureTemplateManager p_226601_,
            final long p_226602_,
            final ChunkPos p_226603_,
            final int p_226604_,
            final LevelHeightAccessor p_226605_,
            final Predicate<Holder<Biome>> p_226606_,
            final CallbackInfoReturnable<StructureStart> cir
        )
    {
        if (cir.getReturnValue() == StructureStart.INVALID_START || !CommonConfiguration.config.getCommonConfig().minimumStructureDistanceEnabled)
        {
            return;
        }

        if (!(generator instanceof IGeneratorNearbyStructureHolder))
        {
            if (CommonConfiguration.config.getCommonConfig().minimumStructureDistanceLogging)
            {
                StructureEssentials.LOGGER.warn("Skipping structure minimum distance check, invalid generator: " + generator);
            }
            return;
        }

        final IGeneratorNearbyStructureHolder nearbyStructureHolder = (IGeneratorNearbyStructureHolder) generator;

        final int distance = CommonConfiguration.config.getCommonConfig().minimumStructureDistance;
        final int xzOffset = 3000000 * distance;
        final int yOffset = this.settings.step() == GenerationStep.Decoration.SURFACE_STRUCTURES ? 2000 : 500;

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

                    if (nearby != null)
                    {
                        break;
                    }
                }
            }

            if (nearby != null)
            {
                if (CommonConfiguration.config.getCommonConfig().minimumStructureDistanceLogging)
                {
                    StructureEssentials.LOGGER.warn(
                        "Prevented structure overlap for: " + registryAccess.registry(Registries.STRUCTURE).get().getKey((Structure) (Object) this) + " at: " + center +
                            " existing structure: " + nearby);
                }
                cir.setReturnValue(StructureStart.INVALID_START);
                return;
            }
        }

        final String name;
        ResourceLocation regID = registryAccess.registry(Registries.STRUCTURE).get().getKey((Structure) (Object) this);
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
