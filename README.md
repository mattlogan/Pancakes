Pancakes
----

Like FragmentManager but for Views

Download
----

```
compile 'me.mattlogan.pancakes:pancakes:2.0.0'
```

Usage
-----

Create a `ViewStack` instance with a `ViewGroup` container and a `ViewStackDelegate`:

```java
ViewStack viewStack = ViewStack.create((ViewGroup) findViewById(R.id.container), this);
```

Create a `ViewFactory` for each `View`:

```java
public static class RedView.Factory implements ViewFactory {
    @Override
    public View createView(Context context, ViewGroup container) {
        return LayoutInflater.from(context).inflate(R.layout.view_red, container, false);
    }
}
```

Add a `View` to your container by pushing a `ViewFactory`:

```java
viewStack.push(new RedView.Factory());
```

Call `pop()` to go back one `View`:

```java
viewStack.pop();
```

Additionally, you can call `peek()` to get the `ViewFactory` that's on top of the stack or `peekView()` to get the `View` that's currently in the supplied container `ViewGroup`.

Persist `ViewFactory` instances, in order, across configuration changes:

```java
@Override
public void onSaveInstanceState(Bundle outState) {
    viewStack.saveToBundle(outState, STACK_TAG);
    super.onSaveInstanceState(outState);
}
```

Rebuild the stack from a `Bundle`:
```java
if (savedInstanceState != null) {
    viewStack.rebuildFromBundle(savedInstanceState, STACK_TAG);
}
```

You can also persist the state of a `View` by having your `View` implement `StatefulView`:

```java
@Override
public void saveState(Bundle bundle) {
    bundle.putInt(SELECTED_RADIO_BUTTON_ID, radioGroup.getCheckedRadioButtonId());
}

@Override
public void recreateState(Bundle bundle) {
    radioGroup.check(bundle.getInt(SELECTED_RADIO_BUTTON_ID));
}
```

Finally, implement `ViewStackDelegate.finishStack()` to take appropriate action when the stack is finished:
```java
@Override
public void finishStack() {
    finish();
}
```

See the [sample app](https://github.com/mattlogan/Pancakes/tree/master/app) for an example implementation.

**Be careful: because `ViewFactory` instances are persisted across configuration changes,
you should not keep references in a `ViewFactory` to any objects that should be garbage collected
on a configuration change. Keep each `ViewFactory` as simple as possible.**

Tests
----

[Yep](https://github.com/mattlogan/Pancakes/blob/master/library/src/test/java/me/mattlogan/library/ViewStackTest.java)

License
-----

```
The MIT License (MIT)

Copyright (c) 2015 Matthew Logan

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```
