package me.mattlogan.pancakes;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ViewGroup;

import me.mattlogan.library.ViewStack;

public class MainActivity extends AppCompatActivity {

    private ViewStack viewStack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        viewStack = ViewStack.create((ViewGroup) findViewById(R.id.container));
        viewStack.push(new RedView(this, viewStack));
    }

    @Override
    public void onBackPressed() {
        if (viewStack.size() > 0) {
            viewStack.pop();
        } else {
            super.onBackPressed();
        }
    }
}
