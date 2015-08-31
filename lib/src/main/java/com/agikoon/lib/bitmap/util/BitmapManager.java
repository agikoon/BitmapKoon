package com.agikoon.lib.bitmap.util;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.util.Log;
import android.webkit.MimeTypeMap;

import java.io.InputStream;

/**
 * Created by JuHH on 15. 6. 25..
 *
 * Bitmap을 불러올때 Out Of Memory가 나오지 않게 관리하는 class
 */
public class BitmapManager {

    final static String TAG = "BitmapManager";

    /**
     * File로 있는 이미지 크기를 반환
     * @param path
     * @return
     */
    public static ImageInfo getBitmapSizeFile(String path) {

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        BitmapFactory.decodeFile(path, options);

        return getImageInfo(options);
    }

    /**
     * Stream 형태로 받은 파일의 크기를 반환
     * @param is
     * @return
     */
    public static ImageInfo getBitmapSizeInputStream(InputStream is) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        Rect rectOut = null;
        BitmapFactory.decodeStream(is, rectOut, options);

        return getImageInfo(options);
    }

    /**
     * Resource에 있는 이미지 크기를 반환한다.
     * @param ctx
     * @param resId
     * @return
     */
    public static ImageInfo getBitmapSizeRes(Context ctx, int resId) {

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        BitmapFactory.decodeResource(ctx.getResources(), resId, options);

        return getImageInfo(options);
    }

    /**
     * 이미지 정보를 반환
     * @param options
     * @return
     */
    public static ImageInfo getImageInfo(BitmapFactory.Options options) {
        if(options.outMimeType == null)
            return null;

        ImageInfo imageInfo = new ImageInfo();
        imageInfo.setWidth(options.outWidth);
        imageInfo.setHeight(options.outHeight);
        imageInfo.setMimeType(options.outMimeType);

        return imageInfo;
    }

    /**
     * Bitmap 변환시 사용되는 메모리 용량을 계산한다.
     * @param imageInfo
     * @return
     */
    private static long getBitmapMemory(ImageInfo imageInfo) {
        int bitmapByte = 2;

        if(imageInfo.getMimeType().equals(MimeTypeMap.getSingleton().getMimeTypeFromExtension("png")))
            bitmapByte = 4;

        // 실제 차지하는 메모리 계산
        long bitmapMemory = imageInfo.getWidth() * imageInfo.getHeight() * bitmapByte;

        return bitmapMemory;
    }

    /**
     * 메모리 체크
     * @param imageInfo
     * @return
     */
    public static boolean isUseMemoryCheck(Context ctx, ImageInfo imageInfo) {

        // Bitmap 가용 메모리
        long useBitmapMemory = getBitmapMemory(imageInfo);
        // 프로세스 가용 메모리
        long freeMemory = Runtime.getRuntime().freeMemory();

        ActivityManager activityManager = (ActivityManager) ctx.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        activityManager.getMemoryInfo(memoryInfo);

        // 프로세스 가용 메모리보다 크면 false 반환
        if(memoryInfo.availMem < useBitmapMemory) {
            Log.e(TAG, "Not Use Memory");
            return false;
        }

        return true;
    }

    /**
     * Bitmap 이미지를 캐쉬에 저장한다.
     * @param key
     * @param value
     */
    private static void putBitmapCache(Context ctx, String key, Bitmap value) {
        if(value == null)
            return;

        ImageInfo imageInfo = new ImageInfo();
        imageInfo.setWidth(value.getWidth());
        imageInfo.setHeight(value.getHeight());

        if(getBitmapCache(ctx, key, imageInfo) == null)
            BitmapLrucache.singleton(ctx).put(key, value);
    }

    /**
     * 캐쉬에 저장되어있는 Bitmap 이미지를 반환한다.
     * 단, 사이즈가 다르면 반환을 안한다.
     * @param key
     * @param imageInfo
     * @return
     */
    private static Bitmap getBitmapCache(Context ctx, String key, ImageInfo imageInfo) {

        Bitmap bitmapCache = BitmapLrucache.singleton(ctx).getLruBitmap(key);
        if(bitmapCache == null)
            return null;

        long bitmapSize = getBitmapMemory(imageInfo);
        int bitmapCacheSize = bitmapCache.getByteCount();

        if(bitmapCacheSize != bitmapSize)
            return null;

        return bitmapCache;
    }


    /**
     * 이미지를 원본형태로 불러와서 반환한다.
     * @param ctx
     * @param resId
     * @return
     */
    public static Bitmap getBitmapRes(Context ctx, int resId) {

        ImageInfo imageInfo = getBitmapSizeRes(ctx, resId);
        if(imageInfo == null)
            return null;

        if(isUseMemoryCheck(ctx, imageInfo)) {
            return BitmapFactory.decodeResource(ctx.getResources(), resId);
        }
        else {
            return null;
        }
    }

    /**
     * 이미지를 scale 값에 따라 줄여서 반환한다.
     * @param ctx
     * @param resId
     * @param scale : 되도록이면 2의 배수
     * @return
     */
    public static Bitmap getBitmapResScale(Context ctx, int resId, int scale) {

        ImageInfo imageInfo = getBitmapSizeRes(ctx, resId);
        if(imageInfo == null) {

            return null;
        }

        if(isUseMemoryCheck(ctx, imageInfo)) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = scale <= 1 ? 2: scale;

            return BitmapFactory.decodeResource(ctx.getResources(), resId, options);
        }
        else {

            return null;
        }
    }

    /**
     * 이미지를 ReSize 시킨다.
     * @param ctx
     * @param resId
     * @param width
     * @param height
     * @return
     */
    public static Bitmap getBitmapResReSize(Context ctx, int resId, int width, int height) {

        ImageInfo rect = getBitmapSizeRes(ctx, resId);
        if(rect == null)
            return null;

        if(isUseMemoryCheck(ctx, rect)) {

            Bitmap bitmap = null;
            if(rect.width > width && rect.height > height) {
                int sampleSize = Math.min(rect.width / width, rect.height / height);
                if (rect.width / sampleSize > width && rect.height / sampleSize > height) {
                    bitmap = getBitmapResScale(ctx, resId, sampleSize);
                }
            }
            else {
                bitmap = getBitmapRes(ctx, resId);
            }

            ImageInfo scaleRect = new ImageInfo();
            scaleRect.setSize(width, height);
            scaleRect.setMimeType(rect.mimeType);

            // 줄이기전에 가용 메모리가 있는지 한번더 체크
            if(isUseMemoryCheck(ctx, scaleRect)) {
                Bitmap scaleBitmap = Bitmap.createScaledBitmap(bitmap, width, height, true);
                if (bitmap.isRecycled() == false)
                    bitmap.recycle();
                return scaleBitmap;
            }
            else {
                if(bitmap != null && bitmap.isRecycled() == false)
                    bitmap.recycle();

                return null;
            }
        }
        else {

            return null;
        }
    }

    /**
     * 파일에서 Bitmap을 불러온다.
     * @param ctx
     * @param path
     * @return
     */
    public static Bitmap getBitmapFile(Context ctx, String path) {

        ImageInfo rect = getBitmapSizeFile(path);
        if(rect == null)
            return null;

        if(isUseMemoryCheck(ctx, rect)) {

            return BitmapFactory.decodeFile(path);
        }
        else {

            return null;
        }
    }

    /**
     * 파일에서 이미지를 불러온다.
     * 불러오면서 scale에 맞게 줄여서 불러온다.
     * @param ctx
     * @param path
     * @param scale
     * @return
     */
    public static Bitmap getBitmapFileScale(Context ctx, String path, int scale) {

        ImageInfo rect = getBitmapSizeFile(path);
        if(rect == null)
            return null;

        if(isUseMemoryCheck(ctx, rect)) {

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = scale <= 1 ? 2: scale;
            return BitmapFactory.decodeFile(path, options);
        }
        else {

            return null;
        }
    }

    /**
     * 변경된 해상도로 이미지를 불러온다.
     * @param ctx
     * @param path
     * @param width
     * @param height
     * @return
     */
    public static Bitmap getBitmapFileReSize(Context ctx, String path, int width, int height) {

        ImageInfo rect = getBitmapSizeFile(path);
        if(rect == null)
            return null;

        if(isUseMemoryCheck(ctx, rect)) {

            Bitmap bitmap = null;
            if(rect.width > width && rect.height > height) {

                int sampleSize = Math.min(rect.width / width, rect.height / height);
                sampleSize = sampleSize > 1 ? sampleSize - 1 : sampleSize;
                if (rect.width / sampleSize > width && rect.height / sampleSize > height) {

                    bitmap = getBitmapFileScale(ctx, path, sampleSize);
                }
            }
            else {
                bitmap = getBitmapFile(ctx, path);
            }

            if(bitmap == null)
                return null;

            ImageInfo scaleRect = new ImageInfo();
            scaleRect.setSize(width, height);
            scaleRect.setMimeType(rect.mimeType);

            // 줄이기전에 가용 메모리가 있는지 한번더 체크
            if(isUseMemoryCheck(ctx, scaleRect)) {

                Bitmap scaleBitmap = Bitmap.createScaledBitmap(bitmap, width, height, true);
                if (bitmap.isRecycled() == false)
                    bitmap.recycle();

                return scaleBitmap;
            }
            else {
                if(bitmap != null && bitmap.isRecycled() == false) {

                    bitmap.recycle();
                }

                return null;
            }
        }
        else {

            return null;
        }
    }

    /**
     * InputStream 에서 불러온다.
     * @param ctx
     * @param is
     * @return
     */
    public static Bitmap getBitmapDecode(Context ctx, InputStream is) {
        ImageInfo rect = getBitmapSizeInputStream(is);
        if(rect == null)
            return null;

        if(isUseMemoryCheck(ctx, rect)) {

            return BitmapFactory.decodeStream(is);
        }
        else {

            return null;
        }
    }

    public static Bitmap getBitmapDecodeScale(Context ctx, InputStream is, int scale) {
        ImageInfo rect = getBitmapSizeInputStream(is);
        if(rect == null)
            return null;

        if(isUseMemoryCheck(ctx, rect)) {
            Rect rectTemp = null;
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = scale <= 1 ? 2: scale;
            return BitmapFactory.decodeStream(is, rectTemp, options);
        }
        else {

            return null;
        }
    }

    public static Bitmap getBitmapDecodeReSize(Context ctx, InputStream is, int width, int height) {
        ImageInfo rect = getBitmapSizeInputStream(is);
        if(rect == null)
            return null;

        if(isUseMemoryCheck(ctx, rect)) {

            Bitmap bitmap = null;
            if(rect.width > width && rect.height > height) {

                int sampleSize = Math.min(rect.width / width, rect.height / height);
                sampleSize = sampleSize > 1 ? sampleSize - 1 : sampleSize;
                if (rect.width / sampleSize > width && rect.height / sampleSize > height) {

                    bitmap = getBitmapDecodeScale(ctx, is, sampleSize);
                }
            }
            else {
                bitmap = getBitmapDecode(ctx, is);
            }

            if(bitmap == null)
                return null;

            ImageInfo scaleRect = new ImageInfo();
            scaleRect.setSize(width, height);
            scaleRect.setMimeType(rect.mimeType);

            // 줄이기전에 가용 메모리가 있는지 한번더 체크
            if(isUseMemoryCheck(ctx, scaleRect)) {

                Bitmap scaleBitmap = Bitmap.createScaledBitmap(bitmap, width, height, true);
                if (bitmap.isRecycled() == false)
                    bitmap.recycle();

                return scaleBitmap;
            }
            else {
                if(bitmap != null && bitmap.isRecycled() == false) {

                    bitmap.recycle();
                }

                return null;
            }
        }
        else {

            return null;
        }
    }

    public static class ImageInfo {
        // 이미지 가로 사이즈
        private int width;
        // 이미지 세로 사이즈
        private int height;
        // 이미지 mime Type
        private String mimeType;

        public void setSize(int width, int height) {
            this.width = width;
            this.height = height;
        }

        public int getWidth() {
            return width;
        }

        public void setWidth(int width) {
            this.width = width;
        }

        public int getHeight() {
            return height;
        }

        public void setHeight(int height) {
            this.height = height;
        }

        public String getMimeType() {
            return mimeType == null ? "" : mimeType;
        }

        public void setMimeType(String mimeType) {
            this.mimeType = mimeType;
        }
    }
}
