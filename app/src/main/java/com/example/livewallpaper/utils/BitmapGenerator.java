package com.example.livewallpaper.utils;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;

public class BitmapGenerator {

    public static final String TAG = BitmapGenerator.class.getSimpleName();

    /** Centre Crop Given Bitmap in Square
     * @NonNull Bitmap - Original Bitmap from Glide
     * */
    public static Bitmap centerSquareCrop(@NonNull Bitmap srcBmp) {
        Bitmap dstBmp;//from  w  w  w .  j  a  v  a2s.  c  o  m
        if (srcBmp.getWidth() >= srcBmp.getHeight()) {

            dstBmp = Bitmap.createBitmap(srcBmp, srcBmp.getWidth() / 2 - srcBmp.getHeight() / 2, 0,
                    srcBmp.getHeight(), srcBmp.getHeight());

        } else {

            dstBmp = Bitmap.createBitmap(srcBmp, 0, srcBmp.getHeight() / 2 - srcBmp.getWidth() / 2,
                    srcBmp.getWidth(), srcBmp.getWidth());
        }
        return dstBmp;
    }


    /** Frame Crop Given Bitmap in Square
     * @NonNull Bitmap - Original Bitmap from Glide
     * frameWidth - Device Original Width in pixels
     * frameHeight - Device Original Height in pixels
     * */
    public static Bitmap cropToFrame(@NonNull final Bitmap src, final int frameWidth, final int frameHeight) {
        final int imageWidth = src.getWidth();
        final int imageHeight = src.getHeight();

        float tempHeight = (float) imageWidth / frameWidth * frameHeight;
        Bitmap frameBitmap;//from  w  w  w  .j a  v  a  2 s.c  o  m
        // TH1
        if (tempHeight > imageHeight) {
            float scale = ((float) frameHeight) / imageHeight;
            Bitmap scaledBitmap = scaleAndRotate(src, scale, 0).getBitmap();
            frameBitmap = crop(scaledBitmap, frameWidth, frameHeight);
            scaledBitmap.recycle();
        } else if (tempHeight < imageHeight) {
            float scale = ((float) frameWidth) / imageWidth;
            Bitmap scaledBitmap = scaleAndRotate(src, scale, 0).getBitmap();
            frameBitmap = crop(scaledBitmap, frameWidth, frameHeight);
            scaledBitmap.recycle();
        } else {
            float scale = ((float) frameWidth) / imageWidth;
            frameBitmap = scaleAndRotate(src, scale, 0).getBitmap();
        }
        return frameBitmap;
    }



