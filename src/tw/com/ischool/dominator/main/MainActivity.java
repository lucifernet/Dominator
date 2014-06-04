package tw.com.ischool.dominator.main;

import ischool.dsa.utility.http.Cancelable;

import java.util.ArrayList;
import java.util.List;

import tw.com.ischool.dominator.NoDdsFragment;
import tw.com.ischool.dominator.R;
import tw.com.ischool.dominator.main.item.ActionItem;
import tw.com.ischool.dominator.main.item.BaseItem;
import tw.com.ischool.dominator.main.item.DisplayStatus;
import tw.com.ischool.dominator.main.item.FragmentItem;
import tw.com.ischool.dominator.main.item.ItemProvider;
import tw.com.ischool.dominator.main.item.StudentItem;
import tw.com.ischool.dominator.main.item.TabsItem;
import tw.com.ischool.dominator.model.APInfoHandler;
import tw.com.ischool.dominator.model.DSS;
import tw.com.ischool.dominator.model.OnCompletedListener;
import tw.com.ischool.dominator.util.ActivityHelper;
import tw.com.ischool.dominator.util.StringUtil;
import tw.com.ischool.oauth2signin.APInfo;
import tw.com.ischool.oauth2signin.AccessToken;
import tw.com.ischool.oauth2signin.SignInActivity;
import tw.com.ischool.oauth2signin.User;
import android.app.ActionBar;
import android.app.ActionBar.OnNavigationListener;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	public static final String TAG = "tw.com.ischool.dominator";
	public static final String PARAM_EXITS = "exits";
	public static final String CONTRACT = "ischool.dss";
	public static final String SELECTED_AP_PREF = "SelectedAPPref";
	public static final String SELECTED_AP = "SelectedAP";

	public static final int REQUEST_CODE_LOGIN = 1;
	// private static final String CURRENT_TAB_INDEX = "current_index";
	private static final String CURRENT_ITEM_INDEX = "current_item_index";
	private static final String CURRENT_DISPLAY_STATUS = "current_display_status";
	// private static final String CURRENT_LOGIN_INFO = "current_login_info";

	public static final int MODE_DISCOVER = 0;
	public static final int MODE_EDITOR_CHOICE = 1;
	public static final int MODE_YOUR_CHANNEL = 2;

	// private static APInfo mCurrentDSNS;
	private static int INIT_FLAG = -1;
	private static FragmentItem mCurrentFragmentItem;

	private ActionBarDrawerToggle mDrawerToggle;
	private DrawerLayout mDrawerLayout;
	private ListView mDrawerListView;
	private ItemAdapter mItemAdapter;
	private Fragment mCurrentFragment;
	// private ViewPager mViewPager;
	// private TabsAdapter mTabsAdapter;
	private FrameLayout mContainer;
	private List<BaseItem> mItemList;
	// private int mCurrentTabIndex = -1;
	private int mCurrentItemIndex = -1;
	private DisplayStatus mCurrentDisplayStatus = DisplayStatus.LOGINED;
	private APInfoHandler mAPInfoHandler;

	// private JSONObject mLoginInfo;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mItemList = new ArrayList<BaseItem>();

		if (savedInstanceState != null) {
			// mCurrentTabIndex = savedInstanceState.getInt(CURRENT_TAB_INDEX);
			mCurrentItemIndex = savedInstanceState.getInt(CURRENT_ITEM_INDEX);
			mCurrentDisplayStatus = (DisplayStatus) savedInstanceState
					.getSerializable(CURRENT_DISPLAY_STATUS);
			mItemList = ItemProvider.getItems(mCurrentDisplayStatus);

			// String loginInfo =
			// savedInstanceState.getString(CURRENT_LOGIN_INFO);
			// mLoginInfo = JSONUtil.parseToJSONObject(loginInfo);
		}

		Intent intent = getIntent();
		if (intent != null && intent.getBooleanExtra(PARAM_EXITS, false)) {
			finish();
			return;
		}

		getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);

		// mViewPager = (ViewPager) findViewById(R.id.pager);
		// mTabsAdapter = new TabsAdapter(this, mViewPager);
		mContainer = (FrameLayout) this.findViewById(R.id.container);
		mDrawerListView = (ListView) this.findViewById(R.id.left_drawer);

		mItemAdapter = new ItemAdapter(this, R.layout.drawer_list_item);
		mDrawerListView.setAdapter(mItemAdapter);
		mDrawerListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long arg3) {
				onItemSelected(position);
			}
		});

		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
				R.drawable.ic_drawer, R.string.drawer_open,
				R.string.drawer_close) {

			/**
			 * Called when a drawer has settled in a completely closed state.
			 */
			public void onDrawerClosed(View view) {
				getActionBar().setTitle(getTitle());
				// invalidateOptionsMenu();
			}

			/** Called when a drawer has settled in a completely open state. */
			public void onDrawerOpened(View drawerView) {
				getActionBar().setTitle(getTitle());
				// invalidateOptionsMenu();
			}
		};
		mDrawerLayout.setDrawerListener(mDrawerToggle);
		getActionBar().setDisplayHomeAsUpEnabled(true);

		ActivityHelper helper = new ActivityHelper(this);
		helper.valid();

		if (savedInstanceState == null)
			this.autoLogin();
		else {
			if (mCurrentItemIndex == -1)
				mCurrentItemIndex = 0;
//			onItemSelected(mCurrentItemIndex);
		}

		INIT_FLAG = 1;

		renderItems(DisplayStatus.LOGINED);
	}

	//
	// /* Called whenever we call invalidateOptionsMenu() */
	// @Override
	// public boolean onPrepareOptionsMenu(Menu menu) {
	// // If the nav drawer is open, hide action items related to the content
	// // view
	// // boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerListView);
	// // menu.findItem(R.id.action_websearch).setVisible(!drawerOpen);
	//
	// // MenuItem reloadItem = menu.findItem(R.id.reload);
	// // if (reloadItem != null)
	// // reloadItem.setVisible(false);
	//
	// MenuItem searchItem = menu.findItem(R.id.search);
	// if (searchItem != null)
	// searchItem.setVisible(false);
	//
	// if (mCurrentItemIndex > 0) {
	//
	// BaseItem item = mItemList.get(mCurrentItemIndex);
	//
	// if (item instanceof FragmentItem) {
	// Class<?> fragmentClass = ((FragmentItem) item)
	// .getFragmentClass();
	// if (IReloadable.class.isAssignableFrom(fragmentClass)) {
	// reloadItem.setVisible(true);
	// }
	//
	// if (ISearchable.class.isAssignableFrom(fragmentClass)) {
	// searchItem.setVisible(true);
	// }
	// } else if (item instanceof TabsItem) {
	// FragmentItem fragmentItem = ((TabsItem) item).getItems().get(
	// mCurrentTabIndex);
	// Class<?> fragmentClass = fragmentItem.getFragmentClass();
	// if (IReloadable.class.isAssignableFrom(fragmentClass)) {
	// reloadItem.setVisible(true);
	// }
	//
	// if (ISearchable.class.isAssignableFrom(fragmentClass)) {
	// searchItem.setVisible(true);
	// }
	// }
	// }
	// return super.onPrepareOptionsMenu(menu);
	// }

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		// 如果回傳 true, 表示這是 icon 被按的事件，而且已經被處理了
		if (mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}
		// Handle your other action bar items...

		// if (item.getItemId() == R.id.reload) {
		// // TODO 這裡要取出目前在 pager 裡的 fragment
		// Fragment fragment = null;
		//
		// if (mViewPager.getVisibility() == View.VISIBLE) {
		// fragment = mTabsAdapter.getRegisteredFragment(mViewPager
		// .getCurrentItem());
		// } else {
		// fragment = this.getFragmentManager().findFragmentById(
		// R.id.container);
		// }
		//
		// if (fragment != null && fragment instanceof IReloadable) {
		//
		// LayoutInflater inflater = (LayoutInflater) this
		// .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		// ImageView iv = (ImageView) inflater.inflate(
		// R.layout.refresh_action_view, null);
		//
		// Animation rotation = AnimationUtils.loadAnimation(this,
		// R.anim.rotate);
		// rotation.setRepeatCount(Animation.INFINITE);
		// iv.startAnimation(rotation);
		// item.setActionView(iv);
		//
		// IReloadable reloadable = (IReloadable) fragment;
		// reloadable
		// .setOnReloadCompletedListener(new OnReloadCompletedListener() {
		//
		// @Override
		// public void onCompleted() {
		// if (item != null
		// && item.getActionView() != null) {
		// item.getActionView().clearAnimation();
		// item.setActionView(null);
		// }
		// }
		// });
		// reloadable.reload();
		// }
		// }

		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		if (mDrawerToggle != null)
			mDrawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);

		mDrawerToggle.onConfigurationChanged(newConfig);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);

		// Associate searchable configuration with the SearchView
		SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
		MenuItem menuItem = menu.findItem(R.id.search);

		if (menuItem != null) {
			SearchView searchView = (SearchView) menuItem.getActionView();
			int id = searchView.getContext().getResources()
					.getIdentifier("android:id/search_src_text", null, null);
			final TextView textView = (TextView) searchView.findViewById(id);
			textView.setHintTextColor(Color.WHITE);

			searchView.setSearchableInfo(searchManager
					.getSearchableInfo(getComponentName()));
		}
		return true;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		// outState.putInt(CURRENT_TAB_INDEX, mCurrentTabIndex);
		outState.putInt(CURRENT_ITEM_INDEX, mCurrentItemIndex);
		outState.putSerializable(CURRENT_DISPLAY_STATUS, mCurrentDisplayStatus);
		// outState.putString(CURRENT_LOGIN_INFO, mLoginInfo.toString());

		super.onSaveInstanceState(outState);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// if (requestCode == REQUEST_CODE_LOGIN
		// && resultCode == LoginActivity.RESULT_CODE_SUCCEED) {
		// renderItems(DisplayStatus.LOGINED);
		//
		// int index = ItemProvider.findIndex(mItemList, ProfilerItem.class);
		//
		// String jsonString = data
		// .getStringExtra(LoginActivity.BUNDLE_LOGIN_INFO);
		// JSONObject json = JSONUtil.parseToJSONObject(jsonString);
		// ProfilerItem pitem = (ProfilerItem) mItemList.get(index);
		// pitem.setProfile(json);
		//
		// index = ItemProvider.findIndex(mItemList, LearningItem.class);
		// onItemSelected(index);
		// }

		if (requestCode == SignInActivity.ACTION_REQUESTCODE) {
			if (resultCode == RESULT_OK) {
				User user = User.get();

				// for (APInfo ap : user.getApplications()) {
				// showLog(ap.getFullName());
				//
				// if(!ap.getContracts().contains(CONTRACT))
				// continue;
				//
				// getActionBar().setTitle(ap.getApName());
				//
				// DSS.setDSNS(ap);
				// }

				onLogined(user);
			} else {
				String errMsg = (data == null) ? "使用者取消登入動作" : data.getExtras()
						.getString(SignInActivity.SIGNIN_ERROR_MESSAGE);
				Toast.makeText(getApplicationContext(), errMsg,
						Toast.LENGTH_LONG).show();
				finish();
			}
		}
	}

	public void autoLogin() {
		if (AccessToken.getCurrentAccessToken() == null) {
			Intent i = new Intent(this, SignInActivity.class);
			startActivityForResult(i, SignInActivity.ACTION_REQUESTCODE);
		} else {
			User user = User.get();
			onLogined(user);
		}
	}

	// public void switchUser(DisplayStatus status, JSONObject userInfo) {
	// mCurrentDisplayStatus = status;
	// // mLoginInfo = userInfo;
	//
	// renderItems(status);
	//
	// int index = ItemProvider.findIndex(mItemList, ProfilerItem.class);
	// ProfilerItem pitem = (ProfilerItem) mItemList.get(index);
	// pitem.setProfile(userInfo);
	//
	// index = ItemProvider.findIndex(mItemList, LearningItem.class);
	// onItemSelected(index);
	// }

	public static FragmentItem getCurrentItem() {
		return mCurrentFragmentItem;
	}

	private void renderItems(DisplayStatus status) {
		List<BaseItem> items = ItemProvider.getItems(status);
		mItemList.clear();
		mItemList.addAll(items);
		// mTabsAdapter.clearTabs();
		mItemAdapter.notifyDataSetChanged();
		mItemAdapter.notifyDataSetInvalidated();

		// mTabsAdapter = new TabsAdapter(this, mViewPager);
	}

	private void onItemSelected(int position) {
		BaseItem item = mItemList.get(position);
		mCurrentItemIndex = position;
		mDrawerListView.setItemChecked(position, true);
		mDrawerLayout.closeDrawer(mDrawerListView);

		if (item instanceof TabsItem) {
			// setTitle(item.getTitle());
			// ActionBar bar = getActionBar();
			// mContainer.setVisibility(View.GONE);
			// mViewPager.setVisibility(View.VISIBLE);
			// bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
			//
			// TabsItem tabItem = (TabsItem) item;
			// mTabsAdapter.clearTabs();
			//
			// for (FragmentItem ft : tabItem.getItems()) {
			// Tab tab = bar.newTab().setText(ft.getTitle())
			// .setIcon(ft.getIcon());
			// mTabsAdapter.addTab(tab, ft);
			//
			// // bar.addTab(tab);
			// }
			// mTabsAdapter.notifyDataSetChanged();
			//
			// if (tabItem.getItems().size() > 0) {
			// mCurrentTabIndex = 0;
			// mViewPager.setCurrentItem(mCurrentTabIndex);
			// }

			mItemAdapter.notifyDataSetChanged();
		} else if (item instanceof FragmentItem) {

			setTitle(item.getTitle());
			mContainer.setVisibility(View.VISIBLE);
			// mViewPager.setVisibility(View.GONE);
			// getActionBar()
			// .setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);

			mCurrentFragmentItem = (FragmentItem) item;

			String className = mCurrentFragmentItem.getFragmentClass()
					.getName();

			FragmentManager fm = this.getFragmentManager();
			Fragment fragment = fm.findFragmentByTag(className);
			if (fragment == null) {
				mCurrentFragment = Fragment.instantiate(this, className);
				FragmentTransaction ft = fm.beginTransaction();
				ft.replace(R.id.container, mCurrentFragment, className);
				ft.commitAllowingStateLoss();
			}

			// if (fragment == null) {
			// mCurrentFragment = Fragment.instantiate(this, className);
			// ft.add(R.id.container, mCurrentFragment, className);
			//
			// } else if (fragment.isDetached()) {
			// mCurrentFragment = fragment;
			// ft.attach(fragment);
			// }

			mItemAdapter.notifyDataSetChanged();
		} else if (item instanceof ActionItem) {
			ActionItem action = (ActionItem) item;
			action.invoke(this);
		}

		invalidateOptionsMenu();
	}

	private void onLogined(User user) {
		// final ProgressDialog dialog = ProgressDialog.show(this,
		// getString(R.string.progress_title),
		// getString(R.string.main_loading_ap));

		mAPInfoHandler = new APInfoHandler(user);

		// mAPInfoHandler.checkAPContracts(MainActivity.this,
		// new OnCompletedListener<Void>() {
		//
		// @Override
		// public void onCompleted(Void result) {
		int appCount = mAPInfoHandler.listApplications().size();

		switch (appCount) {
		case 0:
			showNoAPInfo();
			break;
		case 1:
			showOneAPInfo(mAPInfoHandler.listApplications().get(0));
			break;
		default:
			showAPInfos();
		}
		//
		// dialog.dismiss();
		// }
		// });
	}

	private void showNoAPInfo() {
		Toast.makeText(this, "無任何可登入學校", Toast.LENGTH_LONG).show();
	}

	private void showNoDDS(APInfo ap) {
		String apName = ap.getApName();
		if (!StringUtil.isNullOrWhitespace(ap.getFullName()))
			apName = ap.getFullName();

		String className = NoDdsFragment.class.getName();
		FragmentManager fm = this.getFragmentManager();
		Fragment fragment = Fragment.instantiate(this, className);
		Bundle bundle = new Bundle();
		bundle.putString(NoDdsFragment.PARAM_AP_NAME, apName);
		fragment.setArguments(bundle);
		FragmentTransaction ft = fm.beginTransaction();
		ft.replace(R.id.container, fragment);
		ft.commitAllowingStateLoss();
	}

	private void showOneAPInfo(final APInfo ap) {	
		
		mAPInfoHandler.containContract(ap.getApName(), DSS.CONTRACT,
				new OnCompletedListener<Boolean>() {

					@Override
					public void onCompleted(Boolean containsDSS) {
						if (containsDSS) {
							mDrawerLayout
									.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
							DSS.setDSNS(ap);

							SharedPreferences settings = getSharedPreferences(
									SELECTED_AP_PREF, 0);
							SharedPreferences.Editor PE = settings.edit();
							PE.putString(SELECTED_AP, ap.getApName());
							PE.commit();

							int index = ItemProvider.findIndex(mItemList,
									StudentItem.class);
							onItemSelected(index);
						} else {
							showNoDDS(ap);
							mDrawerLayout
									.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
						}

					}
				}, new Cancelable());

		// if (!mAPInfoHandler.containContract(ap.getApName(), DSS.CONTRACT)) {
		// showNoDDS(ap);
		// mDrawerLayout
		// .setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
		// return;
		// }
		//
		// mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
		// DSS.setDSNS(ap);
		//
		// SharedPreferences settings = getSharedPreferences(SELECTED_AP_PREF,
		// 0);
		// SharedPreferences.Editor PE = settings.edit();
		// PE.putString(SELECTED_AP, ap.getApName());
		// PE.commit();
		//
		// int index = ItemProvider.findIndex(mItemList, StudentItem.class);
		// onItemSelected(index);
	}

	private void showAPInfos() {
		ActionBar bar = this.getActionBar();

		bar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);

		ArrayList<String> array = new ArrayList<String>();

		for (APInfo ap : mAPInfoHandler.listApplications()) {
			String display = ap.getFullName();
			if (StringUtil.isNullOrWhitespace(display))
				display = ap.getApName();
			array.add(display);
		}

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(
				bar.getThemedContext(), android.R.layout.simple_spinner_item,
				android.R.id.text1, array);

		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		this.getActionBar().setListNavigationCallbacks(adapter,
				new OnNavigationListener() {

					@Override
					public boolean onNavigationItemSelected(int itemPosition,
							long itemId) {
						APInfo ap = mAPInfoHandler.listApplications().get(
								itemPosition);
						showOneAPInfo(ap);

						return false;
					}
				});

		int selectedIndex = 0;

		SharedPreferences settings = getSharedPreferences(SELECTED_AP_PREF, 0);
		if (settings != null) {
			String selectedAP = settings.getString(SELECTED_AP,
					StringUtil.EMPTY);
			if (!StringUtil.isNullOrWhitespace(selectedAP)) {
				for (APInfo ap : mAPInfoHandler.listApplications()) {
					if (selectedAP.equals(ap.getApName()))
						break;

					selectedIndex++;
				}
			}
		}

		this.getActionBar().setSelectedNavigationItem(selectedIndex);
	}

	public static int getInitFlag() {
		return INIT_FLAG;
	}

	private class ItemAdapter extends ArrayAdapter<BaseItem> {

		private int _resource;
		// private Context _context;
		private LayoutInflater _inflator;

		public ItemAdapter(Context context, int resource) {
			super(context, resource, mItemList);

			_resource = resource;
			_inflator = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			BaseItem item = mItemAdapter.getItem(position);

			convertView = _inflator.inflate(_resource, null);

			TextView textView = (TextView) convertView
					.findViewById(R.id.txtItem);
			textView.setText(item.getTitle());

			ImageView image = (ImageView) convertView
					.findViewById(R.id.imgItemIcon);
			image.setImageResource(item.getIcon());

			if (position == mCurrentItemIndex) {
				int color = getResources().getColor(
						R.color.drawer_item_selected);
				convertView.setBackgroundColor(color);
			}

			return convertView;
		}

		@Override
		public boolean isEnabled(int position) {
			return true;
		}
	}

	/**
	 * This is a helper class that implements the management of tabs and all
	 * details of connecting a ViewPager with associated TabHost. It relies on a
	 * trick. Normally a tab host has a simple API for supplying a View or
	 * Intent that each tab will show. This is not sufficient for switching
	 * between pages. So instead we make the content part of the tab host 0dp
	 * high (it is not shown) and the TabsAdapter supplies its own dummy view to
	 * show as the tab content. It listens to changes in tabs, and takes care of
	 * switch to the correct paged in the ViewPager whenever the selected tab
	 * changes.
	 */
	// public class TabsAdapter extends ViewPagerAdapter implements
	// ActionBar.TabListener, ViewPager.OnPageChangeListener {
	// private final Context mContext;
	// private final ActionBar mActionBar;
	// private final ViewPager mViewPager;
	// private final ArrayList<FragmentItem> mTabs = new
	// ArrayList<FragmentItem>();
	//
	// public TabsAdapter(Activity activity, ViewPager pager) {
	// super(activity.getFragmentManager());
	// mContext = activity;
	// mActionBar = activity.getActionBar();
	// mViewPager = pager;
	// mViewPager.setAdapter(this);
	// mViewPager.setOnPageChangeListener(this);
	// }
	//
	// public void addTab(ActionBar.Tab tab, FragmentItem fragmentItem) {
	// // TabInfo info = new TabInfo(clss, args);
	// tab.setTag(fragmentItem);
	// tab.setTabListener(this);
	// mTabs.add(fragmentItem);
	// notifyDataSetChanged();
	// mActionBar.addTab(tab);
	// }
	//
	// public void clearTabs() {
	// mActionBar.removeAllTabs();
	// mTabs.clear();
	// notifyDataSetChanged();
	// }
	//
	// @Override
	// public int getCount() {
	// return mTabs.size();
	// }
	//
	// @Override
	// public Fragment getItem(int position) {
	// FragmentItem info = mTabs.get(position);
	// Fragment fragment = (Fragment) Fragment.instantiate(mContext, info
	// .getFragmentClass().getName(), new Bundle());
	//
	// return fragment;
	// }
	//
	// @Override
	// public void onPageScrolled(int position, float positionOffset,
	// int positionOffsetPixels) {
	// }
	//
	// @Override
	// public void onPageSelected(int position) {
	// mActionBar.setSelectedNavigationItem(position);
	//
	// }
	//
	// @Override
	// public void onPageScrollStateChanged(int state) {
	// }
	//
	// @Override
	// public void onTabSelected(Tab tab, FragmentTransaction ft) {
	// Object tag = tab.getTag();
	// for (int i = 0; i < mTabs.size(); i++) {
	// if (mTabs.get(i) == tag) {
	// mViewPager.setCurrentItem(i);
	// }
	// }
	// }
	//
	// @Override
	// public void onTabUnselected(Tab tab, FragmentTransaction ft) {
	// }
	//
	// @Override
	// public void onTabReselected(Tab tab, FragmentTransaction ft) {
	// }
	//
	// @Override
	// protected void onPrimaryItemChanged(int position) {
	// mCurrentFragment = getRegisteredFragment(position);
	// mCurrentFragmentItem = mTabs.get(position);
	// mCurrentTabIndex = position;
	// invalidateOptionsMenu();
	// mDrawerLayout.closeDrawer(mDrawerListView);
	// }
	// }
}
