package com.structureessentials.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceOrTagArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.structure.Structure;

import java.util.*;
import java.util.stream.Collectors;

import static com.structureessentials.StructureEssentials.MODID;

public class Command
{
    public LiteralArgumentBuilder<CommandSourceStack> build(CommandBuildContext buildContext)
    {
        return Commands.literal(MODID)
                .then(
                        Commands.literal("getBiomeTags")
                                .then(Commands.argument("biome", ResourceOrTagArgument.resourceOrTag(buildContext, Registries.BIOME))
                                        .executes(context ->
                                        {
                                            final ResourceKey<Biome>
                                                    biome = ResourceOrTagArgument.getResourceOrTag(context, "biome", Registries.BIOME).unwrap().left().get().key();
                                            List<TagKey<Biome>> biomeTags =
                                                    context.getSource().registryAccess().registry(Registries.BIOME).get().getHolder(biome).get().tags().collect(Collectors.toList());

                                            context.getSource().sendSuccess(Component.literal("Biome tags for: " + biome.location()).withStyle(ChatFormatting.GOLD), false);
                                            for (final TagKey<Biome> biomeTag : biomeTags)
                                            {
                                                context.getSource().sendSuccess(Component.literal("#" + biomeTag.location()), false);
                                            }

                                            return 1;
                                        })))
                .then(
                        Commands.literal("getBiomesForTag")
                                .then(Commands.argument("biome", ResourceOrTagArgument.resourceOrTag(buildContext, Registries.BIOME))
                                        .executes(context ->
                                        {
                                            final TagKey<Biome> biomeTag = ResourceOrTagArgument.getResourceOrTag(context, "biome", Registries.BIOME).unwrap().right().get().key();

                                            context.getSource().sendSuccess(Component.literal("Biomes for tag: " + biomeTag.location()).withStyle(ChatFormatting.GOLD), false);
                                            for (final Holder<Biome> biomeHolder : context.getSource().registryAccess().registry(Registries.BIOME).get().asHolderIdMap())
                                            {
                                                if (biomeHolder.is(biomeTag))
                                                {
                                                    context.getSource().sendSuccess(Component.literal("Biome: " + biomeHolder.unwrapKey().get().location()), false);
                                                }
                                            }

                                            return 1;
                                        })))
                .then(
                        Commands.literal("getStructuresNearby")
                                .requires(stack -> stack.hasPermission(2))
                                .executes(context ->
                                {
                                    final ServerLevel world = context.getSource().getLevel();
                                    final Map<Structure, LongSet> structures = new HashMap<>();

                                    final ChunkPos start = new ChunkPos(new BlockPos(context.getSource().getPosition()));
                                    for (int x = 0; x < 5; x++)
                                    {
                                        for (int z = 0; z < 5; z++)
                                        {
                                            for (final Map.Entry<Structure, LongSet> entry : world.structureManager().getAllStructuresAt(new BlockPos((start.x + x) << 4, 0, (start.z + z) << 4)).entrySet())
                                            {
                                                structures.computeIfAbsent(entry.getKey(), k -> new LongOpenHashSet(entry.getValue())).addAll(entry.getValue());
                                            }
                                        }
                                    }

                                    context.getSource().sendSuccess(Component.literal("Structures nearby: ").withStyle(ChatFormatting.GOLD), false);
                                    Map<BlockPos, String> structurePositions = new HashMap<>();
                                    for (Map.Entry<Structure, LongSet> structureEntry : structures.entrySet())
                                    {
                                        world.structureManager().fillStartsForStructure(structureEntry.getKey(), structureEntry.getValue(),
                                                structureStart ->
                                                {
                                                    structurePositions.put(structureStart.getBoundingBox().getCenter(), context.getSource().registryAccess().registry(Registries.STRUCTURE).get()
                                                            .getKey(structureEntry.getKey()).toString());
                                                }
                                        );
                                    }

                                    final List<Map.Entry<BlockPos, String>> sortedStructures = new ArrayList<>(structurePositions.entrySet());
                                    sortedStructures.sort(Comparator.comparingDouble(p -> p.getKey().distSqr(new BlockPos(context.getSource().getPosition()))));

                                    for (final Map.Entry<BlockPos, String> structureEntry : sortedStructures)
                                    {
                                        context.getSource().sendSuccess(Component.literal(structureEntry.getValue()).append(Component.literal(" " + structureEntry.getKey()).withStyle(ChatFormatting.YELLOW).withStyle(style ->
                                                {
                                                    return style.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                                                            "/tp " + structureEntry.getKey().getX() + " " + structureEntry.getKey().getY() + " " + structureEntry.getKey().getZ()));
                                                }
                                        )), false);
                                    }

                                    return 1;
                                }))
                .then(
                        Commands.literal("getSimilarForBiome")
                                .then(Commands.argument("biome", ResourceOrTagArgument.resourceOrTag(buildContext, Registries.BIOME))
                                        .executes(context ->
                                        {
                                            final ResourceKey<Biome>
                                                    biome = ResourceOrTagArgument.getResourceOrTag(context, "biome", Registries.BIOME).unwrap().left().get().key();
                                            final List<TagKey<Biome>> biomeTags =
                                                    context.getSource().registryAccess().registry(Registries.BIOME).get().getHolder(biome).get().tags().collect(Collectors.toList());

                                            final List<Holder<Biome>> similarBiomes = new ArrayList<>();

                                            for (final Holder<Biome> currentBiome : context.getSource().registryAccess().registry(Registries.BIOME).get().asHolderIdMap())
                                            {
                                                for (final TagKey<Biome> tag : biomeTags)
                                                {
                                                    if (currentBiome.is(tag))
                                                    {
                                                        similarBiomes.add(currentBiome);
                                                    }
                                                }
                                            }

                                            Map<Holder<Biome>, Integer> countMap = new HashMap<>();

                                            for (Holder<Biome> similarBiome : similarBiomes)
                                            {
                                                for (TagKey<Biome> similarBiomeTagKey : similarBiome.tags().toList())
                                                {
                                                    if (biomeTags.contains(similarBiomeTagKey))
                                                    {
                                                        countMap.put(similarBiome, countMap.getOrDefault(similarBiome, 0) + 2);
                                                    } else
                                                    {
                                                        countMap.put(similarBiome, countMap.getOrDefault(similarBiome, 0) - 1);
                                                    }
                                                }
                                            }

                                            final List<Map.Entry<Holder<Biome>, Integer>> sortedBiomeHolders = new ArrayList<>(countMap.entrySet());
                                            sortedBiomeHolders.sort(Comparator.comparingInt(e -> ((Map.Entry<Holder<Biome>, Integer>) e).getValue()).reversed());

                                            Map<TagKey<Biome>, Double> tagCountMap = new HashMap<>();
                                            for (int i = 0; i < sortedBiomeHolders.size(); i++)
                                            {
                                                double weight = ((sortedBiomeHolders.size() / 6d) - i) / (sortedBiomeHolders.size() / 6d);

                                                if (i > sortedBiomeHolders.size() / 6d)
                                                {
                                                    weight = -(i - sortedBiomeHolders.size() * (1 / 6d)) / (sortedBiomeHolders.size() * (5 / 6d));
                                                }


                                                Map.Entry<Holder<Biome>, Integer> biomeHolderEntry = sortedBiomeHolders.get(i);

                                                for (final TagKey<Biome> biomeHolderEntryTag : biomeHolderEntry.getKey().tags().toList())
                                                {
                                                    if (biomeTags.contains(biomeHolderEntryTag))
                                                    {
                                                        tagCountMap.put(biomeHolderEntryTag, tagCountMap.getOrDefault(biomeHolderEntryTag, 0d) + 1 * weight);
                                                    }
                                                }
                                            }

                                            context.getSource().sendSuccess(Component.literal("Similar biomes for: " + biome.location()).withStyle(ChatFormatting.GOLD), false);

                                            for (int i = 0; i < sortedBiomeHolders.size() && i < 10; i++)
                                            {

                                                context.getSource().sendSuccess(Component.literal("Weight:" + sortedBiomeHolders.get(i).getValue() + " Biome: " + sortedBiomeHolders.get(i).getKey().unwrap().left().get().location()), false);
                                            }

                                            final List<Map.Entry<TagKey<Biome>, Double>> sortedBiomeTagKeys = new ArrayList<>(tagCountMap.entrySet());
                                            sortedBiomeTagKeys.sort(Comparator.comparingDouble(e -> ((Map.Entry<TagKey<Biome>, Double>) e).getValue()).reversed());

                                            context.getSource().sendSuccess(Component.literal("Similar biome tags for: " + biome.location()).withStyle(ChatFormatting.GOLD), false);

                                            for (final Map.Entry<TagKey<Biome>, Double> tag : sortedBiomeTagKeys)
                                            {
                                                context.getSource().sendSuccess(Component.literal("Weight:" + Math.round(tag.getValue()) + " Tag: #" + tag.getKey().location()), false);
                                            }

                                            return 1;
                                        })));
    }
}
