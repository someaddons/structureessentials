package com.structureessentials;

import net.minecraft.resources.Identifier;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Timings
{
    public static Map<Identifier, Long> featureTimings   = new ConcurrentHashMap<>();
    public static Map<Identifier, Long> structureTimings = new ConcurrentHashMap<>();
}
