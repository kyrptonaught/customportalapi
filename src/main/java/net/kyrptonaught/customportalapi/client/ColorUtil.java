package net.kyrptonaught.customportalapi.client;

import java.util.HashMap;

public class ColorUtil {
    private static HashMap<Integer, float[]> COLOR_CACHE = new HashMap<>();

    public static float[] getColorForBlock(int color) {
        if (!COLOR_CACHE.containsKey(color)) {
            int l = (color & 16711680) >> 16;
            int m = (color & '\uff00') >> 8;
            int n = (color & 255);
            COLOR_CACHE.put(color, new float[]{(float) l / 255.0F, (float) m / 255.0F, (float) n / 255.0F});
        }
        return COLOR_CACHE.get(color);
    }
}
