package me.mattlogan.library;

import android.os.Bundle;

import java.io.Serializable;

final class ViewData implements Serializable {

    private final ViewFactory viewFactory;
    private final Bundle bundle = new Bundle();

    public ViewData(ViewFactory viewFactory) {
        this.viewFactory = viewFactory;
    }

    public ViewFactory viewFactory() {
        return viewFactory;
    }

    public Bundle bundle() {
        return bundle;
    }
}
