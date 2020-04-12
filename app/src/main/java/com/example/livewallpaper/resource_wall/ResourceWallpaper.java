package com.example.livewallpaper.resource_wall;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.service.wallpaper.WallpaperService;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.WindowManager;
import com.example.livewallpaper.R;
import com.example.livewallpaper.utils.BitmapGenerator;

import java.util.Objects;

public class ResourceWallpaper extends WallpaperService {

    public static final String TAG = ResourceWallpaper.class.getSimpleName();
    private int[] mImagesArray = new int[]{
                R.drawable.image_0, R.drawable.image_1,
                R.drawable.image_2, R.drawable.image_3,
                R.drawable.image_4, R.drawable.image_5,
                R.drawable.image_6, R.drawable.image_7,
                R.drawable.image_8, R.drawable.image_9,
                R.drawable.image_10, R.drawable.image_11,
                R.drawable.image_12, R.drawable.image_13,
                R.drawable.image_14, R.drawable.image_15,
                R.drawable.image_16, R.drawable.image_17,
                R.drawable.image_18, R.drawable.image_19,
                R.drawable.image_20
    };

    public ResourceWallpaper() {
        super();
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public Engine onCreateEngine() {
        return new ResourceEngine();
    }


    class ResourceEngine extends Engine {

        private final String TAG = ResourceEngine.class.getSimpleName();

        // Runnable Threads
        private final Handler handler = new Handler();
        private final Runnable animate = new Runnable() {
            public void run() {
                incrementCounter();
                showNewImage();
            }
        };
        private final Runnable fadeAnimate = new Runnable() {
            public void run() {
                fadeTransition(currentBitmap, currentAlpha);
            }
        };

        private GestureDetector doubleTapDetector;
        private final BitmapFactory.Options options = new BitmapFactory.Options();
        private final BitmapFactory.Options optionsScale = new BitmapFactory.Options();

        // time related parameters
        private long TIME_SECOND = 1000;
        private long timer = 30 * TIME_SECOND;
        private long timeStarted = 0;

        private Bitmap currentBitmap = null;
        private final Paint imagePaint;

        // temporary parameters
        private int desiredMinimumWidth;
        private int desiredMinimumHeight;
        private int currentAlpha;
        private int mImagesArrayIndex = 0;

        // booleans for wallpaper settings
        private boolean allowClickToChange = true;
        private boolean isFadeTransition = true;

        // scope time booleans
        private boolean isVisible = false;
        private boolean imageIsSetup = false;
        private boolean isRotated = false;

        /**
         * Set up the shared preferences listener and initialize the default prefs
         */
        ResourceEngine() {

            currentAlpha = 0;
            imagePaint = new Paint();
            imagePaint.setAlpha(255);

            options.inTempStorage = new byte[16 * 1024];
            optionsScale.inTempStorage = options.inTempStorage;
            optionsScale.inSampleSize = 4;

            setTouchEventsEnabled(true);
            doubleTapDetector = new GestureDetector(getApplicationContext(),
                    new DoubleTapGestureListener(this));
        }


        /**
         * Important Event Listeners for WallpaperService
         */
        @Override
        public void onSurfaceDestroyed(SurfaceHolder surfaceHolder) {
            isVisible = false;
            handler.removeCallbacks(fadeAnimate);
            handler.removeCallbacks(animate);
        }
        @Override
        public void onCreate(SurfaceHolder surfaceHolder) {
            Log.d(TAG, "onCreate: " + getDesiredMinimumWidth());
            Log.d(TAG, "onCreate: " + getDesiredMinimumHeight());
            this.desiredMinimumWidth = getDesiredMinimumWidth();
            this.desiredMinimumHeight = getDesiredMinimumHeight();

            // preview of initial image
            Log.d(TAG, "onCreate: showNewImage called!");
            showNewImage();
        }
        @Override
        public void onDestroy() {
            handler.removeCallbacks(fadeAnimate);
            handler.removeCallbacks(animate);
        }
        /**
         * Doesn't appear to happen on any current phones.. but to future proof.
         */
        @Override
        public void onDesiredSizeChanged(int desiredWidth, int desiredHeight) {
            this.desiredMinimumHeight = desiredHeight;
            this.desiredMinimumWidth = desiredWidth;
        }
        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            // This happens when wallpaper change
            this.desiredMinimumWidth = width;
            this.desiredMinimumHeight = height;
            imageIsSetup = false;
            isRotated = true;
            Log.d(TAG, "onSurfaceChanged: drawBitmap Called!");
            drawBitmap(currentBitmap);
        }
        @Override
        public void onVisibilityChanged(boolean isVisible) {
            this.isVisible = isVisible;

            // onVisibilityChanged can be called on screen rotation.
            if (isVisible) {
                // if there is a bitmap with time left to keep around, redraw it
                if (systemTime() - timeStarted + 100 < timer) {
                    // for some reason, it's sometimes recycled!
                    if (currentBitmap.isRecycled()) {
                        currentBitmap = BitmapFactory.decodeResource(getResources(),
                                mImagesArray[mImagesArrayIndex], options);
                        imageIsSetup = false;
                    }
                    Log.d(TAG, "onVisibilityChanged: drawBitmap Called!");
                    drawBitmap(currentBitmap);
                    // left over timer
                    handler.postDelayed(animate, timer - (systemTime() - timeStarted));
                } else {
                    // otherwise draw a new one since it's time for a new one
                    Log.d(TAG, "onVisibilityChanged: showNewImage Called!");
                    showNewImage();
                }
            } else {
                Log.d(TAG, "onVisibilityChanged: Handlers Removed!");
                handler.removeCallbacks(fadeAnimate);
                handler.removeCallbacks(animate);
                if (isFadeTransition & mImagesArrayIndex>0){
                    imagePaint.setAlpha(255);
                    Log.d(TAG, "onVisibilityChanged: Fading removed!");
                }
            }
        }
        @Override
        public void onTouchEvent(MotionEvent event) {
            this.doubleTapDetector.onTouchEvent(event);
        }


        /**
         * Draw the bitmap to the surface canvas
         *
         * @param bitmap the bitmap to draw
         */
        private void drawBitmap(Bitmap bitmap) {
            Log.d(TAG, "drawBitmap: " + mImagesArrayIndex);

            if (bitmap == null) {
                Log.d(TAG, "bitmap == null!");
                return;
            }

            int virtualWidth = this.desiredMinimumWidth;
            int virtualHeight = this.desiredMinimumHeight;


            // not sure why but it happens
            if (virtualWidth == 0) {
                Log.d(TAG, "virtualWidth == 0 !!");
                return;
            }

            // we only need to convert the image once, then we can reuse it
            if (bitmap == currentBitmap && !imageIsSetup) {
                Log.d(TAG, "drawBitmap: cropping started!");
                System.gc();

                // crop bitmap based on the device dimensions
                bitmap = BitmapGenerator.cropToFrame(bitmap, virtualWidth, virtualHeight);


                imageIsSetup = true;
                if (currentBitmap != null && bitmap != currentBitmap) {
                    currentBitmap.recycle();
                }
                currentBitmap = bitmap;


                System.gc();
                Log.d(TAG, "drawBitmap: cropping finished!");
            }


            // draw actual acquired bitmap to the canvas
            Canvas canvas = null;
            try {
                canvas = getSurfaceHolder().lockCanvas();
                if (canvas != null) {
                    canvas.drawColor(Color.BLACK);
                    Log.d(TAG, "drawBitmap: Canvas Drawing bitmap to screen!");
                    canvas.drawBitmap(bitmap, 0, 0, imagePaint);
                }
            } finally {
                if (canvas != null)
                    getSurfaceHolder().unlockCanvasAndPost(canvas);
            }

        }
        /**
         * Called whenever you want a new image. This will also post the message for
         * when the next frame should be drawn
         */
        void showNewImage() {
            Log.d(TAG, "showNewImage: " + mImagesArrayIndex);

            if (currentBitmap != null)
                currentBitmap.recycle();
            currentBitmap = null;
            System.gc();

            try {

                currentBitmap = BitmapFactory.decodeResource(getResources(),
                        mImagesArray[mImagesArrayIndex], options);
                imageIsSetup = false;
                if (isFadeTransition & mImagesArrayIndex>0) {
                    fadeTransition(currentBitmap, 0);
                } else {
                    Log.d(TAG, "showNewImage: drawBitmap Called!");
                    drawBitmap(currentBitmap);
                }
                System.gc();

            } catch (OutOfMemoryError e) {
                try {
                    System.gc();
                    Log.i(TAG, "Image too big, attempting to scale.");
                    currentBitmap = BitmapFactory.decodeResource(getResources(),
                            mImagesArray[mImagesArrayIndex], optionsScale);
                    Log.d(TAG, "showNewImage: drawBitmap Called!");
                    drawBitmap(currentBitmap);
                    Log.i(TAG, "Scale successful.");
                } catch (OutOfMemoryError e2) {
                    Log.e(TAG, "Scale failed: incremented to new wallpaper");
                    // skip to next image.
                    Log.d(TAG, "showNewImage: showNewImage Called!");
                    showNewImage();
                    return;
                }
            }

            /*
             * This is how it animates. After drawing a frame, ask it to draw another
             * one.
             */
            handler.removeCallbacks(animate);
            if (isVisible) {
                handler.postDelayed(animate, timer);
                timeStarted = systemTime();
            }

            System.gc();
        }


        /**
         * Get Current available device rotation
         */
        private int getRotation() {
            Display display = ((WindowManager)
                    Objects.requireNonNull(getSystemService(WINDOW_SERVICE))).getDefaultDisplay();
            return display.getRotation();
        }


        /**
         * Execute a fade transition. Increments the current alpha then draws at
         * that alpha then posts a message for it to be rerun It should cycle from 0
         * to 255 in 13 frames.
         *
         * @param b     the bitmap to fade
         * @param alpha the alpha to start at, or the current alpha
         */
        private void fadeTransition(Bitmap b, int alpha) {
            currentAlpha = alpha;
            currentAlpha += 255 / 12;
            if (currentAlpha > 255) {
                currentAlpha = 255;
            }
            // Log.v(TAG, "alpha " + currentAlpha);
            imagePaint.setAlpha(currentAlpha);
            Log.d(TAG, "fadeTransition: drawBitmap Called!");
            drawBitmap(b);

            /*
             * This is how it animates. After drawing a frame, ask it to draw another
             * one.
             */
            handler.removeCallbacks(fadeAnimate);
            // stop when at full opacity
            if (isVisible && currentAlpha < 255) {
                handler.post(fadeAnimate);
            }
        }


        /**
         * Convenience method to return a time that is suitable for measuring
         * timeouts in milliseconds
         *
         * @return the system time in milliseconds. could be negative.
         * @see System#nanoTime()
         */
        private long systemTime() {
            return System.nanoTime() / 1000000;
        }
        void incrementCounter() {
            mImagesArrayIndex++;
            if (mImagesArrayIndex >= mImagesArray.length) {
                mImagesArrayIndex = 0;
            }
            Log.d(TAG, "incrementCounter: " + mImagesArrayIndex);
        }
        boolean allowClickToChange() {
            return allowClickToChange;
        }

    }


}
