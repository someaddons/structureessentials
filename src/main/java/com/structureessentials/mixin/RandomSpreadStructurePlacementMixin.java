package com.structureessentials.mixin;

import com.structureessentials.config.CommonConfiguration;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadType;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(RandomSpreadStructurePlacement.class)
public class RandomSpreadStructurePlacementMixin
{
    @Shadow
    @Final
    @Mutable
    private int spacing;

    @Shadow
    @Final
    @Mutable
    private int separation;

    @Inject(method = "<init>(Lnet/minecraft/core/Vec3i;Lnet/minecraft/world/level/levelgen/structure/placement/StructurePlacement$FrequencyReductionMethod;FILjava/util/Optional;IILnet/minecraft/world/level/levelgen/structure/placement/RandomSpreadType;)V"
      , at = @At("RETURN"))
    private void adjustSpacingSeperation(
      final Vec3i p_227000_,
      final StructurePlacement.FrequencyReductionMethod p_227001_,
      final float p_227002_,
      final int p_227003_,
      final Optional p_227004_,
      final int p_227005_,
      final int p_227006_,
      final RandomSpreadType p_227007_,
      final CallbackInfo ci)
    {
        spacing = Mth.clamp((int) Math.round(spacing * CommonConfiguration.config.getCommonConfig().spacingSeparationModifier), 2, 4095);
        separation = Mth.clamp((int) Math.round(separation * CommonConfiguration.config.getCommonConfig().spacingSeparationModifier), 1, 4095);

        if (spacing <= separation)
        {
            spacing = separation + 1;
        }
    }
}
