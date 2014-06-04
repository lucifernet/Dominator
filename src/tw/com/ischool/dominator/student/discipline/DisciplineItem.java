package tw.com.ischool.dominator.student.discipline;

import tw.com.ischool.dominator.R;
import tw.com.ischool.dominator.main.item.DisplayStatus;
import tw.com.ischool.dominator.main.item.FragmentItem;

public class DisciplineItem extends FragmentItem {
	public DisciplineItem(){
		super.init(R.string.student_tab_moral,
				R.drawable.ic_discipline, DisplayStatus.LOGINED,
				DisciplineFragment.class);
	}
}
