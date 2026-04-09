package com.moromoro.heliopause.compat.kubeJS;

import com.moromoro.Heliopause;
import dev.latvian.mods.kubejs.KubeJSPlugin;
import dev.latvian.mods.kubejs.registry.RegistryInfo;

public class HeliopauseKubeJSCompat extends KubeJSPlugin {
    public HeliopauseKubeJSCompat() {
    }
    
    @Override
    public void init() {
        Heliopause.LOGGER.info("HeliopauseKubeJSCompat init called.");
        super.init();
        
        // カスタム鏡筒追加機能
        RegistryInfo.BLOCK.addType("lens_barrel", CustomLensBarrelBuilder.class, id -> new CustomLensBarrelBuilder(id, "barrel"));
        RegistryInfo.BLOCK.addType("lens_barrel_main_mirror", CustomLensBarrelBuilder.class, id -> new CustomLensBarrelBuilder(id, "main"));
        RegistryInfo.BLOCK.addType("lens_barrel_second_mirror", CustomLensBarrelBuilder.class, id -> new CustomLensBarrelBuilder(id, "second"));
        // カスタム黒板追加機能
        //RegistryInfo.BLOCK.addType("blackboard", CustomWrittenBoardBuilder.class, CustomWrittenBoardBuilder::new);
    }
    
    @Override
    public void initStartup() {
        super.initStartup();
    }
    
    @Override
    public void registerEvents() {
    
    }
}
