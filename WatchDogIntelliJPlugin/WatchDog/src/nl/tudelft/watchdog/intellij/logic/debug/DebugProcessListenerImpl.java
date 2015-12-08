package nl.tudelft.watchdog.intellij.logic.debug;

import com.intellij.debugger.engine.DebugProcess;
import com.intellij.debugger.engine.DebugProcessListener;
import com.intellij.debugger.engine.SuspendContext;
import com.intellij.debugger.engine.SuspendContextImpl;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.RemoteConnection;
import com.intellij.execution.configurations.RunProfileState;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.connect.Connector;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.EventSet;
import com.sun.jdi.request.EventRequestManager;
import com.sun.jdi.request.StepRequest;
import nl.tudelft.watchdog.core.util.WatchDogLogger;

/**
 * Created by niels on 3-12-15.
 * Removing abstract makes errors occur
 */
public class DebugProcessListenerImpl implements DebugProcessListener {

    @Override
    public void connectorIsReady() {
        WatchDogLogger.getInstance().logInfo("Connector ready");
    }

    @Override
    public void paused(SuspendContext suspendContext) {
        String message = "Paused: ";
        if (suspendContext instanceof SuspendContextImpl){
            SuspendContextImpl context = (SuspendContextImpl) suspendContext;
            EventSet events = context.getEventSet();
            for (Event event: events){
                message += "\n\t"+event.getClass().toString();
            }
        }
        WatchDogLogger.getInstance().logInfo(message);
    }

    @Override
    public void resumed(SuspendContext suspendContext) {
        String message = "Resumed: ";
//        if (suspendContext instanceof SuspendContextImpl){
//            SuspendContextImpl context = (SuspendContextImpl) suspendContext;
//            EventSet events = context.getEventSet();
//            for (Event event: events){
//                message += "\n\t"+event.getClass().toString();
//            }
//        }
        WatchDogLogger.getInstance().logInfo(message);
    }

    @Override
    public void processDetached(DebugProcess debugProcess, boolean b) {
        WatchDogLogger.getInstance().logInfo("Process detached");
    }

    @Override
    public void processAttached(DebugProcess debugProcess) {
        WatchDogLogger.getInstance().logInfo("Process attached");
    }

    @Override
    public void attachException(RunProfileState runProfileState, ExecutionException e, RemoteConnection remoteConnection) {
        WatchDogLogger.getInstance().logInfo("Attach exception");
    }

    @Override
    public void threadStarted(DebugProcess debugProcess, ThreadReference threadReference) {
        WatchDogLogger.getInstance().logInfo("Thread started");
    }

    @Override
    public void threadStopped(DebugProcess debugProcess, ThreadReference threadReference) {
        WatchDogLogger.getInstance().logInfo("Thread stopped");
    }
}
