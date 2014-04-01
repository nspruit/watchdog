package nl.tudelft.watchdog.logic.interval.activityCheckers;

import nl.tudelft.watchdog.logic.eclipseuireader.events.editor.FocusStartEditorEvent;
import nl.tudelft.watchdog.logic.interval.IntervalManager;

import org.eclipse.swt.custom.CaretEvent;
import org.eclipse.swt.custom.CaretListener;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.texteditor.ITextEditor;

/** A task for when the user is reading. */
public class ReadingCheckerTask extends CheckerTimerTask {

	/** The text. */
	final StyledText styledText;

	/** Whether the user is still actively reading. */
	private boolean isActive;

	/** Cursor listener. */
	private CaretListener caretListener;

	/** Paint redraw listener (e.g. move scrollbar). */
	private PaintListener paintListener;

	/** Callback. */
	private OnInactiveCallback callback;

	/** The editor. */
	private ITextEditor editor;

	/** Workbench part. */
	IWorkbenchPart workbenchPart;

	/** Constructor. */
	public ReadingCheckerTask(IWorkbenchPart part, OnInactiveCallback callback) {
		isActive = true;
		this.callback = callback;
		this.workbenchPart = part;
		this.editor = (ITextEditor) part;
		styledText = (StyledText) editor.getAdapter(Control.class);

		createListeners();
	}

	/** Creates the listeners for reading. */
	private void createListeners() {
		Display.getDefault().asyncExec(new Runnable() {

			@Override
			public void run() {
				// creates a listener for when the user moves the caret (cursor)
				caretListener = new CaretListener() {
					@Override
					public void caretMoved(CaretEvent event) {
						// cursor place changed
						isActive = true;
						// listen just once to not get millions of events fired
						styledText.removeCaretListener(this);
					}
				};

				// creates a listener for redraws of the view, e.g. when
				// scrolled
				paintListener = new PaintListener() {
					@Override
					public void paintControl(PaintEvent e) {
						isActive = true;
						// listen just once to not get millions of events fired
						styledText.removePaintListener(this);
					}
				};
			}
		});
	}

	@Override
	public void run() {
		if (isActive) {
			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					// adds the created listeners to the editor, thus executing
					// them
					styledText.addCaretListener(caretListener);
					styledText.addPaintListener(paintListener);
				}
			});

			isActive = false;
		} else {
			cancel();
			removeListeners();
			createListenerForReactivation();
			callback.onInactive();
		}
	}

	/** Removes all listeners from the caret. */
	private void removeListeners() {
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				styledText.removeCaretListener(caretListener);
				styledText.removePaintListener(paintListener);
			}
		});
	}

	/** Creates the listener for reactivation of this checker. */
	public void createListenerForReactivation() {
		if (!styledText.isDisposed()) {
			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					if (styledText.isDisposed()) {
						return;
					}

					styledText.addCaretListener(new CaretListener() {

						@Override
						public void caretMoved(CaretEvent event) {
							// cursor place changed
							IntervalManager
									.getInstance()
									.getEditorObserveable()
									.notifyObservers(
											new FocusStartEditorEvent(
													workbenchPart));
							// listen just once to not get millions
							// of events fired
							styledText.removeCaretListener(this);
						}
					});

					styledText.addPaintListener(new PaintListener() { // for
								// redraws
								// of
								// the
								// view,
								// e.g.
								// when
								// scrolled
								@Override
								public void paintControl(PaintEvent e) {
									IntervalManager
											.getInstance()
											.getEditorObserveable()
											.notifyObservers(
													new FocusStartEditorEvent(
															workbenchPart));
									// listen just once to not get millions
									// of events fired
									styledText.removePaintListener(this);
								}
							});
				}
			});
		}
	}
}
