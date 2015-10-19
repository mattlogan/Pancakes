package me.mattlogan.library;

import android.animation.Animator;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.util.EmptyStackException;
import java.util.Stack;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

@RunWith(MockitoJUnitRunner.class)
public class ViewStackTest {

    @Mock ViewStackDelegate delegate;
    @Mock ViewGroup container;

    @Mock StackChangedListener stackChangedListener1;
    @Mock StackChangedListener stackChangedListener2;

    ViewStack viewStack;

    @Before
    public void setup() {
        initMocks(this);
        viewStack = ViewStack.create(container, delegate);
        viewStack.addStackChangedListener(stackChangedListener1);
        viewStack.addStackChangedListener(stackChangedListener2);
    }

    @Test
    public void createWithNullContainer() {
        try {
            ViewStack.create(null, delegate);
            fail();
        } catch (NullPointerException e) {
            assertEquals("container == null", e.getMessage());
        }
    }

    @Test
    public void createWithNullDelegate() {
        try {
            ViewStack.create(container, null);
            fail();
        } catch (NullPointerException e) {
            assertEquals("delegate == null", e.getMessage());
        }
    }

    @Test
    public void saveToBundleWithNullBundle() {
        try {
            viewStack.saveToBundle(null, "tag");
            fail();
        } catch (NullPointerException e) {
            assertEquals("bundle == null", e.getMessage());
        }
    }

    @Test
    public void saveToBundleWithNullTag() {
        Bundle bundle = mock(Bundle.class);
        try {
            viewStack.saveToBundle(bundle, null);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("tag is empty", e.getMessage());
        }
    }

    @Test
    public void saveToBundleWithEmptyTag() {
        Bundle bundle = mock(Bundle.class);
        try {
            viewStack.saveToBundle(bundle, "");
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("tag is empty", e.getMessage());
        }
    }

    @Test
    public void saveToBundle() {
        viewStack.push(newMockViewFactory());

        Bundle bundle = mock(Bundle.class);
        viewStack.saveToBundle(bundle, "tag");
        verify(bundle).putSerializable(eq("tag"), isA(Stack.class));
    }

    @Test
    public void rebuildFromBundleWithNullBundle() {
        try {
            viewStack.rebuildFromBundle(null, "tag");
            fail();
        } catch (NullPointerException e) {
            assertEquals("bundle == null", e.getMessage());
        }
    }

    @Test
    public void rebuildFromBundleWithNullTag() {
        Bundle bundle = mock(Bundle.class);
        try {
            viewStack.rebuildFromBundle(bundle, null);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("tag is empty", e.getMessage());
        }
    }

    @Test
    public void rebuildFromBundleWithEmptyTag() {
        Bundle bundle = mock(Bundle.class);
        try {
            viewStack.rebuildFromBundle(bundle, "");
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("tag is empty", e.getMessage());
        }
    }

    @Test
    public void rebuildFromBundleWithNullStack() {
        Bundle bundle = mock(Bundle.class);
        try {
            viewStack.rebuildFromBundle(bundle, "tag");
            fail();
        } catch (NullPointerException e) {
            assertEquals("Bundle doesn't contain any ViewStack state.", e.getMessage());
        }
    }

