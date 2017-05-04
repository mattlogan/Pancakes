package me.mattlogan.pancakes.view;

import android.content.Context;
import android.graphics.Canvas;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import me.mattlogan.library.ViewStack;
import me.mattlogan.pancakes.R;
import me.mattlogan.pancakes.ViewStackActivity;
import me.mattlogan.pancakes.animation.CircularHide;
import me.mattlogan.pancakes.animation.CircularReveal;

public class GreenView extends RelativeLayout {

    public GreenView(Context context, AttributeSet attrs) {
        super(context, attrs);
        Log.d("testing", "GreenView (" + hashCode() + ") created");
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        Log.d("testing", "GreenView (" + hashCode() + ") onFinishInflate");

        if (!(getContext() instanceof ViewStackActivity)) return;

        final ViewStack viewStack = ((ViewStackActivity) getContext()).viewStack();

        findViewById(R.id.green_button_back).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("testing", "GreenView popping itself");
                viewStack.popWithAnimation(new CircularHide());
            }
        });

        findViewById(R.id.green_button_go_to_blue).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("testing", "GreenView pushing BlueView");
                viewStack.pushWithAnimation(R.layout.view_blue, new CircularReveal());
            }
        });
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        Log.d("testing", "GreenView (" + hashCode() + ") onAttachedToWindow");
    }

    // Note: This won't be called when we push the next View onto the stack because this View is
    // kept in the container's view hierarchy. It's visibility is just set to gone.
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        Log.d("testing", "GreenView (" + hashCode() + ") onDetachedFromWindow");
    }

    // Note: These instance state saving methods will only be called if the view has an id.
    @Override
    protected Parcelable onSaveInstanceState() {
        Log.d("testing", "GreenView (" + hashCode() + ") onSaveInstanceState");
        return super.onSaveInstanceState();
    }

    @Override
    protected void onRestoreInstanceState(Parcelable savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Log.d("testing", "GreenView (" + hashCode() + ") onRestoreInstanceState");
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Log.d("testing", "GreenView (" + hashCode() + ") onDraw");
    }
}
