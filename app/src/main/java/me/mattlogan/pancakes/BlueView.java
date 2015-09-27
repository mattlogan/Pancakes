package me.mattlogan.pancakes;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;

import me.mattlogan.library.ViewFactory;
import me.mattlogan.library.ViewStack;

public class BlueView extends RelativeLayout {

    public static class Factory implements ViewFactory {
        @Override
        public View createView(Context context) {
            return new BlueView(context);
        }
    }

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

        Log.d("testing", "BlueView (" + hashCode() + ") created");
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        Log.d("testing", "BlueView (" + hashCode() + ") onAttachedToWindow");
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        Log.d("testing", "BlueView (" + hashCode() + ") onDetachedFromWindow");
    }
}