    /** Secondary Methods */
    private static BitmapDrawable scaleAndRotate(@NonNull Bitmap bitmap, int newWidth, int newHeight, float degrees) {

        // load the original BitMap (500 x 500 px)
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        // int newWidth = 200;
        // int newHeight = 200;

        // calculate the scale - in this case = 0.4f
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;

        // create matrix for the manipulation
        Matrix matrix = new Matrix();
        // resize the bit map
        matrix.postScale(scaleWidth, scaleHeight);
        // rotate the Bitmap
        matrix.postRotate(degrees);

        // recreate the new Bitmap
        Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0, width,
                height, matrix, true);
        return new BitmapDrawable(resizedBitmap);
    }


    private static BitmapDrawable scaleAndRotate(@NonNull Bitmap bitmap, float scale, float degrees) {

        // load the original BitMap (500 x 500 px)
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        float scaleWidth = scale * width;
        float scaleHeight = scale * height;

        // create matrix for the manipulation
        Matrix matrix = new Matrix();
        // resize the bit map
        matrix.postScale(scale, scale);
        // rotate the Bitmap
        matrix.postRotate(degrees);

        // recreate the new Bitmap
        Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
        return new BitmapDrawable(resizedBitmap);
    }
    private static Bitmap crop(Bitmap source, int left, int top, int width, int height) {
        Bitmap cropBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_4444);
        Canvas canvas = new Canvas(cropBitmap);
        canvas.drawBitmap(source, -left, -top, new Paint());
        return cropBitmap;
    }
    private static Bitmap crop(@NonNull Bitmap src, int width, int height) {
        int left = (int) ((src.getWidth() - width) / 2f);
        int top = (int) ((src.getHeight() - height) / 2f);
        return crop(src, left, top, width, height);
    }


    /** Blur Background Dialog Implementation with FastBlur
     * Bitmap screen = BitmapGenerator.takeScreenShot(getActivity());
     * Bitmap blur_back = BitmapGenerator.fastBlur(screen, 10);
     * Drawable background = new BitmapDrawable(getResources(),blur_back);
     * @param activity for screenshot
     * @return bitmap
     */
    public static Bitmap takeScreenShot(Activity activity) {
        View view = activity.getWindow().getDecorView();
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();
        Bitmap b1 = view.getDrawingCache();
        Rect frame = new Rect();
        activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
        int statusBarHeight = frame.top;
        int width = activity.getWindowManager().getDefaultDisplay().getWidth();
        int height = activity.getWindowManager().getDefaultDisplay().getHeight();

        Bitmap b = Bitmap.createBitmap(b1, 0, statusBarHeight, width, height  - statusBarHeight);
        view.destroyDrawingCache();
        return b;
    }
    public static Bitmap fastBlur(Bitmap sentBitmap, int radius) {
        Bitmap bitmap = sentBitmap.copy(sentBitmap.getConfig(), true);

        if (radius < 1) {
            return (null);
        }

        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        int[] pix = new int[w * h];
        Log.e("pix", w + " " + h + " " + pix.length);
        bitmap.getPixels(pix, 0, w, 0, 0, w, h);

        int wm = w - 1;
        int hm = h - 1;
        int wh = w * h;
        int div = radius + radius + 1;

        int[] r = new int[wh];
        int[] g = new int[wh];
        int[] b = new int[wh];
        int rsum, gsum, bsum, x, y, i, p, yp, yi, yw;
        int[] vmin = new int[Math.max(w, h)];

        int divsum = (div + 1) >> 1;
        divsum *= divsum;
        int[] dv = new int[256 * divsum];
        for (i = 0; i < 256 * divsum; i++) {
            dv[i] = (i / divsum);
        }

        yw = yi = 0;

        int[][] stack = new int[div][3];
        int stackpointer;
        int stackstart;
        int[] sir;
        int rbs;
        int r1 = radius + 1;
        int routsum, goutsum, boutsum;
        int rinsum, ginsum, binsum;

        for (y = 0; y < h; y++) {
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
            for (i = -radius; i <= radius; i++) {
                p = pix[yi + Math.min(wm, Math.max(i, 0))];
                sir = stack[i + radius];
                sir[0] = (p & 0xff0000) >> 16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = (p & 0x0000ff);
                rbs = r1 - Math.abs(i);
                rsum += sir[0] * rbs;
                gsum += sir[1] * rbs;
                bsum += sir[2] * rbs;
                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }
            }
            stackpointer = radius;

            for (x = 0; x < w; x++) {

                r[yi] = dv[rsum];
                g[yi] = dv[gsum];
                b[yi] = dv[bsum];

                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;

                stackstart = stackpointer - radius + div;
                sir = stack[stackstart % div];

                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];

                if (y == 0) {
                    vmin[x] = Math.min(x + radius + 1, wm);
                }
                p = pix[yw + vmin[x]];

                sir[0] = (p & 0xff0000) >> 16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = (p & 0x0000ff);

                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];

                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;

                stackpointer = (stackpointer + 1) % div;
                sir = stack[(stackpointer) % div];

                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];

                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];

                yi++;
            }
            yw += w;
        }
        for (x = 0; x < w; x++) {
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
            yp = -radius * w;
            for (i = -radius; i <= radius; i++) {
                yi = Math.max(0, yp) + x;

                sir = stack[i + radius];

                sir[0] = r[yi];
                sir[1] = g[yi];
                sir[2] = b[yi];

                rbs = r1 - Math.abs(i);

                rsum += r[yi] * rbs;
                gsum += g[yi] * rbs;
                bsum += b[yi] * rbs;

                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }

                if (i < hm) {
                    yp += w;
                }
            }
            yi = x;
            stackpointer = radius;
            for (y = 0; y < h; y++) {
                // Preserve alpha channel: ( 0xff000000 & pix[yi] )
                pix[yi] = ( 0xff000000 & pix[yi] ) | ( dv[rsum] << 16 ) | ( dv[gsum] << 8 ) | dv[bsum];

                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;

                stackstart = stackpointer - radius + div;
                sir = stack[stackstart % div];

                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];

                if (x == 0) {
                    vmin[y] = Math.min(y + r1, hm) * w;
                }
                p = x + vmin[y];

                sir[0] = r[p];
                sir[1] = g[p];
                sir[2] = b[p];

                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];

                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;

                stackpointer = (stackpointer + 1) % div;
                sir = stack[stackpointer];

                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];

                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];

                yi += w;
            }
        }

        Log.e("pix", w + " " + h + " " + pix.length);
        bitmap.setPixels(pix, 0, w, 0, 0, w, h);

        return (bitmap);
    }




}
