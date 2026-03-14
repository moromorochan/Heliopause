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
        //public static final TagKey<Item> RESONANCE_3_4 = tag("3_4_resonance");
    }

    public static class Blocks {
        public static final TagKey<Block> WOODEN_LENS_BARREL = tagBlock("lens_barrel_wooden");
        public static final TagKey<Block> BIRON_LENS_BARREL = tagBlock("lens_barrel_alchemy_biron");

        public static final TagKey<Block> MAIN_MIRROR = tagBlock("main_mirror");
        public static final TagKey<Block> SECOND_MIRROR = tagBlock("secondary_mirror");

        // 互換性(仮)
        public static final TagKey<Block> CREATE_BRITTLE = BlockTags.create(new ResourceLocation("create", "brittle"));
    }

    private static TagKey<Item> tagItem(String name) {
        return ItemTags.create(new ResourceLocation(Heliopause.MODID, name));
    }

    private static TagKey<Block> tagBlock(String name) {
        return BlockTags.create(new ResourceLocation(Heliopause.MODID, name));
    }
}
