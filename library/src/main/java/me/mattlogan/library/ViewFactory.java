package me.mattlogan.library;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import java.io.Serializable;

public interface ViewFactory extends Serializable {
    View createView(Context context, ViewGroup container);
}
