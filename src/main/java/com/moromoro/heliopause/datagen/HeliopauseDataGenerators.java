package com.moromoro.heliopause.datagen;

import com.moromoro.Heliopause;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.concurrent.CompletableFuture;

@Mod.EventBusSubscriber(modid = Heliopause.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class HeliopauseDataGenerators {
    @SubscribeEvent
    public static void gatherData(GatherDataEvent event){
        DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

        generator.addProvider(event.includeServer(), new HeliopauseRecipeProvider(packOutput));
        generator.addProvider(event.includeServer(), HeliopauseLootTableProvider.create(packOutput));

        generator.addProvider(event.includeClient(), new HeliopauseBlockStateProvider(packOutput, existingFileHelper));
        generator.addProvider(event.includeClient(), new HeliopauseItemModelProvider(packOutput, existingFileHelper));

        HeliopauseBlockTagGenerator blockTagGenerator = generator.addProvider(event.includeServer(),
            new HeliopauseBlockTagGenerator(packOutput, lookupProvider, existingFileHelper));
        generator.addProvider(event.includeServer(), new HeliopauseItemTagGenerator(packOutput, lookupProvider, blockTagGenerator.contentsGetter(), existingFileHelper));
        
        generator.addProvider(event.includeServer(), new HeliopauseWorldGenProvider(packOutput, lookupProvider));
    }
}
