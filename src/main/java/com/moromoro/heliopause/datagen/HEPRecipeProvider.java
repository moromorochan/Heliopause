package com.moromoro.heliopause.datagen;

import com.moromoro.Heliopause;
import com.moromoro.heliopause.registry.BlockRegistry;
import com.moromoro.heliopause.registry.ItemRegistry;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.crafting.conditions.IConditionBuilder;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Consumer;

public class HEPRecipeProvider extends net.minecraft.data.recipes.RecipeProvider implements IConditionBuilder {

    public HEPRecipeProvider(PackOutput p_248933_) {
        super(p_248933_);
    }

    @Override
    protected void buildRecipes(Consumer<FinishedRecipe> consumer) {

        //金属ブロック、インゴット、ナゲット変換
        metalBlockIngotNuggetRecipe(consumer,
            BlockRegistry.ALCHEMY_BIRON_BLOCK.get(),ItemRegistry.ALCHEMY_BIRON_INGOT.get(),ItemRegistry.ALCHEMY_BIRON_NUGGET.get());
        metalBlockIngotNuggetRecipe(consumer,
            BlockRegistry.GLOWSTONE_ALLOY_BLOCK.get(),ItemRegistry.GLOWSTONE_ALLOY_INGOT.get(),ItemRegistry.GLOWSTONE_ALLOY_NUGGET.get());

        //錬金赤銅の初期ステージ作成
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ItemRegistry.ALCHEMY_BIRON_INGOT.get())
            .pattern("AB")
            .pattern("BA")
            .define('A', Tags.Items.INGOTS_COPPER)
            .define('B', Tags.Items.NUGGETS_GOLD)
            .unlockedBy("has_copper",has(Tags.Items.INGOTS_COPPER))
            .unlockedBy("has_gold",has(Tags.Items.INGOTS_GOLD))
            .save(consumer, new ResourceLocation(Heliopause.MODID, ItemRegistry.ALCHEMY_BIRON_INGOT.getId().getPath()) + "_crafting");

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, BlockRegistry.SIDEROSTAT_TOP.get())
            .pattern("ABA")
            .pattern("ACA")
            .pattern(" D ")
            .define('A', ItemTags.PLANKS)
            .define('B',Tags.Items.GLASS_COLORLESS)
            .define('C',Tags.Items.INGOTS_IRON)
            .define('D',ItemTags.WOODEN_SLABS)
            .unlockedBy("has_glass",has(Tags.Items.GLASS_COLORLESS))
            .save(consumer, new ResourceLocation(Heliopause.MODID, BlockRegistry.SIDEROSTAT_TOP.getId().getPath()));

    }

    protected static void metalBlockIngotNuggetRecipe(Consumer<FinishedRecipe> consumer, ItemLike BlockItem, ItemLike IngotItem, ItemLike NuggetItem){
        //圧縮レシピ
        threeByThreePacker(consumer, RecipeCategory.MISC, IngotItem, NuggetItem);
        threeByThreePacker(consumer, RecipeCategory.BUILDING_BLOCKS, BlockItem, IngotItem);
        //展開レシピ
        unPackerRecipe(consumer, RecipeCategory.MISC, IngotItem,9,BlockItem);
        unPackerRecipe(consumer, RecipeCategory.MISC, NuggetItem,9,IngotItem);
    }
    protected static void unPackerRecipe(Consumer<FinishedRecipe> consumer, RecipeCategory recipeCategory, ItemLike resultItem, int resultCount, ItemLike ingredientItem){
        ShapelessRecipeBuilder.shapeless(recipeCategory,resultItem,resultCount).requires(ingredientItem)
            .unlockedBy(getHasName(ingredientItem),has(ingredientItem))
            .save(consumer,new ResourceLocation(Heliopause.MODID, ForgeRegistries.ITEMS.getKey(resultItem.asItem()).getPath() + "_unpack_from_" + ForgeRegistries.ITEMS.getKey(ingredientItem.asItem()).getPath()));
    }
}
