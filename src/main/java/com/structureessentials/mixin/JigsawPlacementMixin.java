package com.structureessentials.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.pools.DimensionPadding;
import net.minecraft.world.level.levelgen.structure.pools.JigsawPlacement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.pools.alias.PoolAliasLookup;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(JigsawPlacement.class)
public class JigsawPlacementMixin {
    @Inject(method = "addPieces(Lnet/minecraft/world/level/levelgen/structure/Structure$GenerationContext;Lnet/minecraft/core/Holder;Ljava/util/Optional;ILnet/minecraft/core/BlockPos;ZLjava/util/Optional;ILnet/minecraft/world/level/levelgen/structure/pools/alias/PoolAliasLookup;Lnet/minecraft/world/level/levelgen/structure/pools/DimensionPadding;Lnet/minecraft/world/level/levelgen/structure/templatesystem/LiquidSettings;)Ljava/util/Optional;"
            , at = @At("HEAD"), cancellable = true)
    private static void essentials$addPieces(
      final Structure.GenerationContext p_227239_,
      final Holder<StructureTemplatePool> holder,
      final Optional<ResourceLocation> p_227241_,
      final int p_227242_,
      final BlockPos p_227243_,
      final boolean p_227244_,
      final Optional<Heightmap.Types> p_227245_,
      final int p_227246_,
      final PoolAliasLookup p_307522_,
      final DimensionPadding p_348489_,
      final LiquidSettings p_352161_,
      final CallbackInfoReturnable<Optional<Structure.GenerationStub>> cir) {
        if (!holder.isBound()) {
            cir.setReturnValue(Optional.empty());
        }
    }
}
