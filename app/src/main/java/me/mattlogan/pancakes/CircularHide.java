package me.mattlogan.pancakes;

import android.animation.Animator;
import android.view.View;
import android.view.ViewAnimationUtils;

import me.mattlogan.library.AnimatorFactory;

public class CircularHide implements AnimatorFactory {

    @Override
    public Animator createAnimator(View view) {
        // get the center for the clipping circle
        int cx = view.getWidth() / 2;
        int cy = view.getHeight() / 2;

        // get the initial radius for the clipping circle
        int initialRadius = view.getWidth();

        // create the animation (the final radius is zero)
        return ViewAnimationUtils.createCircularReveal(view, cx, cy, initialRadius, 0);
    }
}
