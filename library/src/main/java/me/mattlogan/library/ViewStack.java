package me.mattlogan.library;

import android.view.View;
import android.view.ViewGroup;

import java.util.Stack;

public final class ViewStack {

    private final Stack<View> stack;
    private final ViewGroup container;
    private final ViewStackListener listener;

    public static ViewStack create(ViewGroup container) {
        return new ViewStack(new Stack<View>(), container, ViewStackListener.DEFAULT);
    }

    public static ViewStack create(ViewGroup container, ViewStackListener listener) {
        return new ViewStack(new Stack<View>(), container, listener);
    }

    private ViewStack(Stack<View> stack, ViewGroup container, ViewStackListener listener) {
        this.stack = stack;
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
