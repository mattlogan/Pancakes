package me.mattlogan.library;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import java.util.EmptyStackException;
import java.util.Stack;

import static me.mattlogan.library.Preconditions.checkNotNull;
import static me.mattlogan.library.Preconditions.checkStringNotEmpty;

public final class ViewStack {

    private final Stack<ViewData> stack = new Stack<>();
    private final ViewGroup container;
    private final ViewStackDelegate delegate;

    public static ViewStack create(ViewGroup container, ViewStackDelegate delegate) {
        checkNotNull(container, "container == null");
        checkNotNull(delegate, "delegate == null");
        return new ViewStack(container, delegate);
    }

    private ViewStack(ViewGroup container, ViewStackDelegate delegate) {
        this.container = container;
        this.delegate = delegate;
    }

    public void saveToBundle(Bundle bundle, String tag) {
        checkNotNull(bundle, "bundle == null");
        checkStringNotEmpty(tag, "tag is empty");
        bundle.putSerializable(tag, stack);
    }

    public void rebuildFromBundle(Bundle bundle, String tag) {
        checkNotNull(bundle, "bundle == null");
        checkStringNotEmpty(tag, "tag is empty");
        @SuppressWarnings("unchecked")
        Stack<ViewData> savedStack = (Stack<ViewData>) bundle.getSerializable(tag);
        checkNotNull(savedStack, "Bundle doesn't contain any ViewStack state.");
        for (ViewData viewData : savedStack) {
            stack.push(viewData);
        }
        updateContainer();
    }

    public ViewFactory push(ViewFactory viewFactory) {
        checkNotNull(viewFactory, "viewFactory == null");
        if (stack.size() > 0 && peekView() instanceof StatefulView) {
            ((StatefulView) peekView()).saveState(stack.peek().bundle());
        }
        stack.push(new ViewData(viewFactory));
        updateContainer();
        return viewFactory;
    }

    public ViewFactory pop() {
        if (size() == 0) {
            throw new EmptyStackException();
        }
        if (size() == 1) {
            delegate.finishStack();
            return null;
        }
        ViewData popped = stack.pop();
        updateContainer();
        return popped.viewFactory();
    }

    public ViewFactory peek() {
        if (size() == 0) {
            throw new EmptyStackException();
        }
        return stack.peek().viewFactory();
    }

    public View peekView() {
        if (size() == 0) {
            throw new EmptyStackException();
        }
        return container.getChildAt(0);
    }

    public int size() {
        return stack.size();
    }

    public void clear() {
        stack.clear();
        updateContainer();
    }

    private void updateContainer() {
        container.removeAllViews();
        if (stack.size() > 0) {
            ViewData nextData = stack.peek();
            View view = nextData.viewFactory().createView(container.getContext(), container);
            if (view instanceof StatefulView && !nextData.bundle().isEmpty()) {
                ((StatefulView) view).recreateState(nextData.bundle());
            }
            container.addView(view);
        }
    }
}
