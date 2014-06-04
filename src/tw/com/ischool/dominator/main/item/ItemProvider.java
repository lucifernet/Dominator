package tw.com.ischool.dominator.main.item;

import java.util.ArrayList;
import java.util.List;

public class ItemProvider {
	private static ArrayList<BaseItem> sItems;

	static {
		sItems = new ArrayList<BaseItem>();
		sItems.add(new StudentItem());
		sItems.add(new LogoutItem());
	}

	public static BaseItem getItem(int position) {
		return sItems.get(position);

	}

	public static List<BaseItem> getItems(DisplayStatus... condition) {
		ArrayList<BaseItem> items = new ArrayList<BaseItem>();

		for (BaseItem item : sItems) {
			boolean match = true;

			if (condition != null) {
				for (DisplayStatus status : condition) {
					if (item.getStatus() == DisplayStatus.NORMAL)
						continue;

					if (!item.getStatus().isMember(status)) {
						match = false;
						continue;
					}
				}
			}
			if (match)
				items.add(item);
		}

		return items;
	}

	public static int findIndex(List<BaseItem> itemList, Class<?> class1) {

		for (int i = 0; i < itemList.size(); i++) {
			BaseItem item = itemList.get(i);
			
			if (class1.isAssignableFrom(item.getClass()))
				return i;
		}

		return -1;
	}
}
