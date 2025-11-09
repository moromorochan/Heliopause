package com.moromoro.heliopause.datagen;

import com.moromoro.Heliopause;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class HEPItemTagGenerator extends ItemTagsProvider {
    public HEPItemTagGenerator(PackOutput p_275343_, CompletableFuture<HolderLookup.Provider> p_275729_, CompletableFuture<TagLookup<Block>> p_275322_, @Nullable ExistingFileHelper existingFileHelper) {
        super(p_275343_, p_275729_, p_275322_, Heliopause.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        /*this.tag(TagRegistry.Items.FLINT_MATERIAL)
            .add(
                BlockRegistry.CUT_FLINT.get().asItem(),
                BlockRegistry.CUT_FLINT_STAIRS.get().asItem(),
                BlockRegistry.CUT_FLINT_WALL.get().asItem(),
                BlockRegistry.POLISHED_FLINT.get().asItem(),
                BlockRegistry.POLISHED_FLINT_STAIRS.get().asItem(),
                BlockRegistry.POLISHED_FLINT_WALL.get().asItem(),
                BlockRegistry.POLISHED_FLINT_BRICK.get().asItem(),
                BlockRegistry.POLISHED_FLINT_BRICK_STAIRS.get().asItem(),
                BlockRegistry.POLISHED_FLINT_BRICK_WALL.get().asItem(),
                BlockRegistry.POLISHED_FLINT_PILLAR.get().asItem()
            );*/

        /*for (int i = 0; i < colors.length; i++) {
            this.tag(TagRegistry.Items.CORRUGATED_SHEET)
                .add(BlockRegistry.CORRUGATED_SHEET[i].get().asItem());
            this.tag(TagRegistry.Items.NAILED_CORRUGATED_SHEET)
                .add(BlockRegistry.NAILED_CORRUGATED_SHEET[i].get().asItem());
        }

        for (int i = 0; i < wood_palettes.length; i++) {
            this.tag(TagRegistry.Items.CEILING_FAN)
                .add(BlockRegistry.CEILING_FAN[i].get().asItem());
            this.tag(TagRegistry.Items.CEILING_FAN_LIGHT)
                .add(BlockRegistry.CEILING_FAN_LIGHT[i].get().asItem());
        }
        this.tag(TagRegistry.Items.STURDY_BLOCK)
            .add(BlockRegistry.STURDY_STRUCTURE.get().asItem())
            .add(BlockRegistry.SMOOTH_STURDY_STRUCTURE.get().asItem())
            .add(BlockRegistry.STURDY_BEAM.get().asItem());

        this.tag(TagRegistry.Items.ANDESITE_STRUCTURE_BLOCK)
            .add(BlockRegistry.ANDESITE_STRUCTURE.get().asItem())
            .add(BlockRegistry.SMOOTH_ANDESITE_STRUCTURE.get().asItem())
            .add(BlockRegistry.ANDESITE_BEAM.get().asItem());*/
    }
}
