package com.moromoro.heliopause.registry;

import com.moromoro.Heliopause;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class TagRegistry {
    public static class Items {
        //public static final TagKey<Item> RESONANCE_3_4 = tag("3_4_resonance");
    }

    private static TagKey<Item> tag(String name) {
        return ItemTags.create(new ResourceLocation(Heliopause.MODID, name));
    }
}
