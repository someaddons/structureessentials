package com.structureessentials.mixin;

import com.structureessentials.StructureEssentials;
import net.minecraft.core.Holder;
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

import java.util.List;

@Mixin(ExplorationMapFunction.class)
public class ExplorationMapFunctionMixin
{
    @Shadow @Final @Mutable
    public int searchRadius;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void initSearchRange(
        final List list, final TagKey tagKey, final Holder holder, final byte b, final int i, final boolean bl, final CallbackInfo ci)
    {
        searchRadius = Math.min(searchRadius, StructureEssentials.config.getCommonConfig().mapSearchRadius);
    }
}
