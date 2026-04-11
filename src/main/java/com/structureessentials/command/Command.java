package com.structureessentials.command;

import com.cupboard.util.RegistryLookup;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.structureessentials.StructureEssentials;
import com.structureessentials.Timings;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.Music;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
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
                                RegistryLookup.getHolder(context.getSource().registryAccess(), Registries.BIOME, biome).tags().toList();

                            context.getSource().sendSystemMessage(Component.literal("Biome tags for: " + biome.location()).withStyle(ChatFormatting.GOLD));
                            for (final TagKey<Biome> biomeTag : biomeTags)
                            {
                                context.getSource().sendSystemMessage(Component.literal("#" + biomeTag.location()));
                            }

                            return 1;
                        })))
            .then(
                Commands.literal("showGenerationTimes")
                    .executes(context ->
                    {
                        int count = 0;
                        List<Map.Entry<ResourceLocation, Long>> sortedFeatures = new ArrayList<>(Timings.featureTimings.entrySet());
                        sortedFeatures.sort(Comparator.comparingLong(e -> ((Map.Entry<ResourceLocation, Long>) (e)).getValue()).reversed());
                        context.getSource().sendSystemMessage(Component.literal("Features timings:").withStyle(ChatFormatting.GOLD));
                        StructureEssentials.LOGGER.warn("Placed Feature timings in ms:");
                        for (final Map.Entry<ResourceLocation, Long> entry : sortedFeatures)
                        {
                            count++;

                            if (count < 5)
                            {
                                context.getSource()
                                    .sendSystemMessage(Component.literal("#:" + count + " id: " + entry.getKey() + " time: " + (entry.getValue() / 1000000))
                                        .withStyle(ChatFormatting.WHITE));
                            }

                            StructureEssentials.LOGGER.warn("#:" + count + " id: " + entry.getKey() + " time: " + (entry.getValue() / 100000));
                        }

                        count = 0;
                        List<Map.Entry<ResourceLocation, Long>> sortedStructures = new ArrayList<>(Timings.structureTimings.entrySet());
                        sortedStructures.sort(Comparator.comparingLong(e -> ((Map.Entry<ResourceLocation, Long>) (e)).getValue()).reversed());
                        context.getSource().sendSystemMessage(Component.literal("Structure timings:").withStyle(ChatFormatting.GOLD));
                        StructureEssentials.LOGGER.warn("Structure timings in ms:");
                        for (final Map.Entry<ResourceLocation, Long> entry : sortedStructures)
                        {
                            count++;

                            if (count < 5)
                            {
                                context.getSource()
                                    .sendSystemMessage(Component.literal("#:" + count + " id: " + entry.getKey() + " time: " + (entry.getValue() / 1000000))
                                        .withStyle(ChatFormatting.WHITE));
                            }

                            StructureEssentials.LOGGER.warn("#:" + count + " id: " + entry.getKey() + " time: " + (entry.getValue() / 100000));
                        }

                        return 1;
                    }))
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
                                    structurePositions.put(structureStart.getBoundingBox().getCenter(),
                                        RegistryLookup.getID(context.getSource().registryAccess(), Registries.STRUCTURE, structureEntry.getKey()).toString());
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

                            final Holder<Biome> holder = RegistryLookup.getHolder(context.getSource().registryAccess(), Registries.BIOME, biome);

                            var sortedBiomeHolders = getSimilarBiomesFor(holder, context.getSource().registryAccess());
                            var sortedBiomeTagKeys = getSimilarTagsFor(holder, context.getSource().registryAccess());

                            context.getSource().sendSystemMessage(Component.literal("Similar biome tags for: " + biome.location()).withStyle(ChatFormatting.GOLD));

                            for (int i = 0; i < sortedBiomeHolders.size() && i < 7; i++)
                            {
                                context.getSource()
                                    .sendSystemMessage(Component.literal(
                                        "Weight:" + sortedBiomeHolders.get(i).getValue() + " Biome: " + sortedBiomeHolders.get(i).getKey().unwrap().left().get().location()));
                            }

                            int count = 0;
                            for (final Map.Entry<TagKey<Biome>, Double> tag : sortedBiomeTagKeys)
                            {
                                count++;
                                context.getSource().sendSystemMessage(Component.literal("Weight:" + Math.round(tag.getValue()) + " Tag: #" + tag.getKey().location()));
                                if (count >= 7)
                                {
                                    break;
                                }
                            }

                            for (int i = 0; i < sortedBiomeHolders.size(); i++)
                            {
                                StructureEssentials.LOGGER.info(
                                    "Weight:" + sortedBiomeHolders.get(i).getValue() + " Biome: " + sortedBiomeHolders.get(i).getKey().unwrap().left().get().location());
                            }

                            for (final Map.Entry<TagKey<Biome>, Double> tag : sortedBiomeTagKeys)
                            {
                                StructureEssentials.LOGGER.info("Weight:" + Math.round(tag.getValue()) + " Tag: #" + tag.getKey().location());
                            }

                            return 1;
                        })));
    }

    public static Map<Holder<Biome>, List<Object2DoubleMap.Entry<Holder<Biome>>>> biomeScoreCache = new HashMap<>();

    public static List<Object2DoubleMap.Entry<Holder<Biome>>> getSimilarBiomesFor(final Holder<Biome> biomeHolder, final RegistryAccess registryAccess)
    {
        final var result = biomeScoreCache.get(biomeHolder);
        if (result != null)
        {
            return result;
        }

        final Set<Holder<Biome>> similarBiomes = new HashSet<>();
        final Set<TagKey<Biome>> biomeTags = biomeHolder.tags().collect(Collectors.toSet());

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

        Object2DoubleOpenHashMap<Holder<Biome>> countMap = new Object2DoubleOpenHashMap<>();
        for (Holder<Biome> similarBiome : similarBiomes)
        {
            if (similarBiome.equals(biomeHolder))
            {
                continue;
            }

            int matching = 0;

            for (TagKey<Biome> similarBiomeTagKey : similarBiome.tags().toList())
            {
                if (biomeTags.contains(similarBiomeTagKey))
                {
                    matching++;
                }
            }

            double matchPct = (double) matching / biomeTags.size();
            // Limit impact to 30% of tags
            matchPct = 0.7 + 0.3 * matchPct;
            countMap.put(similarBiome, matchPct);
        }

        final float orgTemperature = getAdjustedTemp(biomeHolder);
        final float downfall = biomeHolder.value().hasPrecipitation() ? biomeHolder.value().getModifiedClimateSettings().downfall() : 0.0f;
        final Biome.Precipitation orgPrecipitation =
            !biomeHolder.value().hasPrecipitation() ? Biome.Precipitation.NONE : orgTemperature >= 0.15F ? Biome.Precipitation.RAIN : Biome.Precipitation.SNOW;
        final String orgName = biomeHolder.unwrapKey().get().location().getPath().toString();
        final Optional<Music> orgMusic = biomeHolder.value().getModifiedSpecialEffects().getBackgroundMusic();
        final int orgSkyColor = biomeHolder.value().getModifiedSpecialEffects().getSkyColor();
        final Set<Holder<PlacedFeature>> orgFeatures = new HashSet<>();
        for (final HolderSet<PlacedFeature> featureSet : biomeHolder.value().getGenerationSettings().features())
        {
            for (final Holder<PlacedFeature> feature : featureSet)
            {
                orgFeatures.add(feature);
            }
        }

        final List<ConfiguredFeature<?, ?>> orgFlowerFeatures = biomeHolder.value().getGenerationSettings().getFlowerFeatures();
        final MobSpawnSettings orgMobSettings = biomeHolder.value().getMobSettings();

        for (ObjectIterator<Object2DoubleMap.Entry<Holder<Biome>>> iterator = countMap.object2DoubleEntrySet().iterator(); iterator.hasNext(); )
        {
            final Object2DoubleMap.Entry<Holder<Biome>> ratedHolderEntry = iterator.next();
            if (ratedHolderEntry.getKey().equals(biomeHolder))
            {
                continue;
            }

            final Holder<Biome> ratedBiomeHolder = ratedHolderEntry.getKey();

            double modifier = 1.0;

            // Downfall 0.0 -> 1.0, indicates biome humidity. Over 0.85 is humid
            final float temp = getAdjustedTemp(ratedBiomeHolder);
            final Biome.Precipitation precipitation =
                !ratedBiomeHolder.value().hasPrecipitation() ? Biome.Precipitation.NONE : temp >= 0.15F ? Biome.Precipitation.RAIN : Biome.Precipitation.SNOW;
            if (orgPrecipitation != precipitation)
            {
                modifier *= 0.7;
            }
            else
            {
                final float downFallDiff =
                    Math.abs(downfall - (ratedBiomeHolder.value().hasPrecipitation() ? biomeHolder.value().getModifiedClimateSettings().downfall() : 0.0f));
                modifier *= (1.0 - (0.1 * downFallDiff));
                final float tempDiff = Math.abs(orgTemperature - getAdjustedTemp(ratedBiomeHolder));
                modifier *= (1.0 - Math.min(0.1, tempDiff * 0.1));
            }

            if (ratedBiomeHolder.toString().contains(orgName))
            {
                modifier *= 1.2;
            }

            if (orgMusic.isPresent())
            {
                if (ratedBiomeHolder.value().getModifiedSpecialEffects().getBackgroundMusic().isPresent() && ratedBiomeHolder.value()
                    .getModifiedSpecialEffects()
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

            if (ratedBiomeHolder.value().getModifiedSpecialEffects().getSkyColor() != orgSkyColor)
            {
                modifier *= 0.9;
            }

            int matchingFeatures = 0;
            int totalFeatures = 0;
            for (final HolderSet<PlacedFeature> featureSet : ratedBiomeHolder.value().getGenerationSettings().features())
            {
                for (final Holder<PlacedFeature> feature : featureSet)
                {
                    totalFeatures++;
                    if (orgFeatures.contains(feature))
                    {
                        matchingFeatures++;
                    }
                }
            }

            int missingFeatures = orgFeatures.size() - matchingFeatures;
            int additionalFeatures = totalFeatures - missingFeatures;
            modifier *= (1.0 - Math.min(0.1, (0.1 * ((double) (missingFeatures * 3) / Math.max(1, orgFeatures.size())))));
            modifier *= (1.0 - (0.05 * ((double) additionalFeatures / Math.max(10, orgFeatures.size()))));

            if (!orgFlowerFeatures.isEmpty())
            {
                int missingFlowerFeatures = 0;
                for (final ConfiguredFeature<?, ?> feature : orgFlowerFeatures)
                {
                    boolean foundFlowerFeature = false;
                    for (final var existing : ratedBiomeHolder.value().getGenerationSettings().getFlowerFeatures())
                    {
                        if (existing.equals(feature))
                        {
                            foundFlowerFeature = true;
                            break;
                        }
                    }

                    if (!foundFlowerFeature)
                    {
                        missingFlowerFeatures++;
                    }
                }
                modifier *= (1.0 - (0.1 * ((double) missingFlowerFeatures / orgFlowerFeatures.size())));
            }

            if (!orgMobSettings.getEntityTypes().isEmpty())
            {
                int missingMobs = 0;
                for (final var type : orgMobSettings.getEntityTypes())
                {
                    if (!ratedBiomeHolder.value().getMobSettings().getEntityTypes().contains(type))
                    {
                        missingMobs++;
                    }
                }

                modifier *= (1.0 - (0.2 * ((double) missingMobs / orgMobSettings.getEntityTypes().size())));
            }

            if (Math.abs(orgMobSettings.getCreatureProbability() - ratedBiomeHolder.value().getMobSettings().getCreatureProbability()) > 0.1)
            {
                modifier *= 0.9;
            }

            ratedHolderEntry.setValue((ratedHolderEntry.getDoubleValue() * modifier));
        }

        final List<Object2DoubleMap.Entry<Holder<Biome>>> sortedBiomeHolders = new ArrayList<>(countMap.object2DoubleEntrySet());
        sortedBiomeHolders.sort(Comparator.comparingDouble(e -> ((Object2DoubleMap.Entry<Holder<Biome>>) e).getDoubleValue()).reversed());
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
        List<Object2DoubleMap.Entry<Holder<Biome>>> sortedBiomeHolders = getSimilarBiomesFor(biomeHolder, registryAccess);

        Map<TagKey<Biome>, Double> tagCountMap = new HashMap<>();

        int biomeCount = sortedBiomeHolders.size();
        for (int i = 0; i < sortedBiomeHolders.size(); i++)
        {
            double weight = ((biomeCount / 6d) - i) / (biomeCount / 6d);

            if (i > biomeCount / 6d)
            {
                weight = -(i - biomeCount * (1 / 6d)) / (biomeCount * (5 / 6d));
            }

            Map.Entry<Holder<Biome>, Double> biomeHolderEntry = sortedBiomeHolders.get(i);

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
