package com.structureessentials.mixin;

import com.structureessentials.config.CommonConfiguration;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
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
import java.util.Optional;

@Mixin(ExplorationMapFunction.class)
public class ExplorationMapFunctionMixin
{
    @Shadow @Final @Mutable
    private int searchRadius;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void initSearchRange(
        final Optional condition,
        final HolderSet destination,
        final Holder mapDecoration,
        final byte zoom,
        final int radius,
        final boolean skipKnownStructures,
        final CallbackInfo ci)
    {
        searchRadius = Math.min(searchRadius, CommonConfiguration.config.getCommonConfig().mapSearchRadius);
    }
}
