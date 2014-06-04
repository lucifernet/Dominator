package tw.com.ischool.dominator.main.item;


public abstract class BaseItem {
	protected DisplayStatus _status = DisplayStatus.NORMAL;
	protected int _title;
	protected int _icon;
	
	protected void init(int title, int icon, DisplayStatus status) {
		_title = title;
		_icon = icon;
		_status = status;	
	}

	public int getTitle() {
		return _title;
	}

	public int getIcon() {
		return _icon;
	}

	public DisplayStatus getStatus() {
		return _status;
	}
}
