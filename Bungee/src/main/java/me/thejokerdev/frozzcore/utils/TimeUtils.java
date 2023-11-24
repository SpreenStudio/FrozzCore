package me.thejokerdev.frozzcore.utils;

public class TimeUtils {
    public static boolean elapsed(long needed, long time) {
        return elapsed(time) >= needed;
    }
    public static long left(long needed, long time){
        return needed-elapsed(time);
    }
    public static long elapsed(long time) {
        return System.currentTimeMillis() - time;
    }
}
