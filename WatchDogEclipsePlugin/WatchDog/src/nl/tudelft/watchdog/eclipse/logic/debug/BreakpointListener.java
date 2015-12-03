package nl.tudelft.watchdog.eclipse.logic.debug;

import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.debug.core.IBreakpointListener;
import org.eclipse.debug.core.model.IBreakpoint;

import nl.tudelft.watchdog.core.util.WatchDogLogger;

public class BreakpointListener implements IBreakpointListener {

	@Override
	public void breakpointAdded(IBreakpoint breakpoint) {
		WatchDogLogger.getInstance().logInfo("Breakpoint added: " + breakpoint);
	}

	@Override
	public void breakpointRemoved(IBreakpoint breakpoint, IMarkerDelta delta) {
		WatchDogLogger.getInstance()
				.logInfo("Breakpoint removed: " + breakpoint);
	}

	@Override
	public void breakpointChanged(IBreakpoint breakpoint, IMarkerDelta delta) {
		WatchDogLogger.getInstance()
				.logInfo("Breakpoint changed: " + breakpoint);
	}

}
