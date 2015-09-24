package me.mattlogan.library;

public interface ViewStackListener {
    boolean onPopRequested(int size);
    void onViewStackChanged(int size);

    ViewStackListener DEFAULT = new ViewStackListener() {
        @Override
        public boolean onPopRequested(int size) {
            return true;
        }

        @Override
        public void onViewStackChanged(int size) {
            // Ignore
        }
    };
}
