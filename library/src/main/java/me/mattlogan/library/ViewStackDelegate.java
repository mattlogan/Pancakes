package me.mattlogan.library;

public interface ViewStackDelegate {
    void onFinishStack();

    ViewStackDelegate DEFAULT = new ViewStackDelegate() {
        @Override
        public void onFinishStack() {
            // Ignore
        }
    };
}
