package com.moromoro.heliopause.registry;

import com.moromoro.Heliopause;
import com.moromoro.heliopause.block.*;
import com.moromoro.heliopause.item.LensBarrelBlockItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Function;
import java.util.function.Supplier;

public class BlockRegistry {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, Heliopause.MODID);

    private static <T extends Block> RegistryObject<Item> registerBlockItem(String name, RegistryObject<T> block) {
        return ItemRegistry.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    private static <T extends Block> RegistryObject<T> registerBlock(String name, Supplier<T> block) {
        RegistryObject<T> blockObject = BLOCKS.register(name, block);
        registerBlockItem(name, blockObject);
        return blockObject;
    }
    
    private static <T extends Block> RegistryObject<Item> registerLensBarrelBlockItem(String name, RegistryObject<T> block) {
        return ItemRegistry.ITEMS.register(name, () -> new LensBarrelBlockItem(block.get(), new Item.Properties()));
    }
    
    private static <T extends Block> RegistryObject<T> registerLensBarrelBlock(String name, Supplier<T> block) {
        RegistryObject<T> blockObject = BLOCKS.register(name, block);
        registerLensBarrelBlockItem(name, blockObject);
        return blockObject;
    }

    // ブロックの作成
    //錬金焚き火
    /*public static final RegistryObject<Block> ALCHEMY_CAMPFIRE =
        registerBlock("alchemy_campfire",
            () -> new AlchemyCampfireBlock(
                true, 0,
                BlockBehaviour.Properties.of()
                    .noOcclusion()
                    .strength(1.0f)
                    .lightLevel(blockState -> blockState.getValue(AlchemyCampfireBlock.LIT) ? 15 : 0)
            ));*/
    //錬金こん炉
    public static final RegistryObject<Block> ALCHEMY_STOVE =
        registerBlock("alchemy_stove", () -> new AlchemyStoveBlock(
            BlockBehaviour.Properties.of()
                .strength(2.0f)
                .sound(SoundType.LANTERN)
                .lightLevel(blockState -> blockState.getValue(AlchemyStoveBlock.LIT) ? 15 : 9)
        ));
    //るつぼ
    public static final RegistryObject<Block> CRUCIBLE =
        registerBlock("crucible", () -> new CrucibleBlock(
            BlockBehaviour.Properties.of()
                .strength(2.0F)
                .sound(SoundType.NETHERITE_BLOCK)
        ));
    //オーブ
    /*public static final RegistryObject<Block> ORB =
        registerBlock("orb",() -> new OrbBlock(
                    BlockBehaviour.Properties.of()
                            .strength(1.0f)
                            .noCollission()
                            .noOcclusion()
                            .noParticlesOnBreak()
                        .noLootTable()
            ));*/

    // 材料保持ブロック
    public static final RegistryObject<Block> STELLAR_INGREDIENT_BLOCK =
        registerBlock("stellar_ingredient_block", ()-> new StellarIngredientBlock(
            BlockBehaviour.Properties.of()
               .strength(0)
               .noCollission()
               .noOcclusion()
               .noParticlesOnBreak()
               .noLootTable()
        ));

    //模造天体コア
    /*public static final RegistryObject<Block> COMET_CORE =
            BLOCKS.register("comet_core",() -> new CometCoreBlock(
                    BlockBehaviour.Properties.of()
                            .strength(1.0f)
                            .noCollission()
                            .noOcclusion()
                            .noParticlesOnBreak()
            ));*/
    //中心星
    /*public static final RegistryObject<Block> CENTRAL_STAR =
        registerBlock("central_star",
            () -> new CentralStarBlock(
                BlockBehaviour.Properties.of()
                    .noLootTable()
                    .strength(0.5f)
                    .lightLevel(blockState -> 15)
            ));*/
    //軌道面インタラクト用ブロック
    /*public static final RegistryObject<Block> ORRERY_SPACE =
        registerBlock("orrery_space",
            ()-> new OrrerySpaceBlock(
                BlockBehaviour.Properties.of()
            ));*/

    //流体ケージ
    public static final RegistryObject<Block> FLUID_CAGE =
        registerBlock("fluid_cage",() -> new FluidCageBlock(
            BlockBehaviour.Properties.of()
                .strength(1.0f)
                .sound(SoundType.WOOD)
            ));

    //シデロスタット(基台)
    public static final RegistryObject<SiderostatBaseBlock> SIDEROSTAT_BASE =
        registerBlock("siderostat_base",() -> new SiderostatBaseBlock(
           BlockBehaviour.Properties.of()
               .strength(1.5f)
               .sound(SoundType.WOOD)
               .noLootTable()
        ));
    //シデロスタット(オーブ)
    /*public static final RegistryObject<SiderostatTopBlock> SIDEROSTAT_ORB =
        registerBlock("siderostat_orb",() -> new SiderostatTopBlock(
            BlockBehaviour.Properties.of()
                .strength(1.5f)
                //.noLootTable()
                .sound(SoundType.COPPER)
        ));*/
    //シデロスタット(弓)
    public static final RegistryObject<Block> SIDEROSTAT_TOP =
        registerBlock("siderostat",() -> new SiderostatTopBlock(
            BlockBehaviour.Properties.of()
                .strength(1.5f)
                .sound(SoundType.WOOD)
                //.noLootTable()
        ));
    //シデロスタット(装飾部)
    /*public static final RegistryObject<Block> SIDEROSTAT_MOTOR =
        registerBlock("siderostat_motor",() -> new Block(
            BlockBehaviour.Properties.of()
                .noLootTable()
        ));*/

    //黒板
    public static final RegistryObject<Block> BLACKBOARD =
        registerBlock("blackboard",() -> new BlackBoardBlock(
            BlockBehaviour.Properties.of()
                .strength(1.5f)
                .sound(SoundType.BONE_BLOCK)
                .requiresCorrectToolForDrops()
        ));

    // 描かれた黒板
    public static final RegistryObject<Block> WRITTEN_BOARD =
        registerBlock("written_board", ()-> new WrittenBoardBlock(
            BlockBehaviour.Properties.copy(BLACKBOARD.get())
        ));

    // 遷天模倣陣
    public static final RegistryObject<Block> ORRERY_CIRCLE_BOARD =
        registerBlock("orrery_circle_board", ()-> new OrreryCircleBoardBlock(
            BlockBehaviour.Properties.copy(BLACKBOARD.get())
        ));

    // 星明かり収斂器架台
    public static final RegistryObject<Block> CONCENTRATOR =
        registerBlock("concentrator", ()-> new ConcentratorBlock(
            BlockBehaviour.Properties.of()
                .strength(1.5f)
                .sound(SoundType.NETHERITE_BLOCK)
                .noOcclusion()
        ));

    /*public static final RegistryObject<Block> STARLIGHT_CONCENTRATOR_INTERFACE =
        registerBlock("starlight_concentrator_interface",
            ()-> new ConcentratorInterfaceBlock(
                BlockBehaviour.Properties.of()
                    .strength(1.5f)
                    .noLootTable()
                    .noOcclusion()
                    .noParticlesOnBreak()
                )
            );*/

    //液体散布器
        //オーブ
    /*public static final RegistryObject<Block> FLUID_SPREADER_ORB =
        registerBlock("fluid_spreader_orb",() -> new FluidSpreaderOrbBlock(
                    BlockBehaviour.Properties.of()
                            .strength(1.0f)
                            .noCollission()
                            .noOcclusion()
                            .noParticlesOnBreak()
                        .noLootTable()
            ));*/
        //塔
    /*public static final RegistryObject<Block> FLUID_SPREADER_TOWER =
            registerBlock("fluid_spreader_tower",() -> new FluidSpreaderTowerBlock(
                   BlockBehaviour.Properties.of()
                           .strength(1.0f)
                           .sound(SoundType.WOOD)
            ));*/
    /*public static final RegistryObject<Block> FLUID_SPREADER =
            BLOCKS.register("fluid_spreader",() -> new FluidSpreaderBlock(
                    BlockBehaviour.Properties.of()
                            .strength(1.0f)
                            .sound(SoundType.COPPER)
            ));*/
    //銅パイプ
    public static final RegistryObject<Block> COPPER_PIPE =
        registerBlock("copper_pipe",() -> new CopperPipeBlock(
            BlockBehaviour.Properties.of()
                .strength(1.0f)
                .sound(SoundType.COPPER)
                .dynamicShape()
        ));
    
    
    public static final RegistryObject<Block> INGREDIENT_COLLECTOR =
        registerBlock("stellar_ingredient_collector",() -> new StellarIngredientCollectorBlock(
            BlockBehaviour.Properties.of()
                .sound(SoundType.METAL)
                .dynamicShape()
        ));
    
    public static final RegistryObject<Block> INGREDIENT_DISPENSER =
        registerBlock("stellar_ingredient_dispenser",() -> new StellarIngredientDispenserBlock(
            BlockBehaviour.Properties.of()
                .sound(SoundType.METAL)
                .dynamicShape()
        ));

    //錬金焙炉
    public static final RegistryObject<Block> ROASTING_TABLE =
        registerBlock("alchemy_roasting_table", () -> new RoastingTableBlock(
            BlockBehaviour.Properties.of()
                .strength(2.0F)
                .sound(SoundType.CHISELED_BOOKSHELF)
                .lightLevel(blockState -> blockState.getValue(RoastingTableBlock.LIT) ? 5 : 0)
        ));

    // 鏡筒 木製
    public static final RegistryObject<LensBarrelBlock> WOODEN_LENS_BARREL_BLOCK =
        registerLensBarrelBlock("wooden_lens_barrel", () -> new LensBarrelBlock(
            BlockBehaviour.Properties.of()
                .strength(1.0F)
                .sound(SoundType.CHERRY_WOOD)
                .noOcclusion()
        ));
    
    // 鏡筒 石製
    public static final RegistryObject<LensBarrelBlock> STONE_LENS_BARREL_BLOCK =
        registerLensBarrelBlock("stone_lens_barrel", () -> new LensBarrelBlock(
            BlockBehaviour.Properties.of()
                .requiresCorrectToolForDrops()
                .strength(1.0F)
                .sound(SoundType.STONE)
                .noOcclusion()
        ));
    
    // 鏡筒 錬金赤銅
    public static final RegistryObject<LensBarrelBlock> ALCHEMY_BIRON_LENS_BARREL_BLOCK =
        registerLensBarrelBlock("alchemy_biron_lens_barrel", () -> new LensBarrelBlock(
            BlockBehaviour.Properties.of()
                .requiresCorrectToolForDrops()
                .strength(1.0F)
                .sound(SoundType.NETHERITE_BLOCK)
                .noOcclusion()
        ));
    
    // 鏡筒 超断熱材
    public static final RegistryObject<LensBarrelBlock> THERMOIMMOBILANT_LENS_BARREL_BLOCK =
        registerLensBarrelBlock("thermoimmobilant_lens_barrel", () -> new LensBarrelBlock(
           BlockBehaviour.Properties.of()
               .requiresCorrectToolForDrops()
               .strength(4.5F)
               .sound(SoundType.LODESTONE)
               .noOcclusion()
        ));
    
    // 鉄の鏡
    public static final RegistryObject<LensBarrelBlock> IRON_MAIN_MIRROR_BLOCK =
        registerLensBarrelBlock("iron_main_mirror", () -> new LensBarrelBlock(
            BlockBehaviour.Properties.copy(WOODEN_LENS_BARREL_BLOCK.get())
        ));
    public static final RegistryObject<LensBarrelBlock> IRON_SECOND_MIRROR_BLOCK =
        registerLensBarrelBlock("iron_second_mirror", () -> new LensBarrelBlock(
            BlockBehaviour.Properties.copy(WOODEN_LENS_BARREL_BLOCK.get())
        ));
    
    // 黒鉛の鏡
    public static final RegistryObject<LensBarrelBlock> GRAPHITE_MAIN_MIRROR_BLOCK =
        registerLensBarrelBlock("graphite_main_mirror", () -> new LensBarrelBlock(
            BlockBehaviour.Properties.copy(STONE_LENS_BARREL_BLOCK.get())
        ));
    public static final RegistryObject<LensBarrelBlock> GRAPHITE_SECOND_MIRROR_BLOCK =
        registerLensBarrelBlock("graphite_second_mirror", () -> new LensBarrelBlock(
            BlockBehaviour.Properties.copy(STONE_LENS_BARREL_BLOCK.get())
        ));
    
    // 銀の鏡
    public static final RegistryObject<LensBarrelBlock> SILVER_MAIN_MIRROR_BLOCK =
        registerLensBarrelBlock("silver_main_mirror", () -> new LensBarrelBlock(
            BlockBehaviour.Properties.copy(ALCHEMY_BIRON_LENS_BARREL_BLOCK.get())
        ));
    public static final RegistryObject<LensBarrelBlock> SILVER_SECOND_MIRROR_BLOCK =
        registerLensBarrelBlock("silver_second_mirror", () -> new LensBarrelBlock(
            BlockBehaviour.Properties.copy(ALCHEMY_BIRON_LENS_BARREL_BLOCK.get())
        ));
    
    // 錬金鋼の鏡
    public static final RegistryObject<LensBarrelBlock> ALCHEMY_STEEL_MAIN_MIRROR_BLOCK =
        registerLensBarrelBlock("alchemy_steel_main_mirror", () -> new LensBarrelBlock(
            BlockBehaviour.Properties.copy(THERMOIMMOBILANT_LENS_BARREL_BLOCK.get())
        ));
    public static final RegistryObject<LensBarrelBlock> ALCHEMY_STEEL_SECOND_MIRROR_BLOCK =
        registerLensBarrelBlock("alchemy_steel_second_mirror", () -> new LensBarrelBlock(
            BlockBehaviour.Properties.copy(THERMOIMMOBILANT_LENS_BARREL_BLOCK.get())
        ));

    /*
    //液体注入器
    public static final RegistryObject<Block> PENETRATOR =
        registerBlock("penetrator", () -> new Block(
           BlockBehaviour.Properties.of()
               .strength(2.0f)
               .sound(SoundType.CHISELED_BOOKSHELF)
        ));

    //液体浸漬器
    public static final RegistryObject<Block> DISSOLVER =
        registerBlock("dissolver", () -> new Block(
            BlockBehaviour.Properties.of()
                .strength(2.0f)
                .sound(SoundType.NETHERITE_BLOCK)
        ));

    //星明かり収斂筒
    public static final RegistryObject<Block> CONVERGE_CYLINDER=
        registerBlock("converge_cylinder", () -> new StarlightCylinderBlock(
            BlockBehaviour.Properties.of()
                .strength(2.0f)
                .sound(SoundType.CHISELED_BOOKSHELF)
        ));

    //星明かり製錬筒
    public static final RegistryObject<Block> REFINERY_CYLINDER=
        registerBlock("refinery_cylinder", () -> new StarlightCylinderBlock(
            BlockBehaviour.Properties.of()
                .strength(2.0f)
                .sound(SoundType.NETHERITE_BLOCK)
        ));
*/

    //錬金赤銅ブロック
    public static final RegistryObject<Block> ALCHEMY_BIRON_BLOCK =
        registerBlock("alchemy_biron_block", () -> new AlchemyBironBlock(
            BlockBehaviour.Properties.of()
                .requiresCorrectToolForDrops()
                .strength(2.0F)
                .sound(SoundType.NETHERITE_BLOCK)
        ));
    //磨かれた錬金赤銅ブロック
    public static final RegistryObject<Block> POLISHED_BIRON_BLOCK =
        registerBlock("polished_biron_block", () -> new Block(
            BlockBehaviour.Properties.copy(ALCHEMY_BIRON_BLOCK.get())
                .sound(SoundType.COPPER)
        ));
    //模様入り錬金赤銅ブロック
    public static final RegistryObject<RectangularBlock> CHISELED_BIRON_BLOCK =
        registerBlock("chiseled_biron_block", () -> new RectangularBlock(
            BlockBehaviour.Properties.copy(ALCHEMY_BIRON_BLOCK.get())
        ));
    //錬金赤銅ガラスブロック
    public static final RegistryObject<RectangularGlassBlock> ALCHEMY_BIRON_GLASS =
        registerBlock("alchemy_biron_glass", () -> new RectangularGlassBlock(
            BlockBehaviour.Properties.copy(Blocks.GLASS)
        ));
    //グロウストーン合金ブロック
    public static final RegistryObject<Block> GLOWSTONE_ALLOY_BLOCK =
        registerBlock("glowstone_alloy_block", () -> new Block(
            BlockBehaviour.Properties.of()
                .strength(2.0F)
                .sound(SoundType.COPPER)
                .lightLevel(state -> 9)
                .mapColor(MapColor.SAND)
        ));
    // 銀ブロック
    public static final RegistryObject<Block> SILVER_BLOCK =
        registerBlock("silver_block", () -> new Block(
            BlockBehaviour.Properties.copy(Blocks.GOLD_BLOCK)
                .mapColor(MapColor.ICE)
        ));
    // 銀鉱石
    public static final RegistryObject<Block> SILVER_ORE_BLOCK =
        registerBlock("silver_ore", () -> new Block(
            BlockBehaviour.Properties.copy(Blocks.GOLD_ORE)
        ));
    public static final RegistryObject<Block> DEEPSLATE_SILVER_ORE_BLOCK =
        registerBlock("deepslate_silver_ore", () -> new Block(
            BlockBehaviour.Properties.copy(Blocks.DEEPSLATE_GOLD_ORE)
        ));
    public static final RegistryObject<Block> RAW_SILVER_BLOCK =
        registerBlock("raw_silver_block", () -> new Block(
            BlockBehaviour.Properties.copy(Blocks.RAW_GOLD_BLOCK)
                .mapColor(MapColor.ICE)
        ));
    /*
    //ミツマタの苗木と葉
    public static final RegistryObject<Block> PAPERBUSH_BLOCK =
        registerBlock("paperbush", () -> new PaperBushPlantBlock(
                            BlockBehaviour.Properties.of().sound(SoundType.AZALEA)
                    )
            );
    public static final RegistryObject<Block> PAPERBUSH_LEAVES_BLOCK =
        registerBlock("paperbush_leaves", () -> new Block(
                            BlockBehaviour.Properties.of().sound(SoundType.AZALEA_LEAVES)
                    )
            );
*/
    //見た目用ブロック作成

    // 月
    /*public static final RegistryObject<Block> SIDEROSTAT_MOON =
        registerBlock("imitation_moon",
            () -> new Block(
                BlockBehaviour.Properties.of()
                    .noOcclusion()
                    .instabreak()
                    .pushReaction(PushReaction.IGNORE)
                    .lightLevel(blockState -> 15)
            ));*/

}
