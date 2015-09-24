package me.mattlogan.pancakes;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;

import me.mattlogan.library.ViewStack;
import me.mattlogan.library.ViewStackListener;

public class MainActivity extends AppCompatActivity implements ViewStackListener {

    private ViewStack viewStack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        viewStack = ViewStack.create((ViewGroup) findViewById(R.id.container), this);
        viewStack.push(new RedView(this, viewStack));
    }

    @Override
    public void onBackPressed() {
        viewStack.pop();
    }

    @Override
    public boolean onPopRequested(int size) {
        if (size <= 1) {
            finish();
            return false;
        }
        return true;
    }

    @Override
    public void onViewStackChanged(int size) {
        Log.d("Pancakes", "View stack changed: " + size);
    }
}
