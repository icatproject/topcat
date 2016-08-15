package org.icatproject.topcat.utils;

public class ConvertUtils {

    public static String bytesToHumanReadable(long bytes) {
        int unit = 1000;
        
        if (bytes < unit) return bytes + " B";

        int exp = (int) (Math.log(bytes) / Math.log(unit));
        char pre = ("kMGTPE").charAt(exp-1);

        return String.format("%.2f %sB", bytes / Math.pow(unit, exp), pre);
    }

}