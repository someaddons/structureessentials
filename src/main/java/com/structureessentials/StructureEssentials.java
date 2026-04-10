package com.structureessentials;

import com.cupboard.util.RegistryLookup;
import com.cupboard.util.ResourceLocation;
import com.structureessentials.command.Command;
import com.structureessentials.config.CommonConfiguration;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static net.minecraft.world.level.biome.Biomes.SMALL_END_ISLANDS;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(StructureEssentials.MODID)
public class StructureEssentials
{
    public static final String MODID  = "structureessentials";
    public static final Logger LOGGER = LogManager.getLogger();
    public static       Random rand   = new Random();

    public StructureEssentials(IEventBus modEventBus, ModContainer modContainer)
    {
        modEventBus.addListener(this::setup);
        NeoForge.EVENT_BUS.addListener(this::commandRegister);
    }

    @SubscribeEvent
    public void commandRegister(RegisterCommandsEvent event)
    {
        event.getDispatcher().register(new Command().build(event.getBuildContext()));
    }

    private void setup(final FMLCommonSetupEvent event)
    {
        LOGGER.info(MODID + " mod initialized");
    }

    public static void onServerStart
        (
            MinecraftServer server
        )
    {
        Timings.featureTimings = new ConcurrentHashMap<>();
        Timings.structureTimings = new ConcurrentHashMap<>();

        final RegistryAccess.Frozen registryAccess = server.registryAccess();
        List<Holder<Structure>> holders = RegistryLookup.getHolders(registryAccess,Registries.STRUCTURE);
        final Registry<Biome> biomeRegistry = RegistryLookup.getRegistry(registryAccess,Registries.BIOME);

        final Registry<StructureSet> structureSetRegistry = RegistryLookup.getRegistry(registryAccess,Registries.STRUCTURE_SET);

        if (CommonConfiguration.config.getCommonConfig().logDuplicatedSalt)
        {
            Int2ObjectOpenHashMap<Set<String>> structureSetIds = new Int2ObjectOpenHashMap();
            for (final Map.Entry<ResourceKey<StructureSet>, StructureSet> entry : structureSetRegistry.entrySet())
            {
                final int salt = entry.getValue().placement().salt();
                structureSetIds.putIfAbsent(salt, new HashSet<>());
                structureSetIds.get(salt).add(entry.getKey().identifier().toString());
            }

            for (final Int2ObjectMap.Entry<Set<String>> entry : structureSetIds.int2ObjectEntrySet())
            {
                if (entry.getValue().size() > 1)
                {
                    StructureEssentials.LOGGER.warn(
                        "Non-unique structure_set salt:" + entry.getIntKey() + " potentially creating overlapping structures detected. Structure sets: " + entry.getValue());
                }
            }
        }

        if (!CommonConfiguration.config.getCommonConfig().autoBiomeCompat)
        {
            return;
        }

        final Map<ResourceLocation, TagKey<Biome>> directReplacementTags = new HashMap<>();
        directReplacementTags.put(ResourceLocation.withDefaultNamespace("deep_ocean"), BiomeTags.IS_DEEP_OCEAN);
        directReplacementTags.put(ResourceLocation.withDefaultNamespace("ocean"), BiomeTags.IS_OCEAN);
        directReplacementTags.put(ResourceLocation.withDefaultNamespace("river"), BiomeTags.IS_RIVER);
        directReplacementTags.put(ResourceLocation.withDefaultNamespace("badlands"), BiomeTags.IS_BADLANDS);
        directReplacementTags.put(ResourceLocation.withDefaultNamespace("eroded_badlands"), BiomeTags.IS_BADLANDS);
        directReplacementTags.put(ResourceLocation.withDefaultNamespace("wooded_badlands"), BiomeTags.IS_BADLANDS);
        directReplacementTags.put(ResourceLocation.withDefaultNamespace("windswept_hills"), BiomeTags.IS_HILL);
        directReplacementTags.put(ResourceLocation.withDefaultNamespace("windswept_gravelly_hills"), BiomeTags.IS_HILL);
        directReplacementTags.put(ResourceLocation.withDefaultNamespace("taiga"), BiomeTags.IS_TAIGA);
        directReplacementTags.put(ResourceLocation.withDefaultNamespace("jungle"), BiomeTags.IS_JUNGLE);
        directReplacementTags.put(ResourceLocation.withDefaultNamespace("forest"), BiomeTags.IS_FOREST);
        directReplacementTags.put(ResourceLocation.withDefaultNamespace("savanna"), BiomeTags.IS_SAVANNA);
        directReplacementTags.put(ResourceLocation.withDefaultNamespace("deep_dark"), BiomeTags.HAS_ANCIENT_CITY);

        directReplacementTags.put(ResourceLocation.withDefaultNamespace("plains"), Tags.Biomes.IS_PLAINS);
        directReplacementTags.put(ResourceLocation.withDefaultNamespace("snowy_plains"), Tags.Biomes.IS_SNOWY_PLAINS);
        directReplacementTags.put(ResourceLocation.withDefaultNamespace("desert"), Tags.Biomes.IS_DESERT);
        directReplacementTags.put(ResourceLocation.withDefaultNamespace("swamp"), Tags.Biomes.IS_SWAMP);
        directReplacementTags.put(ResourceLocation.withDefaultNamespace("flower_forest"), Tags.Biomes.IS_FLOWER_FOREST);
        directReplacementTags.put(ResourceLocation.withDefaultNamespace("birch_forest"), Tags.Biomes.IS_BIRCH_FOREST);
        directReplacementTags.put(ResourceLocation.withDefaultNamespace("old_growth_birch_forest"), Tags.Biomes.IS_OLD_GROWTH);
        directReplacementTags.put(ResourceLocation.withDefaultNamespace("old_growth_pine_taiga"), Tags.Biomes.IS_OLD_GROWTH);
        directReplacementTags.put(ResourceLocation.withDefaultNamespace("old_growth_spruce_taiga"), Tags.Biomes.IS_OLD_GROWTH);
        directReplacementTags.put(ResourceLocation.withDefaultNamespace("stony_shore"), Tags.Biomes.IS_STONY_SHORES);
        directReplacementTags.put(ResourceLocation.withDefaultNamespace("mushroom_fields"), Tags.Biomes.IS_MUSHROOM);
        directReplacementTags.put(ResourceLocation.withDefaultNamespace("lush_caves"), Tags.Biomes.IS_LUSH);
        directReplacementTags.put(ResourceLocation.withDefaultNamespace("warped_forest"), Tags.Biomes.IS_NETHER_FOREST);
        directReplacementTags.put(ResourceLocation.withDefaultNamespace("crimson_forest"), Tags.Biomes.IS_NETHER_FOREST);

        Set<TagKey<Biome>> DEFINING_TAGS =
            Set.of(BiomeTags.IS_RIVER,
                BiomeTags.IS_DEEP_OCEAN,
                BiomeTags.IS_OCEAN,
                BiomeTags.IS_BEACH,
                BiomeTags.IS_BADLANDS,
                BiomeTags.IS_SAVANNA,
                BiomeTags.IS_TAIGA,
                BiomeTags.IS_JUNGLE,
                BiomeTags.IS_FOREST,
                BiomeTags.IS_MOUNTAIN,
                createBiomeTag("forge", "is_cave"),
                createBiomeTag("forge", "is_plains"),
                createBiomeTag("forge", "is_desert"),
                createBiomeTag("forge", "is_snowy"),
                createBiomeTag("forge", "is_lush"),
                createBiomeTag("forge", "is_dead"),
                createBiomeTag("forge", "is_underground"),
                createBiomeTag("forge", "is_void"),
                createBiomeTag("forge", "is_sandy"),
                createBiomeTag("forge", "is_swamp"),
                createBiomeTag("c", "no_default_monsters"),
                createBiomeTag("c", "is_void"),
                createBiomeTag("c", "is_dense_vegetation"),
                createBiomeTag("c", "is_sparse_vegetation"),
                createBiomeTag("c", "is_plains"),
                createBiomeTag("c", "is_windswept"),
                createBiomeTag("c", "is_shallow_ocean"),
                createBiomeTag("c", "is_underground"),
                createBiomeTag("c", "is_cave"),
                createBiomeTag("c", "is_lush"),
                createBiomeTag("c", "is_dead"),
                createBiomeTag("c", "is_sandy"),
                createBiomeTag("c", "is_snowy"),
                createBiomeTag("c", "is_icy"),
                createBiomeTag("c", "is_swamp"),
                createBiomeTag("c", "is_aquatic")
            );

        for (final Holder<Structure> holder : holders)
        {
            LinkedHashSet<Holder<Biome>> biomeHolderSet = new LinkedHashSet<>(holder.value().biomes().size());
            float minTemp = 1000;
            float maxTemp = -1000;

            float maxDownfall = -1000;
            float minDownfall = 1000;

            Object2IntOpenHashMap<TagKey<Biome>> allowedDefiningTags = new Object2IntOpenHashMap<>();

            for (Holder<Biome> biome : holder.value().biomes())
            {
                if (!holder.isBound())
                {
                    continue;
                }

                biomeHolderSet.add(biome);
                float temp = Command.getAdjustedTemp(biome);
                if (temp < minTemp)
                {
                    minTemp = temp;
                }

                if (temp > maxTemp)
                {
                    maxTemp = temp;
                }

                final float downFall = biome.value().getModifiedClimateSettings().downfall();
                if (downFall < minDownfall)
                {
                    minDownfall = downFall;
                }

                if (downFall > maxDownfall)
                {
                    maxDownfall = downFall;
                }

                for (final TagKey<Biome> biomeTagKey : DEFINING_TAGS)
                {
                    if (biome.is(biomeTagKey))
                    {
                        allowedDefiningTags.put(biomeTagKey, allowedDefiningTags.getOrDefault(biomeTagKey, 0) + 1);
                    }
                }
            }

            Set<TagKey<Biome>> deniedDefiningSet = new HashSet<>();

            for (final TagKey<Biome> biomeTagKey : DEFINING_TAGS)
            {
                if (!allowedDefiningTags.containsKey(biomeTagKey))
                {
                    deniedDefiningSet.add(biomeTagKey);
                }
            }

            Set<TagKey<Biome>> requiredDefiningSet = new HashSet<>();

            for (final var entry : allowedDefiningTags.object2IntEntrySet())
            {
                if (entry.getIntValue() == holder.value().biomes().size())
                {
                    requiredDefiningSet.add(entry.getKey());
                }
            }

            if (biomeHolderSet.isEmpty())
            {
                continue;
            }

            minTemp -= 0.35f;
            maxTemp += 0.35f;
            minDownfall -= 0.35f;
            maxDownfall += 0.35f;

            final Map<ServerLevel, Set<Holder<Biome>>> allowedDimensions = new HashMap<>();
            for (final var level : server.getAllLevels())
            {
                final Set<Holder<Biome>> dimensionbiomes = level.getChunkSource().getGenerator().getBiomeSource().possibleBiomes();
                allowedDimensions.put(level, dimensionbiomes);
            }

            for (Iterator<Map.Entry<ServerLevel, Set<Holder<Biome>>>> iterator = allowedDimensions.entrySet().iterator(); iterator.hasNext(); )
            {
                final var dimensionBiomes = iterator.next();

                boolean contained = false;
                for (final var biomeHolder : biomeHolderSet)
                {
                    if (dimensionBiomes.getValue().contains(biomeHolder))
                    {
                        contained = true;
                        break;
                    }
                }

                if (!contained || !CommonConfiguration.config.getCommonConfig().dimensionWhitelist.contains(dimensionBiomes.getKey().dimension().identifier().toString()))
                {
                    iterator.remove();
                }
            }

            Set<TagKey<Biome>> addedTags = new HashSet<>(requiredDefiningSet);
            Set<Holder<Biome>> addedTagBiomes = new HashSet<>();
            Set<Holder<Biome>> toAdd = new HashSet<>();

            for (final TagKey<Biome> biomeTag : requiredDefiningSet)
            {
                for (final Holder<Biome> tagBiome : RegistryLookup.getHolders(registryAccess, Registries.BIOME,biomeTag))
                {
                    if (!biomeHolderSet.contains(tagBiome))
                    {
                        toAdd.add(tagBiome);
                    }
                }
            }

            for (Holder<Biome> biome : biomeHolderSet)
            {
                final TagKey<Biome> tag = directReplacementTags.get(biomeRegistry.getKey(biome.value()));
                // Check tag and if applicable add all its values
                if (tag != null && !addedTags.contains(tag) && biome.is(tag))
                {
                    for (final Holder<Biome> tagBiome : RegistryLookup.getHolders(registryAccess, Registries.BIOME,tag))
                    {
                        if (biomeHolderSet.contains(tagBiome))
                        {
                            continue;
                        }

                        toAdd.add(tagBiome);
                        addedTagBiomes.add(tagBiome);
                    }
                    addedTags.add(tag);
                }
            }

            Object2DoubleOpenHashMap<Holder<Biome>> highScoreBiomes = new Object2DoubleOpenHashMap<>();
            Object2DoubleOpenHashMap<Holder<Biome>> potentialBiomes = new Object2DoubleOpenHashMap<>();
            for (Holder<Biome> biome : biomeHolderSet)
            {
                final List<Object2DoubleMap.Entry<Holder<Biome>>> similar = Command.getSimilarBiomesFor(biome, registryAccess);

                for (int i = 0; i < similar.size() && i < 200; i++)
                {
                    final Object2DoubleMap.Entry<Holder<Biome>> entry = similar.get(i);
                    if (biomeHolderSet.contains(entry.getKey()))
                    {
                        continue;
                    }

                    double percent = entry.getDoubleValue();
                    double previousValue = potentialBiomes.getOrDefault(entry.getKey(), 0);
                    if (percent >= (toAdd.contains(entry.getKey()) ? 0.85 * 0.85 : 0.85) * CommonConfiguration.config.getCommonConfig().autoBiomeCompatStrictness)
                    {
                        highScoreBiomes.put(entry.getKey(), percent);
                    }

                    potentialBiomes.put(entry.getKey(), previousValue + percent);
                }
            }

            for (final var entry : potentialBiomes.object2DoubleEntrySet())
            {
                entry.setValue(entry.getDoubleValue() / biomeHolderSet.size());
            }

            if (highScoreBiomes.isEmpty())
            {
                continue;
            }

            final ArrayList<Object2DoubleMap.Entry<Holder<Biome>>> sortedPotentialBiomes = new ArrayList<>(potentialBiomes.object2DoubleEntrySet());
            sortedPotentialBiomes.sort(Comparator.comparingDouble(e -> ((Object2DoubleMap.Entry<Holder<Biome>>) e).getDoubleValue()).reversed());

            Object2DoubleMap.Entry<Holder<Biome>> bestSimilarityScore = sortedPotentialBiomes.get(0);
            if (bestSimilarityScore == null)
            {
                continue;
            }

            toAdd.clear();

            double minSimilarity = bestSimilarityScore.getDoubleValue() * 0.8 * CommonConfiguration.config.getCommonConfig().autoBiomeCompatStrictness;
            for (final Object2DoubleMap.Entry<Holder<Biome>> scoredBiome : sortedPotentialBiomes)
            {
                if (scoredBiome.getDoubleValue() < minSimilarity)
                {
                    break;
                }

                if (highScoreBiomes.containsKey(scoredBiome.getKey()))
                {
                    toAdd.add(scoredBiome.getKey());
                }
            }

            for (final Iterator<Holder<Biome>> iterator = toAdd.iterator(); iterator.hasNext(); )
            {
                final var biomeHolder = iterator.next();

                if (!biomeHolder.isBound() || biomeHolder.unwrapKey().isEmpty())
                {
                    iterator.remove();
                    continue;
                }

                final ResourceKey<Biome> biomeKey = biomeHolder.unwrapKey().get();
                if (biomeKey == Biomes.THE_END || biomeKey == Biomes.THE_VOID || biomeKey == SMALL_END_ISLANDS)
                {
                    iterator.remove();
                    continue;
                }

                boolean containedDimension = false;

                for (final Set<Holder<Biome>> dimensionBiomes : allowedDimensions.values())
                {
                    if (dimensionBiomes.contains(biomeHolder))
                    {
                        containedDimension = true;
                        break;
                    }
                }

                if (!containedDimension)
                {
                    iterator.remove();
                    continue;
                }

                if (biomeHolder.value().getGenerationSettings().features().isEmpty())
                {
                    iterator.remove();
                    continue;
                }

                float temp = Command.getAdjustedTemp(biomeHolder);
                final float downFall = biomeHolder.value().getModifiedClimateSettings().downfall();

                if (!(temp > minTemp && temp < maxTemp && downFall < maxDownfall && downFall > minDownfall))
                {
                    iterator.remove();
                    continue;
                }

                if (!biomeHolder.tags().anyMatch(tag -> tag.location().getPath().contains("structure")))
                {
                    iterator.remove();
                    continue;
                }

                boolean shouldRemove = false;
                for (final TagKey<Biome> biomeTagKey : requiredDefiningSet)
                {
                    if (!biomeHolder.is(biomeTagKey) && !addedTagBiomes.contains(biomeHolder))
                    {
                        shouldRemove = true;
                    }
                }

                if (shouldRemove)
                {
                    iterator.remove();
                    continue;
                }

                for (final TagKey<Biome> biomeTagKey : deniedDefiningSet)
                {
                    if (biomeHolder.is(biomeTagKey))
                    {
                        iterator.remove();
                        break;
                    }
                }
            }

            if (!toAdd.isEmpty())
            {
                String tagName = biomeRegistry.getKey(holder.value().biomes().iterator().next().value()).toString();

                if (holder.value().biomes().unwrap().left().isPresent())
                {
                    tagName = holder.value().biomes().unwrap().left().get().location().toString();
                }

                if (CommonConfiguration.config.getCommonConfig().autoBiomeCompatLogging)
                {
                    StructureEssentials.LOGGER.warn(
                        "Adding Biomes to structure: " + holder.unwrapKey().get().identifier() + " tag:" + tagName + " mins:" + ((int) (minSimilarity * 1000)) / 1000.0 + " biomes: "
                            + toAdd.stream()
                            .map(e -> e.unwrapKey().get().identifier() + ":" + ((int) (potentialBiomes.getOrDefault(e, 0) * 1000)) / 1000.0)
                            .toList());
                }

                biomeHolderSet.addAll(toAdd);

                if (holder.value().modifiableStructureInfo() instanceof IStructureModifier structureModifier)
                {
                    structureModifier.setStructureBiomes(HolderSet.direct(new ArrayList<>(biomeHolderSet)));
                }
            }
        }

        Command.biomeScoreCache.clear();
    }

    public static ResourceLocation createResourcelocation(final String namespace, final String path)
    {
        return ResourceLocation.fromNamespaceAndPath(namespace, path);
    }

    public static TagKey<Biome> createBiomeTag(final String namespace, final String path)
    {
        return TagKey.create(Registries.BIOME, createResourcelocation(namespace, path));
    }
}
