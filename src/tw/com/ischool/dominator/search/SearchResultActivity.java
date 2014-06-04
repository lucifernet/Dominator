package tw.com.ischool.dominator.search;

import tw.com.ischool.dominator.R;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SearchView;
import android.widget.TextView;

public class SearchResultActivity extends Activity {

	// private TextView mTxtTitle;
	// private FragmentItem mSelectedItem;
	// private TextView mTxtCount;
	private String mKeyword;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search_result);

		// getActionBar().setIcon(R.drawable.ic_1know);

		// mTxtTitle = (TextView) this.findViewById(R.id.search_title);
		// mSelectedItem = MainActivity.getCurrentItem();
		// mTxtTitle.setText(mSelectedItem.getTitle());
		//
		// mTxtCount = (TextView) this.findViewById(R.id.search_count);

		if (savedInstanceState == null)
			handleIntent(getIntent());
//		else {
//			Intent intent = new Intent(Intent.ACTION_SEARCH);
//			intent.putExtras(savedInstanceState);
//			handleIntent(intent);
//		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.search_result, menu);

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
	protected void onNewIntent(Intent intent) {
		// TODO Auto-generated method stub
		super.onNewIntent(intent);

		handleIntent(intent);
	}

	private void handleIntent(Intent intent) {

		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			mKeyword = intent.getStringExtra(SearchManager.QUERY);

			String title = getString(R.string.title_activity_search_result);
			title = String.format(title, mKeyword);
			setTitle(title);
		}

		Bundle args = new Bundle();
		args.putString(SearchManager.QUERY, mKeyword);

		FragmentManager fm = getFragmentManager();
		FragmentTransaction ft = fm.beginTransaction();
		Fragment fragment;
		// if (MainActivity.getCurrentItem() instanceof StudentItem) {
		fragment = Fragment.instantiate(this,
				StudentResultFragment.class.getName(), args);

		// }
		ft.replace(R.id.search_container, fragment).commitAllowingStateLoss();
	}
}