    @Test
    public void rebuildFromBundle() {
        StackChangedListener stackChangedListener = mock(StackChangedListener.class);
        viewStack.addStackChangedListener(stackChangedListener);

        Stack<ViewFactory> stack = new Stack<>();

        Context context = mock(Context.class);
        when(container.getContext()).thenReturn(context);

        ViewFactory bottom = mock(ViewFactory.class);
        View bottomView = mock(View.class);
        when(bottom.createView(context, container)).thenReturn(bottomView);
        when(bottomView.getViewTreeObserver()).thenReturn(mock(ViewTreeObserver.class));
        stack.push(bottom);

        ViewFactory top = mock(ViewFactory.class);
        View topView = mock(View.class);
        when(top.createView(context, container)).thenReturn(topView);
        when(topView.getViewTreeObserver()).thenReturn(mock(ViewTreeObserver.class));
        stack.push(top);

        // First time return 1, second time return 2
        when(container.getChildCount()).thenAnswer(new Answer() {
            int count = 0;

            public Object answer(InvocationOnMock invocation) {
                if (count == 0) {
                    count++;
                    return 1;
                }
                return 2;
            }
        });

        when(container.getChildAt(0)).thenReturn(bottomView);

        Bundle bundle = mock(Bundle.class);
        when(bundle.getSerializable("tag")).thenReturn(stack);

        viewStack.rebuildFromBundle(bundle, "tag");

        assertEquals(2, viewStack.size());
        verify(container).addView(bottomView);
        verify(container).addView(topView);
        verify(bottomView).setVisibility(View.GONE);
        verifyStackChangedListenersNotified(1);
    }

    @Test
    public void pushWithNullViewFactory() {
        try {
            viewStack.push(null);
            fail();
        } catch (NullPointerException e) {
            assertEquals("viewFactory == null", e.getMessage());
        }
    }

    @Test
    public void pushFirstTime() {
        StackChangedListener stackChangedListener = mock(StackChangedListener.class);
        viewStack.addStackChangedListener(stackChangedListener);

        // Applies to both bottom and top
        Context context = mock(Context.class);
        when(container.getContext()).thenReturn(context);

        // Bottom
        View view = mock(View.class);
        ViewFactory viewFactory = mock(ViewFactory.class);

        when(viewFactory.createView(context, container)).thenReturn(view);

        when(container.getChildCount()).thenReturn(1);

        viewStack.push(viewFactory);

        assertEquals(1, viewStack.size());
        verify(container).addView(view);
        verifyStackChangedListenersNotified(1);
    }

    @Test
    public void pushSecondTime() {
        // Applies to both bottom and top
        Context context = mock(Context.class);
        when(container.getContext()).thenReturn(context);

        // Bottom
        View bottomView = mock(View.class);
        ViewFactory bottomViewFactory = mock(ViewFactory.class);

        when(bottomViewFactory.createView(context, container)).thenReturn(bottomView);

        viewStack.push(bottomViewFactory);

        // Top
        View topView = mock(View.class);
        ViewFactory topViewFactory = mock(ViewFactory.class);

        when(topViewFactory.createView(context, container)).thenReturn(topView);

        when(container.getChildCount()).thenReturn(2);
        when(container.getChildAt(0)).thenReturn(bottomView);

        viewStack.push(topViewFactory);

        assertEquals(2, viewStack.size());
        verify(container).addView(bottomView);
        verify(container).addView(topView);
        verify(bottomView).setVisibility(View.GONE);
        verifyStackChangedListenersNotified(2);
    }

    @Test
    public void pushWithAnimationWithNullViewFactory() {
        try {
            viewStack.pushWithAnimation(null, mock(AnimatorFactory.class));
            fail();
        } catch (NullPointerException e) {
            assertEquals("viewFactory == null", e.getMessage());
        }
    }

    @Test
    public void pushWithAnimationWithNullAnimatorFactory() {
        try {
            viewStack.pushWithAnimation(mock(ViewFactory.class), null);
            fail();
        } catch (NullPointerException e) {
            assertEquals("animatorFactory == null", e.getMessage());
        }
    }

