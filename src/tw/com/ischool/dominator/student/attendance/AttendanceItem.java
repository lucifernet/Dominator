package tw.com.ischool.dominator.student.attendance;

import tw.com.ischool.dominator.R;
import tw.com.ischool.dominator.main.item.DisplayStatus;
import tw.com.ischool.dominator.main.item.FragmentItem;

public class AttendanceItem extends FragmentItem {
	public AttendanceItem(){
		super.init(R.string.student_tab_attendance,
				R.drawable.ic_attendance, DisplayStatus.LOGINED,
				AttendanceFragment.class);
	}
}
