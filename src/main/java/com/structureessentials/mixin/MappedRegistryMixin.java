package com.structureessentials.mixin;

import com.structureessentials.StructureEssentials;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;

@Mixin(value = MappedRegistry.class, priority = 10000)
public abstract class MappedRegistryMixin implements Registry
{
    @Redirect(method = "freeze", at = @At(value = "INVOKE", target = "Ljava/util/List;isEmpty()Z"), require = 0)
    private boolean onfreeze(final List instance)
    {
        if (!StructureEssentials.config.getCommonConfig().warnMissingRegistryEntry)
        {
            return instance.isEmpty();
        }

        if (!instance.isEmpty())
        {
            StructureEssentials.LOGGER.error("Unbound values in registry " + key() + ": " + instance);
        }

        return true;
    }
}
