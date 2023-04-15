package com.structureessentials.mixin;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(StructureTemplate.StructureBlockInfo.class)
public class BlockInfoMixin
{
    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(final BlockPos p_74679_, final BlockState state, final CompoundTag nbt, final CallbackInfo ci)
    {
        if (state.is(Blocks.JIGSAW) && nbt != null && nbt.contains("final_state"))
        {
            String s = nbt.getString("final_state");
            try {
                BlockStateParser.parseForBlock(Registry.BLOCK, s, true);
            } catch (CommandSyntaxException commandsyntaxexception) {
                throw new RuntimeException(commandsyntaxexception);
            }
        }
    }
}
