package me.mattlogan.pancakes.view;

import android.content.Context;
import android.graphics.Canvas;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;

import me.mattlogan.library.ViewStack;
import me.mattlogan.pancakes.animation.CircularHide;
import me.mattlogan.pancakes.R;
import me.mattlogan.pancakes.ViewStackActivity;

public class BlueView extends RelativeLayout {

    public BlueView(Context context, AttributeSet attrs) {
        super(context, attrs);
        Log.d("testing", "BlueView (" + hashCode() + ") created");
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        Log.d("testing", "BlueView (" + hashCode() + ") onFinishInflate");

        if (!(getContext() instanceof ViewStackActivity)) return;

        final ViewStack viewStack = ((ViewStackActivity) getContext()).viewStack();

        findViewById(R.id.blue_button_back).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("testing", "BlueView popping itself");
                viewStack.popWithAnimation(new CircularHide());
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

    // Note: These instance state saving methods will only be called if the view has an id.
    @Override
    protected Parcelable onSaveInstanceState() {
        Log.d("testing", "BlueView (" + hashCode() + ") onSaveInstanceState");
        return super.onSaveInstanceState();
    }

    @Override
    protected void onRestoreInstanceState(Parcelable savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Log.d("testing", "BlueView (" + hashCode() + ") onRestoreInstanceState");
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Log.d("testing", "BlueView (" + hashCode() + ") onDraw");
    }
}
