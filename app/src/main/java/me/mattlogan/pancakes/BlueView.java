package me.mattlogan.pancakes;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import me.mattlogan.library.ViewFactory;
import me.mattlogan.library.ViewStack;

public class BlueView extends RelativeLayout {

    public static class Factory implements ViewFactory {
        @Override
        public View createView(Context context, ViewGroup container) {
            return LayoutInflater.from(context).inflate(R.layout.view_blue, container, false);
        }
    }

    public BlueView(Context context, AttributeSet attrs) {
        super(context, attrs);
        Log.d("testing", "BlueView (" + hashCode() + ") created");
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        Log.d("testing", "BlueView (" + hashCode() + ") onFinishInflate");

        final ViewStack viewStack = ((ViewStackActivity) getContext()).viewStack();

        findViewById(R.id.blue_button_back).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                viewStack.pop();
            }
        });
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
