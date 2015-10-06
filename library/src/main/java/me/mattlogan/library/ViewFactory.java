package me.mattlogan.library;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import java.io.Serializable;

/**
 * Interface for deferred creation of View instances.
 */
public interface ViewFactory extends Serializable {
    View createView(Context context, ViewGroup container);
}
