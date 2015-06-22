package nl.tudelft.watchdog.ui.wizards.projectregistration;

import com.intellij.ide.wizard.CommitStepException;
import nl.tudelft.watchdog.logic.network.NetworkUtils;
import nl.tudelft.watchdog.ui.wizards.IdEnteredEndingStepBase;
import nl.tudelft.watchdog.ui.wizards.RegistrationWizardBase;

/**
 * Possible finishing page in the wizard. If the user exists on the server, or
 * the server is not reachable, the user can exit here.
 */
public class ProjectIdEnteredEndingStep extends IdEnteredEndingStepBase {

	/** Constructor. */
	public ProjectIdEnteredEndingStep(int pageNumber, RegistrationWizardBase wizard) {
		super("project", pageNumber, wizard);
	}

	@Override
	protected String buildTransferURLforId() {
		return NetworkUtils.buildExistingProjectURL(id);
	}

	@Override
	protected String getId() {
		return  getWizard().projectWelcomeStep.getId();
	}

	@Override
	protected void setId() {
		getWizard().setProjectId(id);
	}

    @Override
    protected void commit(CommitType commitType) {
        getWizard().performFinish();
    }

    @Override
    public boolean canFinish() {
        return true;
    }
}
