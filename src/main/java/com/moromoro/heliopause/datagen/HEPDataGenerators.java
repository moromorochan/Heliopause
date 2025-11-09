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
public class HEPDataGenerators {
    @SubscribeEvent
    public static void gatherData(GatherDataEvent event){
        DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

        generator.addProvider(event.includeServer(), new HEPRecipeProvider(packOutput));
        generator.addProvider(event.includeServer(), HEPLootTableProvider.create(packOutput));

        generator.addProvider(event.includeClient(), new HEPBlockStateProvider(packOutput, existingFileHelper));
        generator.addProvider(event.includeClient(), new HEPItemModelProvider(packOutput, existingFileHelper));

        HEPBlockTagGenerator blockTagGenerator = generator.addProvider(event.includeServer(),
            new HEPBlockTagGenerator(packOutput, lookupProvider, existingFileHelper));
        generator.addProvider(event.includeServer(), new HEPItemTagGenerator(packOutput, lookupProvider, blockTagGenerator.contentsGetter(), existingFileHelper));
    }
}
