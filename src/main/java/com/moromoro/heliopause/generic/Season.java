package com.moromoro.heliopause.generic;

import com.moromoro.heliopause.compat.TerraFirmaCraftModCompat;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;

import static com.moromoro.ConfigHolder.SEASON_OFFSET;

public class Season {

    public static final int MONTH_DATES = 8;
    public static final int SEASON_MONTHS = 3;

    public static final long SEASON_LENGTH = MONTH_DATES * SEASON_MONTHS;
    public static final long YEAR_LENGTH = SEASON_LENGTH * 4;

    public static long getDayInYear(ServerLevel level){
        // TFCが導入されているならTFCのカレンダーを取得
        if(TerraFirmaCraftModCompat.isLoadedTFC){
            return TerraFirmaCraftModCompat.getTFCDayInYear(level);
        }
        // ないならgameTimeから変換
        long totalTicks = level.getGameTime();
        long totalDays = totalTicks / 24000L;
        return Math.floorMod(totalDays + SEASON_OFFSET.get(), YEAR_LENGTH);
    }

    public static int getMonth(long dayInYear){
        return (int) Math.floorDiv(dayInYear, MONTH_DATES);
    }

    public static int getSeason(long dayInYear){
        return (int) Math.floorDiv(dayInYear, SEASON_LENGTH);
    }

    public static String getDateTranslatable(long dayInYear, long timeInDay){
        // 日
        long time = timeInDay % 24000L;
        // 午前午後と日付ズレ
        if (time >= 18000L) {
            dayInYear = Math.floorMod(dayInYear + 1, Season.YEAR_LENGTH);
        }

        int month = Season.getMonth(dayInYear);
        String monthString = Component.translatable("season.heliopause.month."+ (month+1)).getString();
        int dayInMonth = (int) (dayInYear - month*Season.MONTH_DATES);
        String dayString = Component.translatable("season.heliopause.date", String.format("%2s",dayInMonth+1)).getString();
        return monthString + dayString;// "12月 4 日"
    }

    public static String getTimeTranslatable(long timeInDay){
        long time = timeInDay % 24000L;
        String meridiemString = (time < 6000L || time >= 18000L)  ?
            Component.translatable("season.heliopause.AM").getString():Component.translatable("season.heliopause.PM").getString();//"午前":"午後";
        int hour = (int) ((time + 6000L) % 12000L / 1000);
        int minute = (int)((time % 1000L) * (60f/1000));
        String timeString = Component.translatable("season.heliopause.time", String.format("%2s",hour), String.format("%02d",minute)).getString();
        return meridiemString + " " + timeString;// "午後 11:02"
    }
}
