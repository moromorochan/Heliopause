package com.moromoro.heliopause.registry;

import com.moromoro.Heliopause;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ParticleRegistry {
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES = DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, Heliopause.MODID);

    public static void register(IEventBus eventBus){PARTICLE_TYPES.register(eventBus);}


    public static final RegistryObject<SimpleParticleType> FLUID_SPREAD_PARTICLES =
            PARTICLE_TYPES.register("fluid_spread_particles", () -> new SimpleParticleType(true));
}
