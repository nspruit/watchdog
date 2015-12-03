package nl.tudelft.watchdog.intellij;

import com.intellij.debugger.DebuggerManager;
import com.intellij.debugger.DebuggerManagerEx;
import com.intellij.debugger.engine.DebugProcess;
import com.intellij.debugger.engine.SuspendContextImpl;
import com.intellij.debugger.engine.evaluation.EvaluationListener;
import com.intellij.debugger.impl.*;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.icons.AllIcons;
import com.intellij.ide.actions.ViewToolWindowButtonsAction;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.xdebugger.impl.DebuggerSupport;
import com.intellij.xdebugger.impl.breakpoints.ui.BreakpointPanelProvider;
import com.sun.jdi.event.EventSet;
import nl.tudelft.watchdog.core.logic.network.JsonTransferer;
import nl.tudelft.watchdog.core.logic.network.ServerCommunicationException;
import nl.tudelft.watchdog.core.ui.wizards.User;
import nl.tudelft.watchdog.intellij.logic.InitializationManager;
import nl.tudelft.watchdog.core.logic.ui.events.WatchDogEvent;
import nl.tudelft.watchdog.intellij.logic.debug.DebugProcessListenerImpl;
import nl.tudelft.watchdog.intellij.ui.preferences.Preferences;
import nl.tudelft.watchdog.core.ui.preferences.ProjectPreferenceSetting;
import nl.tudelft.watchdog.intellij.ui.wizards.projectregistration.ProjectRegistrationWizard;
import nl.tudelft.watchdog.intellij.ui.wizards.userregistration.UserProjectRegistrationWizard;
import nl.tudelft.watchdog.core.util.WatchDogGlobals;
import nl.tudelft.watchdog.core.util.WatchDogLogger;
import nl.tudelft.watchdog.intellij.util.WatchDogUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.io.File;
import java.io.OutputStream;

public class WatchDogStartUp implements ProjectComponent {

    /**
     * Current Project.
     */
    private Project project;

    /**
     * The warning displayed when WatchDog is not registered. Note: JLabel requires html tags for a new line (and other formatting).
     */
    public static final String WATCHDOG_UNREGISTERED_WARNING = "<html>Warning: You can only use WatchDog when you register it.<br><br>Last chance: Register now, anonymously, without filling the survey?";

    /**
     * Whether the user has cancelled the user project registration wizard.
     */
    private boolean userProjectRegistrationCancelled = false;

    private WindowFocusListener windowFocusListener;


    public WatchDogStartUp(Project project) {
        this.project = project;
    }

    public void initComponent() {
        WatchDogUtils.setActiveProject(project);
        WatchDogGlobals.setLogDirectory(PluginManager.getPlugin(PluginId.findId("nl.tudelft.watchdog")).getPath().toString() + File.separator + "logs" + File.separator);
        System.out.println("logdir: "+WatchDogGlobals.getLogDirectory());
        WatchDogGlobals.setPreferences(Preferences.getInstance());
        WatchDogGlobals.hostIDE = WatchDogGlobals.IDE.INTELLIJ;
    }

    public void disposeComponent() {
        // intentionally left empty
    }

    @NotNull
    public String getComponentName() {
        return "WatchDog";
    }

    public void projectOpened() {
        windowFocusListener = new WindowAdapter() {
            @Override
            public void windowGainedFocus(WindowEvent e) {
                WatchDogUtils.setActiveProject(project);
            }
        };
        WindowManager.getInstance().getFrame(project).addWindowFocusListener(windowFocusListener);

        checkWhetherToDisplayUserProjectRegistrationWizard();

        if (WatchDogUtils.isEmpty(WatchDogGlobals.getPreferences().getUserId())
                || userProjectRegistrationCancelled) {
            return;
        }

        checkIsProjectAlreadyRegistered();
        checkWhetherToDisplayProjectWizard();
        checkWhetherToStartWatchDog();

        //extra
        //DebuggerManager.getInstance(project).addDebugProcessListener(null);
        DebuggerManagerEx.getInstanceEx(project).addDebuggerManagerListener(new DebuggerManagerListener() {
            @Override
            public void sessionCreated(DebuggerSession debuggerSession) {
                WatchDogLogger.getInstance().logInfo("Debug session created: "+debuggerSession);
//                debuggerSession.getXDebugSession().getDebugProcess().getBreakpointHandlers()[0].
            }

            @Override
            public void sessionAttached(DebuggerSession debuggerSession) {
                WatchDogLogger.getInstance().logInfo("Debug session attached: "+debuggerSession);
            }

            @Override
            public void sessionDetached(DebuggerSession debuggerSession) {
                WatchDogLogger.getInstance().logInfo("Debug session detached: "+debuggerSession);
            }

            @Override
            public void sessionRemoved(DebuggerSession debuggerSession) {
                WatchDogLogger.getInstance().logInfo("Debug session removed: "+debuggerSession);
            }
        });

        DebuggerManagerEx.getInstanceEx(project).getContextManager().addListener(new DebuggerContextListener() {
            @Override
            public void changeEvent(@NotNull DebuggerContextImpl debuggerContext, DebuggerSession.Event event) {
                WatchDogLogger.getInstance().logInfo("Change event: "+event);
//                debuggerContext.getDebugProcess().addEvaluationListener(new EvaluationListener() {
//                    @Override
//                    public void evaluationStarted(SuspendContextImpl suspendContext) {
//                       // EventSet set = suspendContext.getEventSet(); Why doesn't this worl
//                    }
//
//                    @Override
//                    public void evaluationFinished(SuspendContextImpl suspendContext) {
//
//                    }
//                });
            }
        });

        DebuggerSupport[] supports=com.intellij.debugger.ui.JavaDebuggerSupport.getDebuggerSupports();
        for (DebuggerSupport support: supports){
            support.getBreakpointPanelProvider().addListener(new BreakpointPanelProvider.BreakpointsListener() {
                @Override
                public void breakpointsChanged() {
                    WatchDogLogger.getInstance().logInfo("Breakpoints changed, current breakpoints:\n\t"+DebuggerManagerEx.getInstanceEx(project).getBreakpointManager().getBreakpoints());
                }
            },project,null);
        }

//        DebuggerManagerEx.getInstanceEx(project).getBreakpointManager().
    }

