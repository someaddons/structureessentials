package com.structureessentials.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.pools.DimensionPadding;
import net.minecraft.world.level.levelgen.structure.pools.JigsawPlacement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.pools.alias.PoolAliasLookup;
import net.minecraft.world.level.levelgen.structure.structures.JigsawStructure;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(JigsawPlacement.class)
public class JigsawPlacementMixin
{
    @Inject(method = "addPieces(Lnet/minecraft/world/level/levelgen/structure/Structure$GenerationContext;Lnet/minecraft/core/Holder;Ljava/util/Optional;ILnet/minecraft/core/BlockPos;ZLjava/util/Optional;Lnet/minecraft/world/level/levelgen/structure/structures/JigsawStructure$MaxDistance;Lnet/minecraft/world/level/levelgen/structure/pools/alias/PoolAliasLookup;Lnet/minecraft/world/level/levelgen/structure/pools/DimensionPadding;Lnet/minecraft/world/level/levelgen/structure/templatesystem/LiquidSettings;)Ljava/util/Optional;"
        , at = @At("HEAD"), cancellable = true)
    private static void essentials$addPieces(
        final Structure.GenerationContext context,
        final Holder<StructureTemplatePool> startPool,
        final Optional<Identifier> startJigsaw,
        final int maxDepth,
        final BlockPos position,
        final boolean doExpansionHack,
        final Optional<Heightmap.Types> projectStartToHeightmap,
        final JigsawStructure.MaxDistance maxDistanceFromCenter,
        final PoolAliasLookup poolAliasLookup,
        final DimensionPadding dimensionPadding,
        final LiquidSettings liquidSettings,
        final CallbackInfoReturnable<Optional<Structure.GenerationStub>> cir)
    {
        if (!startPool.isBound())
        {
            cir.setReturnValue(Optional.empty());
        }
    }
}
