package com.structureessentials.mixin;

import com.structureessentials.StructureEssentials;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.progress.ChunkProgressListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public class LevelCreatedCallback
{
    @Inject(method = "createLevels", at = @At("TAIL"))
    private void afterLevelCreation(final ChunkProgressListener p_129816_, final CallbackInfo ci)
    {
        StructureEssentials.onServerStart((MinecraftServer) (Object) this);
    }
}
