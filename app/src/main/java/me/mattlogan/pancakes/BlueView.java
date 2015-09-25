package me.mattlogan.pancakes;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;

import me.mattlogan.library.ViewStack;
import me.mattlogan.library.ViewStackActivity;

public class BlueView extends RelativeLayout {


    public BlueView(final Context context) {
        super(context);
        final ViewStack viewStack = ((ViewStackActivity) context).viewStack();
        LayoutInflater.from(context).inflate(R.layout.view_blue, this, true);

        setBackgroundColor(Color.BLUE);

        findViewById(R.id.blue_button_back).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                viewStack.pop();
            }
        });
    }
}
