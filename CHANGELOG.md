Version 1.0.0 *9-26-2015*
----------------------------

Initial release.

Version 1.0.1 *9-30-2015*
----------------------------

Throw EmptyStackException in pop() and peek() if internal stack size is 0.

Version 2.0.0 *10-2-2015*
----------------------------

- Added `peekView()` method to `ViewStack`
- Added `StatefulView` interface for saving view state
- Added `ViewGroup` container parameter to `ViewFactory.createView()` (breaking API change)

Version 3.0.0 *10-7-2015*
----------------------------

- Removed `StatefulView` interface (breaking API change)
- Now keeping all Views in container's hierarchy -- use `Visibility.GONE` to prevent overdraw. This allows for `onSaveInstanceState(Bundle)` and `onRestoreInstanceState(Bundle)` to be used for ALL Views, so long as each View has an ID.
- Added `AnimatorFactory` along with `pushWithAnimation(ViewFactory, AnimatorFactory)` and `popWithAnimation(AnimatorFactory)` for transition animations

Version 3.1.0 *10-19-2015*
----------------------------

- Added `StackChangedListener` interface

Version 4.0.0 *6-13-2016*
----------------------------

- Fixed `ClassCastException` in `rebuildFromBundle(Bundle)`
- Changed API to accept layout id ints instead of `ViewFactory`s
- Separated StackChangedListener into two methods: `onViewAdded(View)` and `onViewRemoved()`
- Made push/pop/peek return Views
