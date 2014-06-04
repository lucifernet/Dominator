package tw.com.ischool.dominator.main.item;

import java.util.ArrayList;


public abstract class TabsItem extends BaseItem {
	protected ArrayList<FragmentItem> _tabList;
		
	protected void init(int title, int icon, DisplayStatus status) {		
		super.init(title, icon, status);
		
		_tabList = new ArrayList<FragmentItem>();
	}
	
	protected void add(FragmentItem fragmentItem){
		_tabList.add(fragmentItem);
	}
	
	public ArrayList<FragmentItem> getItems(){
		return _tabList;
	}
}
