package me.mattlogan.pancakes;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;

import me.mattlogan.library.ViewStack;

public class BlueView extends RelativeLayout {

    private final ViewStack viewStack;

    public BlueView(final Context context, final ViewStack viewStack) {
        super(context);
        this.viewStack = viewStack;
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
