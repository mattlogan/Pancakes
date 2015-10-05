package me.mattlogan.pancakes;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;

import me.mattlogan.library.StatefulView;
import me.mattlogan.library.ViewFactory;
import me.mattlogan.library.ViewStack;

public class BlueView extends RelativeLayout implements StatefulView {

    public static class Factory implements ViewFactory {
        @Override
        public View createView(Context context, ViewGroup container) {
            return LayoutInflater.from(context).inflate(R.layout.view_blue, container, false);
        }
    }

    private static final String SELECTED_RADIO_BUTTON_ID = "selected_radio_button_id";

    private RadioGroup radioGroup;

    public BlueView(Context context, AttributeSet attrs) {
        super(context, attrs);
        Log.d("testing", "BlueView (" + hashCode() + ") created");
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        Log.d("testing", "BlueView (" + hashCode() + ") onFinishInflate");

        final ViewStack viewStack = ((ViewStackActivity) getContext()).viewStack();

        setBackgroundColor(Color.BLUE);

        findViewById(R.id.blue_button_back).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                viewStack.pop();
            }
        });

        radioGroup = (RadioGroup) findViewById(R.id.blue_radio_group);
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

    @Override
    public void saveState(Bundle bundle) {
        bundle.putInt(SELECTED_RADIO_BUTTON_ID, radioGroup.getCheckedRadioButtonId());
    }

    @Override
    public void recreateState(Bundle bundle) {
        radioGroup.check(bundle.getInt(SELECTED_RADIO_BUTTON_ID));
    }
}
