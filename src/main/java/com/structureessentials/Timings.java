package com.structureessentials;

import net.minecraft.resources.ResourceLocation;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Timings
{
    public static Map<ResourceLocation, Long> featureTimings   = new ConcurrentHashMap<>();
    public static Map<ResourceLocation, Long> structureTimings = new ConcurrentHashMap<>();
}
