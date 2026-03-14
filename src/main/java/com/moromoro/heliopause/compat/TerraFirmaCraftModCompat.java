package com.moromoro.heliopause.compat;

import com.moromoro.heliopause.generic.Season;
import net.dries007.tfc.util.calendar.Calendars;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.eventbus.api.IEventBus;

public class TerraFirmaCraftModCompat {
    public static boolean isLoadedTFC = false;
    public static void init(IEventBus modEventBus){
        isLoadedTFC = true;
    }

    public static long getTFCDayInYear(ServerLevel level){
        if(!isLoadedTFC){
            return -1;
        }
        try {
            return (long)Math.floor((Calendars.get(level).getCalendarFractionOfYear() * Season.YEAR_LENGTH)-0.25f);
        }catch (NoClassDefFoundError error){
            return -1;
        }
    }
}
