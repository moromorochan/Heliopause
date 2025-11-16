package com.moromoro.heliopause.registry;

import com.moromoro.Heliopause;
import com.moromoro.heliopause.screen.RoastingTableMenu;
import com.moromoro.heliopause.screen.SiderostatMenu;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.network.IContainerFactory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class MenuTypeRegistry {
    public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(ForgeRegistries.MENU_TYPES, Heliopause.MODID);

    private static <T extends AbstractContainerMenu>RegistryObject<MenuType<T>> registerMenuType(String name, IContainerFactory<T> factory) {
        return MENU_TYPES.register(name, () -> IForgeMenuType.create(factory));
    }

    public static final RegistryObject<MenuType<RoastingTableMenu>> ROASTING_TABLE_MENU =
            registerMenuType("roasting_menu", RoastingTableMenu::new);
    public static final RegistryObject<MenuType<SiderostatMenu>> SIDEROSTAT_MENU =
        registerMenuType("siderostat_menu", SiderostatMenu::new);

    public static void register(IEventBus eventBus){
        MENU_TYPES.register(eventBus);
    }
}
