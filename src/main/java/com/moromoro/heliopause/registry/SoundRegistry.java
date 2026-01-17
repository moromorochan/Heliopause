package com.moromoro.heliopause.registry;

import com.moromoro.Heliopause;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class SoundRegistry {
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, Heliopause.MODID);

    public static final RegistryObject<SoundEvent> SIDEROSTAT_WINDING =
        SOUNDS.register("siderostat_winding",
            () -> SoundEvent.createFixedRangeEvent(new ResourceLocation(Heliopause.MODID,"siderostat_winding"), 160.0F));

    public static final RegistryObject<SoundEvent> SIDEROSTAT_LOCK =
        SOUNDS.register("siderostat_lock",
            () -> SoundEvent.createFixedRangeEvent(new ResourceLocation(Heliopause.MODID,"siderostat_lock"), 160.0F));

    public static final RegistryObject<SoundEvent> RIPPLE =
        SOUNDS.register("ripple",
            () -> SoundEvent.createFixedRangeEvent(new ResourceLocation(Heliopause.MODID,"ripple"), 160.0F));

    public static final RegistryObject<SoundEvent> ORRERY_ROTATE =
        SOUNDS.register("orrery_rotate",
            () -> SoundEvent.createFixedRangeEvent(new ResourceLocation(Heliopause.MODID, "orrery_rotate"),200.0F));
}
