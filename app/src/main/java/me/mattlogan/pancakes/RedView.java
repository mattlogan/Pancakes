package me.mattlogan.pancakes;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;

import me.mattlogan.library.ViewFactory;
import me.mattlogan.library.ViewStack;

public class RedView extends RelativeLayout {

    public static class Factory implements ViewFactory {
        @Override
        public View createView(Context context) {
            return new RedView(context);
        }
    }

    public RedView(final Context context) {
        super(context);
        final ViewStack viewStack = ((ViewStackActivity) context).viewStack();
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
                viewStack.push(new GreenView.Factory());
            }
        });
    }
}
