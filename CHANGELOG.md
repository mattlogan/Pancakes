Version 1.0.0 *9-26-2015*
----------------------------

Initial release.

Version 1.0.1 *9-30-2015*
----------------------------

Throw EmptyStackException in pop() and peek() if internal stack size is 0.

Version 1.0.1 *10-2-2015*
----------------------------

- Added `peekView()` method to `ViewStack`
- Added `StatefulView` interface for saving view state
- Added `ViewGroup` container parameter to `ViewFactory.createView()` (breaking API change)
