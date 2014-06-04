package tw.com.ischool.dominator.main.item;

import android.app.Activity;

public abstract class ActionItem extends BaseItem {

	protected PaddingTask _paddingTask;

	protected void init(int title, int icon, DisplayStatus status,
			PaddingTask task) {
		// TODO Auto-generated method stub
		super.init(title, icon, status);

		_paddingTask = task;
	}

	public void invoke(Activity activity) {
		_paddingTask.invoke(activity);
	}

	public interface PaddingTask {
		void invoke(Activity activity);
	}
}
