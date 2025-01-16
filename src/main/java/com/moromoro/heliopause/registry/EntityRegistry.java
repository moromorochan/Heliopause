package com.moromoro.heliopause.registry;

import com.moromoro.Heliopause;
import com.moromoro.heliopause.blockEntity.CrucibleBlockEntity;
import com.moromoro.heliopause.blockEntity.FluidSpreaderOrbBlockEntity;
import com.moromoro.heliopause.entity.OrreryInteractionOperatorEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class EntityRegistry {
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, Heliopause.MODID);

    public static void register(IEventBus eventBus){
        ENTITIES.register(eventBus);
    }

    // エンティティの作成
    public static final RegistryObject<EntityType<OrreryInteractionOperatorEntity>> ORRERY_INTERACTION_OPERATOR_E =
        ENTITIES.register("orrery_interaction_operator",() ->
            EntityType.Builder.<OrreryInteractionOperatorEntity>of(
                OrreryInteractionOperatorEntity::new, MobCategory.MISC
                )
                .sized(0.8f,0.2f)
                .build(new ResourceLocation(Heliopause.MODID, "orrery_interaction_operator").toString())
        );
}
