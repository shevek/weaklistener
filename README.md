Introduction
------------

If a GUI wants to listen to changes in a domain object, it must
register a listener. If the GUI might be closed before the domain
object is destroyed, it must either reliably unregister itself, or
risk a memory leak of one GUI.

Or it could use a WeakListener.

Usage
-----

	class MyModel { public void addChangeListener(ChangeListener l); }
	MyModel model = ...;

	class MyListener implements ChangeListener { ... }
	MyListener listener = ...;	// Keep this reference.

	model.addChangeListener(WeakListeners.change(model, listener));

Now, when MyListener is garbage collected, it will automatically
unregistre from MyModel.

Mistakes
--------

	model.addChangeListener(WeakListeners.change(model, new MyListener()));

There is no strong reference to MyListener, so it will garbage-collect
immediately.

Credits
-------

This library was inspired by but not derived from the similarly named
code in the OpenIDE Utilities (NetBeans) library.
