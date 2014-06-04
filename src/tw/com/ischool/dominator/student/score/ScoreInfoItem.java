package tw.com.ischool.dominator.student.score;

import tw.com.ischool.dominator.R;
import tw.com.ischool.dominator.main.item.DisplayStatus;
import tw.com.ischool.dominator.main.item.FragmentItem;

public class ScoreInfoItem extends FragmentItem {
	public ScoreInfoItem(){
		super.init(R.string.student_tab_score,
				R.drawable.ic_score, DisplayStatus.LOGINED,
				ScoreFragment.class);
	}
}
