package com.agikoon.lib.bitmap;

import android.content.Context;
import android.graphics.Bitmap;

import com.agikoon.lib.bitmap.util.BitmapManager;

/**
 * Created by JuHH on 15. 6. 25..
 */
public abstract class BitmapTaskRes extends BitmapTask {

    public BitmapTaskRes(Context context, Object object) {
        super(context, object);
    }

    /**
     * [0] : Resource ID 값
     * @param params
     * @return null 일경우에는 Bitmap 을 못불러옴
     */
    @Override
    protected Bitmap doInBackground(Object[] params) {

        // Error 체크
        if (params == null)
            throw new NullPointerException("Not Null params");

        if(params[0] instanceof Integer == false)
            throw new NullPointerException("Resource is Integer Path");

        int resId = Integer.parseInt(params[0].toString());

        return BitmapManager.getBitmapRes(context, resId);
    }
}
