package me.mattlogan.library;

import android.view.View;

public interface PopAnimation {
    void animate(View view, Callback callback);

    PopAnimation NONE = new PopAnimation() {
        @Override
        public void animate(View view, Callback callback) {
            callback.animationDone();
        }
    };

    interface Callback {
        void animationDone();
    }
}
