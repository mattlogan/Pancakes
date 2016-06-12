package me.mattlogan.library;

import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.test.InstrumentationRegistry;
import android.view.View;
import android.view.ViewGroup;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import android.support.test.runner.AndroidJUnit4;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.TwoLineListItem;

import java.util.EmptyStackException;

@RunWith(AndroidJUnit4.class)
public class ViewStackTest {

    ViewStackDelegate delegate;
    ViewGroup container;

    StackChangedListener stackChangedListener1;
    StackChangedListener stackChangedListener2;

    ViewStack viewStack;

    @LayoutRes private static int BOTTOM_LAYOUT_RES = android.R.layout.simple_list_item_1;
    @LayoutRes private static int TOP_LAYOUT_RES = android.R.layout.simple_list_item_2;

    @Before
    public void setup() {
        delegate = mock(ViewStackDelegate.class);
        container = new FrameLayout(InstrumentationRegistry.getContext());
        stackChangedListener1 = mock(StackChangedListener.class);
        stackChangedListener2 = mock(StackChangedListener.class);

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
        try {
            viewStack.saveToBundle(new Bundle(), null);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("tag is empty", e.getMessage());
        }
    }

    @Test
    public void saveToBundleWithEmptyTag() {
        try {
            viewStack.saveToBundle(new Bundle(), "");
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("tag is empty", e.getMessage());
        }
    }

    @Test
    public void saveToBundle() {
        viewStack.push(BOTTOM_LAYOUT_RES);

        Bundle bundle = new Bundle();
        viewStack.saveToBundle(bundle, "tag");
        assertNotNull(bundle.getParcelable("tag"));
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
        Bundle bundle = new Bundle();
        try {
            viewStack.rebuildFromBundle(bundle, null);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("tag is empty", e.getMessage());
        }
    }

    @Test
    public void rebuildFromBundleWithEmptyTag() {
        Bundle bundle = new Bundle();
        try {
            viewStack.rebuildFromBundle(bundle, "");
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("tag is empty", e.getMessage());
        }
    }

    @Test
    public void rebuildFromBundleWithNullStack() {
        Bundle bundle = new Bundle();
        try {
            viewStack.rebuildFromBundle(bundle, "tag");
            fail();
        } catch (NullPointerException e) {
            assertEquals("Bundle doesn't contain any ViewStack state.", e.getMessage());
        }
    }

    @Test
    public void rebuildFromBundle() {
        ParcelableIntStack intStack = new ParcelableIntStack();
        intStack.push(BOTTOM_LAYOUT_RES);
        intStack.push(TOP_LAYOUT_RES);

        Bundle bundle = new Bundle();
        bundle.putParcelable("tag", intStack);

        viewStack.rebuildFromBundle(bundle, "tag");

        assertNumberOfViews(2);
        assertBottomViewIsCorrectType();
        assertTopViewIsCorrectType();
        assertEquals(View.GONE, container.getChildAt(0).getVisibility());
        assertEquals(View.VISIBLE, container.getChildAt(1).getVisibility());
        verifyStackChangedListenersNotified(1);
    }

    @Test
    public void pushReturnsCorrectView() {
        View view = viewStack.push(BOTTOM_LAYOUT_RES);
        assertTrue(view instanceof TextView);
    }

    @Test
    public void pushFirstTime() {
        viewStack.push(BOTTOM_LAYOUT_RES);

        assertNumberOfViews(1);
        assertBottomViewIsCorrectType();
        verifyStackChangedListenersNotified(1);
    }

    @Test
    public void pushSecondTime() {
        viewStack.push(BOTTOM_LAYOUT_RES);
        viewStack.push(TOP_LAYOUT_RES);

        assertNumberOfViews(2);
        assertBottomViewIsCorrectType();
        assertTopViewIsCorrectType();
        assertEquals(View.GONE, container.getChildAt(0).getVisibility());
        assertEquals(View.VISIBLE, container.getChildAt(1).getVisibility());
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
    public void pushWithAnimationReturnsCorrectView() {
        AnimatorFactory animatorFactory = AnimatorFactory.NONE;

        View view = viewStack.pushWithAnimation(BOTTOM_LAYOUT_RES, animatorFactory);
        assertTrue(view instanceof TextView);
    }

    @Test
    public void pushWithAnimationFirstTime() {
        AnimatorFactory animatorFactory = AnimatorFactory.NONE;

        viewStack.pushWithAnimation(BOTTOM_LAYOUT_RES, animatorFactory);
        viewStack.pushAnimatorListener.onAnimationEnd(null);

        assertNumberOfViews(1);
        assertBottomViewIsCorrectType();

        verifyStackChangedListenersNotified(1);
    }

    @Test
    public void pushWithAnimationSecondTime() {
        viewStack.push(BOTTOM_LAYOUT_RES);

        viewStack.pushWithAnimation(TOP_LAYOUT_RES, AnimatorFactory.NONE);
        viewStack.pushAnimatorListener.onAnimationEnd(null);

        assertNumberOfViews(2);
        assertBottomViewIsCorrectType();
        assertTopViewIsCorrectType();
        assertEquals(View.GONE, container.getChildAt(0).getVisibility());
        assertEquals(View.VISIBLE, container.getChildAt(1).getVisibility());

        verifyStackChangedListenersNotified(2);
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

        viewStack.pop();

        verify(delegate).finishStack();
    }

    @Test
    public void popWithSizeMoreThanOne() {
        viewStack.push(BOTTOM_LAYOUT_RES);
        viewStack.push(TOP_LAYOUT_RES);

        viewStack.pop();

        assertNumberOfViews(1);
        assertBottomViewIsCorrectType();
        verifyStackChangedListenersNotified(3);
    }

    @Test
    public void popWithAnimation() {
        viewStack.push(BOTTOM_LAYOUT_RES);
        viewStack.push(TOP_LAYOUT_RES);

        viewStack.popWithAnimation(AnimatorFactory.NONE);
        viewStack.popAnimationListener.onAnimationEnd(null);
        assertNumberOfViews(1);
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

        assertEquals(TOP_LAYOUT_RES, result);
    }

    @Test
    public void size() {
        viewStack.push(BOTTOM_LAYOUT_RES);
        viewStack.push(TOP_LAYOUT_RES);

        assertNumberOfViews(2);
    }

    @Test
    public void clear() {
        viewStack.push(BOTTOM_LAYOUT_RES);
        viewStack.push(TOP_LAYOUT_RES);

        viewStack.clear();

        assertNumberOfViews(0);

        verifyStackChangedListenersNotified(3);
    }

    private void verifyStackChangedListenersNotified(int times) {
        verify(stackChangedListener1, times(times)).onStackChanged();
        verify(stackChangedListener2, times(times)).onStackChanged();
    }

    private void assertBottomViewIsCorrectType() {
        assertTrue(container.getChildAt(0) instanceof TextView);
    }

    private void assertTopViewIsCorrectType() {
        assertTrue(container.getChildAt(1) instanceof TwoLineListItem);
    }

    private void assertNumberOfViews(int numberOfViews) {
        assertEquals(numberOfViews, container.getChildCount());
        assertEquals(numberOfViews, viewStack.size());
    }
}
