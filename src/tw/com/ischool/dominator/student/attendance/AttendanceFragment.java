package tw.com.ischool.dominator.student.attendance;

import ischool.dsa.utility.DSRequest;
import ischool.dsa.utility.DSResponse;
import ischool.dsa.utility.XmlHelper;
import ischool.dsa.utility.XmlUtil;
import ischool.dsa.utility.http.Cancelable;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.w3c.dom.Element;

import tw.com.ischool.dominator.R;
import tw.com.ischool.dominator.main.MainActivity;
import tw.com.ischool.dominator.model.DSATask;
import tw.com.ischool.dominator.model.DSS;
import tw.com.ischool.dominator.model.OnFragmentShowListener;
import tw.com.ischool.dominator.model.OnReceiveListener;
import tw.com.ischool.dominator.student.StudentActivity;
import tw.com.ischool.dominator.util.ActivityHelper;
import tw.com.ischool.dominator.util.StringUtil;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class AttendanceFragment extends Fragment implements
		OnFragmentShowListener {

	private static final String PARAM_ATTENDANCE = "attendance";
	private static final String PARAM_START_CALENDAR = "start";
	private static final String PARAM_END_CALENDAR = "end";
	private static final String PARAM_SELECTED_TYPE = "type";

	private ActivityHelper mActivityHelper;
	private Activity mActivity;
	private Element mStudentElement;
	private Element mAttendanceElement;
	private LinearLayout mContainerType;
	private ListView mListView;
	private AbsAdapter mAdapter;
	private Button mBtnStart;
	private Button mBtnEnd;
	private Button mClickedButton;
	private List<Element> mAllAbsenceElements = new ArrayList<Element>();
	private List<Element> mDateElements = new ArrayList<Element>();
	private List<Element> mDisplayElements = new ArrayList<Element>();
	private Calendar mStartCalender;
	private Calendar mEndCalender;
	private String mCurrentSelectedType;
//	private PullToRefreshLayout mPullToRefreshLayout;
	private boolean mFragmentShowed = false;
	private boolean mActivityCreated = false;

//	private OnRefreshListener mRefreshListener = new OnRefreshListener() {
//
//		@Override
//		public void onRefreshStarted(View view) {
//			String xmlString = getArguments().getString(
//					StudentActivity.PARAM_STUDENT);
//			mStudentElement = XmlHelper.parseXml(xmlString);
//
//			String studentId = XmlUtil.getElementText(mStudentElement,
//					"StudentID");
//
//			Element request = XmlUtil.createElement("Request");
//			XmlUtil.addElement(request, "RefStudentId", studentId);
//
//			DSS.sendRequest(mActivity, "student.GetAttendance", request,
//					new OnReceiveListener<DSResponse>() {
//
//						@Override
//						protected void onReceive(DSResponse result) {
//							mAttendanceElement = result.getContent();
//
//							mAllAbsenceElements = XmlUtil.selectElements(
//									mAttendanceElement, "Attendance");
//
//							filterAbsense(null, null);
//							mPullToRefreshLayout.setRefreshComplete();
//						}
//
//						@Override
//						protected void onError(Exception e) {
//							Toast.makeText(mActivity, e.toString(),
//									Toast.LENGTH_LONG).show();
//							mPullToRefreshLayout.setRefreshComplete();
//						}
//					}, new Cancelable());
//		}
//	};

	private OnClickListener mBtnOnClickListener = new OnClickListener() {

		@Override
		public void onClick(final View v) {
			mClickedButton = (Button) v;
			Calendar c = (Calendar) mClickedButton.getTag();
			if (c == null) {
				c = Calendar.getInstance(Locale.getDefault());
			}

			final DatePickerDialog dialog = new DatePickerDialog(mActivity,
					mDateListener, c.get(Calendar.YEAR), c.get(Calendar.MONTH),
					c.get(Calendar.DAY_OF_MONTH));

			dialog.setButton(DialogInterface.BUTTON_NEGATIVE,
					mActivity.getString(R.string.attendance_unlimit),
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface d, int which) {
							Button button = (Button) v;
							button.setTag(null);
							button.setText(R.string.attendance_unlimit);
							dialog.getDatePicker().setTag(
									R.string.attendance_unlimit);
						}
					});
			dialog.show();
		}
	};

	DatePickerDialog.OnDateSetListener mDateListener = new DatePickerDialog.OnDateSetListener() {

		@Override
		public void onDateSet(DatePicker view, int year, int monthOfYear,
				int dayOfMonth) {

			if (view.getTag() == null) {
				Calendar c = Calendar.getInstance(Locale.getDefault());
				c.set(Calendar.YEAR, year);
				c.set(Calendar.MONTH, monthOfYear);
				c.set(Calendar.DAY_OF_MONTH, dayOfMonth);

				Date date = c.getTime();
				String string = StringUtil.toDateString(date);
				mClickedButton.setText(string);
				mClickedButton.setTag(c);
			}

			view.setTag(null);

			mStartCalender = (Calendar) mBtnStart.getTag();
			mEndCalender = (Calendar) mBtnEnd.getTag();

			filterAbsense(mStartCalender, mEndCalender);
		}
	};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_student_attendance,
				container, false);

		// Retrieve the PullToRefreshLayout from the content view
