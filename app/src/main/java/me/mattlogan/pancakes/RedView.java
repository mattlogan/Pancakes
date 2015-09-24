package me.mattlogan.pancakes;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;

import me.mattlogan.library.ViewStack;

public class RedView extends RelativeLayout {

    private final ViewStack viewStack;

    public RedView(final Context context, final ViewStack viewStack) {
        super(context);
        this.viewStack = viewStack;
        LayoutInflater.from(context).inflate(R.layout.view_red, this, true);

        setBackgroundColor(Color.RED);

        findViewById(R.id.red_button_back).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                viewStack.pop();
            }
        });

        findViewById(R.id.red_button_go_to_green).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                viewStack.push(new GreenView(context, viewStack));
            }
        });
    }
}
