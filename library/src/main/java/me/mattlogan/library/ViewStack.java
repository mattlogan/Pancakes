package me.mattlogan.library;

import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;

import java.util.Stack;

public final class ViewStack {

    private static final String STACK_TAG = "stack";

    private final Stack<ViewFactory> stack = new Stack<>();
    private final ViewGroup container;

    public static ViewStack create(ViewGroup container) {
        return new ViewStack(container);
    }

    private ViewStack(ViewGroup container) {
        this.container = container;
    }

    public void push(ViewFactory viewFactory) {
        stack.push(viewFactory);
        updateContainer();
    }

    @SuppressWarnings("all")
    public void rebuildFromBundle(Bundle bundle) {
        stack.addAll((Stack<ViewFactory>) bundle.getSerializable(STACK_TAG));
        updateContainer();
    }

    public void pop() {
        stack.pop();
        updateContainer();
    }

    public int size() {
        return stack.size();
    }

    private void updateContainer() {
        Log.d("testing", "updateContainer: " + container);
        container.removeAllViews();
        if (stack.size() > 0) {
            container.addView(stack.peek().createView(container.getContext()));
            Log.d("testing", "view: " + container.getChildAt(0));
        }
    }

    public void saveToBundle(Bundle bundle) {
        bundle.putSerializable(STACK_TAG, stack);
    }
}