//		mPullToRefreshLayout = (PullToRefreshLayout) view
//				.findViewById(R.id.ptr_attendance);

		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		mActivity = getActivity();
		mActivityHelper = new ActivityHelper(mActivity);

		mListView = (ListView) mActivity.findViewById(R.id.lvDetail);
		mAdapter = new AbsAdapter();
		mListView.setAdapter(mAdapter);
		mContainerType = (LinearLayout) mActivity
				.findViewById(R.id.container_attendance_type);
		
		mBtnStart = (Button) mActivity.findViewById(R.id.btnStart);
		mBtnEnd = (Button) mActivity.findViewById(R.id.btnEnd);

		mBtnStart.setOnClickListener(mBtnOnClickListener);
		mBtnEnd.setOnClickListener(mBtnOnClickListener);

		if (savedInstanceState != null) {
			mStudentElement = XmlHelper.parseXml(savedInstanceState
					.getString(StudentActivity.PARAM_STUDENT));
			mAttendanceElement = XmlHelper.parseXml(savedInstanceState
					.getString(PARAM_ATTENDANCE));
			mAllAbsenceElements = XmlUtil.selectElements(mAttendanceElement,
					"Attendance");

			mStartCalender = StringUtil.parseToCalendar(savedInstanceState
					.getString(PARAM_START_CALENDAR));
			mEndCalender = StringUtil.parseToCalendar(savedInstanceState
					.getString(PARAM_END_CALENDAR));

			mBtnStart.setTag(mStartCalender);
			mBtnEnd.setTag(mEndCalender);

			if (mStartCalender != null) {
				mBtnStart.setText(StringUtil.toDateString(mStartCalender));
			}

			if (mEndCalender != null) {
				mBtnEnd.setText(StringUtil.toDateString(mEndCalender));
			}

			filterAbsense(mStartCalender, mEndCalender);

			mCurrentSelectedType = savedInstanceState
					.getString(PARAM_SELECTED_TYPE);
			for (int i = 0; i < mContainerType.getChildCount(); i++) {
				View view = mContainerType.getChildAt(i);
				String type = (String) view.getTag();
				if (type.equalsIgnoreCase(mCurrentSelectedType)) {
					this.changeSelectedType(view);
					break;
				}
			}
		} else {
			mActivityCreated = true;
			loadAttendance();
		}

		// Now setup the PullToRefreshLayout
		// ActionBarPullToRefresh.from(mActivity).allChildrenArePullable()
		// .listener(mRefreshListener).setup(mPullToRefreshLayout);

	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putString(StudentActivity.PARAM_STUDENT,
				XmlHelper.convertToString(mStudentElement));
		outState.putString(PARAM_ATTENDANCE,
				XmlHelper.convertToString(mAttendanceElement));
		outState.putString(PARAM_START_CALENDAR,
				StringUtil.toDateString(mStartCalender));
		outState.putString(PARAM_END_CALENDAR,
				StringUtil.toDateString(mEndCalender));
		outState.putString(PARAM_SELECTED_TYPE, mCurrentSelectedType);

		super.onSaveInstanceState(outState);
	}

	@Override
	public void onFragmentShowed() {
		mFragmentShowed = true;

		if (mAttendanceElement == null) {
			loadAttendance();
		}
	}

	private void loadAttendance() {
		if (!mFragmentShowed || !mActivityCreated) {
			return;
		}

		String xmlString = getArguments().getString(
				StudentActivity.PARAM_STUDENT);
		mStudentElement = XmlHelper.parseXml(xmlString);

		String studentId = XmlUtil.getElementText(mStudentElement, "StudentID");

		Element request = XmlUtil.createElement("Request");
		XmlUtil.addElement(request, "RefStudentId", studentId);

//		final ProgressDialog progress = ProgressDialog.show(mActivity,
//				mActivity.getString(R.string.progress_title),
//				mActivity.getString(R.string.attendance_loading));

		final ProgressDialog dialog = new ProgressDialog(mActivity);
		dialog.setTitle(mActivity.getString(R.string.progress_title));
		dialog.setMessage(mActivity.getString(R.string.attendance_loading));
		dialog.setCancelable(false);

		final Cancelable cancelable = new Cancelable();

		final DSATask<DSRequest> task = DSS.sendRequest(mActivity, "student.GetAttendance", request,
				new OnReceiveListener<DSResponse>() {

					@Override
					protected void onReceive(DSResponse result) {
						mAttendanceElement = result.getContent();

						mAllAbsenceElements = XmlUtil.selectElements(
								mAttendanceElement, "Attendance");

						filterAbsense(null, null);
						dialog.dismiss();
					}

					@Override
					protected void onError(Exception e) {
						Toast.makeText(mActivity, e.toString(),
								Toast.LENGTH_LONG).show();
						dialog.dismiss();
					}
				}, cancelable);
		
		dialog.setButton(DialogInterface.BUTTON_NEGATIVE,
				getString(R.string.cancel),
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						cancelable.setCancel(true);

						if (task != null)
							task.cancel(true);

						dialog.dismiss();
					}
				});

		if (task != null)
			dialog.show();
		else
			Log.d(MainActivity.TAG, "Task is null");
	}

	private void filterAbsense(Calendar start, Calendar end) {
		mDisplayElements.clear();
		mDateElements.clear();

		for (Element e : mAllAbsenceElements) {
			String dateString = e.getAttribute("OccurDate");
			Calendar date = StringUtil.parseToCalendar(dateString);

			if (end != null && date.after(end))
				continue;
			if (start != null && date.before(start))
				continue;

			mDateElements.add(e);
			mDisplayElements.add(e);
		}
		mAdapter.notifyDataSetChanged();
		bindAbsense();
	}

	private void changeSelectedType(View selectedView) {
		for (int i = 0; i < mContainerType.getChildCount(); i++) {
			View child = mContainerType.getChildAt(i);
			if (child != selectedView) {
				child.setBackgroundResource(R.drawable.back);
			} else {
				child.setBackgroundResource(R.drawable.back2);
			}
		}

		mCurrentSelectedType = (String) selectedView.getTag();
		final String allString = mActivity.getString(R.string.attendance_all);

		mDisplayElements.clear();
		if (mCurrentSelectedType.equals(allString)) {
			mDisplayElements.addAll(mDateElements);
		} else {
			for (Element dateElement : mDateElements) {
				Element detail = XmlUtil.selectElement(dateElement, "Detail");

				for (Element e : XmlUtil.selectElements(detail, "Period")) {
					if (e.getAttribute("AbsenceType").equals(
							mCurrentSelectedType)) {
						mDisplayElements.add(dateElement);
						break;
					}
				}
			}
		}

		mAdapter.notifyDataSetChanged();
	}

	private void bindAbsense() {
		TextView emptyText = (TextView)mActivity.findViewById(android.R.id.empty);	
		mListView.setEmptyView(emptyText);
		
		mContainerType.removeAllViews();
		if (mAllAbsenceElements == null || mAllAbsenceElements.size() == 0)
			return;

		HashMap<String, Integer> absMap = new HashMap<String, Integer>();
		final String allString = mActivity.getString(R.string.attendance_all);

		absMap.put(allString, 0);
		for (Element e : mDisplayElements) {

			Element detail = XmlUtil.selectElement(e, "Detail");

			for (Element p : XmlUtil.selectElements(detail, "Period")) {
				String pname = p.getAttribute("AbsenceType");
				int value = 1;
				if (absMap.containsKey(pname)) {
					value += absMap.get(pname);
				}
				absMap.put(pname, value);

				value = absMap.get(allString) + 1;
				absMap.put(allString, value);
			}
		}

		int dp5 = mActivityHelper.getScreen().toPixelInt(5);

		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

		List<String> keys = new ArrayList<String>(absMap.keySet());
		Collections.sort(keys, new Comparator<String>() {

			@Override
			public int compare(String lhs, String rhs) {
				if (lhs.equals(allString))
					return -1;
				if (rhs.equals(allString))
					return 1;
				return lhs.compareTo(rhs);
			}
		});

		for (String type : keys) {
			TextView textView = new TextView(mActivity);
			textView.setGravity(Gravity.CENTER);
			textView.setMinimumWidth(mActivityHelper.getScreen()
					.toPixelInt(100));
			textView.setBackgroundResource(R.drawable.back);
			textView.setPadding(dp5 * 2, dp5, dp5 * 2, dp5);

			int count = absMap.get(type);
			String display = "%s ( %d )";
			display = String.format(display, type, count);
			textView.setText(display);
			textView.setTag(type);

			textView.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					changeSelectedType(v);
				}
			});

			mContainerType.addView(textView, params);

			if (type.equals(allString)) {
				changeSelectedType(textView);
			}
		}
	}

	private class AbsAdapter extends ArrayAdapter<Element> {
		private LayoutInflater _inflater;

		public AbsAdapter() {
			super(mActivity, R.layout.item_attendance, mDisplayElements);

			_inflater = (LayoutInflater) mActivity
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			if (convertView == null) {
				holder = new ViewHolder();
				convertView = _inflater.inflate(R.layout.item_attendance, null);
				holder.txtDate = (TextView) convertView
						.findViewById(R.id.txtDate);
				holder.txtDetail = (TextView) convertView
						.findViewById(R.id.txtDetail);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			Element dayElement = mDisplayElements.get(position);
			holder.txtDate.setText(dayElement.getAttribute("OccurDate"));

			HashMap<String, String> absMap = new HashMap<String, String>();

			Element detail = XmlUtil.selectElement(dayElement, "Detail");

			for (Element p : XmlUtil.selectElements(detail, "Period")) {
				String pname = p.getAttribute("AbsenceType");
				String value = p.getTextContent();

				if (absMap.containsKey(pname)) {
					value = absMap.get(pname) + "、" + value;
				}
				absMap.put(pname, value);
			}

			StringBuilder sb = new StringBuilder();

			for (String key : absMap.keySet()) {
				if (sb.length() > 0) {
					sb.append("\n");
				}
				sb.append(key).append(" : ").append(absMap.get(key));
			}

			holder.txtDetail.setText(sb.toString());
			return convertView;
		}
		
		@Override
		public boolean isEnabled(int position) {
		    return false;
		}
	}

	class ViewHolder {
		TextView txtDate;
		TextView txtDetail;
	}
}
