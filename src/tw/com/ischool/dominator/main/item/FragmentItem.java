package tw.com.ischool.dominator.main.item;

import android.app.Fragment;

public abstract class FragmentItem extends BaseItem {
	protected Class<? extends Fragment> _fragment;

	protected void init(int title, int icon, DisplayStatus status,
			Class<? extends Fragment> fragment) {
		super.init(title, icon, status);
		_fragment = fragment;
	}

	public Class<? extends Fragment> getFragmentClass() {
		return _fragment;
	}
}
