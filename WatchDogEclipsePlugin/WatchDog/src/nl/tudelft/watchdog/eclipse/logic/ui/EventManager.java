package nl.tudelft.watchdog.eclipse.logic.ui;

import org.eclipse.ui.texteditor.ITextEditor;

import nl.tudelft.watchdog.core.logic.document.Document;
import nl.tudelft.watchdog.core.logic.document.EditorWrapperBase;
import nl.tudelft.watchdog.core.logic.interval.IDEIntervalManagerBase;
import nl.tudelft.watchdog.core.logic.ui.EventManagerBase;
import nl.tudelft.watchdog.core.logic.ui.events.EditorEvent;
import nl.tudelft.watchdog.core.logic.ui.events.WatchDogEvent;
import nl.tudelft.watchdog.eclipse.logic.document.DocumentCreator;
import nl.tudelft.watchdog.eclipse.logic.document.EditorWrapper;
import nl.tudelft.watchdog.eclipse.logic.interval.intervaltypes.JUnitInterval;

/**
 * Manager for {@link EditorEvent}s. Links such events to actions in the
 * IntervalManager, i.e. manages the creation and deletion of intervals based on
 * the incoming events. This class therefore contains the logic of when and how
 * new intervals are created, and how WatchDog reacts to incoming events
 * generated by its listeners.
 */
public class EventManager extends EventManagerBase {

	/** Constructor. */
	public EventManager(final IDEIntervalManagerBase intervalManager,
			int userActivityTimeout) {
		super(intervalManager, userActivityTimeout);
	}

	@Override
	protected void addJUnitInterval(WatchDogEvent event) {
		JUnitInterval junitInterval = (JUnitInterval) event.getSource();
		intervalManager.addInterval(junitInterval);
	}

	@Override
	protected EditorWrapperBase createEditorWrapper(Object editor) {
		return new EditorWrapper((ITextEditor) editor);
	}

	@Override
	protected Document createDocument(Object editor) {
		return DocumentCreator.createDocument((ITextEditor) editor);
	}

}