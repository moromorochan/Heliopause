package com.moromoro.heliopause.datagen;

import com.moromoro.Heliopause;
import com.moromoro.heliopause.item.ImitationCoreItem;
import com.moromoro.heliopause.recipe.MoonlightPouringRecipe;
import com.moromoro.heliopause.recipe.MoonlightPouringRecipeBuilder;
import com.moromoro.heliopause.registry.BlockRegistry;
import com.moromoro.heliopause.registry.ItemRegistry;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.crafting.conditions.IConditionBuilder;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

public class HEPRecipeProvider extends net.minecraft.data.recipes.RecipeProvider implements IConditionBuilder {

    public HEPRecipeProvider(PackOutput p_248933_) {
        super(p_248933_);
    }

    @Override
    protected void buildRecipes(Consumer<FinishedRecipe> consumer) {

        //金属ブロック、インゴット、ナゲット変換
        metalBlockIngotNuggetRecipe(consumer,
            BlockRegistry.ALCHEMY_BIRON_BLOCK.get(), ItemRegistry.ALCHEMY_BIRON_INGOT.get(), ItemRegistry.ALCHEMY_BIRON_NUGGET.get());
        metalBlockIngotNuggetRecipe(consumer,
            BlockRegistry.GLOWSTONE_ALLOY_BLOCK.get(), ItemRegistry.GLOWSTONE_ALLOY_INGOT.get(), ItemRegistry.GLOWSTONE_ALLOY_NUGGET.get());

        //錬金赤銅の初期ステージ作成
        /*ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ItemRegistry.ALCHEMY_BIRON_INGOT.get())
            .pattern("AB")
            .pattern("BA")
            .define('A', Tags.Items.INGOTS_COPPER)
            .define('B', Tags.Items.NUGGETS_GOLD)
            .unlockedBy("has_copper", has(Tags.Items.INGOTS_COPPER))
            .unlockedBy("has_gold", has(Tags.Items.INGOTS_GOLD))
            .save(consumer, new ResourceLocation(Heliopause.MODID, "crafting/"+ ItemRegistry.ALCHEMY_BIRON_INGOT.getId().getPath()));
*/
        // 黒板
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, BlockRegistry.BLACKBOARD.get(),16)
            .pattern("AAA")
            .pattern("BBB")
            .define('A', Items.CHARCOAL)
            .define('B', ItemTags.LOGS)
            .unlockedBy("has_charcoal", has(Items.CHARCOAL))
            .save(consumer, new ResourceLocation(Heliopause.MODID, "crafting/"+ BlockRegistry.BLACKBOARD.getId().getPath()));

