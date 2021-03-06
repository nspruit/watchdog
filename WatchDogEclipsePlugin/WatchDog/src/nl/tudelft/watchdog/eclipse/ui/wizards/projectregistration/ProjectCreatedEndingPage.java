package nl.tudelft.watchdog.eclipse.ui.wizards.projectregistration;

import org.apache.commons.lang.WordUtils;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.swt.widgets.Composite;

import nl.tudelft.watchdog.core.logic.network.JsonTransferer;
import nl.tudelft.watchdog.core.logic.network.ServerCommunicationException;
import nl.tudelft.watchdog.core.ui.wizards.Project;
import nl.tudelft.watchdog.core.util.WatchDogGlobals;
import nl.tudelft.watchdog.core.util.WatchDogLogger;
import nl.tudelft.watchdog.eclipse.ui.preferences.Preferences;
import nl.tudelft.watchdog.eclipse.ui.util.UIUtils;
import nl.tudelft.watchdog.eclipse.ui.wizards.FinishableWizardPage;
import nl.tudelft.watchdog.eclipse.ui.wizards.RegistrationEndingPageBase;
import nl.tudelft.watchdog.eclipse.ui.wizards.RegistrationWizardBase;
import nl.tudelft.watchdog.eclipse.ui.wizards.userregistration.UserProjectRegistrationWizard;
import nl.tudelft.watchdog.eclipse.ui.wizards.userregistration.UserRegistrationPage;

/**
 * Possible finishing page in the wizard. If the project exists on the server,
 * or the server is not reachable, the user can exit here.
 */
public class ProjectCreatedEndingPage extends RegistrationEndingPageBase {

	/** The top-level composite. */
	private Composite topComposite;

	private Composite dynamicComposite;

	private String concludingMessage;

	/** Constructor. */
	public ProjectCreatedEndingPage(int pageNumber) {
		super("Project-ID created.", pageNumber);
		concludingMessage = "You can change these and other WatchDog settings in the Eclipse preferences."
				+ ProjectIdEnteredEndingPage.ENCOURAGING_END_MESSAGE;
	}

	@Override
	protected void makeRegistration() {
		Project project = new Project(Preferences.getInstance().getUserId());

		ProjectSliderPage sliderPage;
		ProjectRegistrationPage projectPage = null;
		if (getPreviousPage() instanceof ProjectRegistrationPage) {
			projectPage = (ProjectRegistrationPage) getPreviousPage();
		} else if (getPreviousPage() instanceof ProjectSliderPage) {
			sliderPage = (ProjectSliderPage) getPreviousPage();
			projectPage = (ProjectRegistrationPage) getPreviousPage()
					.getPreviousPage();

			project.productionPercentage = sliderPage.percentageProductionSlider
					.getSelection();
			project.useJunitOnlyForUnitTesting = sliderPage
					.usesJunitForUnitTestingOnly();
			project.followTestDrivenDesign = sliderPage.usesTestDrivenDesing();
		}

		if (projectPage == null) {
			messageTitle = "How did you get here?";
			messageBody = "We couldn't figure out which wizard page\n";
			messageBody += "you came from. Please restart the registration.";
			WatchDogLogger.getInstance().logSevere("Unknown previous page");
			return;
		}

		// initialize from projectPage
		project.belongToASingleSoftware = !projectPage.noSingleProjectButton
				.getSelection();
		project.name = projectPage.projectNameInput.getText();
		project.website = projectPage.projectWebsite.getText();
		project.usesContinuousIntegration = projectPage
				.usesContinuousIntegration();
		project.usesJunit = projectPage.usesJunit();
		project.usesOtherTestingFrameworks = projectPage
				.usesOtherTestingFrameworks();
		project.usesOtherTestingForms = projectPage.usesOtherTestingForms();

		windowTitle = "Registration Summary";

		try {
			id = new JsonTransferer().registerNewProject(project);
		} catch (ServerCommunicationException exception) {
			successfulRegistration = false;
			messageTitle = "Problem creating new project!";
			messageBody = WordUtils.wrap(exception.getMessage(), 100, null,
					true);
			messageBody += "\nAre you connected to the internet, and is port 80 open?";
			messageBody += "\nPlease contact us via www.testroots.org. \nWe'll troubleshoot the issue!";
			WatchDogLogger.getInstance().logSevere(exception);
			return;
		}

		successfulRegistration = true;

		((RegistrationWizardBase) getWizard()).setProjectId(id);

		messageTitle = "New project registered!";
		messageBody = "Your new project id is registered: ";
	}

	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if (visible) {
			if (dynamicComposite != null) {
				dynamicComposite.dispose();
			}
			makeRegistration();
			createPageContent();
			dynamicComposite.layout(true);
			topComposite.layout(true);
		}
	}

	private void createPageContent() {
		setTitle(windowTitle);
		if (isThisProjectWizard()) {
			dynamicComposite = UIUtils.createGridedComposite(topComposite, 1);
			dynamicComposite.setLayoutData(UIUtils.createFullGridUsageData());
			createProjectRegistrationSummary();
			return;
		} else {
			UserProjectRegistrationWizard wizard = (UserProjectRegistrationWizard) getWizard();
			UserRegistrationPage userRegistrationPage = wizard.userRegistrationPage;
			dynamicComposite = UIUtils.createGridedComposite(topComposite, 1);
			dynamicComposite.setLayoutData(UIUtils.createFullGridUsageData());
			if (wizard.userWelcomePage.getRegisterNewId()) {
				createDebugSurveyInfo();
				userRegistrationPage
						.createUserRegistrationSummary(dynamicComposite);
			}
			createProjectRegistrationSummary();
			return;
		}
	}

	/**
	 * Shows the label and link to ask the new user to fill out the survey on
	 * debugging.
	 */
	private void createDebugSurveyInfo() {
		UIUtils.createBoldLabel(WatchDogGlobals.DEBUG_SURVEY_TEXT,
				dynamicComposite);
		UIUtils.createStartDebugSurveyLink(dynamicComposite);
	}

	private void createProjectRegistrationSummary() {
		if (successfulRegistration) {
			FinishableWizardPage.createSuccessMessage(dynamicComposite,
					messageTitle, messageBody, id);
			UIUtils.createLabel(concludingMessage, dynamicComposite);
		} else {
			FinishableWizardPage.createFailureMessage(dynamicComposite,
					messageTitle, messageBody);
			setPageComplete(false);
		}
	}

	private boolean isThisProjectWizard() {
		IWizard wizard = getWizard();
		return wizard instanceof ProjectRegistrationWizard;
	}

	@Override
	public void createControl(Composite parent) {
		topComposite = UIUtils.createGridedComposite(parent, 1);
		UIUtils.createLabel("", topComposite);
		setControl(topComposite);
	}
}
