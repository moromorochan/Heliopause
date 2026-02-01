package com.moromoro.heliopause.generic;

import net.minecraft.server.level.ServerLevel;

public class Season {

    public static final int MONTH_DATES = 8;
    public static final int SEASON_MONTHS = 3;

    public static final long SEASON_LENGTH = MONTH_DATES * SEASON_MONTHS;
    public static final long YEAR_LENGTH = SEASON_LENGTH * 4;

    public static final int SPRING_INDEX = 0;
    public static final int SUMMER_INDEX = 1;
    public static final int AUTUMN_INDEX = 2;
    public static final int WINTER_INDEX = 3;

    private static long dateOffset = 0;

    public static long getDayInYear(ServerLevel level){
        long totalTicks = level.getGameTime();
        long totalDays = totalTicks / 24000L;
        return Math.floorMod(totalDays + dateOffset, YEAR_LENGTH);
    }

    public static int getMonth(long dayInYear){
        return (int) Math.floorDiv(dayInYear, MONTH_DATES);
    }

    public static int getSeason(long dayInYear){
        return (int) Math.floorDiv(dayInYear, SEASON_LENGTH);
    }

    public static long getDateOffset() {
        return dateOffset;
    }

    public static void setDateOffset(int newOffset) {
        dateOffset = newOffset;
    }
}
