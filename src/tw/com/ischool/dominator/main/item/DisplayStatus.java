package tw.com.ischool.dominator.main.item;

import java.io.Serializable;

public class DisplayStatus implements Serializable {
	// NORMAL(2), SEPARATION(3), LOGINED(5), UNLOGIN(7);

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final DisplayStatus NORMAL;	
	public static final DisplayStatus LOGINED;
	public static final DisplayStatus GUEST;

	static {
		NORMAL = new DisplayStatus(2);
		// SEPARATION = new DisplayStatus(3);
		LOGINED = new DisplayStatus(5);
		GUEST = new DisplayStatus(7);
	}

	private int _value;

	private DisplayStatus(int value) {
		_value = value;
	}

	private int value() {
		return _value;
	}

	public boolean isMember(DisplayStatus status) {
		if (_value % status.value() == 0)
			return true;
		return false;
	}

	public static DisplayStatus combine(DisplayStatus... displayStatus) {
		int value = 1;
		for (DisplayStatus status : displayStatus) {
			value = value * status.value();
		}
		return new DisplayStatus(value);
	}
}