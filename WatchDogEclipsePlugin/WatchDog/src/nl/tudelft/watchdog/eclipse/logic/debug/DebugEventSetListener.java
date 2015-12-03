package nl.tudelft.watchdog.eclipse.logic.debug;

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.IDebugEventSetListener;

import nl.tudelft.watchdog.core.util.WatchDogLogger;

public class DebugEventSetListener implements IDebugEventSetListener {

	@Override
	public void handleDebugEvents(DebugEvent[] events) {
		for (DebugEvent event : events) {
			WatchDogLogger.getInstance().logInfo("Debug event: " + event);
		}
	}
}
