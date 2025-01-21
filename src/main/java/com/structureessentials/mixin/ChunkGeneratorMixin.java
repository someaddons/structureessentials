package com.structureessentials.mixin;

import com.structureessentials.StructureEssentials;
import net.minecraft.world.level.chunk.ChunkGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ChunkGenerator.class)
public class ChunkGeneratorMixin
{
    @ModifyVariable(method = "findNearestMapStructure", argsOnly = true, ordinal = 0, at = @At("HEAD"))
    private int setRadius(int org)
    {
        return Math.min(StructureEssentials.config.getCommonConfig().globalSearchRadius, org);
    }

    @ModifyVariable(method = "getNearestGeneratedStructure(Ljava/util/Set;Lnet/minecraft/world/level/LevelReader;Lnet/minecraft/world/level/StructureManager;IIIZJLnet/minecraft/world/level/levelgen/structure/placement/RandomSpreadStructurePlacement;)Lcom/mojang/datafixers/util/Pair;", argsOnly = true, ordinal = 2, at = @At("HEAD"))
    private static int setRadius2(int org)
    {
        return Math.min(StructureEssentials.config.getCommonConfig().globalSearchRadius, org);
    }
}
