package com.structureessentials.mixin;

import com.structureessentials.config.CommonConfiguration;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.ResourceOrTagKeyArgument;
import net.minecraft.server.commands.LocateCommand;
import net.minecraft.world.level.levelgen.structure.Structure;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LocateCommand.class)
public class LocateCommandMixin
{
    @ModifyConstant(method = "locateStructure", constant = @Constant(intValue = 100), require = 0)
    private static int setRadius(int org)
    {
        return CommonConfiguration.config.getCommonConfig().locateSearchRadius;
    }

    @Unique
    private static int prevLimit = 0;

    @Unique
    private static int prevTimeout = 0;

    @Inject(method = "locateStructure", at = @At("HEAD"))
    private static void adjustLimit(final CommandSourceStack p_214472_, final ResourceOrTagKeyArgument.Result<Structure> p_249893_, final CallbackInfoReturnable<Integer> cir)
    {
        prevLimit = CommonConfiguration.config.getCommonConfig().globalSearchRadius;
        prevTimeout = CommonConfiguration.config.getCommonConfig().structureSearchTimeout;
        CommonConfiguration.config.getCommonConfig().globalSearchRadius = CommonConfiguration.config.getCommonConfig().locateSearchRadius;
        CommonConfiguration.config.getCommonConfig().structureSearchTimeout = prevTimeout += 30;
    }

    @Inject(method = "locateStructure", at = @At("RETURN"))
    private static void restoreLimit(final CommandSourceStack p_214472_, final ResourceOrTagKeyArgument.Result<Structure> p_249893_, final CallbackInfoReturnable<Integer> cir)
    {
        CommonConfiguration.config.getCommonConfig().globalSearchRadius = prevLimit;
        CommonConfiguration.config.getCommonConfig().structureSearchTimeout = prevTimeout;
    }
}
