package com.structureessentials;

import com.structureessentials.command.Command;
import com.structureessentials.config.CommonConfiguration;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraftforge.common.Tags;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(StructureEssentials.MODID)
public class StructureEssentials
{
    public static final String MODID  = "structureessentials";
    public static final Logger LOGGER = LogManager.getLogger();
    public static       Random rand   = new Random();

    public StructureEssentials()
    {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        Mod.EventBusSubscriber.Bus.FORGE.bus().get().addListener(this::commandRegister);
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

    public static void onServerStart(MinecraftServer server)
    {
        Timings.featureTimings = new ConcurrentHashMap<>();
        Timings.structureTimings = new ConcurrentHashMap<>();

        if (!CommonConfiguration.config.getCommonConfig().autoBiomeCompat)
        {
            return;
        }

        final RegistryAccess.Frozen registryAccess = server.registryAccess();
        List<Holder.Reference<Structure>> holders = registryAccess.registryOrThrow(Registries.STRUCTURE).holders().toList();
        final Registry<Biome> biomeRegistry = registryAccess.registry(Registries.BIOME).get();

        final Map<ResourceLocation, TagKey<Biome>> directReplacementTags = new HashMap<>();
        directReplacementTags.put(new ResourceLocation("deep_ocean"), BiomeTags.IS_DEEP_OCEAN);
        directReplacementTags.put(new ResourceLocation("ocean"), BiomeTags.IS_OCEAN);
        directReplacementTags.put(new ResourceLocation("river"), BiomeTags.IS_RIVER);
        directReplacementTags.put(new ResourceLocation("badlands"), BiomeTags.IS_BADLANDS);
        directReplacementTags.put(new ResourceLocation("eroded_badlands"), BiomeTags.IS_BADLANDS);
        directReplacementTags.put(new ResourceLocation("wooded_badlands"), BiomeTags.IS_BADLANDS);
        directReplacementTags.put(new ResourceLocation("windswept_hills"), BiomeTags.IS_HILL);
        directReplacementTags.put(new ResourceLocation("windswept_gravelly_hills"), BiomeTags.IS_HILL);
        directReplacementTags.put(new ResourceLocation("taiga"), BiomeTags.IS_TAIGA);
        directReplacementTags.put(new ResourceLocation("jungle"), BiomeTags.IS_JUNGLE);
        directReplacementTags.put(new ResourceLocation("forest"), BiomeTags.IS_FOREST);
        directReplacementTags.put(new ResourceLocation("savanna"), BiomeTags.IS_SAVANNA);
        directReplacementTags.put(new ResourceLocation("deep_dark"), BiomeTags.HAS_ANCIENT_CITY);

        directReplacementTags.put(new ResourceLocation("plains"), Tags.Biomes.IS_PLAINS);
        directReplacementTags.put(new ResourceLocation("desert"), Tags.Biomes.IS_DESERT);
        directReplacementTags.put(new ResourceLocation("swamp"), Tags.Biomes.IS_SWAMP);
        directReplacementTags.put(new ResourceLocation("mushroom_fields"), Tags.Biomes.IS_MUSHROOM);
        directReplacementTags.put(new ResourceLocation("lush_caves"), Tags.Biomes.IS_LUSH);

        for (final Holder.Reference<Structure> holder : holders)
        {
            LinkedHashSet<Holder<Biome>> biomeHolderSet = new LinkedHashSet<>(holder.value().biomes().size());
            float minTemp = 1000;
            float maxTemp = -1000;

            float maxDownfall = -1000;
            float minDownfall = 1000;

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

                if (!contained)
                {
                    iterator.remove();
                }
            }

            Set<TagKey<Biome>> addedTags = new HashSet<>();

            Set<Holder<Biome>> toAdd = new HashSet<>();
            for (Holder<Biome> biome : biomeHolderSet)
            {
                final TagKey<Biome> tag = directReplacementTags.get(biomeRegistry.getKey(biome.value()));
                // Check tag and if applicable add all its values
                if (tag != null && !addedTags.contains(tag) && biome.is(tag))
                {
                    for (final Holder<Biome> tagBiome : biomeRegistry.getOrCreateTag(tag))
                    {
                        if (biomeHolderSet.contains(tagBiome))
                        {
                            continue;
                        }

                        if (tagBiome.value().getGenerationSettings().features().isEmpty())
                        {
                            continue;
                        }

                        float temp = Command.getAdjustedTemp(tagBiome);
                        final float downFall = tagBiome.value().getModifiedClimateSettings().downfall();

                        if (temp > minTemp && temp < maxTemp && downFall < maxDownfall && downFall > minDownfall)
                        {
                            toAdd.add(tagBiome);
                        }
                    }
                    addedTags.add(tag);
                }
            }

            Object2DoubleOpenHashMap<Holder<Biome>> potentialBiomes = new Object2DoubleOpenHashMap<>();
            for (Holder<Biome> biome : biomeHolderSet)
            {
                final List<Object2IntMap.Entry<Holder<Biome>>> similar = Command.getSimilarBiomesFor(biome, registryAccess);
                int orgScore = similar.get(0).getIntValue();

                for (int i = 1; i < similar.size() && i < 200; i++)
                {
                    final Object2IntMap.Entry<Holder<Biome>> entry = similar.get(i);
                    if (biomeHolderSet.contains(entry.getKey()) || entry.getKey().value().getGenerationSettings().features().isEmpty()
                        || entry.getKey().unwrapKey().get() == Biomes.THE_END
                        || entry.getIntValue() <= 1)
                    {
                        continue;
                    }

                    double percent = ((double) entry.getIntValue() / orgScore);
                    double previousValue = potentialBiomes.getOrDefault(entry.getKey(), 0);
                    percent = (percent * percent) + (percent >= 0.5 * CommonConfiguration.config.getCommonConfig().autoBiomeCompatStrictness && previousValue < 100 ? 100 : 0);
                    potentialBiomes.put(entry.getKey(), previousValue + percent);
                }
            }

            if (!potentialBiomes.isEmpty())
            {
                double similarityThreshold =
                    100 + ((0.74 * 0.74) + Math.log(biomeHolderSet.size()) * 0.1905) * CommonConfiguration.config.getCommonConfig().autoBiomeCompatStrictness;
                for (Iterator<Holder<Biome>> iterator = toAdd.iterator(); iterator.hasNext(); )
                {
                    final var tagAdded = iterator.next();
                    double score = potentialBiomes.getOrDefault(tagAdded, 0);
                    if (score < (similarityThreshold - 100) / 1.3)
                    {
                        iterator.remove();
                    }
                }

                final ArrayList<Object2DoubleMap.Entry<Holder<Biome>>> sortedBiomeHolders = new ArrayList<>(potentialBiomes.object2DoubleEntrySet());
                sortedBiomeHolders.sort(Comparator.comparingDouble(e -> ((Object2DoubleMap.Entry<Holder<Biome>>) e).getDoubleValue()).reversed());

                for (var sortedBiome : sortedBiomeHolders)
                {
                    if (similarityThreshold < sortedBiome.getDoubleValue())
                    {
                        // Check fitting, temp/downfall
                        float temp = Command.getAdjustedTemp(sortedBiome.getKey());
                        final float downFall = sortedBiome.getKey().value().getModifiedClimateSettings().downfall();
                        if (temp > minTemp && temp < maxTemp && downFall < maxDownfall && downFall > minDownfall && !sortedBiome.getKey().toString().contains("small")
                            && !sortedBiome.getKey().is(BiomeTags.IS_RIVER))
                        {
                            toAdd.add(sortedBiome.getKey());
                        }
                    }
                    else
                    {
                        break;
                    }
                }
            }

            for (Iterator<Holder<Biome>> iterator = toAdd.iterator(); iterator.hasNext(); )
            {
                final var biomeHolder = iterator.next();
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
                    double similarityThreshold =
                        100 + ((0.74 * 0.74) + Math.log(biomeHolderSet.size()) * 0.1905) * CommonConfiguration.config.getCommonConfig().autoBiomeCompatStrictness;
                    StructureEssentials.LOGGER.warn(
                        "Adding Biomes to structure: " + holder.key().location() + " tag:" + tagName + " mins:" + ((int) (similarityThreshold * 1000)) / 1000.0 + " biomes: "
                            + toAdd.stream()
                            .map(e -> e.unwrapKey().get().location() + ":" + ((int) (potentialBiomes.getOrDefault(e, 0) * 1000)) / 1000.0)
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
}
