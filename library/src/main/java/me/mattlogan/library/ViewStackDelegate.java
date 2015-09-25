package me.mattlogan.library;

public interface ViewStackDelegate {
    boolean shouldUpdateViewStack(int oldSize, int newSize);
    void onViewStackUpdated(int size);

    ViewStackDelegate DEFAULT = new ViewStackDelegate() {
        @Override
        public boolean shouldUpdateViewStack(int oldSize, int newSize) {
            return newSize != 0;
        }

        @Override
        public void onViewStackUpdated(int size) {
            // Ignore
        }
    };
}