    @Test
    public void pushWithAnimationFirstTime() {
        // Applies to both bottom and top
        Context context = mock(Context.class);
        when(container.getContext()).thenReturn(context);

        // Top
        View view = mock(View.class);
        ViewFactory viewFactory = mock(ViewFactory.class);
        ViewTreeObserver observer = mock(ViewTreeObserver.class);

        when(viewFactory.createView(context, container)).thenReturn(view);
        when(view.getViewTreeObserver()).thenReturn(observer);

        AnimatorFactory animatorFactory = mock(AnimatorFactory.class);
        Animator animator = mock(Animator.class);
        when(animatorFactory.createAnimator(view)).thenReturn(animator);

        ViewFactory result = viewStack.pushWithAnimation(viewFactory, animatorFactory);
        assertSame(viewFactory, result);

        assertEquals(1, viewStack.size());
        verify(container).addView(view);

        verifyStackChangedListenersNotified(1);

        ArgumentCaptor<FirstLayoutListener> firstLayoutListenerArgument =
                ArgumentCaptor.forClass(FirstLayoutListener.class);

        verify(observer).addOnGlobalLayoutListener(firstLayoutListenerArgument.capture());

        firstLayoutListenerArgument.getValue().onFirstLayout(view);

        ArgumentCaptor<Animator.AnimatorListener> animatorListenerArgument =
                ArgumentCaptor.forClass(Animator.AnimatorListener.class);

        verify(animator).addListener(animatorListenerArgument.capture());
        verify(animator).start();

        when(container.getChildCount()).thenReturn(1);
        when(container.getChildAt(0)).thenReturn(view);

        animatorListenerArgument.getValue().onAnimationEnd(animator);

        verify(container).getChildCount();
        verify(view, times(0)).setVisibility(View.GONE);
    }

    @Test
    public void pushWithAnimationSecondTime() {
        // Applies to both bottom and top
        Context context = mock(Context.class);
        when(container.getContext()).thenReturn(context);

        // Bottom
        View bottomView = mock(View.class);
        ViewFactory bottomViewFactory = mock(ViewFactory.class);
        ViewTreeObserver bottomObserver = mock(ViewTreeObserver.class);

        when(bottomViewFactory.createView(context, container)).thenReturn(bottomView);
        when(bottomView.getViewTreeObserver()).thenReturn(bottomObserver);

        viewStack.push(bottomViewFactory);

        // Top
        View topView = mock(View.class);
        ViewFactory topViewFactory = mock(ViewFactory.class);
        ViewTreeObserver topObserver = mock(ViewTreeObserver.class);

        when(topViewFactory.createView(context, container)).thenReturn(topView);
        when(topView.getViewTreeObserver()).thenReturn(topObserver);

        AnimatorFactory animatorFactory = mock(AnimatorFactory.class);
        Animator animator = mock(Animator.class);
        when(animatorFactory.createAnimator(topView)).thenReturn(animator);

        ViewFactory result = viewStack.pushWithAnimation(topViewFactory, animatorFactory);
        assertSame(topViewFactory, result);

        assertEquals(2, viewStack.size());
        verify(container).addView(bottomView);
        verify(container).addView(topView);

        verifyStackChangedListenersNotified(2);

        ArgumentCaptor<FirstLayoutListener> firstLayoutListenerArgument =
                ArgumentCaptor.forClass(FirstLayoutListener.class);

        verify(topObserver).addOnGlobalLayoutListener(firstLayoutListenerArgument.capture());

        firstLayoutListenerArgument.getValue().onFirstLayout(topView);

        ArgumentCaptor<Animator.AnimatorListener> animatorListenerArgument =
                ArgumentCaptor.forClass(Animator.AnimatorListener.class);

        verify(animator).addListener(animatorListenerArgument.capture());
        verify(animator).start();

        when(container.getChildCount()).thenReturn(2);
        when(container.getChildAt(0)).thenReturn(bottomView);

        animatorListenerArgument.getValue().onAnimationEnd(animator);

        verify(bottomView).setVisibility(View.GONE);
    }

    @Test
    public void popWithSizeZero() {
        try {
            viewStack.pop();
            fail();
        } catch (EmptyStackException e) {
            assertNotNull(e);
        }
    }

    @Test
    public void popWithSizeOne() {
        viewStack.push(newMockViewFactory());

        ViewFactory result = viewStack.pop();

        assertNull(result);
        verify(delegate).finishStack();
    }

