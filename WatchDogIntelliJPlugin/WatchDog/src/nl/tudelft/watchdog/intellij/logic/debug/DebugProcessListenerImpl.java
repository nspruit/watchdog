package nl.tudelft.watchdog.intellij.logic.debug;

import com.intellij.debugger.engine.DebugProcess;
import com.intellij.debugger.engine.DebugProcessListener;
import com.intellij.debugger.engine.SuspendContext;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.RemoteConnection;
import com.intellij.execution.configurations.RunProfileState;

/**
 * Created by niels on 3-12-15.
 * Removing abstract makes errors occur
 */
public abstract class DebugProcessListenerImpl implements DebugProcessListener {
    @Override
    public void connectorIsReady() {

    }

    @Override
    public void paused(SuspendContext suspendContext) {

    }

    @Override
    public void resumed(SuspendContext suspendContext) {

    }

    @Override
    public void processDetached(DebugProcess debugProcess, boolean b) {

    }

    @Override
    public void processAttached(DebugProcess debugProcess) {

    }

    @Override
    public void attachException(RunProfileState runProfileState, ExecutionException e, RemoteConnection remoteConnection) {

    }


    public void threadStarted(DebugProcess debugProcess, com.sun.jdi.ThreadReference threadReference) {

    }


    public void threadStopped(DebugProcess debugProcess, com.sun.jdi.ThreadReference threadReference) {

    }
}
