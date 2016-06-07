package me.mattlogan.library;

import android.support.annotation.LayoutRes;
import android.view.View;
import android.view.ViewGroup;

/**
 * A simple interface which allows us to mock the process of inflating views for testing.
 */
interface PancakesViewInflater {
    View inflateView(@LayoutRes int layoutResource, ViewGroup container);
}
