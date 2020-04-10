/**
 *
 */
package com.example.livewallpaper.resource_wall;

import android.view.MotionEvent;
import android.view.GestureDetector.SimpleOnGestureListener;

/**
 * @author yincrash
 *
 */
public class DoubleTapGestureListener extends SimpleOnGestureListener {

    private final ResourceWallpaper.ResourceEngine resourceEngine;

    public DoubleTapGestureListener(ResourceWallpaper.ResourceEngine resourceEngine) {
        this.resourceEngine = resourceEngine;
    }

    /* (non-Javadoc)
     * @see android.view.GestureDetector.SimpleOnGestureListener#onDoubleTap(android.view.MotionEvent)
     */
    @Override
    public boolean onDoubleTap(MotionEvent e) {
        if (this.resourceEngine.allowClickToChange()) {
            resourceEngine.incrementCounter();
            resourceEngine.showNewImage();
        }
        return true;
    }


}

