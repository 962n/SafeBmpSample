package com.sample.bitmap.safe.safebitmapsample;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.util.Log;

import com.nostra13.universalimageloader.cache.memory.MemoryCache;
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * ImageLoaderライブラリのMemoryCacehクラス(Bitmap破棄処理つき)
 * ※cacheの削除処理がfinalで宣言されており、overrideできないので処理をそのままもってきたもの。
 */
public class LruMemoryCacheWithRecycle implements MemoryCache {

    private final static String TAG = LruMemoryCacheWithRecycle.class.getSimpleName();

    private final LinkedHashMap<String, Bitmap> map;

    private final int maxSize;

    /** Size of this cache in bytes */
    private int size;


    /**
     * Creates default implementation of {@link MemoryCache} - {@link LruMemoryCache}<br />
     * Default cache size = 1/8 of available app memory.
     */
    public static MemoryCache createMemoryCache(Context context, int memoryCacheSize) {
        if (memoryCacheSize == 0) {
            ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            int memoryClass = am.getMemoryClass();
            memoryCacheSize = 1024 * 1024 * memoryClass / 8;
        }
        return new LruMemoryCache(memoryCacheSize);
    }

    /** @param maxSize Maximum sum of the sizes of the Bitmaps in this cache */
    private LruMemoryCacheWithRecycle(int maxSize) {
        if (maxSize <= 0) {
            throw new IllegalArgumentException("maxSize <= 0");
        }
        this.maxSize = maxSize;
        this.map = new LinkedHashMap<String, Bitmap>(0, 0.75f, true);
    }

    /**
     * Returns the Bitmap for {@code key} if it exists in the cache. If a Bitmap was returned, it is moved to the head
     * of the queue. This returns null if a Bitmap is not cached.
     */
    @Override
    public final Bitmap get(String key) {
        if (key == null) {
            throw new NullPointerException("key == null");
        }

        synchronized (this) {
            return map.get(key);
        }
    }

    /** Caches {@code Bitmap} for {@code key}. The Bitmap is moved to the head of the queue. */
    @Override
    public final boolean put(String key, Bitmap value) {
        Log.d(TAG,"put ");
        if (key == null || value == null) {
            throw new NullPointerException("key == null || value == null");
        }

        synchronized (this) {
            size += sizeOf(key, value);
            Bitmap previous = map.put(key, value);
            if (previous != null) {
                Log.d(TAG,"put recycle");
                previous.recycle();
                size -= sizeOf(key, previous);
            }
        }

        trimToSize(maxSize);
        return true;
    }

    /**
     * Remove the eldest entries until the total of remaining entries is at or below the requested size.
     *
     * @param maxSize the maximum size of the cache before returning. May be -1 to evict even 0-sized elements.
     */
    private void trimToSize(int maxSize) {
        Log.d(TAG,"trimToSize ");
        while (true) {
            String key;
            Bitmap value;
            synchronized (this) {
                if (size < 0 || (map.isEmpty() && size != 0)) {
                    throw new IllegalStateException(getClass().getName() + ".sizeOf() is reporting inconsistent results!");
                }

                if (size <= maxSize || map.isEmpty()) {
                    break;
                }

                Map.Entry<String, Bitmap> toEvict = map.entrySet().iterator().next();
                if (toEvict == null) {
                    break;
                }
                key = toEvict.getKey();
                value = toEvict.getValue();

                if(value != null){
                    Log.d(TAG,"trimToSize recycle");
                    value.recycle();
                }

                map.remove(key);
                size -= sizeOf(key, value);
            }
        }
    }

    /** Removes the entry for {@code key} if it exists. */
    @Override
    public final Bitmap remove(String key) {
        Log.d(TAG,"remove ");
        if (key == null) {
            throw new NullPointerException("key == null");
        }

        synchronized (this) {
            Bitmap previous = map.remove(key);
            if (previous != null) {
                size -= sizeOf(key, previous);
                Log.d(TAG,"remove recycle");
                previous.recycle();
            }
            return previous;
        }
    }

    @Override
    public Collection<String> keys() {
        synchronized (this) {
            return new HashSet<String>(map.keySet());
        }
    }

    @Override
    public void clear() {
        Log.d(TAG,"clear ");
        trimToSize(-1); // -1 will evict 0-sized elements
    }

    /**
     * Returns the size {@code Bitmap} in bytes.
     * <p/>
     * An entry's size must not change while it is in the cache.
     */
    private int sizeOf(String key, Bitmap value) {
        return value.getRowBytes() * value.getHeight();
    }

    @Override
    public synchronized final String toString() {
        return String.format("LruCache[maxSize=%d]", maxSize);
    }

}
