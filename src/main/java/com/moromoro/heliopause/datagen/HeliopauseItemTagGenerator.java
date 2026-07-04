package com.moromoro.heliopause.datagen;

import com.moromoro.Heliopause;
import com.moromoro.heliopause.registry.BlockRegistry;
import com.moromoro.heliopause.registry.ItemRegistry;
import com.moromoro.heliopause.registry.TagRegistry;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class HeliopauseItemTagGenerator extends ItemTagsProvider {
    public HeliopauseItemTagGenerator(PackOutput p_275343_, CompletableFuture<HolderLookup.Provider> p_275729_, CompletableFuture<TagLookup<Block>> p_275322_, @Nullable ExistingFileHelper existingFileHelper) {
        super(p_275343_, p_275729_, p_275322_, Heliopause.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        this.tag(Tags.Items.GLASS)
            .add(
                ItemRegistry.OPTICAL_GLASS.get(),
                BlockRegistry.ALCHEMY_BIRON_GLASS.get().asItem()
            );
        this.tag(Tags.Items.GLASS_COLORLESS)
            .add(
                ItemRegistry.OPTICAL_GLASS.get()
            );
        
        this.copy(TagRegistry.Blocks.FORGE_SILVER_ORE, TagRegistry.Items.FORGE_SILVER_ORE);
        this.copy(TagRegistry.Blocks.FORGE_RAW_SILVER, TagRegistry.Items.FORGE_RAW_SILVER_BLOCK);
        this.tag(TagRegistry.Items.FORGE_RAW_SILVER_ITEM)
            .add(
                ItemRegistry.RAW_SILVER.get()
            );
        this.tag(TagRegistry.Items.FORGE_SILVER_INGOTS)
            .add(
                ItemRegistry.SILVER_INGOT.get()
            );
        this.tag(TagRegistry.Items.FORGE_SILVER_NUGGETS)
            .add(
                ItemRegistry.SILVER_NUGGET.get()
            );
        
        this.copy(TagRegistry.Blocks.ALCHEMY_BIRON_BLOCKS, TagRegistry.Items.ALCHEMY_BIRON_BLOCKS);
        
        /*this.copy(TagRegistry.Blocks.LENS_BARREL, TagRegistry.Items.LENS_BARREL);
        this.copy(TagRegistry.Blocks.MAIN_MIRROR, TagRegistry.Items.MAIN_MIRROR);
        this.copy(TagRegistry.Blocks.SECOND_MIRROR, TagRegistry.Items.SECOND_MIRROR);*/
    }
}
