package com.structureessentials.mixin;

import com.structureessentials.config.CommonConfiguration;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.storage.loot.functions.ExplorationMapFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ExplorationMapFunction.class)
public class ExplorationMapFunctionMixin
{
    @Shadow @Final @Mutable
    public int searchRadius;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void initSearchRange(
      final LootItemCondition[] p_210652_,
      final TagKey p_210653_,
      final MapDecoration.Type p_210654_,
      final byte p_210655_,
      final int p_210656_,
      final boolean p_210657_,
      final CallbackInfo ci)
    {
        searchRadius = Math.min(searchRadius, CommonConfiguration.config.getCommonConfig().mapSearchRadius);
    }
}
