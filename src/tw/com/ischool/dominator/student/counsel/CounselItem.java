package tw.com.ischool.dominator.student.counsel;

import tw.com.ischool.dominator.R;
import tw.com.ischool.dominator.main.item.DisplayStatus;
import tw.com.ischool.dominator.main.item.FragmentItem;

public class CounselItem extends FragmentItem {
	
	public CounselItem(){
		super.init(R.string.student_tab_counsel,
				R.drawable.ic_counsel, DisplayStatus.LOGINED,
				CounselFragment.class);
	}
}
