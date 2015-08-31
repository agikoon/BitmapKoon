package com.agikoon.lib.bitmap;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;

/**
 * Created by JuHH on 15. 6. 26..
 */
public abstract class BitmapTask extends AsyncTask<Object, Void, Bitmap> {

    /**
     * Context
     */
    protected Context context;
    /**
     * Object
     */
    protected Object object;

    public abstract void resultBitmapTask(Bitmap bitmap, Object object);

    public BitmapTask(Context context, Object object) {
        this.context = context;
        this.object = object;
    }

    /**
     * 결과
     *
     * @param bitmap
     */
    @Override
    protected void onPostExecute(Bitmap bitmap) {

        if(isCancelled())
            return;

        if (bitmap != null) {
            resultBitmapTask(bitmap, object);
        }
    }
}
