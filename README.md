Pancakes
----

Like FragmentManager but for Views


Usage
-----

Create a `ViewStack` instance with a `ViewGroup` container and a `ViewStackDelegate`: 

```java
ViewStack viewStack = ViewStack.create((ViewGroup) findViewById(R.id.container), this);
```

For each `View`, create a corresponding implementation of the `ViewFactory` interface:

```java
public final class RedViewFactory implements ViewFactory {
    @Override
    public View createView(Context context) {
        return new RedView(context);
    }
}
```

Add a `View` to your container by pushing a `ViewFactory`:

```java
viewStack.push(new RedView.Factory());
```

Or call `pop()` to go back one `View`:

```java
viewStack.pop();
```

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

Implement `ViewStackDelegate` to take appropriate action when the stack is finished:
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

Download
----

Coming soon

License
-----

Coming soon
