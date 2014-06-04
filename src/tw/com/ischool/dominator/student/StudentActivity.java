package tw.com.ischool.dominator.student;

import ischool.dsa.utility.XmlHelper;

import java.util.ArrayList;

import org.w3c.dom.Element;

import tw.com.ischool.dominator.R;
import tw.com.ischool.dominator.main.item.FragmentItem;
import tw.com.ischool.dominator.model.OnFragmentShowListener;
import tw.com.ischool.dominator.student.attendance.AttendanceItem;
import tw.com.ischool.dominator.student.baseinfo.BaseInfoItem;
import tw.com.ischool.dominator.student.counsel.CounselItem;
import tw.com.ischool.dominator.student.discipline.DisciplineItem;
import tw.com.ischool.dominator.student.score.ScoreInfoItem;
import tw.com.ischool.dominator.util.ViewPagerAdapter;
import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.Menu;

public class StudentActivity extends Activity {

	public static final String PARAM_STUDENT = "studentInfo";
	public static final String PARAM_PHOTO = "photo";
	
	private Element mStudentElement;
	private TabsAdapter mTabsAdapter;
	private ViewPager mViewPager;
	private Bitmap mStudentPhoto;
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_student);
		
		getActionBar().setTitle(R.string.title_activity_student);
		//getActionBar().setDisplayHomeAsUpEnabled(true);

		String studentString;
		
		if (savedInstanceState == null) {
			Intent intent = getIntent();
			studentString = intent.getStringExtra(PARAM_STUDENT);
			mStudentPhoto = (Bitmap)intent.getParcelableExtra(PARAM_PHOTO);
		} else {
			studentString = savedInstanceState.getString(PARAM_STUDENT);
			mStudentPhoto = (Bitmap)savedInstanceState.getParcelable(PARAM_PHOTO);
		}
		
		mStudentElement = XmlHelper.parseXml(studentString);
		

		// ViewPager and its adapters use support library
		// fragments, so use getSupportFragmentManager.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mTabsAdapter = new TabsAdapter(this, mViewPager);
		mViewPager.setAdapter(mTabsAdapter);

		// Specify that tabs should be displayed in the action bar.
		final ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		// Add 3 tabs, specifying the tab's text and TabListener
		mTabsAdapter.addTab(new BaseInfoItem());
		mTabsAdapter.addTab(new AttendanceItem());
		mTabsAdapter.addTab(new DisciplineItem());
		mTabsAdapter.addTab(new ScoreInfoItem());
		mTabsAdapter.addTab(new CounselItem());
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putString(PARAM_STUDENT,
				XmlHelper.convertToString(mStudentElement));
		outState.putParcelable(PARAM_PHOTO, mStudentPhoto);
		
		super.onSaveInstanceState(outState);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.student, menu);
		return true;
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
	public class TabsAdapter extends ViewPagerAdapter implements
			ActionBar.TabListener, ViewPager.OnPageChangeListener {
		private final Context mContext;
		private final ActionBar mActionBar;
		private final ViewPager mViewPager;
		private final ArrayList<FragmentItem> mTabs = new ArrayList<FragmentItem>();

		public TabsAdapter(Activity activity, ViewPager pager) {
			super(activity.getFragmentManager());
			mContext = activity;
			mActionBar = activity.getActionBar();
			mViewPager = pager;
			mViewPager.setAdapter(this);
			mViewPager.setOnPageChangeListener(this);
		}

		public void addTab(FragmentItem fragmentItem) {
			// TabInfo info = new TabInfo(clss, args);

			Tab tab = mActionBar.newTab().setText(fragmentItem.getTitle())
					.setIcon(fragmentItem.getIcon());

			tab.setTag(fragmentItem);
			tab.setTabListener(this);
			mTabs.add(fragmentItem);
			notifyDataSetChanged();
			mActionBar.addTab(tab);
		}

		public void clearTabs() {
			mActionBar.removeAllTabs();
			mTabs.clear();
			notifyDataSetChanged();
		}

		@Override
		public int getCount() {
			return mTabs.size();
		}

		@Override
		public Fragment getItem(int position) {
			FragmentItem info = mTabs.get(position);
			Fragment fragment = (Fragment) Fragment.instantiate(mContext, info
					.getFragmentClass().getName(), new Bundle());

			Bundle argument = new Bundle();
			argument.putString(PARAM_STUDENT,
					XmlHelper.convertToString(mStudentElement));
			argument.putParcelable(PARAM_PHOTO, mStudentPhoto);
			fragment.setArguments(argument);

			return fragment;
		}

		@Override
		public void onPageScrolled(int position, float positionOffset,
				int positionOffsetPixels) {
		}

		@Override
		public void onPageSelected(int position) {
			mActionBar.setSelectedNavigationItem(position);

		}

		@Override
		public void onPageScrollStateChanged(int state) {
		}

		@Override
		public void onTabSelected(Tab tab, FragmentTransaction ft) {
			Object tag = tab.getTag();
			for (int i = 0; i < mTabs.size(); i++) {
				if (mTabs.get(i) == tag) {
					mViewPager.setCurrentItem(i);
				}
			}
		}

		@Override
		public void onTabUnselected(Tab tab, FragmentTransaction ft) {
		}

		@Override
		public void onTabReselected(Tab tab, FragmentTransaction ft) {
		}

		@Override
		protected void onPrimaryItemChanged(int position) {
			Fragment fragment = getRegisteredFragment(position);
			if (fragment instanceof OnFragmentShowListener) {
				OnFragmentShowListener listener = (OnFragmentShowListener) fragment;
				listener.onFragmentShowed();
			}
			// mCurrentFragment = getRegisteredFragment(position);
			// mCurrentFragmentItem = mTabs.get(position);
			// mCurrentTabIndex = position;
			invalidateOptionsMenu();
			// mDrawerLayout.closeDrawer(mDrawerListView);
		}
	}
}
