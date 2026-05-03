package com.moromoro.heliopause.datagen;

import com.moromoro.Heliopause;
import com.moromoro.heliopause.registry.BlockRegistry;
import com.moromoro.heliopause.registry.TagRegistry;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class HeliopauseBlockTagGenerator extends BlockTagsProvider {
    public HeliopauseBlockTagGenerator(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, Heliopause.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.@NotNull Provider provider) {
        this.tag(BlockTags.MINEABLE_WITH_PICKAXE)
            .add(
                BlockRegistry.ALCHEMY_BIRON_BLOCK.get(),
                BlockRegistry.POLISHED_BIRON_BLOCK.get(),
                BlockRegistry.CHISELED_BIRON_BLOCK.get(),
                BlockRegistry.GLOWSTONE_ALLOY_BLOCK.get(),
                BlockRegistry.SILVER_BLOCK.get(),
                BlockRegistry.SILVER_ORE_BLOCK.get(),
                BlockRegistry.DEEPSLATE_SILVER_ORE_BLOCK.get(),
                BlockRegistry.RAW_SILVER_BLOCK.get(),
                BlockRegistry.COPPER_PIPE.get(),
                BlockRegistry.ALCHEMY_STOVE.get(),
                BlockRegistry.CRUCIBLE.get(),
                BlockRegistry.BLACKBOARD.get(),
                BlockRegistry.WRITTEN_BOARD.get(),
                BlockRegistry.ORRERY_CIRCLE_BOARD.get(),
                BlockRegistry.CONCENTRATOR.get(),
                BlockRegistry.WOODEN_LENS_BARREL_BLOCK.get(),
                BlockRegistry.IRON_MAIN_MIRROR_BLOCK.get(),
                BlockRegistry.IRON_SECOND_MIRROR_BLOCK.get(),
                BlockRegistry.STONE_LENS_BARREL_BLOCK.get(),
                BlockRegistry.GRAPHITE_MAIN_MIRROR_BLOCK.get(),
                BlockRegistry.GRAPHITE_SECOND_MIRROR_BLOCK.get(),
                BlockRegistry.ALCHEMY_BIRON_LENS_BARREL_BLOCK.get(),
                BlockRegistry.SILVER_MAIN_MIRROR_BLOCK.get(),
                BlockRegistry.SILVER_SECOND_MIRROR_BLOCK.get(),
                BlockRegistry.THERMOIMMOBILANT_LENS_BARREL_BLOCK.get(),
                BlockRegistry.QUINCE_STEEL_MAIN_MIRROR_BLOCK.get(),
                BlockRegistry.QUINCE_STEEL_SECOND_MIRROR_BLOCK.get()
            );

        this.tag(BlockTags.MINEABLE_WITH_AXE)
            .add(
                BlockRegistry.FLUID_CAGE.get(),
                BlockRegistry.ROASTING_TABLE.get(),
                //BlockRegistry.FLUID_SPREADER_TOWER.get(),
                BlockRegistry.SIDEROSTAT_BASE.get(),
                BlockRegistry.SIDEROSTAT_TOP.get(),
                //BlockRegistry.CONCENTRATOR.get(),
                BlockRegistry.WOODEN_LENS_BARREL_BLOCK.get(),
                BlockRegistry.IRON_MAIN_MIRROR_BLOCK.get(),
                BlockRegistry.IRON_SECOND_MIRROR_BLOCK.get(),
                BlockRegistry.THERMOIMMOBILANT_LENS_BARREL_BLOCK.get(),
                BlockRegistry.QUINCE_STEEL_MAIN_MIRROR_BLOCK.get(),
                BlockRegistry.QUINCE_STEEL_SECOND_MIRROR_BLOCK.get()
            );
        
        this.tag(BlockTags.NEEDS_IRON_TOOL)
            .add(
                BlockRegistry.SILVER_ORE_BLOCK.get(),
                BlockRegistry.DEEPSLATE_SILVER_ORE_BLOCK.get(),
                BlockRegistry.RAW_SILVER_BLOCK.get(),
                BlockRegistry.SILVER_BLOCK.get(),
                BlockRegistry.THERMOIMMOBILANT_LENS_BARREL_BLOCK.get(),
                BlockRegistry.QUINCE_STEEL_MAIN_MIRROR_BLOCK.get(),
                BlockRegistry.QUINCE_STEEL_SECOND_MIRROR_BLOCK.get()
            );
        this.tag(TagRegistry.Blocks.FORGE_ORE_STONE)
            .add(
                BlockRegistry.SILVER_ORE_BLOCK.get()
            );
        this.tag(TagRegistry.Blocks.FORGE_ORE_DEEPSLATE)
            .add(
                BlockRegistry.DEEPSLATE_SILVER_ORE_BLOCK.get()
            );
        this.tag(TagRegistry.Blocks.FORGE_SILVER_ORE)
            .add(
                BlockRegistry.SILVER_ORE_BLOCK.get(),
                BlockRegistry.DEEPSLATE_SILVER_ORE_BLOCK.get()
            );
        this.tag(TagRegistry.Blocks.FORGE_RAW_SILVER)
            .add(
                BlockRegistry.RAW_SILVER_BLOCK.get()
            );
        this.tag(TagRegistry.Blocks.FORGE_SILVER_BLOCK)
            .add(
                BlockRegistry.SILVER_BLOCK.get()
            );

        /*this.block(BlockTags.CAMPFIRES)
            .add(
                BlockRegistry.ALCHEMY_CAMPFIRE.get()
            );*/

        /*this.tag(TagRegistry.Blocks.WOODEN_LENS_BARREL)
            .add(
                BlockRegistry.WOODEN_LENS_BARREL_BLOCK.get(),
                BlockRegistry.IRON_MAIN_MIRROR_BLOCK.get(),
                BlockRegistry.IRON_SECOND_MIRROR_BLOCK.get()
            );
        this.tag(TagRegistry.Blocks.ALCHEMY_BIRON_LENS_BARREL)
            .add(
                BlockRegistry.ALCHEMY_BIRON_LENS_BARREL_BLOCK.get(),
                BlockRegistry.SILVER_MAIN_MIRROR_BLOCK.get(),
                BlockRegistry.SILVER_SECOND_MIRROR_BLOCK.get()
            );*/
        this.tag(TagRegistry.Blocks.LENS_BARREL)
            .add(
                BlockRegistry.WOODEN_LENS_BARREL_BLOCK.get(),
                BlockRegistry.STONE_LENS_BARREL_BLOCK.get(),
                BlockRegistry.ALCHEMY_BIRON_LENS_BARREL_BLOCK.get(),
                BlockRegistry.IRON_MAIN_MIRROR_BLOCK.get(),
                BlockRegistry.IRON_SECOND_MIRROR_BLOCK.get(),
                BlockRegistry.GRAPHITE_MAIN_MIRROR_BLOCK.get(),
                BlockRegistry.GRAPHITE_SECOND_MIRROR_BLOCK.get(),
                BlockRegistry.SILVER_MAIN_MIRROR_BLOCK.get(),
                BlockRegistry.SILVER_SECOND_MIRROR_BLOCK.get(),
                BlockRegistry.THERMOIMMOBILANT_LENS_BARREL_BLOCK.get(),
                BlockRegistry.QUINCE_STEEL_MAIN_MIRROR_BLOCK.get(),
                BlockRegistry.QUINCE_STEEL_SECOND_MIRROR_BLOCK.get()
            );
        this.tag(TagRegistry.Blocks.MAIN_MIRROR)
            .add(
                BlockRegistry.IRON_MAIN_MIRROR_BLOCK.get(),
                BlockRegistry.GRAPHITE_MAIN_MIRROR_BLOCK.get(),
                BlockRegistry.SILVER_MAIN_MIRROR_BLOCK.get(),
                BlockRegistry.QUINCE_STEEL_MAIN_MIRROR_BLOCK.get()
            );
        this.tag(TagRegistry.Blocks.SECOND_MIRROR)
            .add(
                BlockRegistry.IRON_SECOND_MIRROR_BLOCK.get(),
                BlockRegistry.GRAPHITE_SECOND_MIRROR_BLOCK.get(),
                BlockRegistry.SILVER_SECOND_MIRROR_BLOCK.get(),
                BlockRegistry.QUINCE_STEEL_SECOND_MIRROR_BLOCK.get()
            );

        this.tag(TagRegistry.Blocks.CREATE_BRITTLE/*AllTags.AllBlockTags.BRITTLE.block*/)
            .add(
                BlockRegistry.FLUID_CAGE.get(),
                BlockRegistry.SIDEROSTAT_BASE.get(),
                BlockRegistry.SIDEROSTAT_TOP.get()
            );
    }
}
