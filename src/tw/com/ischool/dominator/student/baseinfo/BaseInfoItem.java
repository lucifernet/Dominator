package tw.com.ischool.dominator.student.baseinfo;

import tw.com.ischool.dominator.R;
import tw.com.ischool.dominator.main.item.DisplayStatus;
import tw.com.ischool.dominator.main.item.FragmentItem;

public class BaseInfoItem extends FragmentItem {
	public BaseInfoItem(){
		super.init(R.string.student_tab_base_info,
				R.drawable.ic_baseinfo, DisplayStatus.LOGINED,
				BaseInfoFragment.class);
	}
}
