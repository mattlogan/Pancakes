package me.mattlogan.library;

import android.view.View;

public interface PushAnimation {
    void animate(View view, Callback callback);

    PushAnimation NONE = new PushAnimation() {
        @Override
        public void animate(View view, Callback callback) {
            callback.animationDone();
        }
    };

    interface Callback {
        void animationDone();
    }
}
