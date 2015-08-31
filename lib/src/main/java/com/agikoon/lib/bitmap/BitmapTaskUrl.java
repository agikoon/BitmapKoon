package com.agikoon.lib.bitmap;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Bitmap;

import com.agikoon.lib.bitmap.util.BitmapManager;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by JuHH on 15. 6. 30..
 */
public abstract class BitmapTaskUrl extends BitmapTask {

    public BitmapTaskUrl(Context context, Object object) {
        super(context, object);
    }

    /**
     * 중요 ***
     * Object Type
     * - [0] : String(Url Address) 타입
     * @param params
     * @return null is no bitmap
     */
    @Override
    protected Bitmap doInBackground(final Object... params) {

        // Error 체크
        if (params == null)
            throw new NullPointerException("Not Null params");

        String mUrl = params[0].toString();
        String fileName = getUrlToFileName(mUrl);

        try {
            URL url = new URL(mUrl);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setDoInput(true);
            // 접속 TimeOut
            con.setConnectTimeout(5000);
            // 읽는 TimeOut
            con.setReadTimeout(30000);
            con.connect();

            InputStream input = con.getInputStream();

            File saveDir = context.getCacheDir();
            if (saveDir.exists() == false)
                saveDir.mkdirs();

            // 파일 위치 생성
            File saveFile = new File(saveDir.getAbsolutePath(), fileName);

            OutputStream output = new FileOutputStream(saveFile);
            byte[] buffer = new byte[2048];
            int length;
            while ((length = input.read(buffer)) >= 0) {
                output.write(buffer, 0, length);
            }

            output.flush();
            output.close();

            return BitmapManager.getBitmapFile(context, saveFile.getAbsolutePath());

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private String getUrlToFileName(String url) {
        // 파일명 구하기
        int fragment = url.lastIndexOf('#');
        if (fragment > 0) {
            url = url.substring(0, fragment);
        }

        int query = url.lastIndexOf('?');
        if (query > 0) {
            url = url.substring(0, query);
        }

        int filenamePos = url.lastIndexOf('/');
        return 0 <= filenamePos ? url.substring(filenamePos + 1) : url;
    }
}
