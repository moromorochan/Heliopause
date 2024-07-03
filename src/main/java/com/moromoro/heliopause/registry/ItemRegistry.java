package com.moromoro.heliopause.registry;

import com.moromoro.Heliopause;
import com.moromoro.heliopause.item.FluidBottle;
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

        //金属ブロック
    public static final RegistryObject<Item> ALCHEMY_BIRON_BLOCK_ITEM =
            ITEMS.register("alchemy_biron_block", () -> new BlockItem(BlockRegistry.ALCHEMY_BIRON_BLOCK.get(), new Item.Properties()));
    public static final RegistryObject<Item> GLOWSTONE_ALLOY_BLOCK_ITEM =
            ITEMS.register("glowstone_alloy_block", () -> new BlockItem(BlockRegistry.GLOWSTONE_ALLOY_BLOCK.get(), new Item.Properties()));

    // アイテムの作成
        //容器
    public static final RegistryObject<FluidBottle> VIAL_ITEM =
            ITEMS.register("vial",()->new FluidBottle(new Item.Properties(),500,50));
    public static final RegistryObject<FluidBottle> LARGE_BOTTLE_ITEM =
            ITEMS.register("large_bottle",()->new FluidBottle(new Item.Properties(),2000,200));
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
