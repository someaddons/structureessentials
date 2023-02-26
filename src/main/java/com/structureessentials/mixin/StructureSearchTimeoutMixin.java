package com.structureessentials.mixin;

import com.mojang.datafixers.util.Pair;
import com.structureessentials.StructureEssentials;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.placement.ConcentricRingsStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;

@Mixin(ChunkGenerator.class)
public class StructureSearchTimeoutMixin
{
    @Unique
    private long time = 0;

    @Inject(method = "getNearestGeneratedStructure(Ljava/util/Set;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/level/StructureManager;Lnet/minecraft/core/BlockPos;ZLnet/minecraft/world/level/levelgen/structure/placement/ConcentricRingsStructurePlacement;)Lcom/mojang/datafixers/util/Pair;"
            , at = @At("HEAD"))
    private void onSearchStart(Set<Holder<Structure>> p_223182_, ServerLevel p_223183_, StructureManager p_223184_, BlockPos p_223185_, boolean p_223186_, ConcentricRingsStructurePlacement p_223187_, CallbackInfoReturnable<Pair<BlockPos, Holder<Structure>>> cir)
    {
        time = System.currentTimeMillis();
    }

    @Inject(method = "getNearestGeneratedStructure(Ljava/util/Set;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/level/StructureManager;Lnet/minecraft/core/BlockPos;ZLnet/minecraft/world/level/levelgen/structure/placement/ConcentricRingsStructurePlacement;)Lcom/mojang/datafixers/util/Pair;"
            , at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/chunk/ChunkGenerator;getStructureGeneratingAt(Ljava/util/Set;Lnet/minecraft/world/level/LevelReader;Lnet/minecraft/world/level/StructureManager;ZLnet/minecraft/world/level/levelgen/structure/placement/StructurePlacement;Lnet/minecraft/world/level/ChunkPos;)Lcom/mojang/datafixers/util/Pair;"), cancellable = true)
    private void onSearchTiming(Set<Holder<Structure>> holderSet, ServerLevel p_223183_, StructureManager p_223184_, BlockPos p_223185_, boolean p_223186_, ConcentricRingsStructurePlacement p_223187_, CallbackInfoReturnable<Pair<BlockPos, Holder<Structure>>> cir)
    {
        if (time != 0 && System.currentTimeMillis() - time > StructureEssentials.config.getCommonConfig().structureSearchTimeout * 1000L)
        {
            StructureEssentials.LOGGER.info("Structure searched for " + getStructurename(holderSet) + " timed out, took: " + (System.currentTimeMillis() - time) / 1000 + " seconds.");
            cir.setReturnValue(null);
        }
    }

    @Unique
    private static long staticTime = 0;

    @Inject(method = "getNearestGeneratedStructure(Ljava/util/Set;Lnet/minecraft/world/level/LevelReader;Lnet/minecraft/world/level/StructureManager;IIIZJLnet/minecraft/world/level/levelgen/structure/placement/RandomSpreadStructurePlacement;)Lcom/mojang/datafixers/util/Pair;"
            , at = @At("HEAD"))
    private static void onSearchStartStatic(Set<Holder<Structure>> p_223189_, LevelReader p_223190_, StructureManager p_223191_, int p_223192_, int p_223193_, int p_223194_, boolean p_223195_, long p_223196_, RandomSpreadStructurePlacement p_223197_, CallbackInfoReturnable<Pair<BlockPos, Holder<Structure>>> cir)
    {
        staticTime = System.currentTimeMillis();
    }

    @Inject(method = "getNearestGeneratedStructure(Ljava/util/Set;Lnet/minecraft/world/level/LevelReader;Lnet/minecraft/world/level/StructureManager;IIIZJLnet/minecraft/world/level/levelgen/structure/placement/RandomSpreadStructurePlacement;)Lcom/mojang/datafixers/util/Pair;"
            , at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/chunk/ChunkGenerator;getStructureGeneratingAt(Ljava/util/Set;Lnet/minecraft/world/level/LevelReader;Lnet/minecraft/world/level/StructureManager;ZLnet/minecraft/world/level/levelgen/structure/placement/StructurePlacement;Lnet/minecraft/world/level/ChunkPos;)Lcom/mojang/datafixers/util/Pair;"))
    private static void onSearchStartStaticTiming(Set<Holder<Structure>> holderSet, LevelReader p_223190_, StructureManager p_223191_, int p_223192_, int p_223193_, int p_223194_, boolean p_223195_, long p_223196_, RandomSpreadStructurePlacement p_223197_, CallbackInfoReturnable<Pair<BlockPos, Holder<Structure>>> cir)
    {
        if (staticTime != 0 && System.currentTimeMillis() - staticTime > StructureEssentials.config.getCommonConfig().structureSearchTimeout * 1000L)
        {
            StructureEssentials.LOGGER.info("Structure searched for " + getStructurename(holderSet) + " timed out, took: " + (System.currentTimeMillis() - staticTime) / 1000 + " seconds.");
            cir.setReturnValue(null);
        }
    }

    @Unique
    private static String getStructurename(Set<Holder<Structure>> holderSet)
    {
        for (final Holder<Structure> holder : holderSet)
        {
            if (holder.unwrapKey().isPresent())
            {
                return holder.unwrapKey().get().location().toString();
            }
        }

        return "unkown structure";
    }
}
