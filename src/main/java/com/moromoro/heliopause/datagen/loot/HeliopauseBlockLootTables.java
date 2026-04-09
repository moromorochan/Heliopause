package com.moromoro.heliopause.datagen.loot;

import com.moromoro.heliopause.registry.BlockRegistry;
import com.moromoro.heliopause.registry.ItemRegistry;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.RegistryObject;

import java.util.Set;

public class HeliopauseBlockLootTables extends BlockLootSubProvider {
    public HeliopauseBlockLootTables() {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags());
    }

    @Override
    protected void generate() {
        this.dropSelf(BlockRegistry.ALCHEMY_BIRON_BLOCK.get());
        this.dropSelf(BlockRegistry.POLISHED_BIRON_BLOCK.get());
        this.dropSelf(BlockRegistry.CHISELED_BIRON_BLOCK.get());
        this.add(BlockRegistry.ALCHEMY_BIRON_GLASS.get(), BlockLootSubProvider::createSilkTouchOnlyTable);
        
        this.dropSelf(BlockRegistry.GLOWSTONE_ALLOY_BLOCK.get());
        
        this.dropSelf(BlockRegistry.SILVER_BLOCK.get());
        this.add(BlockRegistry.SILVER_ORE_BLOCK.get(), block -> createOreDrop(block, ItemRegistry.RAW_SILVER.get()));
        this.add(BlockRegistry.DEEPSLATE_SILVER_ORE_BLOCK.get(), block -> createOreDrop(block, ItemRegistry.RAW_SILVER.get()));
        this.dropSelf(BlockRegistry.RAW_SILVER_BLOCK.get());

        this.dropSelf(BlockRegistry.ALCHEMY_STOVE.get());
        this.dropSelf(BlockRegistry.ROASTING_TABLE.get());
        this.dropSelf(BlockRegistry.CRUCIBLE.get());
        this.dropSelf(BlockRegistry.FLUID_CAGE.get());

        this.dropSelf(BlockRegistry.COPPER_PIPE.get());
        //this.dropSelf(BlockRegistry.FLUID_SPREADER_TOWER.get());

        /*this.dropSelf(BlockRegistry.PAPERBUSH_LEAVES_BLOCK.get());
        this.dropSelf(BlockRegistry.PAPERBUSH_BLOCK.get());*/

        //this.dropSelf(BlockRegistry.ALCHEMY_CAMPFIRE.get());
        /*this.add(BlockRegistry.SIDEROSTAT_MOON.get(), block ->
            LootTable.lootTable().withPool(
                LootPool.lootPool()
                    .name("main")
                    .setRolls(ConstantValue.exactly(1))
                    .add(
                        LootItem.lootTableItem(ItemRegistry.MOONSTONE.get())
                            .apply(SetItemCountFunction.setCount(UniformGenerator.between(2, 4)))
                    )
            )
        );*/
        this.dropSelf(BlockRegistry.SIDEROSTAT_TOP.get());
        //this.dropOther(BlockRegistry.SIDEROSTAT_ORB.get(),BlockRegistry.SIDEROSTAT_BASE.get());

        /*this.dropSelf(BlockRegistry.PENETRATOR.get());
        this.dropSelf(BlockRegistry.DISSOLVER.get());
        this.dropSelf(BlockRegistry.CONVERGE_CYLINDER.get());
        this.dropSelf(BlockRegistry.REFINERY_CYLINDER.get());*/

        this.dropSelf(BlockRegistry.CONCENTRATOR.get());

        this.dropSelf(BlockRegistry.WOODEN_LENS_BARREL_BLOCK.get());
        this.dropSelf(BlockRegistry.STONE_LENS_BARREL_BLOCK.get());
        this.dropSelf(BlockRegistry.ALCHEMY_BIRON_LENS_BARREL_BLOCK.get());
        this.dropSelf(BlockRegistry.THERMOIMMOBILANT_LENS_BARREL_BLOCK.get());
        this.dropSelf(BlockRegistry.GRAPHITE_MAIN_MIRROR_BLOCK.get());
        this.dropSelf(BlockRegistry.GRAPHITE_SECOND_MIRROR_BLOCK.get());
        this.dropSelf(BlockRegistry.IRON_MAIN_MIRROR_BLOCK.get());
        this.dropSelf(BlockRegistry.IRON_SECOND_MIRROR_BLOCK.get());
        this.dropSelf(BlockRegistry.SILVER_MAIN_MIRROR_BLOCK.get());
        this.dropSelf(BlockRegistry.SILVER_SECOND_MIRROR_BLOCK.get());
        this.dropSelf(BlockRegistry.QUINCE_STEEL_MAIN_MIRROR_BLOCK.get());
        this.dropSelf(BlockRegistry.QUINCE_STEEL_SECOND_MIRROR_BLOCK.get());

        this.dropSelf(BlockRegistry.BLACKBOARD.get());
        this.dropOther(BlockRegistry.WRITTEN_BOARD.get(), BlockRegistry.BLACKBOARD.get());
        this.dropOther(BlockRegistry.ORRERY_CIRCLE_BOARD.get(), BlockRegistry.BLACKBOARD.get());
        this.dropOther(BlockRegistry.ALT_AZIMUTH_CIRCLE_BOARD.get(), BlockRegistry.BLACKBOARD.get());
        this.dropSelf(BlockRegistry.CONCENTRATOR.get());

    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return BlockRegistry.BLOCKS.getEntries().stream().map(RegistryObject::get)::iterator;
    }
}
