package com.moromoro.heliopause.datagen;

import com.moromoro.Heliopause;
import com.moromoro.heliopause.block.LensBarrelBlock;
import com.moromoro.heliopause.recipe.MoonlightPouringRecipe;
import com.moromoro.heliopause.recipe.MoonlightPouringRecipeBuilder;
import com.moromoro.heliopause.registry.BlockRegistry;
import com.moromoro.heliopause.registry.FluidRegistry;
import com.moromoro.heliopause.registry.ItemRegistry;
import com.moromoro.heliopause.registry.TagRegistry;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
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
import net.minecraftforge.registries.RegistryObject;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class HeliopauseRecipeProvider extends net.minecraft.data.recipes.RecipeProvider implements IConditionBuilder {

    public HeliopauseRecipeProvider(PackOutput p_248933_) {
        super(p_248933_);
    }

    @Override
    protected void buildRecipes(Consumer<FinishedRecipe> consumer) {

        //金属ブロック、インゴット、ナゲット変換
        metalBlockIngotNuggetRecipe(consumer,
            BlockRegistry.ALCHEMY_BIRON_BLOCK.get(), ItemRegistry.ALCHEMY_BIRON_INGOT.get(), ItemRegistry.ALCHEMY_BIRON_NUGGET.get());
        metalBlockIngotNuggetRecipe(consumer,
            BlockRegistry.GLOWSTONE_ALLOY_BLOCK.get(), ItemRegistry.GLOWSTONE_ALLOY_INGOT.get(), ItemRegistry.GLOWSTONE_ALLOY_NUGGET.get());
        metalBlockIngotNuggetRecipe(consumer,
            BlockRegistry.SILVER_BLOCK.get(), ItemRegistry.SILVER_INGOT.get(), ItemRegistry.SILVER_NUGGET.get());
        threeByThreePacker(consumer, RecipeCategory.MISC, BlockRegistry.RAW_SILVER_BLOCK.get(), ItemRegistry.RAW_SILVER.get());
        unPackerRecipe(consumer, RecipeCategory.MISC, ItemRegistry.RAW_SILVER.get(), 9, BlockRegistry.RAW_SILVER_BLOCK.get());
        oreSmelting(consumer, List.of(BlockRegistry.SILVER_ORE_BLOCK.get(), BlockRegistry.DEEPSLATE_SILVER_ORE_BLOCK.get(), ItemRegistry.RAW_SILVER.get()), RecipeCategory.MISC, ItemRegistry.SILVER_INGOT.get(), 1.0f, 200, "silver");
        oreBlasting(consumer, List.of(BlockRegistry.SILVER_ORE_BLOCK.get(), BlockRegistry.DEEPSLATE_SILVER_ORE_BLOCK.get(), ItemRegistry.RAW_SILVER.get()), RecipeCategory.MISC, ItemRegistry.SILVER_INGOT.get(), 1.0f, 100, "silver");
        
        // 磨かれた錬金赤銅ブロック
        unPackerRecipe(consumer, RecipeCategory.MISC, ItemRegistry.ALCHEMY_BIRON_INGOT.get(), 9, BlockRegistry.POLISHED_BIRON_BLOCK.get());
        
        // 模様入り錬金赤銅ブロック
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, BlockRegistry.CHISELED_BIRON_BLOCK.get(),4)
            .pattern(" B ")
            .pattern("BAB")
            .pattern(" B ")
            .define('A', TagRegistry.Items.ALCHEMY_BIRON_BLOCKS)
            .define('B', ItemRegistry.ALCHEMY_BIRON_INGOT.get())
            .unlockedBy("has_alchemy_biron", has(ItemRegistry.ALCHEMY_BIRON_INGOT.get()))
            .save(consumer, new ResourceLocation(Heliopause.MODID, "crafting/"+ BlockRegistry.CHISELED_BIRON_BLOCK.getId().getPath()));
        // 模様入り 石切りから
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(TagRegistry.Items.ALCHEMY_BIRON_BLOCKS), RecipeCategory.BUILDING_BLOCKS, BlockRegistry.CHISELED_BIRON_BLOCK.get(), 3)
            .unlockedBy("has_alchemy_biron", has(ItemRegistry.ALCHEMY_BIRON_INGOT.get()))
            .save(consumer, new ResourceLocation(Heliopause.MODID, "stone_cutting/"+ BlockRegistry.CHISELED_BIRON_BLOCK.getId().getPath()));
        // リサイクル
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ItemRegistry.ALCHEMY_BIRON_INGOT.get(), 3)
            .requires(BlockRegistry.CHISELED_BIRON_BLOCK.get())
            .unlockedBy("has_alchemy_biron", has(ItemRegistry.ALCHEMY_BIRON_INGOT.get()))
            .save(consumer, new ResourceLocation(Heliopause.MODID, "crafting/"+ BlockRegistry.CHISELED_BIRON_BLOCK.getId().getPath()+"_recycling"));
        
        // 錬金赤銅ガラス
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, BlockRegistry.ALCHEMY_BIRON_GLASS.get(),4)
            .pattern(" B ")
            .pattern("BAB")
            .pattern(" B ")
            .define('A', Tags.Items.GLASS_COLORLESS)
            .define('B', ItemRegistry.ALCHEMY_BIRON_INGOT.get())
            .unlockedBy("has_alchemy_biron", has(ItemRegistry.ALCHEMY_BIRON_INGOT.get()))
            .save(consumer, new ResourceLocation(Heliopause.MODID, "crafting/"+ BlockRegistry.ALCHEMY_BIRON_GLASS.getId().getPath()));
        
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
        
        // ホイロ
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, BlockRegistry.ROASTING_TABLE.get())
            .pattern("ABA")
            .pattern("ACA")
            .define('A', ItemTags.PLANKS)
            .define('B', Items.PAPER)
            .define('C', Items.FURNACE)
            .unlockedBy("has_charcoal", has(Items.CHARCOAL))
            .save(consumer, new ResourceLocation(Heliopause.MODID, "crafting/"+ BlockRegistry.ROASTING_TABLE.getId().getPath()));
        
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
        
        // コイル
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ItemRegistry.GRAVITY_COIL.get(), 4)
            .pattern("A")
            .pattern("A")
            .define('A', ItemRegistry.GLOWSTONE_ALLOY_INGOT.get())
            .unlockedBy("has_glowstone_alloy", has(ItemRegistry.GLOWSTONE_ALLOY_INGOT.get()))
            .save(consumer, new ResourceLocation(Heliopause.MODID, "crafting/"+ItemRegistry.GRAVITY_COIL.getId().getPath()));
        
        // 反重力ディスペンサー
        /*ShapedRecipeBuilder.shaped(RecipeCategory.TRANSPORTATION, BlockRegistry.INGREDIENT_DISPENSER.get())
            .pattern("A A")
            .pattern("B B")
            .pattern("A A")
            .define('A', Tags.Items.INGOTS_IRON)
            .define('B', ItemRegistry.GRAVITY_COIL.get())
            .unlockedBy("has_glowstone_alloy", has(ItemRegistry.GLOWSTONE_ALLOY_INGOT.get()))
            .save(consumer, new ResourceLocation(Heliopause.MODID, "crafting/"+BlockRegistry.INGREDIENT_DISPENSER.getId().getPath()));*/
        
        // 反重力コレクター
        /*ShapedRecipeBuilder.shaped(RecipeCategory.TRANSPORTATION, BlockRegistry.INGREDIENT_COLLECTOR.get())
            .pattern("ABA")
            .define('A', Tags.Items.INGOTS_IRON)
            .define('B', ItemRegistry.GRAVITY_COIL.get())
            .unlockedBy("has_glowstone_alloy", has(ItemRegistry.GLOWSTONE_ALLOY_INGOT.get()))
            .save(consumer, new ResourceLocation(Heliopause.MODID, "crafting/"+BlockRegistry.INGREDIENT_COLLECTOR.getId().getPath()));*/
        
        // 模造天体コア
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ItemRegistry.IMITATION_CORE_ITEM.get())
            .requires(Items.PAPER)
            .unlockedBy("has_siderostat", has(BlockRegistry.SIDEROSTAT_TOP.get()))
            .save(consumer, new ResourceLocation(Heliopause.MODID, "crafting/"+ItemRegistry.IMITATION_CORE_ITEM.getId().getPath()));

        // 月明かり注入
        // 天青石
        MoonlightPouringRecipeBuilder.moonlightPouring(
                Ingredient.of(Tags.Items.SAND),
                ItemRegistry.CELESTITE.get(),1,
                600
            ).unlockedBy("has_sand", has(Tags.Items.SAND))
            .save(consumer, new ResourceLocation(Heliopause.MODID,MoonlightPouringRecipe.Type.ID +"/"+ ItemRegistry.CELESTITE.getId().getPath()));

        // 錬金赤銅インゴット
        MoonlightPouringRecipeBuilder.moonlightPouring(
                Ingredient.of(Tags.Items.INGOTS_COPPER),
                ItemRegistry.ALCHEMY_BIRON_INGOT.get(),1,
                600
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
                    400
                ).unlockedBy("has_sapling", has(sapling))
                .save(consumer, new ResourceLocation(Heliopause.MODID, MoonlightPouringRecipe.Type.ID +"/"+ sapling));
        }

        // 方解石
        MoonlightPouringRecipeBuilder.moonlightPouring(
            Ingredient.of(ItemRegistry.CELESTITE.get()),
            Items.CALCITE,1,
            600
            ).unlockedBy("has_celestite", has(ItemRegistry.CELESTITE.get()))
            .save(consumer, new ResourceLocation(Heliopause.MODID, MoonlightPouringRecipe.Type.ID + "/" + Items.CALCITE));
        
        // 液化星明かり分離
        SimpleCookingRecipeBuilder.smelting(
            Ingredient.of(FluidRegistry.STARRY_MIXTURE.bucket().get()),
            RecipeCategory.MISC, FluidRegistry.LIQUEFIED_TWILIGHT.bucket().get(), 1.0f, 200)
            .unlockedBy("has_starry_mixture", has(FluidRegistry.STARRY_MIXTURE.bucket().get()))
            .save(consumer, new ResourceLocation(Heliopause.MODID, FluidRegistry.LIQUEFIED_TWILIGHT.bucket().getId().getPath()+"_from_smelting"));
        MoonlightPouringRecipeBuilder.moonlightPouring(
                Ingredient.of(FluidRegistry.STARRY_MIXTURE.bucket().get()),
                FluidRegistry.LIQUEFIED_STARLIGHT.bucket().get(),1,
                200
            ).unlockedBy("has_starry_mixture", has(FluidRegistry.STARRY_MIXTURE.bucket().get()))
            .save(consumer, new ResourceLocation(Heliopause.MODID, FluidRegistry.LIQUEFIED_STARLIGHT.bucket().getId().getPath()+"_from_moonlight_pouring"));
        
        // 鏡筒・鏡
        
        // 木製
        {
            String unlock = "has_optical_glass";
            InventoryChangeTrigger.TriggerInstance trigger = has(ItemRegistry.OPTICAL_GLASS.get());
            RegistryObject<LensBarrelBlock> barrel = BlockRegistry.WOODEN_LENS_BARREL_BLOCK;
            RegistryObject<LensBarrelBlock> main = BlockRegistry.IRON_MAIN_MIRROR_BLOCK;
            RegistryObject<LensBarrelBlock> second = BlockRegistry.IRON_SECOND_MIRROR_BLOCK;
            
            ShapedRecipeBuilder.shaped(RecipeCategory.MISC, barrel.get())
                .pattern("ABA")
                .define('A', ItemTags.PLANKS)
                .define('B', Items.BARREL)
                .unlockedBy(unlock, trigger)
                .save(consumer, new ResourceLocation(Heliopause.MODID, "crafting/"+barrel.getId().getPath()));
            ShapedRecipeBuilder.shaped(RecipeCategory.MISC, main.get())
                .pattern("C")
                .pattern("A")
                .pattern("B")
                .define('A', barrel.get())
                .define('B', ItemRegistry.OPTICAL_GLASS.get())
                .define('C', Tags.Items.INGOTS_IRON)
                .unlockedBy(unlock, trigger)
                .save(consumer, new ResourceLocation(Heliopause.MODID, "crafting/"+main.getId().getPath()));
            ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, second.get())
                .requires(main.get())
                .unlockedBy(unlock, trigger)
                .save(consumer, new ResourceLocation(Heliopause.MODID, "crafting/"+second.getId().getPath()+"_from_main_mirror"));
            ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, main.get())
                .requires(second.get())
                .unlockedBy(unlock, trigger)
                .save(consumer, new ResourceLocation(Heliopause.MODID, "crafting/"+main.getId().getPath()+"_from_second_mirror"));
        }
        // 石製
        {
            String unlock = "has_optical_glass";
            InventoryChangeTrigger.TriggerInstance trigger = has(ItemRegistry.OPTICAL_GLASS.get());
            RegistryObject<LensBarrelBlock> barrel = BlockRegistry.STONE_LENS_BARREL_BLOCK;
            RegistryObject<LensBarrelBlock> main = BlockRegistry.GRAPHITE_MAIN_MIRROR_BLOCK;
            RegistryObject<LensBarrelBlock> second = BlockRegistry.GRAPHITE_SECOND_MIRROR_BLOCK;
            
            ShapedRecipeBuilder.shaped(RecipeCategory.MISC, barrel.get())
                .pattern("ABA")
                .define('A', Items.STONE_BRICKS)
                .define('B', Items.BARREL)
                .unlockedBy(unlock, trigger)
                .save(consumer, new ResourceLocation(Heliopause.MODID, "crafting/"+barrel.getId().getPath()));
            ShapedRecipeBuilder.shaped(RecipeCategory.MISC, main.get())
                .pattern("C")
                .pattern("A")
                .pattern("B")
                .define('A', barrel.get())
                .define('B', ItemRegistry.OPTICAL_GLASS.get())
                .define('C', Items.CHARCOAL)
                .unlockedBy(unlock, trigger)
                .save(consumer, new ResourceLocation(Heliopause.MODID, "crafting/"+main.getId().getPath()));
            ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, second.get())
                .requires(main.get())
                .unlockedBy(unlock, trigger)
                .save(consumer, new ResourceLocation(Heliopause.MODID, "crafting/"+second.getId().getPath()+"_from_main_mirror"));
            ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, main.get())
                .requires(second.get())
                .unlockedBy(unlock, trigger)
                .save(consumer, new ResourceLocation(Heliopause.MODID, "crafting/"+main.getId().getPath()+"_from_second_mirror"));
        }
        // 錬金赤銅
        {
            String unlock = "has_silver";
            InventoryChangeTrigger.TriggerInstance trigger = has(ItemRegistry.SILVER_INGOT.get());
            RegistryObject<LensBarrelBlock> barrel = BlockRegistry.ALCHEMY_BIRON_LENS_BARREL_BLOCK;
            RegistryObject<LensBarrelBlock> main = BlockRegistry.SILVER_MAIN_MIRROR_BLOCK;
            RegistryObject<LensBarrelBlock> second = BlockRegistry.SILVER_SECOND_MIRROR_BLOCK;
            
            ShapedRecipeBuilder.shaped(RecipeCategory.MISC, barrel.get())
                .pattern("ABA")
                .define('A', ItemRegistry.ALCHEMY_BIRON_INGOT.get())
                .define('B', Items.BARREL)
                .unlockedBy(unlock, trigger)
                .save(consumer, new ResourceLocation(Heliopause.MODID, "crafting/"+barrel.getId().getPath()));
            ShapedRecipeBuilder.shaped(RecipeCategory.MISC, main.get())
                .pattern("C")
                .pattern("A")
                .pattern("B")
                .define('A', barrel.get())
                .define('B', ItemRegistry.OPTICAL_GLASS.get())
                .define('C', TagRegistry.Items.FORGE_SILVER_INGOTS)
                .unlockedBy(unlock, trigger)
                .save(consumer, new ResourceLocation(Heliopause.MODID, "crafting/"+main.getId().getPath()));
            ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, second.get())
                .requires(main.get())
                .unlockedBy(unlock, trigger)
                .save(consumer, new ResourceLocation(Heliopause.MODID, "crafting/"+second.getId().getPath()+"_from_main_mirror"));
            ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, main.get())
                .requires(second.get())
                .unlockedBy(unlock, trigger)
                .save(consumer, new ResourceLocation(Heliopause.MODID, "crafting/"+main.getId().getPath()+"_from_second_mirror"));
        }
        // 超断熱材
        {
            String unlock = "has_thermoimmobilant";
            InventoryChangeTrigger.TriggerInstance trigger = has(ItemRegistry.THERMOIMMOBILANT.get());
            RegistryObject<LensBarrelBlock> barrel = BlockRegistry.THERMOIMMOBILANT_LENS_BARREL_BLOCK;
            RegistryObject<LensBarrelBlock> main = BlockRegistry.ALCHEMY_STEEL_MAIN_MIRROR_BLOCK;
            RegistryObject<LensBarrelBlock> second = BlockRegistry.ALCHEMY_STEEL_SECOND_MIRROR_BLOCK;
            
            ShapedRecipeBuilder.shaped(RecipeCategory.MISC, barrel.get())
                .pattern("ABA")
                .define('A', ItemRegistry.THERMOIMMOBILANT.get())
                .define('B', Items.BARREL)
                .unlockedBy(unlock, trigger)
                .save(consumer, new ResourceLocation(Heliopause.MODID, "crafting/"+barrel.getId().getPath()));
            ShapedRecipeBuilder.shaped(RecipeCategory.MISC, main.get())
                .pattern(" C ")
                .pattern("DAD")
                .pattern(" B ")
                .define('A', barrel.get())
                .define('B', ItemRegistry.OPTICAL_GLASS.get())
                .define('C', ItemRegistry.ALCHEMY_STEEL_INGOT.get())
                .define('D', ItemRegistry.GRAVITY_COIL.get())
                .unlockedBy(unlock, trigger)
                .save(consumer, new ResourceLocation(Heliopause.MODID, "crafting/"+main.getId().getPath()));
            ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, second.get())
                .requires(main.get())
                .unlockedBy(unlock, trigger)
                .save(consumer, new ResourceLocation(Heliopause.MODID, "crafting/"+second.getId().getPath()+"_from_main_mirror"));
            ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, main.get())
                .requires(second.get())
                .unlockedBy(unlock, trigger)
                .save(consumer, new ResourceLocation(Heliopause.MODID, "crafting/"+main.getId().getPath()+"_from_second_mirror"));
        }
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
