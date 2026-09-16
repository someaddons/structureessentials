package com.structureessentials.mixin;

import com.structureessentials.config.CommonConfiguration;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.structure.placement.AbstractSpreadingStructurePlacement;
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

    @Inject(method = "<init>(Lnet/minecraft/core/Vec3i;Lnet/minecraft/world/level/levelgen/structure/placement/AbstractSpreadingStructurePlacement$FrequencyReductionMethod;FILjava/util/Optional;IILnet/minecraft/world/level/levelgen/structure/placement/RandomSpreadType;)V"
      , at = @At("RETURN"))
    private void adjustSpacingSeperation(
        final Vec3i locateOffset,
        final AbstractSpreadingStructurePlacement.FrequencyReductionMethod frequencyReductionMethod,
        final float frequency,
        final int salt,
        final Optional exclusionZone,
        final int a,
        final int b,
        final RandomSpreadType spreadType,
        final CallbackInfo ci)
    {
        spacing = Mth.clamp((int) Math.round(spacing * CommonConfiguration.config.getCommonConfig().spacingSeparationModifier), 1, 4095);
        separation = Mth.clamp((int) Math.round(separation * CommonConfiguration.config.getCommonConfig().spacingSeparationModifier), 0, 4095);

        if (spacing <= separation)
        {
            spacing = separation + 1;
        }
    }
}
