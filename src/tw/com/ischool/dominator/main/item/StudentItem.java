package tw.com.ischool.dominator.main.item;

import tw.com.ischool.dominator.R;
import tw.com.ischool.dominator.student.StudentFragment;

public class StudentItem extends FragmentItem {
	public StudentItem() {
		super.init(R.string.item_student,
				R.drawable.ic_student, DisplayStatus.LOGINED,
				StudentFragment.class);

		// super.add(new YourKnowledgeItem());
		// super.add(new YourNotesItem());
	}
}