        // コンパス
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ItemRegistry.COMPASS_ITEM.get())
            .pattern(" A ")
            .pattern("A B")
            .define('A', ItemRegistry.ALCHEMY_BIRON_INGOT.get())
            .define('B', Items.CALCITE)
            .unlockedBy("has_charcoal", has(Items.CHARCOAL))
            .save(consumer, new ResourceLocation(Heliopause.MODID, "crafting/"+ ItemRegistry.COMPASS_ITEM.getId().getPath()));

        // シデロスタット
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, BlockRegistry.SIDEROSTAT_TOP.get())
            .pattern("ABA")
            .pattern("ACA")
            .pattern(" D ")
            .define('A', ItemTags.PLANKS)
            .define('B', Tags.Items.GLASS_COLORLESS)
            .define('C', Tags.Items.INGOTS_IRON)
            .define('D', ItemTags.WOODEN_SLABS)
            .unlockedBy("has_glass", has(Tags.Items.GLASS_COLORLESS))
            .save(consumer, new ResourceLocation(Heliopause.MODID,"crafting/"+ BlockRegistry.SIDEROSTAT_TOP.getId().getPath()));

        //るつぼ
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, BlockRegistry.CRUCIBLE.get())
            .pattern("A A")
            .pattern("A A")
            .pattern("AAA")
            .define('A', ItemRegistry.ALCHEMY_BIRON_INGOT.get())
            .unlockedBy("has_biron", has(ItemRegistry.ALCHEMY_BIRON_INGOT.get()))
            .save(consumer, new ResourceLocation(Heliopause.MODID, "crafting/"+BlockRegistry.CRUCIBLE.getId().getPath()));

        // 月明かり注入
        // 天青石
        MoonlightPouringRecipeBuilder.moonlightPouring(
                Ingredient.of(Tags.Items.SAND),
                ItemRegistry.CELESTITE.get(),1,
                600,0.1f
            ).unlockedBy("has_sand", has(Tags.Items.SAND))
            .save(consumer, new ResourceLocation(Heliopause.MODID,MoonlightPouringRecipe.Type.ID +"/"+ ItemRegistry.CELESTITE.getId().getPath()));

        // 錬金赤銅インゴット
        MoonlightPouringRecipeBuilder.moonlightPouring(
                Ingredient.of(Tags.Items.INGOTS_COPPER),
                ItemRegistry.ALCHEMY_BIRON_INGOT.get(),1,
                600,0.1f
            ).unlockedBy("has_copper", has(Tags.Items.INGOTS_COPPER))
            .save(consumer, new ResourceLocation(Heliopause.MODID,MoonlightPouringRecipe.Type.ID +"/"+ ItemRegistry.ALCHEMY_BIRON_INGOT.getId().getPath()));

        // 苗木から葉ブロック
        // マップを用意
        Map<Item, Item> saplingRecipeSets = new LinkedHashMap<>();
        saplingRecipeSets.put(Items.OAK_SAPLING, Blocks.OAK_LEAVES.asItem());
        saplingRecipeSets.put(Items.SPRUCE_SAPLING, Blocks.SPRUCE_LEAVES.asItem());
        saplingRecipeSets.put(Items.BIRCH_SAPLING, Blocks.BIRCH_LEAVES.asItem());
        saplingRecipeSets.put(Items.JUNGLE_SAPLING, Blocks.JUNGLE_LEAVES.asItem());
        saplingRecipeSets.put(Items.ACACIA_SAPLING, Blocks.ACACIA_LEAVES.asItem());
        saplingRecipeSets.put(Items.DARK_OAK_SAPLING, Blocks.DARK_OAK_LEAVES.asItem());
        saplingRecipeSets.put(Items.MANGROVE_PROPAGULE, Blocks.MANGROVE_LEAVES.asItem());
        saplingRecipeSets.put(Items.CHERRY_SAPLING, Blocks.CHERRY_LEAVES.asItem());
        saplingRecipeSets.put(Items.AZALEA, Blocks.AZALEA_LEAVES.asItem());
        saplingRecipeSets.put(Items.FLOWERING_AZALEA,Blocks.FLOWERING_AZALEA_LEAVES.asItem());

        for (Map.Entry<Item, Item> recipeSet : saplingRecipeSets.entrySet()) {
            Item sapling = recipeSet.getKey();
            Item leaves = recipeSet.getValue();

            MoonlightPouringRecipeBuilder.moonlightPouring(
                    Ingredient.of(sapling),
                    leaves, 1,
                    400, 0.1f
                ).unlockedBy("has_sapling", has(sapling))
                .save(consumer, new ResourceLocation(Heliopause.MODID, MoonlightPouringRecipe.Type.ID +"/"+ sapling.toString()));
        }

        // 方解石
        MoonlightPouringRecipeBuilder.moonlightPouring(
            Ingredient.of(ItemRegistry.CELESTITE.get()),
            Items.CALCITE,1,
            600,0.1f
        ).unlockedBy("has_celestite", has(ItemRegistry.CELESTITE.get()))
            .save(consumer, new ResourceLocation(Heliopause.MODID, MoonlightPouringRecipe.Type.ID + "/" + Items.CALCITE));

        // 衛星コア
        /*MoonlightPouringRecipeBuilder.moonlightPouring(
                Ingredient.of(ItemRegistry.IMITATION_CORE_ITEM.get()),
                ImitationCoreItem.getImitationCoreWithTag("satellite"),
                600,0.1f
            ).unlockedBy("has_imitation_core", has(ItemRegistry.IMITATION_CORE_ITEM.get()))
            .save(consumer, new ResourceLocation(Heliopause.MODID, MoonlightPouringRecipe.Type.ID + "/" + ItemRegistry.IMITATION_CORE_ITEM.getId().getPath()+"_satellite"));
*/
    }

    protected static void metalBlockIngotNuggetRecipe(Consumer<FinishedRecipe> consumer, ItemLike BlockItem, ItemLike IngotItem, ItemLike NuggetItem) {
        //圧縮レシピ
        threeByThreePacker(consumer, RecipeCategory.MISC, IngotItem, NuggetItem);
        threeByThreePacker(consumer, RecipeCategory.BUILDING_BLOCKS, BlockItem, IngotItem);
        //展開レシピ
        unPackerRecipe(consumer, RecipeCategory.MISC, IngotItem, 9, BlockItem);
        unPackerRecipe(consumer, RecipeCategory.MISC, NuggetItem, 9, IngotItem);
    }

    protected static void unPackerRecipe(Consumer<FinishedRecipe> consumer, RecipeCategory recipeCategory, ItemLike resultItem, int resultCount, ItemLike ingredientItem) {
        ShapelessRecipeBuilder.shapeless(recipeCategory, resultItem, resultCount).requires(ingredientItem)
            .unlockedBy(getHasName(ingredientItem), has(ingredientItem))
            .save(consumer, new ResourceLocation(Heliopause.MODID, ForgeRegistries.ITEMS.getKey(resultItem.asItem()).getPath() + "_unpack_from_" + ForgeRegistries.ITEMS.getKey(ingredientItem.asItem()).getPath()));
    }


}
