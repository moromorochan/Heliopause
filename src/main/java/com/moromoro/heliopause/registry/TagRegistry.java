package com.moromoro.heliopause.registry;

import com.moromoro.Heliopause;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class TagRegistry {
    public static class Items {
        //public static final TagKey<Item> RESONANCE_3_4 = block("3_4_resonance");
        /*public static final TagKey<Item> LENS_BARREL = tagItem("lens_barrel");
        public static final TagKey<Item> MAIN_MIRROR = tagItem("main_mirror");
        public static final TagKey<Item> SECOND_MIRROR = tagItem("secondary_mirror");*/
        public static final TagKey<Item> FORGE_SILVER_ORE = ItemTags.create(new ResourceLocation("forge","ores/silver"));
        public static final TagKey<Item> FORGE_RAW_SILVER_BLOCK = ItemTags.create(new ResourceLocation("forge","storage_blocks/raw_silver"));
        public static final TagKey<Item> FORGE_RAW_SILVER_ITEM = ItemTags.create(new ResourceLocation("forge", "raw_materials/silver"));
        public static final TagKey<Item> FORGE_SILVER_INGOTS = ItemTags.create(new ResourceLocation("forge", "ingots/silver"));
        public static final TagKey<Item> FORGE_SILVER_NUGGETS = ItemTags.create(new ResourceLocation("forge", "nuggets/silver"));
    }

    public static class Blocks {
        public static final TagKey<Block> LENS_BARREL = tagBlock("lens_barrel");

        public static final TagKey<Block> MAIN_MIRROR = tagBlock("main_mirror");
        public static final TagKey<Block> SECOND_MIRROR = tagBlock("secondary_mirror");

        // 互換性
        public static final TagKey<Block> FORGE_ORE_STONE = BlockTags.create(new ResourceLocation("forge","ores_in_ground/stone"));
        public static final TagKey<Block> FORGE_ORE_DEEPSLATE = BlockTags.create(new ResourceLocation("forge","ores_in_ground/deepslate"));
        public static final TagKey<Block> FORGE_SILVER_ORE = BlockTags.create(new ResourceLocation("forge","ores/silver"));
        public static final TagKey<Block> FORGE_RAW_SILVER = BlockTags.create(new ResourceLocation("forge","storage_blocks/raw_silver"));
        public static final TagKey<Block> FORGE_SILVER_BLOCK = BlockTags.create(new ResourceLocation("forge", "storage_blocks/silver"));
        
        public static final TagKey<Block> CREATE_BRITTLE = BlockTags.create(new ResourceLocation("create", "brittle"));
    }

    private static TagKey<Item> tagItem(String name) {
        return ItemTags.create(new ResourceLocation(Heliopause.MODID, name));
    }

    private static TagKey<Block> tagBlock(String name) {
        return BlockTags.create(new ResourceLocation(Heliopause.MODID, name));
    }
}
