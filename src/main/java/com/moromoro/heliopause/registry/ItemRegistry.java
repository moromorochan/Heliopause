package com.moromoro.heliopause.registry;

import com.moromoro.Heliopause;
import com.moromoro.heliopause.item.*;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.antlr.v4.tool.Rule;

public class ItemRegistry {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Heliopause.MODID);

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
    
    // 鏡筒
    /*public static final RegistryObject<Item> WOODEN_LENS_BARREL_ITEM =
        ITEMS.register("wooden_lens_barrel", () ->
            );
    
    public static final RegistryObject<Item> ALCHEMY_BIRON_LENS_BARREL_ITEM =
        ITEMS.register("alchemy_biron_lens_barrel", () ->
            new lensBarrelBlockItem(BlockRegistry.ALCHEMY_BIRON_LENS_BARREL_BLOCK.get(), new Item.Properties()));
    
    public static final RegistryObject<Item> IRON_MAIN_MIRROR_ITEM =
        ITEMS.register("iron_main_mirror", () ->
            new lensBarrelBlockItem(BlockRegistry.IRON_MAIN_MIRROR_BLOCK.get(), new Item.Properties()));
    
    public static final RegistryObject<Item> IRON_SECOND_MIRROR_ITEM =
        ITEMS.register("iron_second_mirror", () ->
            new lensBarrelBlockItem(BlockRegistry.IRON_SECOND_MIRROR_BLOCK.get(), new Item.Properties()));
    
    public static final RegistryObject<Item> SILVER_MAIN_MIRROR_ITEM =
        ITEMS.register("silver_main_mirror", () ->
            new lensBarrelBlockItem(BlockRegistry.SILVER_MAIN_MIRROR_BLOCK.get(), new Item.Properties()));
    
    public static final RegistryObject<Item> SILVER_SECOND_MIRROR_ITEM =
        ITEMS.register("silver_second_mirror", () ->
            new lensBarrelBlockItem(BlockRegistry.SILVER_SECOND_MIRROR_BLOCK.get(), new Item.Properties()));*/

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
        /*public static final RegistryObject<ChalkItem> CHALK_ITEM =
                ITEMS.register("chalk", () -> new ChalkItem(new Item.Properties()));*/

    public static final RegistryObject<CompassItem> COMPASS_ITEM =
        ITEMS.register("compass",() -> new CompassItem(new Item.Properties()));
        /*public static final RegistryObject<RulerItem> RULER_ITEM =
            ITEMS.register("ruler",() -> new RulerItem(new Item.Properties()));*/

        //模造天体コア
    public static final RegistryObject<Item> IMITATION_CORE_ITEM =
        ITEMS.register("imitation_core", () -> new ImitationCoreItem(new Item.Properties()));

        //金属
    public static final RegistryObject<Item> ALCHEMY_BIRON_INGOT =
        ITEMS.register("alchemy_biron_ingot", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> ALCHEMY_BIRON_NUGGET =
        ITEMS.register("alchemy_biron_nugget", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> GLOWSTONE_ALLOY_INGOT =
        ITEMS.register("glowstone_alloy_ingot", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> GLOWSTONE_ALLOY_NUGGET =
        ITEMS.register("glowstone_alloy_nugget", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> RAW_SILVER =
        ITEMS.register("raw_silver", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> SILVER_INGOT =
        ITEMS.register("silver_ingot", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> SILVER_NUGGET =
        ITEMS.register("silver_nugget", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> QUINCE_STEEL_INGOT =
        ITEMS.register("quince_steel_ingot", () -> new Item(new Item.Properties()));

        // 素材
    public static final RegistryObject<Item> OPTICAL_GLASS =
        ITEMS.register("optical_glass", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> THERMOIMMOBILANT =
        ITEMS.register("thermoimmobilant", () -> new Item(new Item.Properties()));

    //鉱石
    public static final RegistryObject<Item> CELESTITE =
        ITEMS.register("celestite_shard",() -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> MOONSTONE =
        ITEMS.register("moonstone",() -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> QUICKSILVER =
        ITEMS.register("quicksilver",() -> new Item(new Item.Properties()));
}
