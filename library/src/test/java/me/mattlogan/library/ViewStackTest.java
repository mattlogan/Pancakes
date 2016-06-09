package me.mattlogan.library;

import android.animation.Animator;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.EmptyStackException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
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

public class ViewStackTest {

    @Mock ViewStackDelegate delegate;
    @Mock ViewGroup container;

    @Mock StackChangedListener stackChangedListener1;
    @Mock StackChangedListener stackChangedListener2;

    @Mock PancakesViewInflater layoutInflater;

    ViewStack viewStack;

    @LayoutRes int BOTTOM_LAYOUT_RES = 8675309;
    @LayoutRes int TOP_LAYOUT_RES = 42;

    @Before
    public void setup() {
        initMocks(this);
        viewStack = ViewStack.create(container, delegate, layoutInflater);
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
        viewStack.push(BOTTOM_LAYOUT_RES);

        Bundle bundle = mock(Bundle.class);
        viewStack.saveToBundle(bundle, "tag");
        verify(bundle).putParcelable(eq("tag"), isA(ParcelableIntStack.class));
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

        ParcelableIntStack stack = new ParcelableIntStack();

        Context context = mock(Context.class);
        when(container.getContext()).thenReturn(context);

        View bottomView = mock(View.class);
        when(layoutInflater.inflateView(BOTTOM_LAYOUT_RES, container)).thenReturn(bottomView);

        View topView = mock(View.class);
        when(layoutInflater.inflateView(TOP_LAYOUT_RES, container)).thenReturn(topView);

        stack.push(BOTTOM_LAYOUT_RES);
        stack.push(TOP_LAYOUT_RES);

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
        when(bundle.getParcelable("tag")).thenReturn(stack);

        viewStack.rebuildFromBundle(bundle, "tag");

        assertEquals(2, viewStack.size());
        verify(container).addView(bottomView);
        verify(container).addView(topView);
        verify(bottomView).setVisibility(View.GONE);
        verifyStackChangedListenersNotified(1);
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
        when(layoutInflater.inflateView(BOTTOM_LAYOUT_RES, container)).thenReturn(view);

        when(container.getChildCount()).thenReturn(1);

        viewStack.push(BOTTOM_LAYOUT_RES);

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

        when(layoutInflater.inflateView(BOTTOM_LAYOUT_RES, container)).thenReturn(bottomView);

        viewStack.push(BOTTOM_LAYOUT_RES);

        // Top
        View topView = mock(View.class);

        when(layoutInflater.inflateView(TOP_LAYOUT_RES, container)).thenReturn(topView);

        when(container.getChildCount()).thenReturn(2);
        when(container.getChildAt(0)).thenReturn(bottomView);

        viewStack.push(TOP_LAYOUT_RES);

        assertEquals(2, viewStack.size());
        verify(container).addView(bottomView);
        verify(container).addView(topView);
        verify(bottomView).setVisibility(View.GONE);
        verifyStackChangedListenersNotified(2);
    }

