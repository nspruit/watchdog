package nl.tudelft.watchdog.interval.active;

import java.util.Timer;

import nl.tudelft.watchdog.interval.ActivityType;
import nl.tudelft.watchdog.interval.activityCheckers.OnInactiveCallBack;
import nl.tudelft.watchdog.interval.activityCheckers.TypingCheckerTask;

import org.eclipse.ui.IWorkbenchPart;

/**
 * An interval for when the user is currently typing, connected to the
 * {@link ActivityType#Typing} activity.
 */
public class ActiveTypingInterval extends ActiveInterval {

	private TypingCheckerTask task;

	/** Constructor. */
	public ActiveTypingInterval(IWorkbenchPart part) {
		super(part);

		checkForChangeTimer = new Timer();
	}

	@Override
	public void addTimeoutListener(long timeout,
			OnInactiveCallBack callbackWhenFinished) {
		task = new TypingCheckerTask(this.getPart(), callbackWhenFinished);
		checkForChangeTimer.schedule(new TypingCheckerTask(this.getEditor(),
				callbackWhenFinished), timeout, timeout);
	}

	@Override
	public ActivityType getActivityType() {
		return ActivityType.Typing;
	}

	@Override
	public void listenForReactivation() {
		// TODO (MMB) task would be null if addTimeoutListener had not been
		// called before listenForReactivation ?
		assert (task != null);
		task.listenForReactivation();
	}

}