    public void projectClosed() {
        if (!WatchDogUtils.isWatchDogActive(project)) {
            return;
        }

        InitializationManager intervalInitializationManager = InitializationManager.getInstance(project.getName());
        intervalInitializationManager.getEventManager().update(new WatchDogEvent(this, WatchDogEvent.EventType.END_IDE));
        intervalInitializationManager.getIntervalManager().closeAllIntervals();
        intervalInitializationManager.getTransferManager().sendIntervalsImmediately();
        intervalInitializationManager.shutdown(project.getName());

        JFrame frame = WindowManager.getInstance().getFrame(project);
        if (frame != null) {
            frame.removeWindowFocusListener(windowFocusListener);
        }
    }

    /**
     * Checks whether there is a registered WatchDog user
     */
    private void checkWhetherToDisplayUserProjectRegistrationWizard() {
        Preferences preferences = Preferences.getInstance();
        ProjectPreferenceSetting projectSetting = preferences.getOrCreateProjectSetting(project.getName());
        if (!WatchDogUtils.isEmpty(WatchDogGlobals.getPreferences().getUserId())
                || (projectSetting.startupQuestionAsked && !projectSetting.enableWatchdog)) {
            return;
        }

        UserProjectRegistrationWizard wizard = new UserProjectRegistrationWizard("User and Project Registration", project);
        wizard.show();
        if (wizard.getExitCode() == DialogWrapper.CANCEL_EXIT_CODE) {
            if (Messages.YES == Messages.showYesNoDialog(WATCHDOG_UNREGISTERED_WARNING, "WatchDog is not registered!", Messages.getQuestionIcon())) {
                makeSilentRegistration();
            } else {
                userProjectRegistrationCancelled = true;
                preferences.registerProjectUse(project.getName(), false);
            }
        }
    }

    private void makeSilentRegistration() {
        String userId = "";
        Preferences preferences = Preferences.getInstance();
        if (preferences.getUserId() == null || preferences.getUserId().isEmpty()) {

            User user = new User();
            user.programmingExperience = "NA";
            try {
                userId = new JsonTransferer().registerNewUser(user);
            } catch (ServerCommunicationException exception) {
                WatchDogLogger.getInstance().logSevere(exception);
            }

            if (WatchDogUtils.isEmptyOrHasOnlyWhitespaces(userId)) {
                return;
            }

            preferences.setUserId(userId);
            preferences.registerProjectId(WatchDogUtils.getProjectName(), "");
        }

        registerAnonymousProject(preferences.getUserId());
    }

    private void registerAnonymousProject(String userId) {
        String projectId = "";
        Preferences preferences = Preferences.getInstance();
        try {
            projectId = new JsonTransferer().registerNewProject(new nl.tudelft.watchdog.core.ui.wizards.Project(userId));
        } catch (ServerCommunicationException exception) {
            WatchDogLogger.getInstance().logSevere(exception);
        }

        if (WatchDogUtils.isEmptyOrHasOnlyWhitespaces(projectId)) {
            return;
        }

        preferences.registerProjectId(WatchDogUtils.getProjectName(), projectId);
        preferences.registerProjectUse(WatchDogUtils.getProjectName(), true);
    }

    private void checkIsProjectAlreadyRegistered() {
        if (!WatchDogGlobals.getPreferences().isProjectRegistered(project.getName())) {
            boolean useWatchDogInThisWorkspace = Messages.YES ==
                    Messages.showYesNoDialog("Should WatchDog be active in this workspace?", "WatchDog Workspace Registration", AllIcons.General.QuestionDialog);
            WatchDogLogger.getInstance().logInfo("Registering workspace...");
            WatchDogGlobals.getPreferences().registerProjectUse(project.getName(), useWatchDogInThisWorkspace);
        }
    }

    private void checkWhetherToDisplayProjectWizard() {
        ProjectPreferenceSetting setting = WatchDogGlobals.getPreferences()
                .getOrCreateProjectSetting(project.getName());
        if (setting.enableWatchdog && WatchDogUtils.isEmpty(setting.projectId)) {
            ProjectRegistrationWizard wizard =  new ProjectRegistrationWizard("Project Registration", project);
            wizard.show();
            if (wizard.getExitCode() == DialogWrapper.CANCEL_EXIT_CODE) {
                registerAnonymousProject(WatchDogGlobals.getPreferences().getUserId());
            }
        }
    }

    private void checkWhetherToStartWatchDog() {
        ProjectPreferenceSetting setting = WatchDogGlobals.getPreferences()
                .getOrCreateProjectSetting(project.getName());
        if (setting.enableWatchdog) {
            WatchDogLogger.getInstance().logInfo("Starting WatchDog ...");
            InitializationManager.getInstance(project.getName());
            WatchDogUtils.setWatchDogActiveForProject(project);
            new ViewToolWindowButtonsAction().setSelected(null, true);
        }
    }

}
