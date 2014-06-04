/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tw.com.ischool.dominator.util;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;

/**
 * Implementation of {@link android.support.v4.view.PagerAdapter} that
 * represents each page as a {@link Fragment} that is persistently kept in the
 * fragment manager as long as the user can return to the page.
 */
public abstract class ViewPagerAdapter extends PagerAdapter {
	private static final String TAG = "FragmentPagerAdapter";
	private static final boolean DEBUG = false;

	private final FragmentManager mFragmentManager;
	private FragmentTransaction mCurTransaction = null;
	private Fragment mCurrentPrimaryItem = null;
	private SparseArray<Fragment> mRegisteredFragments = null;

	public ViewPagerAdapter(FragmentManager fm) {
		mFragmentManager = fm;
		mRegisteredFragments = new SparseArray<Fragment>();
	}

	/**
	 * Return the Fragment associated with a specified position.
	 */
	public abstract Fragment getItem(int position);

	@Override
	public void startUpdate(View container) {
	}

	@Override
	public Object instantiateItem(View container, int position) {
		if (mCurTransaction == null) {
			mCurTransaction = mFragmentManager.beginTransaction();
		}

		// Do we already have this fragment?
		String name = makeFragmentName(container.getId(), position);
		Fragment fragment = mFragmentManager.findFragmentByTag(name);
		if (fragment != null) {
			if (DEBUG)
				Log.v(TAG, "Attaching item #" + position + ": f=" + fragment);
			mCurTransaction.attach(fragment);
		} else {
			fragment = getItem(position);
			if (DEBUG)
				Log.v(TAG, "Adding item #" + position + ": f=" + fragment);
			mCurTransaction.add(container.getId(), fragment,
					makeFragmentName(container.getId(), position));
		}
		if (fragment != mCurrentPrimaryItem) {
			fragment.setMenuVisibility(false);
		}

		// save register fragment
		mRegisteredFragments.put(position, fragment);

		return fragment;
	}

	@Override
	public void destroyItem(View container, int position, Object object) {
		mRegisteredFragments.remove(position);

		if (mCurTransaction == null) {
			mCurTransaction = mFragmentManager.beginTransaction();
		}
		if (DEBUG)
			Log.v(TAG, "Detaching item #" + position + ": f=" + object + " v="
					+ ((Fragment) object).getView());
		mCurTransaction.detach((Fragment) object);
	}

	@Override
	public void setPrimaryItem(View container, int position, Object object) {
		Fragment fragment = (Fragment) object;
		if (fragment != mCurrentPrimaryItem) {
			if (mCurrentPrimaryItem != null) {
				mCurrentPrimaryItem.setMenuVisibility(false);
			}
			if (fragment != null) {
				fragment.setMenuVisibility(true);
			}
			mCurrentPrimaryItem = fragment;
			onPrimaryItemChanged(position);
		}
	}

	@Override
	public void finishUpdate(View container) {
		if (mCurTransaction != null) {
			mCurTransaction.commitAllowingStateLoss();
			mCurTransaction = null;
			mFragmentManager.executePendingTransactions();
		}
	}

	@Override
	public boolean isViewFromObject(View view, Object object) {
		return ((Fragment) object).getView() == view;
	}

	@Override
	public Parcelable saveState() {
		return null;
	}

	@Override
	public void restoreState(Parcelable state, ClassLoader loader) {
	}

	protected abstract void onPrimaryItemChanged(int position);
	
	public Fragment getRegisteredFragment(int position) {
		return mRegisteredFragments.get(position);
	}

	public static String makeFragmentName(int viewId, int index) {
		return "android:switcher:" + viewId + ":" + index;
	}
}