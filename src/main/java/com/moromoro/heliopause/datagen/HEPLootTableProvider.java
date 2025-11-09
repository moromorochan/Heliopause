package com.moromoro.heliopause.datagen;

import com.moromoro.heliopause.datagen.loot.HEPBlockLootTables;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

import java.util.List;
import java.util.Set;

public class HEPLootTableProvider {
    public static LootTableProvider create(PackOutput packOutput) {
        return new LootTableProvider(packOutput, Set.of(), List.of(
            new LootTableProvider.SubProviderEntry(HEPBlockLootTables::new, LootContextParamSets.BLOCK)
        ));
    }
}
