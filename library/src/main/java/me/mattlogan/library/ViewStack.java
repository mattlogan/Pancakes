package me.mattlogan.library;

import android.view.View;
import android.view.ViewGroup;

import java.util.Stack;

public final class ViewStack {

    private final Stack<View> stack;
    private final ViewGroup container;
    private final ViewStackListener listener;

    public static ViewStack create(ViewGroup container) {
        return new ViewStack(container, ViewStackListener.DEFAULT);
    }

    public static ViewStack create(ViewGroup container, ViewStackListener listener) {
        return new ViewStack(container, listener);
    }

    private ViewStack(ViewGroup container, ViewStackListener listener) {
        this.stack = new Stack<>();
        this.container = container;
        this.listener = listener;
    }

    public View push(View view) {
        stack.push(view);
        updateContainer();
        listener.onViewStackChanged(stack.size());
        return view;
    }

    public View pop() {
        boolean shouldPop = listener.onPopRequested(stack.size());
        if (!shouldPop) return null;
        View view = stack.pop();
        updateContainer();
        listener.onViewStackChanged(stack.size());
        return view;
    }

    public View peek() {
        return stack.peek();
    }

    public int size() {
        return stack.size();
    }

    private void updateContainer() {
        container.removeAllViews();
        if (stack.size() > 0) {
            container.addView(stack.peek());
        }
    }
}
