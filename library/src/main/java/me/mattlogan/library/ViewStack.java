package me.mattlogan.library;

import android.view.View;
import android.view.ViewGroup;

import java.util.Stack;

public final class ViewStack {

    private final Stack<View> stack;
    private final ViewGroup container;

    public static ViewStack create(ViewGroup container) {
        return new ViewStack(new Stack<View>(), container);
    }

    private ViewStack(Stack<View> stack, ViewGroup container) {
        this.stack = stack;
        this.container = container;
    }

    public View push(View view) {
        stack.push(view);
        updateContainer();
        return view;
    }

    public View pop() {
        View view = stack.pop();
        updateContainer();
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
