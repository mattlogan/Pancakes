package me.mattlogan.pancakes;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;

import me.mattlogan.library.ViewStack;


    private ViewStack viewStack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        viewStack = ViewStack.create((ViewGroup) findViewById(R.id.container), this);
        viewStack.push(new RedView(this, viewStack));
    }

    @Override
    }

    @Override
    public void onBackPressed() {
        viewStack.pop();
    }

    @Override
    }
}
