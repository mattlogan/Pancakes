package me.mattlogan.pancakes;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ViewGroup;

import me.mattlogan.library.ViewStack;

public class MainActivity extends AppCompatActivity implements ViewStackActivity {

    private ViewStack viewStack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewStack = ViewStack.create((ViewGroup) findViewById(R.id.container));

        if (savedInstanceState != null) {
            viewStack.rebuildFromBundle(savedInstanceState);
        } else {
            viewStack.push(new RedView.Factory());
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        viewStack.saveToBundle(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onBackPressed() {
        viewStack.pop();
    }

    @Override
    public ViewStack viewStack() {
        return viewStack;
    }
}
