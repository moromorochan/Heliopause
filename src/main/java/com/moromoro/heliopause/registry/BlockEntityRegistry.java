package com.moromoro.heliopause.registry;

import com.moromoro.Heliopause;
import com.moromoro.heliopause.blockEntity.*;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class BlockEntityRegistry {
    public static final DeferredRegister<BlockEntityType<?>> BLOCKENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, Heliopause.MODID);

    public static void register(IEventBus eventBus){
        BLOCKENTITIES.register(eventBus);
    }

    // ブロックエンティティの作成
        //るつぼ
    public static final RegistryObject<BlockEntityType<CrucibleBlockEntity>> CRUCIBLE_BE =
            BLOCKENTITIES.register("crucible",() ->
                    BlockEntityType.Builder.of(
                            CrucibleBlockEntity::new,
                            BlockRegistry.CRUCIBLE.get()
                    ).build(null)
            );
        //液球
    /*public static final RegistryObject<BlockEntityType<OrbBlockEntity>> ORB_BE =
            BLOCKENTITIES.register("orb",() ->
                    BlockEntityType.Builder.of(
                            OrbBlockEntity::new,
                            BlockRegistry.ORB.get()
                    ).build(null)
            );*/
        //液体ケージ
    public static final RegistryObject<BlockEntityType<FluidCageBlockEntity>> FLUID_CAGE_BE =
            BLOCKENTITIES.register("fluid_cage",() ->
                    BlockEntityType.Builder.of(
                            FluidCageBlockEntity::new,
                            BlockRegistry.FLUID_CAGE.get()
                    ).build(null)
            );
        //流体散布塔 中心星
    /*public static final RegistryObject<BlockEntityType<FluidSpreaderOrbBlockEntity>> FLUID_SPREADER_ORB_BE =
                BLOCKENTITIES.register("fluid_spreader_orb",()->
                        BlockEntityType.Builder.of(
                                FluidSpreaderOrbBlockEntity::new,
                                BlockRegistry.FLUID_SPREADER_ORB.get()
                        ).build(null)
                );*/
        //中心星
    /*public static final RegistryObject<BlockEntityType<CentralStarBlockEntity>> CENTRAL_STAR_BE =
        BLOCKENTITIES.register("central_star",()->
            BlockEntityType.Builder.of(
                CentralStarBlockEntity::new,
                BlockRegistry.CENTRAL_STAR.get()
            ).build(null)
        );*/
        //焙炉
    public static final RegistryObject<BlockEntityType<RoastingTableBlockEntity>> ROASTING_TABLE_BE =
            BLOCKENTITIES.register("alchemy_roasting_table",() ->
                BlockEntityType.Builder.of(
                    RoastingTableBlockEntity::new,
                    BlockRegistry.ROASTING_TABLE.get()
                ).build(null)
            );

        // 描かれた黒板
    public static final RegistryObject<BlockEntityType<WrittenBoardBlockEntity>> WRITTEN_BOARD_BE =
        BLOCKENTITIES.register("written_board",() ->
                BlockEntityType.Builder.of(
                    WrittenBoardBlockEntity::new,
                    BlockRegistry.WRITTEN_BOARD.get()
                ).build(null)
            );

    public static final RegistryObject<BlockEntityType<OrreryCircleBoardBlockEntity>> ORRERY_CIRCLE_BOARD_BE =
        BLOCKENTITIES.register("orrery_circle_board", () ->
                BlockEntityType.Builder.of(
                    OrreryCircleBoardBlockEntity::new,
                    BlockRegistry.ORRERY_CIRCLE_BOARD.get()
                ).build(null)
            );

    // 星明かり収斂器架台
    public static final RegistryObject<BlockEntityType<ConcentratorBlockEntity>> CONCENTRATOR_BE =
        BLOCKENTITIES.register("concentrator", () ->
            BlockEntityType.Builder.of(
                ConcentratorBlockEntity::new,
                BlockRegistry.CONCENTRATOR.get()
            ).build(null)
        );

    // シデロスタット
    public static final RegistryObject<BlockEntityType<SiderostatBlockEntity>> SIDEROSTAT_BE =
        BLOCKENTITIES.register("siderostat",() ->
            BlockEntityType.Builder.of(
                SiderostatBlockEntity::new,
                BlockRegistry.SIDEROSTAT_BASE.get()
            ).build(null)
            );

    // 材料保持ブロック
    public static final RegistryObject<BlockEntityType<StellarIngredientBlockEntity>> STELLAR_INGREDIENT_BE =
        BLOCKENTITIES.register("stellar_ingredient_block",() ->
            BlockEntityType.Builder.of(
                StellarIngredientBlockEntity::new,
                BlockRegistry.STELLAR_INGREDIENT_BLOCK.get()
            ).build(null)
            );
        //模造天体コア
    /*public static final RegistryObject<BlockEntityType<CometCoreBlockEntity>> COMET_CORE_BE =
                BLOCKENTITIES.register("comet_core",() ->
                        BlockEntityType.Builder.of(
                                CometCoreBlockEntity::new,
                                BlockRegistry.COMET_CORE.get()
                        ).build(null)
                );*/
    
    // 周天材料コレクター/ディスペンサー
    public static final RegistryObject<BlockEntityType<StellarIngredientDispenserBlockEntity>> INGREDIENT_DISPENSER_BE =
        BLOCKENTITIES.register("stellar_ingredient_dispenser", () ->
            BlockEntityType.Builder.of(
                StellarIngredientDispenserBlockEntity::new,
                BlockRegistry.INGREDIENT_DISPENSER.get()
            ).build(null)
            );
    public static final RegistryObject<BlockEntityType<StellarIngredientCollectorBlockEntity>> INGREDIENT_COLLECTOR_BE =
        BLOCKENTITIES.register("stellar_ingredient_collector", () ->
            BlockEntityType.Builder.of(
                StellarIngredientCollectorBlockEntity::new,
                BlockRegistry.INGREDIENT_COLLECTOR.get()
            ).build(null)
        );
}

