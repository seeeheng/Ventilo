package sg.gov.dsta.mobileC3.ventilo.util.component;

import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

import com.android.volley.toolbox.ImageLoader;

import sg.gov.dsta.mobileC3.ventilo.util.constant.MemorySize;


public class ImageClass extends LruCache<String, Bitmap> implements
        ImageLoader.ImageCache {

    public static int getDefaultLruCacheSize() {
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / MemorySize.KILO_BYTE);
        final int cacheSize = maxMemory / MemorySize.BITS_IN_BYTE;

        return cacheSize;
    }

    public ImageClass() {
        this(getDefaultLruCacheSize());
    }

    public ImageClass(int sizeInKiloBytes) {
        super(sizeInKiloBytes);
    }

    @Override
    protected int sizeOf(String key, Bitmap value) {
        return value.getRowBytes() * value.getHeight() / MemorySize.KILO_BYTE;
    }

    @Override
    public Bitmap getBitmap(String url) {
        return get(url);
    }

    @Override
    public void putBitmap(String url, Bitmap bitmap) {
        put(url, bitmap);
    }
}
