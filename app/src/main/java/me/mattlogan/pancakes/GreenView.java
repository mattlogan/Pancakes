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

public class GreenView extends RelativeLayout implements StatefulView {

    public static class Factory implements ViewFactory {
        @Override
        public View createView(Context context, ViewGroup container) {
            return LayoutInflater.from(context).inflate(R.layout.view_green, container, false);
        }
    }

    private static final String SELECTED_RADIO_BUTTON_ID = "selected_radio_button_id";

    private RadioGroup radioGroup;

    public GreenView(Context context, AttributeSet attrs) {
        super(context, attrs);
        Log.d("testing", "GreenView (" + hashCode() + ") created");
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        Log.d("testing", "GreenView (" + hashCode() + ") onFinishInflate");

        final ViewStack viewStack = ((ViewStackActivity) getContext()).viewStack();

        setBackgroundColor(Color.GREEN);

        findViewById(R.id.green_button_back).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                viewStack.pop();
            }
        });

        findViewById(R.id.green_button_go_to_blue).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                viewStack.push(new BlueView.Factory());
            }
        });

        radioGroup = (RadioGroup) findViewById(R.id.green_radio_group);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        Log.d("testing", "GreenView (" + hashCode() + ") onAttachedToWindow");
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        Log.d("testing", "GreenView (" + hashCode() + ") onDetachedFromWindow");
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