    @Test
    public void pushWithAnimationWithNullAnimatorFactory() {
        try {
            viewStack.pushWithAnimation(BOTTOM_LAYOUT_RES, null);
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
        ViewTreeObserver observer = mock(ViewTreeObserver.class);

        when(layoutInflater.inflateView(BOTTOM_LAYOUT_RES, container)).thenReturn(view);
        when(view.getViewTreeObserver()).thenReturn(observer);

        AnimatorFactory animatorFactory = mock(AnimatorFactory.class);
        Animator animator = mock(Animator.class);
        when(animatorFactory.createAnimator(view)).thenReturn(animator);

        int result = viewStack.pushWithAnimation(BOTTOM_LAYOUT_RES, animatorFactory);
        assertEquals(result, BOTTOM_LAYOUT_RES);

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
        ViewTreeObserver bottomObserver = mock(ViewTreeObserver.class);

        when(layoutInflater.inflateView(BOTTOM_LAYOUT_RES, container)).thenReturn(bottomView);
        when(bottomView.getViewTreeObserver()).thenReturn(bottomObserver);

        viewStack.push(BOTTOM_LAYOUT_RES);

        // Top
        View topView = mock(View.class);
        ViewTreeObserver topObserver = mock(ViewTreeObserver.class);

        when(layoutInflater.inflateView(TOP_LAYOUT_RES, container)).thenReturn(topView);
        when(topView.getViewTreeObserver()).thenReturn(topObserver);

        AnimatorFactory animatorFactory = mock(AnimatorFactory.class);
        Animator animator = mock(Animator.class);
        when(animatorFactory.createAnimator(topView)).thenReturn(animator);

        int result = viewStack.pushWithAnimation(TOP_LAYOUT_RES, animatorFactory);
        assertEquals(TOP_LAYOUT_RES, result);

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
        viewStack.push(BOTTOM_LAYOUT_RES);

        int result = viewStack.pop();

        assertEquals(ViewStack.SINGLE_VIEW, result);
        verify(delegate).finishStack();
    }

    @Test
    public void popWithSizeMoreThanOne() {
        Context context = mock(Context.class);
        when(container.getContext()).thenReturn(context);

        View bottomView = mock(View.class);

        when(layoutInflater.inflateView(BOTTOM_LAYOUT_RES, container)).thenReturn(bottomView);

        viewStack.push(BOTTOM_LAYOUT_RES);

        View topView = mock(View.class);

        when(layoutInflater.inflateView(TOP_LAYOUT_RES, container)).thenReturn(topView);

        viewStack.push(TOP_LAYOUT_RES);

        when(container.getChildCount()).thenReturn(2);
        when(container.getChildAt(0)).thenReturn(bottomView);
        when(container.getChildAt(1)).thenReturn(topView);

        int result = viewStack.pop();
        assertSame(TOP_LAYOUT_RES, result);

        verify(bottomView).setVisibility(View.VISIBLE);
        verify(container).removeView(topView);

        verifyStackChangedListenersNotified(3);
    }

    @Test
    public void popWithAnimation() {
        Context context = mock(Context.class);
        when(container.getContext()).thenReturn(context);

        View bottomView = mock(View.class);

        when(layoutInflater.inflateView(BOTTOM_LAYOUT_RES, container)).thenReturn(bottomView);

        viewStack.push(BOTTOM_LAYOUT_RES);

        View topView = mock(View.class);

        when(layoutInflater.inflateView(TOP_LAYOUT_RES, container)).thenReturn(topView);

        viewStack.push(TOP_LAYOUT_RES);

        when(container.getChildCount()).thenReturn(2);
        when(container.getChildAt(0)).thenReturn(bottomView);
        when(container.getChildAt(1)).thenReturn(topView);

        AnimatorFactory animatorFactory = mock(AnimatorFactory.class);
        Animator animator = mock(Animator.class);
        when(animatorFactory.createAnimator(topView)).thenReturn(animator);

        int result = viewStack.popWithAnimation(animatorFactory);
        assertSame(TOP_LAYOUT_RES, result);

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
        viewStack.push(BOTTOM_LAYOUT_RES);

        viewStack.push(TOP_LAYOUT_RES);

        int result = viewStack.peek();

        assertSame(TOP_LAYOUT_RES, result);
    }

    @Test
    public void size() {
        viewStack.push(BOTTOM_LAYOUT_RES);
        viewStack.push(TOP_LAYOUT_RES);

        assertEquals(2, viewStack.size());
    }

    @Test
    public void clear() {
        viewStack.push(BOTTOM_LAYOUT_RES);
        viewStack.push(TOP_LAYOUT_RES);

        reset(container);

        viewStack.clear();

        verify(container).removeAllViews();
        verifyNoMoreInteractions(container);

        verifyStackChangedListenersNotified(3);
    }

    private void verifyStackChangedListenersNotified(int times) {
        verify(stackChangedListener1, times(times)).onStackChanged();
        verify(stackChangedListener2, times(times)).onStackChanged();
    }
}
