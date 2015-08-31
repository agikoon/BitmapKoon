package com.agikoon.lib.bitmap.util;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

/**
 * Created by JuHH on 15. 6. 26..
 */
public class BitmapLrucache extends LruCache<String, Bitmap> {

    private static BitmapLrucache mBitmapLrucache = null;

    /**
     * singleton
     * @param ctx
     * @return BitmapLrucache
     */
    @TargetApi(8)
    public static BitmapLrucache singleton(Context ctx) {

        if (mBitmapLrucache == null) {
            int memClass = ((ActivityManager) ctx.getSystemService(Context.ACTIVITY_SERVICE)).getMemoryClass();
            int maxCache = 1024 * 1024 * memClass * 8;
            if(maxCache < 0)
                maxCache = Integer.MAX_VALUE;
            mBitmapLrucache = new BitmapLrucache(maxCache);
        }

        return mBitmapLrucache;
    }

    /**
     * @param maxSize for caches that do not override {@link #sizeOf}, this is
     *                the maximum number of entries in the cache. For all other caches,
     *                this is the maximum sum of the sizes of the entries in this cache.
     */
    public BitmapLrucache(int maxSize) {
        super(maxSize);
    }

    public void setLruBitmap(String key, Bitmap value) {
        if (getLruBitmap(key) == null) {
            this.put(key, value);
        }
    }

    public Bitmap getLruBitmap(String key) {
        return this.get(key);
    }
}
