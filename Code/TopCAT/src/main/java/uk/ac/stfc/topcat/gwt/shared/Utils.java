package uk.ac.stfc.topcat.gwt.shared;

import com.google.gwt.i18n.client.NumberFormat;


public class Utils {
    /**
     * Get human readable bytes format in SI or binary units.
     * 
     * @author aioobe/BalusC from http://stackoverflow.com/a/3758880 
     * 
     * @param bytes
     * @param si
     * @return
     */
    public static String byteCountToDisplaySizeForClient(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp-1) + (si ? "" : "i");
        
        double size = bytes / Math.pow(unit, exp);
        // can't use string.format on gwt client code. Using NumberFormat.
        String num = NumberFormat.getFormat("#.0").format(size);
        
        return num + " " + pre + "B";
    }

}


