package com.moromoro.heliopause.registry;

import com.moromoro.Heliopause;
import com.moromoro.heliopause.item.ChalkItem;
import com.moromoro.heliopause.item.FluidBottle;
import com.moromoro.heliopause.item.PipeBlockItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ItemRegistry {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Heliopause.MODID);

    //ブロックアイテムの作成
        //機械
    public static final RegistryObject<Item> ALCHEMY_STOVE_ITEM =
            ITEMS.register("alchemy_stove", () -> new BlockItem(BlockRegistry.ALCHEMY_STOVE.get(), new Item.Properties()));
    public static final RegistryObject<Item> CRUCIBLE_ITEM =
            ITEMS.register("crucible", () -> new BlockItem(BlockRegistry.CRUCIBLE.get(), new Item.Properties()));
    public static final RegistryObject<Item> ROASTING_TABLE_ITEM =
            ITEMS.register("alchemy_roasting_table", () -> new BlockItem(BlockRegistry.ROASTING_TABLE.get(), new Item.Properties()));
    public static final RegistryObject<Item> FLUID_CAGE_ITEM =
            ITEMS.register("fluid_cage", () -> new BlockItem(BlockRegistry.FLUID_CAGE.get(), new Item.Properties()));
    public static final RegistryObject<Item> FLUID_SPREADER_TOWER_ITEM =
            ITEMS.register("fluid_spreader_tower", () -> new BlockItem(BlockRegistry.FLUID_SPREADER_TOWER.get(), new Item.Properties()));

    //模造天体コア
    /*public static final RegistryObject<Item> COMET_CORE_ITEM =
            ITEMS.register("comet_core", () -> new BlockItem(BlockRegistry.COMET_CORE.get(), new Item.Properties()));*/

    //パイプ
    public static final RegistryObject<Item> LOW_COPPER_PIPE_ITEM =
            ITEMS.register("low_copper_pipe", () -> new PipeBlockItem
                    (BlockRegistry.COPPER_PIPE.get(),1, new Item.Properties(),"low_copper_pipe"));
    public static final RegistryObject<Item> MEDIUM_COPPER_PIPE_ITEM =
            ITEMS.register("medium_copper_pipe", () -> new PipeBlockItem
                    (BlockRegistry.COPPER_PIPE.get(),2, new Item.Properties(),"medium_copper_pipe"));
    public static final RegistryObject<Item> HIGH_COPPER_PIPE_ITEM =
            ITEMS.register("high_copper_pipe", () -> new PipeBlockItem
                    (BlockRegistry.COPPER_PIPE.get(),3, new Item.Properties(),"high_copper_pipe"));

        //金属ブロック
    public static final RegistryObject<Item> ALCHEMY_BIRON_BLOCK_ITEM =
            ITEMS.register("alchemy_biron_block", () -> new BlockItem(BlockRegistry.ALCHEMY_BIRON_BLOCK.get(), new Item.Properties()));
    public static final RegistryObject<Item> GLOWSTONE_ALLOY_BLOCK_ITEM =
            ITEMS.register("glowstone_alloy_block", () -> new BlockItem(BlockRegistry.GLOWSTONE_ALLOY_BLOCK.get(), new Item.Properties()));

        //植物ブロック
    public static final RegistryObject<Item> PAPERBUSH_BLOCK_ITEM =
            ITEMS.register("paperbush", () -> new BlockItem(BlockRegistry.PAPERBUSH_BLOCK.get(), new Item.Properties()));
    public static final RegistryObject<Item> PAPERBUSH_LEAVES_BLOCK_ITEM =
            ITEMS.register("paperbush_leaves", () -> new BlockItem(BlockRegistry.PAPERBUSH_LEAVES_BLOCK.get(), new Item.Properties()));

    // アイテムの作成
        //植物アイテム
    public static final RegistryObject<Item> PAPERBUSH_TWIGS_ITEM =
            ITEMS.register("paperbush_twigs", () -> new Item(new Item.Properties()));

        //容器
    public static final RegistryObject<FluidBottle> VIAL_ITEM =
            ITEMS.register("vial",()->new FluidBottle(new Item.Properties(),500,50));
    public static final RegistryObject<FluidBottle> LARGE_BOTTLE_ITEM =
            ITEMS.register("large_bottle",()->new FluidBottle(new Item.Properties(),2000,200));

        //ツール
        public static final RegistryObject<ChalkItem> CHALK_ITEM =
                ITEMS.register("chalk", () -> new ChalkItem(new Item.Properties()));

        //模造天体コア
    public static final RegistryObject<Item> IMITATION_CORE_ITEM =
            ITEMS.register("imitation_core", () -> new Item(new Item.Properties()));

        //金属
    public static final RegistryObject<Item> ALCHEMY_BIRON_INGOT_ITEM =
            ITEMS.register("alchemy_biron_ingot", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> ALCHEMY_BIRON_NUGGET_ITEM =
            ITEMS.register("alchemy_biron_nugget", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> GLOWSTONE_ALLOY_INGOT_ITEM =
            ITEMS.register("glowstone_alloy_ingot", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> GLOWSTONE_ALLOY_NUGGET_ITEM =
            ITEMS.register("glowstone_alloy_nugget", () -> new Item(new Item.Properties()));
}
