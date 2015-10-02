package me.mattlogan.library;

import android.os.Bundle;

public interface StatefulView {
    void saveState(Bundle bundle);
    void recreateState(Bundle bundle);
}
