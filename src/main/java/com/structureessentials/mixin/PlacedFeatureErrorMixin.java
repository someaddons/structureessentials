package com.structureessentials.mixin;

import com.structureessentials.StructureEssentials;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(PlacedFeature.class)
public abstract class PlacedFeatureErrorMixin
{
    @Shadow
    protected abstract boolean placeWithContext(final PlacementContext p_226369_, final RandomSource p_226370_, final BlockPos p_226371_);

    PlacedFeature self = (PlacedFeature) (Object) this;

    @Redirect(method = "place", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/levelgen/placement/PlacedFeature;placeWithContext(Lnet/minecraft/world/level/levelgen/placement/PlacementContext;Lnet/minecraft/util/RandomSource;Lnet/minecraft/core/BlockPos;)Z"))
    private boolean onPlace(PlacedFeature feature, PlacementContext context, RandomSource randomSource, BlockPos pos)
    {
        try
        {
            return placeWithContext(context, randomSource, pos);
        }
        catch (Exception e)
        {
            if (self.feature() instanceof Holder.Reference)
            {
                StructureEssentials.LOGGER.warn("Feature: " + ((Holder.Reference) self.feature()).key() + " errored during placement at " + pos);
                return false;
            }

            StructureEssentials.LOGGER.warn("Unkown feature +" + self.feature() + " errored during placement at " + pos, e);
            return false;
        }
    }

    @Redirect(method = "placeWithBiomeCheck", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/levelgen/placement/PlacedFeature;placeWithContext(Lnet/minecraft/world/level/levelgen/placement/PlacementContext;Lnet/minecraft/util/RandomSource;Lnet/minecraft/core/BlockPos;)Z"))
    private boolean onPlaceWithBiome(PlacedFeature feature, PlacementContext context, RandomSource randomSource, BlockPos pos)
    {
        try
        {
            return placeWithContext(context, randomSource, pos);
        }
        catch (Exception e)
        {
            if (self.feature() instanceof Holder.Reference)
            {
                StructureEssentials.LOGGER.warn("Feature: " + ((Holder.Reference) self.feature()).key() + " errored during placement at " + pos);
                return false;
            }

            StructureEssentials.LOGGER.warn("Unkown feature" + self.feature() + " errored during placement at " + pos, e);
            return false;
        }
    }
}