    @Test
    public void popWithSizeMoreThanOne() {
        Context context = mock(Context.class);
        when(container.getContext()).thenReturn(context);

        ViewFactory bottomViewFactory = mock(ViewFactory.class);
        View bottomView = mock(View.class);

        when(bottomViewFactory.createView(context, container)).thenReturn(bottomView);

        viewStack.push(bottomViewFactory);

        ViewFactory topViewFactory = mock(ViewFactory.class);
        View topView = mock(View.class);

        when(topViewFactory.createView(context, container)).thenReturn(topView);

        viewStack.push(topViewFactory);

        when(container.getChildCount()).thenReturn(2);
        when(container.getChildAt(0)).thenReturn(bottomView);
        when(container.getChildAt(1)).thenReturn(topView);

        ViewFactory result = viewStack.pop();
        assertSame(topViewFactory, result);

        verify(bottomView).setVisibility(View.VISIBLE);
        verify(container).removeView(topView);

        verifyStackChangedListenersNotified(3);
    }

    @Test
    public void popWithAnimation() {
        Context context = mock(Context.class);
        when(container.getContext()).thenReturn(context);

        ViewFactory bottomViewFactory = mock(ViewFactory.class);
        View bottomView = mock(View.class);

        when(bottomViewFactory.createView(context, container)).thenReturn(bottomView);

        viewStack.push(bottomViewFactory);

        ViewFactory topViewFactory = mock(ViewFactory.class);
        View topView = mock(View.class);

        when(topViewFactory.createView(context, container)).thenReturn(topView);

        viewStack.push(topViewFactory);

        when(container.getChildCount()).thenReturn(2);
        when(container.getChildAt(0)).thenReturn(bottomView);
        when(container.getChildAt(1)).thenReturn(topView);

        AnimatorFactory animatorFactory = mock(AnimatorFactory.class);
        Animator animator = mock(Animator.class);
        when(animatorFactory.createAnimator(topView)).thenReturn(animator);

        ViewFactory result = viewStack.popWithAnimation(animatorFactory);
        assertSame(topViewFactory, result);

        verify(bottomView).setVisibility(View.VISIBLE);

        ArgumentCaptor<Animator.AnimatorListener> animatorListenerArgument =
                ArgumentCaptor.forClass(Animator.AnimatorListener.class);

        verify(animator).addListener(animatorListenerArgument.capture());
        verify(animator).start();

        animatorListenerArgument.getValue().onAnimationEnd(animator);

        verify(container).removeView(topView);

        verifyStackChangedListenersNotified(3);
    }

    @Test
    public void peekWithSizeZero() {
        try {
            viewStack.peek();
            fail();
        } catch (EmptyStackException e) {
            assertNotNull(e);
        }
    }

    @Test
    public void peek() {
        viewStack.push(newMockViewFactory());

        ViewFactory top = newMockViewFactory();
        viewStack.push(top);

        ViewFactory result = viewStack.peek();

        assertSame(top, result);
    }

    @Test
    public void size() {
        viewStack.push(newMockViewFactory());
        viewStack.push(newMockViewFactory());

        assertEquals(2, viewStack.size());
    }

    @Test
    public void clear() {
        viewStack.push(newMockViewFactory());
        viewStack.push(newMockViewFactory());

        reset(container);

        viewStack.clear();

        verify(container).removeAllViews();
        verifyNoMoreInteractions(container);

        verifyStackChangedListenersNotified(3);
    }

    private ViewFactory newMockViewFactory() {
        Context context = mock(Context.class);
        View view = mock(View.class);
        ViewFactory viewFactory = mock(ViewFactory.class);
        ViewTreeObserver observer = mock(ViewTreeObserver.class);

        when(container.getContext()).thenReturn(context);
        when(viewFactory.createView(context, container)).thenReturn(view);
        when(view.getViewTreeObserver()).thenReturn(observer);

        return viewFactory;
    }

    private void verifyStackChangedListenersNotified(int times) {
        verify(stackChangedListener1, times(times)).onStackChanged();
        verify(stackChangedListener2, times(times)).onStackChanged();
    }
}
