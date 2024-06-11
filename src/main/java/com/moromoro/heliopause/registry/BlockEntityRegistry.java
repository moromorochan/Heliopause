package com.moromoro.heliopause.registry;

import com.moromoro.Heliopause;
import com.moromoro.heliopause.blockEntity.*;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class BlockEntityRegistry {
    public static final DeferredRegister<BlockEntityType<?>> BLOCKENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, Heliopause.MODID);

    // ブロックエンティティの作成
    public static final RegistryObject<BlockEntityType<CrucibleBlockEntity>> CRUCIBLE_BE =
            BLOCKENTITIES.register("crucible",() ->
                    BlockEntityType.Builder.of(
                            CrucibleBlockEntity::new,
                            BlockRegistry.CRUCIBLE.get()
                    ).build(null)
            );
    public static final RegistryObject<BlockEntityType<OrbBlockEntity>> ORB_BE =
            BLOCKENTITIES.register("orb",() ->
                    BlockEntityType.Builder.of(
                            OrbBlockEntity::new,
                            BlockRegistry.ORB.get()
                    ).build(null)
            );
}

