package com.structureessentials.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceOrTagArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.Music;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
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

                            context.getSource().sendSystemMessage(Component.literal("Biome tags for: " + biome.location()).withStyle(ChatFormatting.GOLD));
                            for (final TagKey<Biome> biomeTag : biomeTags)
                            {
                                context.getSource().sendSystemMessage(Component.literal("#" + biomeTag.location()));
                            }

                            return 1;
                        })))
            .then(
                Commands.literal("getBiomesForTag")
                    .then(Commands.argument("biome", ResourceOrTagArgument.resourceOrTag(buildContext, Registries.BIOME))
                        .executes(context ->
                        {
                            final TagKey<Biome> biomeTag = ResourceOrTagArgument.getResourceOrTag(context, "biome", Registries.BIOME).unwrap().right().get().key();

                            context.getSource().sendSystemMessage(Component.literal("Biomes for tag: " + biomeTag.location()).withStyle(ChatFormatting.GOLD));
                            for (final Holder<Biome> biomeHolder : context.getSource().registryAccess().registry(Registries.BIOME).get().asHolderIdMap())
                            {
                                if (biomeHolder.is(biomeTag))
                                {
                                    context.getSource().sendSystemMessage(Component.literal("Biome: " + biomeHolder.unwrapKey().get().location()));
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

                        final ChunkPos start = new ChunkPos(BlockPos.containing(context.getSource().getPosition()));
                        for (int x = -5; x < 5; x++)
                        {
                            for (int z = -5; z < 5; z++)
                            {
                                for (final Map.Entry<Structure, LongSet> entry : world.structureManager()
                                    .getAllStructuresAt(new BlockPos((start.x + x) << 4, 0, (start.z + z) << 4))
                                    .entrySet())
                                {
                                    structures.computeIfAbsent(entry.getKey(), k -> new LongOpenHashSet(entry.getValue())).addAll(entry.getValue());
                                }
                            }
                        }

                        context.getSource().sendSystemMessage(Component.literal("Structures nearby: ").withStyle(ChatFormatting.GOLD));
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
                        sortedStructures.sort(Comparator.comparingDouble(p -> p.getKey().distSqr(BlockPos.containing(context.getSource().getPosition()))));

                        for (final Map.Entry<BlockPos, String> structureEntry : sortedStructures)
                        {
                            context.getSource()
                                .sendSystemMessage(Component.literal(structureEntry.getValue())
                                    .append(Component.literal(" " + structureEntry.getKey()).withStyle(ChatFormatting.YELLOW).withStyle(style ->
                                        {
                                            return style.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                                                "/tp " + structureEntry.getKey().getX() + " " + structureEntry.getKey().getY() + " " + structureEntry.getKey().getZ()));
                                        }
                                    )));
                        }

                        return 1;
                    }))
            .then(
                Commands.literal("getSimilarForBiome")
                    .then(Commands.argument("biome", ResourceOrTagArgument.resourceOrTag(buildContext, Registries.BIOME))
                        .executes(context ->
                        {
                            Command.biomeScoreCache.clear();
                            final ResourceKey<Biome>
                                biome = ResourceOrTagArgument.getResourceOrTag(context, "biome", Registries.BIOME).unwrap().left().get().key();

                            final Holder<Biome> holder = context.getSource().registryAccess().registry(Registries.BIOME).get().getHolder(biome).get();

                            var sortedBiomeHolders = getSimilarBiomesFor(holder, context.getSource().registryAccess());
                            var sortedBiomeTagKeys = getSimilarTagsFor(holder, context.getSource().registryAccess());

                            context.getSource().sendSystemMessage(Component.literal("Similar biome tags for: " + biome.location()).withStyle(ChatFormatting.GOLD));

                            for (int i = 0; i < sortedBiomeHolders.size() && i < 10; i++)
                            {

                                context.getSource()
                                    .sendSystemMessage(Component.literal(
                                        "Weight:" + sortedBiomeHolders.get(i).getValue() + " Biome: " + sortedBiomeHolders.get(i).getKey().unwrap().left().get().location()));
                            }

                            for (final Map.Entry<TagKey<Biome>, Double> tag : sortedBiomeTagKeys)
                            {
                                context.getSource().sendSystemMessage(Component.literal("Weight:" + Math.round(tag.getValue()) + " Tag: #" + tag.getKey().location()));
                            }

                            return 1;
                        })));
    }

    public static Map<Holder<Biome>, List<Object2IntMap.Entry<Holder<Biome>>>> biomeScoreCache = new HashMap<>();

    public static List<Object2IntMap.Entry<Holder<Biome>>> getSimilarBiomesFor(final Holder<Biome> biomeHolder, final RegistryAccess registryAccess)
    {
        final var result = biomeScoreCache.get(biomeHolder);
        if (result != null)
        {
            return result;
        }

        final List<Holder<Biome>> similarBiomes = new ArrayList<>();
        final List<TagKey<Biome>> biomeTags = biomeHolder.tags().collect(Collectors.toList());

        for (final Holder<Biome> currentBiome : registryAccess.registry(Registries.BIOME).get().asHolderIdMap())
        {
            for (final TagKey<Biome> tag : biomeTags)
            {
                if (currentBiome.is(tag))
                {
                    similarBiomes.add(currentBiome);
                }
            }
        }

        Object2IntOpenHashMap<Holder<Biome>> countMap = new Object2IntOpenHashMap<>();

        for (Holder<Biome> similarBiome : similarBiomes)
        {
            for (TagKey<Biome> similarBiomeTagKey : similarBiome.tags().toList())
            {
                if (biomeTags.contains(similarBiomeTagKey))
                {
                    countMap.put(similarBiome, countMap.getOrDefault(similarBiome, 0) + 2);
                }
                else
                {
                    countMap.put(similarBiome, countMap.getOrDefault(similarBiome, 0) - 1);
                }
            }
        }

        final float orgTemperature = getAdjustedTemp(biomeHolder);
        final float downfall = biomeHolder.value().hasPrecipitation() ? biomeHolder.value().climateSettings.downfall() : 0.0f;
        final String orgName = biomeHolder.unwrapKey().get().location().getPath().toString();
        final Optional<Music> orgMusic = biomeHolder.value().getSpecialEffects().getBackgroundMusic();
        final int orgSkyColor = biomeHolder.value().getSpecialEffects().getSkyColor();
        final int orgFeatureCount = biomeHolder.value().getGenerationSettings().features().size();
        final List<HolderSet<PlacedFeature>> orgFeatures = biomeHolder.value().getGenerationSettings().features();


        for (final Object2IntMap.Entry<Holder<Biome>> ratedHolderEntry : countMap.object2IntEntrySet())
        {
            if (ratedHolderEntry.getKey().equals(biomeHolder))
            {
                continue;
            }

            final Holder<Biome> ratedBiomeHolder = ratedHolderEntry.getKey();
            double modifier = 1.0;
            final float downFallDiff =
                Math.abs(downfall - (ratedBiomeHolder.value().hasPrecipitation() ? biomeHolder.value().climateSettings.downfall() : 0.0f));
            modifier *= 1.0 - Math.min(0.2, downFallDiff);
            final float tempDiff = Math.abs(orgTemperature - getAdjustedTemp(ratedBiomeHolder));
            modifier *= 1.0 - Math.min(0.5, tempDiff / 2);

            if (ratedBiomeHolder.toString().contains(orgName))
            {
                modifier *= 1.3;
            }

            if (orgMusic.isPresent())
            {
                if (ratedBiomeHolder.value().getSpecialEffects().getBackgroundMusic().isPresent() && ratedBiomeHolder.value()
                    .getSpecialEffects()
                    .getBackgroundMusic()
                    .get()
                    .getEvent()
                    .equals(orgMusic.get().getEvent()))
                {
                    modifier *= 1.2;
                }
                else
                {
                    modifier *= 0.9;
                }
            }

            if (ratedBiomeHolder.value().getSpecialEffects().getSkyColor() != orgSkyColor)
            {
                modifier *= 0.9;
            }

            if (orgFeatureCount > 7 && ratedBiomeHolder.value().getGenerationSettings().features().size() <= 2)
            {
                modifier *= 0.7;
            }
            else if (orgFeatureCount > 2 && ratedBiomeHolder.value().getGenerationSettings().features().size() > 2)
            {
                int missing = 0;

                final var ratedFeatures = ratedBiomeHolder.value().getGenerationSettings().features().get(ratedBiomeHolder.value().getGenerationSettings().features().size() - 2);
                for (final Holder<PlacedFeature> feature : orgFeatures.get(orgFeatureCount - 2))
                {
                    if (!ratedFeatures.contains(feature))
                    {
                        missing++;
                    }
                }

                modifier *= 1.0 - (missing * 0.02);
            }

            ratedHolderEntry.setValue((int) (ratedHolderEntry.getIntValue() * modifier));
        }

        final int ownerScore = countMap.removeInt(biomeHolder);
        final List<Object2IntMap.Entry<Holder<Biome>>> sortedBiomeHolders = new ArrayList<>(countMap.object2IntEntrySet());
        sortedBiomeHolders.sort(Comparator.comparingInt(e -> ((Object2IntMap.Entry<Holder<Biome>>) e).getIntValue()).reversed());
        sortedBiomeHolders.add(0, new CustomEntry(biomeHolder, ownerScore));
        biomeScoreCache.put(biomeHolder, sortedBiomeHolders);
        return sortedBiomeHolders;
    }

    private static class CustomEntry implements Object2IntMap.Entry<Holder<Biome>>
    {
        final Holder<Biome> holder;
        final int           value;

        private CustomEntry(final Holder<Biome> holder, final int value)
        {
            this.holder = holder;
            this.value = value;
        }

        @Override
        public int getIntValue()
        {
            return value;
        }

        @Override
        public int setValue(final int value)
        {
            throw new RuntimeException("Not Allowed to set value");
        }

        @Override
        public Holder<Biome> getKey()
        {
            return holder;
        }
    }

    public static List<Map.Entry<TagKey<Biome>, Double>> getSimilarTagsFor(final Holder<Biome> biomeHolder, final RegistryAccess registryAccess)
    {
        final List<TagKey<Biome>> biomeTags = biomeHolder.tags().collect(Collectors.toList());
        List<Object2IntMap.Entry<Holder<Biome>>> sortedBiomeHolders = getSimilarBiomesFor(biomeHolder, registryAccess);

        Map<TagKey<Biome>, Double> tagCountMap = new HashMap<>();

        int biomeCount = sortedBiomeHolders.size();
        for (int i = 0; i < sortedBiomeHolders.size(); i++)
        {
            double weight = ((biomeCount / 6d) - i) / (biomeCount / 6d);

            if (i > biomeCount / 6d)
            {
                weight = -(i - biomeCount * (1 / 6d)) / (biomeCount * (5 / 6d));
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

        final List<Map.Entry<TagKey<Biome>, Double>> sortedBiomeTagKeys = new ArrayList<>(tagCountMap.entrySet());
        sortedBiomeTagKeys.sort(Comparator.comparingDouble(e -> ((Map.Entry<TagKey<Biome>, Double>) e).getValue()).reversed());

        return sortedBiomeTagKeys;
    }

    public static float getAdjustedTemp(final Holder<Biome> holder)
    {
        final Biome biome = holder.value();
        final float temp = biome.getBaseTemperature();
        if (temp == 0.5f || temp == 0.8f)
        {
            final String biomeString = holder.unwrapKey().get().location().toString();
            if (biomeString.contains("hot") || biomeString.contains("warm") || biomeString.contains("desert"))
            {
                return 0.95f;
            }

            if (biomeString.contains("snowy") || biomeString.contains("cold") || biomeString.contains("frozen") || biomeString.contains("ice"))
            {
                return 0f;
            }

            return temp;
        }
        return temp;
    }
}
