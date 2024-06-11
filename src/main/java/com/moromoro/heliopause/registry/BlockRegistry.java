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
    public static final RegistryObject<Block> ALCHEMY_REACTOR =
            BLOCKS.register("alchemy_reactor", () -> new ReactorBlock());
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
                    //.noOcclusion()
            ));
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
}
