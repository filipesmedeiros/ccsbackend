package utils;

public class Date {

    public static long timestampMinusHours(int hours) {
        long milis = hours * 60 * 60 * 1000;
        return System.currentTimeMillis() - milis;
    }
}
