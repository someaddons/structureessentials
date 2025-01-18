package com.structureessentials.mixin;

import com.structureessentials.StructureEssentials;
import net.minecraft.core.Holder;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.storage.loot.functions.ExplorationMapFunction;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ExplorationMapFunction.class)
public class ExplorationMapFunctionMixin
{
    @Shadow @Final @Mutable
    private int searchRadius;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void initSearchRange(
        final List p_298451_,
        final TagKey p_210653_,
        final Holder p_336106_,
        final byte p_210655_,
        final int p_210656_,
        final boolean p_210657_,
        final CallbackInfo ci)
    {
        searchRadius = Math.min(searchRadius, StructureEssentials.config.getCommonConfig().mapSearchRadius);
    }
}
