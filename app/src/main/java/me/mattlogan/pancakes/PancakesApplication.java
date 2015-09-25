package me.mattlogan.pancakes;

import android.app.Application;

import com.squareup.leakcanary.LeakCanary;

public class PancakesApplication extends Application {

    @Override public void onCreate() {
        super.onCreate();
        LeakCanary.install(this);
    }
}
