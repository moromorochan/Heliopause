package com.moromoro.heliopause.registry;

import com.moromoro.Heliopause;
import com.moromoro.heliopause.block.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class BlockRegistry {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, Heliopause.MODID);

    // ブロックの作成
    //錬金こん炉
    public static final RegistryObject<Block> ALCHEMY_STOVE =
            BLOCKS.register("alchemy_stove", AlchemyStoveBlock::new);
    //るつぼ
    public static final RegistryObject<Block> CRUCIBLE =
            BLOCKS.register("crucible", () -> new CrucibleBlock(
                            BlockBehaviour.Properties.of()
                                    .strength(2.0F)
                                    .sound(SoundType.NETHERITE_BLOCK)
                    )
            );
    //オーブ
    public static final RegistryObject<Block> ORB =
            BLOCKS.register("orb",() -> new OrbBlock(
                    BlockBehaviour.Properties.of()
                            .strength(1.0f)
                            .noCollission()
                            .noOcclusion()
                            .noParticlesOnBreak()
            ));

    //模造天体コア
    public static final RegistryObject<Block> COMET_CORE =
            BLOCKS.register("comet_core",() -> new CometCoreBlock(
                    BlockBehaviour.Properties.of()
                            .strength(1.0f)
                            .noCollission()
                            .noOcclusion()
                            .noParticlesOnBreak()
            ));
    //流体ケージ
    public static final RegistryObject<Block> FLUID_CAGE =
            BLOCKS.register("fluid_cage",() -> new FluidCageBlock(
                    BlockBehaviour.Properties.of()
                            .strength(1.0f)
                            .sound(SoundType.WOOD)
            ));
    //液体散布器
    public static final RegistryObject<Block> FLUID_SPREADER =
            BLOCKS.register("fluid_spreader",() -> new FluidSpreaderBlock(
                    BlockBehaviour.Properties.of()
                            .strength(1.0f)
                            .sound(SoundType.COPPER)
            ));
    //銅パイプ
    public static final RegistryObject<Block> COPPER_PIPE =
            BLOCKS.register("copper_pipe",() -> new CopperPipeBlock(
                    BlockBehaviour.Properties.of()
                            .strength(1.0f)
                            .sound(SoundType.COPPER)
                            .dynamicShape()
            ));

    //錬金焙炉
    public static final RegistryObject<Block> ROASTING_TABLE =
            BLOCKS.register("alchemy_roasting_table", RoastingTableBlock::new);
    //錬金赤銅ブロック
    public static final RegistryObject<Block> ALCHEMY_BIRON_BLOCK =
            BLOCKS.register("alchemy_biron_block", () -> new Block(
                            BlockBehaviour.Properties.of()
                                    .strength(2.0F)
                                    .sound(SoundType.NETHERITE_BLOCK)
                    )
            );
    //グロウストーン合金ブロック
    public static final RegistryObject<Block> GLOWSTONE_ALLOY_BLOCK =
            BLOCKS.register("glowstone_alloy_block", () -> new Block(
                            BlockBehaviour.Properties.of()
                                    .strength(2.0F)
                                    .sound(SoundType.COPPER)
                                    .lightLevel(state -> 9)
                    )
            );
    //ミツマタの苗木と葉
    public static final RegistryObject<Block> PAPERBUSH_BLOCK =
            BLOCKS.register("paperbush", () -> new PaperBushPlantBlock(
                            BlockBehaviour.Properties.of().sound(SoundType.AZALEA)
                    )
            );
    public static final RegistryObject<Block> PAPERBUSH_LEAVES_BLOCK =
            BLOCKS.register("paperbush_leaves", () -> new Block(
                            BlockBehaviour.Properties.of().sound(SoundType.AZALEA_LEAVES)
                    )
            );
}
