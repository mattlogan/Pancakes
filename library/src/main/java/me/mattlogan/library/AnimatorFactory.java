package me.mattlogan.library;

import android.animation.Animator;
import android.animation.TimeInterpolator;
import android.view.View;

public interface AnimatorFactory {
    Animator createAnimator(View view);

    AnimatorFactory NONE = new AnimatorFactory() {
        @Override
        public Animator createAnimator(View view) {
            return new Animator() {
                @Override
                public long getStartDelay() {
                    return 0;
                }

                @Override
                public void setStartDelay(long startDelay) {

                }

                @Override
                public Animator setDuration(long duration) {
                    return null;
                }

                @Override
                public long getDuration() {
                    return 0;
                }

                @Override
                public void setInterpolator(TimeInterpolator value) {

                }

                @Override
                public boolean isRunning() {
                    return false;
                }
            };
        }
    };
}
