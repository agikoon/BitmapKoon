package com.agikoon.lib.bitmap;

import android.content.Context;
import android.graphics.Bitmap;

import com.agikoon.lib.bitmap.util.BitmapManager;

import java.io.File;

/**
 * Created by JuHH on 15. 6. 26..
 */
public abstract class BitmapTaskFile extends BitmapTask {

    public BitmapTaskFile(Context context, Object object) {
        super(context, object);
    }

    /**
     * params[0] = Full File Path
     */
    @Override
    protected Bitmap doInBackground(Object... params) {
        // Error 체크
        if (params == null || params.length == 0)
            throw new NullPointerException("Not Null params");

        String mPath = params[0].toString();

        // 파일 체크
        File checkFile = new File(mPath);
        if(checkFile.isFile() == false)
            throw new NullPointerException("Only File Load");

        return BitmapManager.getBitmapFile(context, mPath);
    }
}
