package com.moromoro.heliopause.datagen;

import com.moromoro.Heliopause;
import com.moromoro.heliopause.registry.BlockRegistry;
import com.moromoro.heliopause.registry.TagRegistry;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class HEPBlockTagGenerator extends BlockTagsProvider {
    public HEPBlockTagGenerator(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, Heliopause.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.@NotNull Provider provider) {
        this.tag(BlockTags.MINEABLE_WITH_PICKAXE)
            .add(
                BlockRegistry.ALCHEMY_BIRON_BLOCK.get(),
                BlockRegistry.GLOWSTONE_ALLOY_BLOCK.get(),
                BlockRegistry.COPPER_PIPE.get(),
                BlockRegistry.ALCHEMY_STOVE.get(),
                BlockRegistry.CRUCIBLE.get(),
                BlockRegistry.BLACKBOARD.get(),
                BlockRegistry.WRITTEN_BOARD.get(),
                BlockRegistry.ORRERY_CIRCLE_BOARD.get(),
                BlockRegistry.ALT_AZIMUTH_CIRCLE_BOARD.get(),
                BlockRegistry.CONCENTRATOR.get()
            );

        this.tag(BlockTags.MINEABLE_WITH_AXE)
            .add(
                BlockRegistry.FLUID_CAGE.get(),
                BlockRegistry.ROASTING_TABLE.get(),
                //BlockRegistry.FLUID_SPREADER_TOWER.get(),
                BlockRegistry.SIDEROSTAT_BASE.get(),
                BlockRegistry.SIDEROSTAT_TOP.get(),
                BlockRegistry.CONCENTRATOR.get(),
                BlockRegistry.WOODEN_LENS_BARREL_BLOCK.get(),
                BlockRegistry.WOODEN_MAIN_MIRROR_BLOCK.get(),
                BlockRegistry.WOODEN_SECOND_MIRROR_BLOCK.get()
            );

        /*this.tag(BlockTags.CAMPFIRES)
            .add(
                BlockRegistry.ALCHEMY_CAMPFIRE.get()
            );*/

        this.tag(TagRegistry.Blocks.WOODEN_LENS_BARREL)
            .add(
                BlockRegistry.WOODEN_LENS_BARREL_BLOCK.get(),
                BlockRegistry.WOODEN_MAIN_MIRROR_BLOCK.get(),
                BlockRegistry.WOODEN_SECOND_MIRROR_BLOCK.get()
            );
        this.tag(TagRegistry.Blocks.MAIN_MIRROR)
            .add(
              BlockRegistry.WOODEN_MAIN_MIRROR_BLOCK.get()
            );
        this.tag(TagRegistry.Blocks.SECOND_MIRROR)
            .add(
                BlockRegistry.WOODEN_SECOND_MIRROR_BLOCK.get()
            );

        this.tag(TagRegistry.Blocks.CREATE_BRITTLE/*AllTags.AllBlockTags.BRITTLE.tag*/)
            .add(
                BlockRegistry.FLUID_CAGE.get(),
                BlockRegistry.SIDEROSTAT_BASE.get(),
                BlockRegistry.SIDEROSTAT_TOP.get()
            );
    }
}
