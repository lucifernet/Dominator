package tw.com.ischool.dominator.student.counsel;

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
import java.util.List;
import java.util.Locale;

import org.w3c.dom.Element;

import tw.com.ischool.dominator.R;
import tw.com.ischool.dominator.model.DSATask;
import tw.com.ischool.dominator.model.DSS;
import tw.com.ischool.dominator.model.OnCompletedListener;
import tw.com.ischool.dominator.model.OnFragmentShowListener;
import tw.com.ischool.dominator.model.OnReceiveListener;
import tw.com.ischool.dominator.student.StudentActivity;
import tw.com.ischool.dominator.util.StringUtil;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class CounselFragment extends Fragment implements OnFragmentShowListener {

	private static final String PARAM_START_CALENDAR = "start";
	private static final String PARAM_END_CALENDAR = "end";
	private static final String PARAM_SELECTED_TYPE = "type";
	private static final String PARAM_INTERVIEW = "interview";
	private static final String PARAM_CARE = "care";
	private static final String PARAM_MEETING = "meeting";

	private String COUNSEL_ALL;
	private String COUNSEL_INTERVIEW;
	private String COUNSEL_MEETING;
	private String COUNSEL_CARE;
	private String mCurrentSelectedType;
	private Activity mActivity;
	private Button mBtnStart;
	private Button mBtnEnd;
	private Button mClickedButton;
	private Calendar mStartCalender;
	private Calendar mEndCalender;
	private boolean mFragmentShowed = false;
	private boolean mActivityCreated = false;
	private List<Element> mAllElements = new ArrayList<Element>();
	private List<Element> mDateElements = new ArrayList<Element>();
	private List<Element> mDisplayElements = new ArrayList<Element>();
	private ListView mListView;
	private CounselAdapter mAdapter;
	private Element mStudentElement;
	private Element mInterviewElement;
	private Element mMeetingElement;
	private Element mCareElement;
	private TextView mTxtCountAll;
	private TextView mTxtInterviewCount;
	private TextView mTxtMeetingCount;
	private TextView mTxtCareCount;
	private View mContainerAll;
	private View mContainerInterview;
	private View mContainerMeeting;
	private View mContainerCare;
	private LinearLayout mContainerCounselType;
	private ProgressDialog mProgress;
//	private PullToRefreshLayout mPullToRefreshLayout;
	private Cancelable mCancelable;
	private DSATask<DSRequest> mTask1;
	private DSATask<DSRequest> mTask2;
	private DSATask<DSRequest> mTask3;

//	private OnRefreshListener mRefreshListener = new OnRefreshListener() {
//
//		@Override
//		public void onRefreshStarted(View view) {
//			loadData(new OnCompletedListener<Void>() {
//
//				@Override
//				public void onCompleted(Void result) {
//					filterDate(mStartCalender, mEndCalender);
//					mPullToRefreshLayout.setRefreshComplete();
//				}
//			});
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

			filterDate(mStartCalender, mEndCalender);
		}
	};

	private OnClickListener mTypeClickListener = new OnClickListener() {
		@Override
		public void onClick(View view) {
			changeSelectedType(view);
		}
	};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_student_counsel,
				container, false);

		mBtnStart = (Button) view.findViewById(R.id.btnStart);
		mBtnEnd = (Button) view.findViewById(R.id.btnEnd);
		mListView = (ListView) view.findViewById(R.id.lvRecord);
		mTxtCountAll = (TextView) view.findViewById(R.id.txtCountAll);
		mTxtInterviewCount = (TextView) view
				.findViewById(R.id.txtInterviewCount);
		mTxtMeetingCount = (TextView) view.findViewById(R.id.txtMeetingCount);
		mTxtCareCount = (TextView) view.findViewById(R.id.txtCareCount);
		mContainerAll = view.findViewById(R.id.container_all);
		mContainerInterview = view.findViewById(R.id.container_interview);
		mContainerMeeting = view.findViewById(R.id.container_meeting);
		mContainerCare = view.findViewById(R.id.container_care);
		mContainerCounselType = (LinearLayout) view
				.findViewById(R.id.container_counsel_type);

//		mPullToRefreshLayout = (PullToRefreshLayout) view
//				.findViewById(R.id.ptr_counsel);		
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		mActivity = getActivity();

		COUNSEL_ALL = mActivity.getString(R.string.counsel_all);
		COUNSEL_INTERVIEW = mActivity.getString(R.string.counsel_interview);
		COUNSEL_MEETING = mActivity.getString(R.string.counsel_meeting);
		COUNSEL_CARE = mActivity.getString(R.string.counsel_care);

		mAdapter = new CounselAdapter();
		mListView.setAdapter(mAdapter);

		mBtnStart.setOnClickListener(mBtnOnClickListener);
		mBtnEnd.setOnClickListener(mBtnOnClickListener);

//		ActionBarPullToRefresh.from(mActivity).allChildrenArePullable()
//				.listener(mRefreshListener).setup(mPullToRefreshLayout);

		if (savedInstanceState != null) {
			mStudentElement = XmlHelper.parseXml(savedInstanceState
					.getString(StudentActivity.PARAM_STUDENT));
			mInterviewElement = XmlHelper.parseXml(savedInstanceState
					.getString(PARAM_INTERVIEW));
			mAllElements = XmlUtil.selectElements(mInterviewElement, "Record");

			mMeetingElement = XmlHelper.parseXml(savedInstanceState
					.getString(PARAM_MEETING));
			mAllElements.addAll(XmlUtil.selectElements(mMeetingElement,
					"MeetingRecord"));

			mCareElement = XmlHelper.parseXml(savedInstanceState
					.getString(PARAM_CARE));
			mAllElements.addAll(XmlUtil.selectElements(mCareElement,
					"CareRecord"));

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

			filterDate(mStartCalender, mEndCalender);

			mCurrentSelectedType = savedInstanceState
					.getString(PARAM_SELECTED_TYPE);
			for (int i = 0; i < mContainerCounselType.getChildCount(); i++) {
				View view = mContainerCounselType.getChildAt(i);
				String type = (String) view.getTag();
				if (type.equalsIgnoreCase(mCurrentSelectedType)) {
					this.changeSelectedType(view);
					break;
				}
			}
		} else {
			String xmlString = getArguments().getString(
					StudentActivity.PARAM_STUDENT);
			mStudentElement = XmlHelper.parseXml(xmlString);

			mActivityCreated = true;
			loadDataWhenReady();
		}
	}

	@Override
	public void onFragmentShowed() {
		mFragmentShowed = true;

		loadDataWhenReady();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putString(StudentActivity.PARAM_STUDENT,
				XmlHelper.convertToString(mStudentElement));
		outState.putString(PARAM_INTERVIEW,
				XmlHelper.convertToString(mInterviewElement));
		outState.putString(PARAM_MEETING,
				XmlHelper.convertToString(mMeetingElement));
		outState.putString(PARAM_CARE, XmlHelper.convertToString(mCareElement));
		outState.putString(PARAM_START_CALENDAR,
				StringUtil.toDateString(mStartCalender));
		outState.putString(PARAM_END_CALENDAR,
				StringUtil.toDateString(mEndCalender));
		outState.putString(PARAM_SELECTED_TYPE, mCurrentSelectedType);

		super.onSaveInstanceState(outState);
	}

	private void loadDataWhenReady() {
		if (!mFragmentShowed || !mActivityCreated) {
			return;
		}

		mProgress = new ProgressDialog(mActivity);
		mProgress.setTitle(mActivity.getString(R.string.progress_title));
		mProgress.setMessage(mActivity.getString(R.string.counsel_interview_loading));
		mProgress.setCancelable(false);
		//TODO
//		mProgress = ProgressDialog.show(mActivity,
//				mActivity.getString(R.string.progress_title),
//				mActivity.getString(R.string.counsel_interview_loading));

		mCancelable = new Cancelable();
		
		mProgress.setButton(DialogInterface.BUTTON_NEGATIVE,
				getString(R.string.cancel),
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						mCancelable.setCancel(true);

						if (mTask1 != null)
							mTask1.cancel(true);
						
						if (mTask2 != null)
							mTask2.cancel(true);
						
						if (mTask3 != null)
							mTask3.cancel(true);

						dialog.dismiss();
					}
				});
		
		loadData(new OnCompletedListener<Void>() {

			@Override
			public void onCompleted(Void result) {
				filterDate(null, null);
				mProgress.dismiss();
			}
		});
	}

	private void loadData(final OnCompletedListener<Void> listener) {
		if (!mFragmentShowed || !mActivityCreated) {
			return;
		}

		String studentId = XmlUtil.getElementText(mStudentElement, "StudentID");

		Element request = XmlUtil.createElement("Request");
		XmlUtil.addElement(request, "StudentID", studentId);

		mAllElements.clear();
		mInterviewElement = null;
		mMeetingElement = null;
		mCareElement = null;

		mTask1 = DSS.sendRequest(mActivity, "student_counsel.GetInterviewRecord",
				request, new OnReceiveListener<DSResponse>() {

					@Override
					protected void onReceive(DSResponse result) {
						mInterviewElement = result.getContent();

						mAllElements.addAll(XmlUtil.selectElements(
								mInterviewElement, "Record"));

						checkCompleted(listener);
					}

					@Override
					protected void onError(Exception e) {
						mInterviewElement = XmlUtil.createElement("Response");
						checkCompleted(listener);

						Toast.makeText(mActivity, e.toString(),
								Toast.LENGTH_LONG).show();
					}
				}, mCancelable);

		mTask2 = DSS.sendRequest(mActivity, "student_counsel.GetMeetingRecord", request,
				new OnReceiveListener<DSResponse>() {

					@Override
					protected void onReceive(DSResponse result) {
						mMeetingElement = result.getContent();

						mAllElements.addAll(XmlUtil.selectElements(
								mMeetingElement, "MeetingRecord"));

						checkCompleted(listener);
					}

					@Override
					protected void onError(Exception e) {
						mMeetingElement = XmlUtil.createElement("Response");
						checkCompleted(listener);
						Toast.makeText(mActivity, e.toString(),
								Toast.LENGTH_LONG).show();
					}
				},mCancelable);

		mTask3 = DSS.sendRequest(mActivity, "student_counsel.GetCareRecord", request,
				new OnReceiveListener<DSResponse>() {

					@Override
					protected void onReceive(DSResponse result) {
						mCareElement = result.getContent();

						mAllElements.addAll(XmlUtil.selectElements(
								mCareElement, "CareRecord"));

						checkCompleted(listener);
					}

					@Override
					protected void onError(Exception e) {
						mCareElement = XmlUtil.createElement("Response");
						checkCompleted(listener);
						Toast.makeText(mActivity, e.toString(),
								Toast.LENGTH_LONG).show();
					}
				},mCancelable);
	}

	private void checkCompleted(OnCompletedListener<Void> listener) {
		if (mInterviewElement == null || mMeetingElement == null
				|| mCareElement == null)
			return;

		TextView emptyText = (TextView) mActivity
				.findViewById(R.id.empty_view_counsel);
		mListView.setEmptyView(emptyText);
		
		if (listener != null)
			listener.onCompleted(null);

	}

	private void filterDate(Calendar start, Calendar end) {
		mDisplayElements.clear();
		mDateElements.clear();

		for (Element e : mAllElements) {

			Calendar date = getElementDate(e);

			if (end != null && date.after(end))
				continue;
			if (start != null && date.before(start))
				continue;

			mDateElements.add(e);
			mDisplayElements.add(e);
		}

		Collections.sort(mDisplayElements, new Comparator<Element>() {

			@Override
			public int compare(Element lhs, Element rhs) {
				Calendar ldate = getElementDate(lhs);
				Calendar rdate = getElementDate(rhs);
				
				return ldate.compareTo(rdate);
			}
		});

		mAdapter.notifyDataSetChanged();
		bindAbsense();
	}

	private Calendar getElementDate(Element e) {
		Calendar date = null;

		// TODO 這裡要判斷 element 的類型
		if (e.getNodeName().equals("Record")) {
			String dateString = XmlUtil.getElementText(e, "InterviewDate");
			date = StringUtil.parseToCalendar(dateString);
		} else if (e.getNodeName().equals("MeetingRecord")) {
			String dateString = XmlUtil.getElementText(e, "MeetingDate");
			date = StringUtil.parseToCalendar(dateString);
		} else if (e.getNodeName().equals("CareRecord")) {
			String dateString = XmlUtil.getElementText(e, "FileDate");
			date = StringUtil.parseToCalendar(dateString);
		}
		return date;
	}

	private void bindAbsense() {

		int countAll = 0, countInterview = 0, countMeeting = 0, countCare = 0;

		for (Element e : mDisplayElements) {

			// TODO 這裡要判斷 element 的類型來調整統計
			if (e.getNodeName().equals("Record")) {
				countInterview++;

			} else if (e.getNodeName().equals("MeetingRecord")) {
				countMeeting++;
			} else if (e.getNodeName().equals("CareRecord")) {
				countCare++;
			}
			countAll++;
		}

		mTxtCountAll.setText(String.valueOf(countAll));
		mContainerAll.setTag(COUNSEL_ALL);
		mContainerAll.setOnClickListener(mTypeClickListener);

		mTxtInterviewCount.setText(String.valueOf(countInterview));
		mContainerInterview.setTag(COUNSEL_INTERVIEW);
		mContainerInterview.setOnClickListener(mTypeClickListener);

		mTxtMeetingCount.setText(String.valueOf(countMeeting));
		mContainerMeeting.setTag(COUNSEL_MEETING);
		mContainerMeeting.setOnClickListener(mTypeClickListener);

		mTxtCareCount.setText(String.valueOf(countCare));
		mContainerCare.setTag(COUNSEL_CARE);
		mContainerCare.setOnClickListener(mTypeClickListener);

		changeSelectedType(mContainerAll);

	}

	private void changeSelectedType(View selectedView) {
		for (int i = 0; i < mContainerCounselType.getChildCount(); i++) {
			View child = mContainerCounselType.getChildAt(i);
			if (child != selectedView) {
				child.setBackgroundResource(R.drawable.back);
			} else {
				child.setBackgroundResource(R.drawable.back2);
			}
		}

		mCurrentSelectedType = (String) selectedView.getTag();
		final String allString = mActivity.getString(R.string.counsel_all);

		mDisplayElements.clear();
		if (mCurrentSelectedType.equals(allString)) {
			mDisplayElements.addAll(mDateElements);
		} else {
			for (Element dateElement : mDateElements) {
				// TODO 這裡要判斷 element 的類型來調整統計
				if (mCurrentSelectedType.equals(COUNSEL_INTERVIEW)
						&& dateElement.getNodeName().equals("Record"))
					mDisplayElements.add(dateElement);
				else if (mCurrentSelectedType.equals(COUNSEL_MEETING)
						&& dateElement.getNodeName().equals("MeetingRecord"))
					mDisplayElements.add(dateElement);
				else if (mCurrentSelectedType.equals(COUNSEL_CARE)
						&& dateElement.getNodeName().equals("CareRecord"))
					mDisplayElements.add(dateElement);
			}
		}

		mAdapter.notifyDataSetChanged();
	}

	private class CounselAdapter extends BaseAdapter {
		private LayoutInflater _inflater;

		public CounselAdapter() {
			_inflater = (LayoutInflater) mActivity
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public int getCount() {
			return mDisplayElements.size();
		}

		@Override
		public Object getItem(int arg0) {
			return mDisplayElements.get(arg0);
		}

		@Override
		public long getItemId(int arg0) {
			return arg0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup arg2) {

			Element element = mDisplayElements.get(position);

			// 判斷 element 的類型
			if (element.getNodeName().equals("Record")) {
				convertView = _inflater.inflate(R.layout.item_interview, null);
				TextView txtInterviewNo = (TextView) convertView
						.findViewById(R.id.txtInterviewNo);
				TextView txtInterviewDate = (TextView) convertView
						.findViewById(R.id.txtInterviewDate);
				TextView txtPlace = (TextView) convertView
						.findViewById(R.id.txtPlace);
				TextView txtGradeYear = (TextView) convertView
						.findViewById(R.id.txtGradeYear);
				TextView txtClassName = (TextView) convertView
						.findViewById(R.id.txtClassName);
				TextView txtStudentName = (TextView) convertView
						.findViewById(R.id.txtStudentName);
				TextView txtTeacherName = (TextView) convertView
						.findViewById(R.id.txtTeacherName);
				TextView txtInterviewType = (TextView) convertView
						.findViewById(R.id.txtInterviewType);
				TextView txtIntervieweeType = (TextView) convertView
						.findViewById(R.id.txtIntervieweeType);
				TextView txtCause = (TextView) convertView
						.findViewById(R.id.txtCause);
				TextView txtAttendees = (TextView) convertView
						.findViewById(R.id.txtAttendees);
				TextView txtCounselType = (TextView) convertView
						.findViewById(R.id.txtCounselType);
				TextView txtCounselTypeKind = (TextView) convertView
						.findViewById(R.id.txtCounselTypeKind);
				TextView txtContentDigest = (TextView) convertView
						.findViewById(R.id.txtContentDigest);
				TextView txtAuthor = (TextView) convertView
						.findViewById(R.id.txtAuthor);

				txtGradeYear.setText(XmlUtil.getElementText(mStudentElement,
						"GradeYear"));
				txtClassName.setText(XmlUtil.getElementText(mStudentElement,
						"ClassName"));
				txtStudentName.setText(XmlUtil.getElementText(mStudentElement,
						"StudentName"));

				txtInterviewNo.setText(XmlUtil.getElementText(element,
						"InterviewNo"));

				txtTeacherName.setText(XmlUtil.getElementText(element,
						"TeacherName"));
				txtInterviewType.setText(XmlUtil.getElementText(element,
						"InterviewType"));
				txtIntervieweeType.setText(XmlUtil.getElementText(element,
						"IntervieweeType"));

				txtCause.setText(XmlUtil.getElementText(element, "Cause"));
				txtContentDigest.setText(XmlUtil.getElementText(element,
						"ContentDigest"));

				txtInterviewDate.setText(quoteData(element, "InterviewDate",
						"InterviewTime"));

				String place = XmlUtil.getElementText(element, "Place");
				place = StringUtil.isNullOrWhitespace(place) ? "--" : place;
				txtPlace.setText(place);

				txtAuthor.setText(quoteData(element, "AuthorName", "AuthorID"));

				txtAttendees.setText(appendString(XmlUtil.selectElement(
						element, "Attendees")));
				txtCounselType.setText(appendString(XmlUtil.selectElement(
						element, "CounselType")));
				txtCounselTypeKind.setText(appendString(XmlUtil.selectElement(
						element, "CounselTypeKind")));

			} else if (element.getNodeName().equals("MeetingRecord")) {

				convertView = _inflater.inflate(R.layout.item_meeting, null);

				TextView txtNo = (TextView) convertView
						.findViewById(R.id.txtNo);
				TextView txtDate = (TextView) convertView
						.findViewById(R.id.txtDate);
				TextView txtPlace = (TextView) convertView
						.findViewById(R.id.txtPlace);
				TextView txtGradeYear = (TextView) convertView
						.findViewById(R.id.txtGradeYear);
				TextView txtClassName = (TextView) convertView
						.findViewById(R.id.txtClassName);
				TextView txtStudentName = (TextView) convertView
						.findViewById(R.id.txtStudentName);
				TextView txtCause = (TextView) convertView
						.findViewById(R.id.txtCause);
				TextView txtAttendees = (TextView) convertView
						.findViewById(R.id.txtAttendees);
				TextView txtCounselType = (TextView) convertView
						.findViewById(R.id.txtCounselType);
				TextView txtCounselTypeKind = (TextView) convertView
						.findViewById(R.id.txtCounselTypeKind);
				TextView txtContentDigest = (TextView) convertView
						.findViewById(R.id.txtContentDigest);
				TextView txtAuthor = (TextView) convertView
						.findViewById(R.id.txtAuthor);

				txtGradeYear.setText(XmlUtil.getElementText(mStudentElement,
						"GradeYear"));
				txtClassName.setText(XmlUtil.getElementText(mStudentElement,
						"ClassName"));
				txtStudentName.setText(XmlUtil.getElementText(mStudentElement,
						"StudentName"));

				txtNo.setText(XmlUtil.getElementText(element, "CaseNo"));

				txtCause.setText(XmlUtil.getElementText(element, "Cause"));
				txtContentDigest.setText(XmlUtil.getElementText(element,
						"ContentDigest"));
				txtDate.setText(quoteData(element, "MeetingDate", "MeetingTime"));

				String place = XmlUtil.getElementText(element, "Place");
				place = StringUtil.isNullOrWhitespace(place) ? "--" : place;
				txtPlace.setText(place);

				txtAuthor.setText(quoteData(element, "AuthorName", "AuthorID"));

				txtAttendees.setText(appendString(XmlUtil.selectElement(
						element, "Attendees")));
				txtCounselType.setText(appendString(XmlUtil.selectElement(
						element, "CounselType")));
				txtCounselTypeKind.setText(appendString(XmlUtil.selectElement(
						element, "CounselTypeKind")));
			} else if (element.getNodeName().equals("CareRecord")) {

				convertView = _inflater.inflate(R.layout.item_care, null);

				TextView txtNo = (TextView) convertView
						.findViewById(R.id.txtNo);
				TextView txtDate = (TextView) convertView
						.findViewById(R.id.txtDate);
				TextView txtGradeYear = (TextView) convertView
						.findViewById(R.id.txtGradeYear);
				TextView txtClassName = (TextView) convertView
						.findViewById(R.id.txtClassName);
				TextView txtStudentName = (TextView) convertView
						.findViewById(R.id.txtStudentName);
				TextView txtCategory = (TextView) convertView
						.findViewById(R.id.txtCategory);
				TextView txtOrigin = (TextView) convertView
						.findViewById(R.id.txtOrigin);
				TextView txtSuperiority = (TextView) convertView
						.findViewById(R.id.txtSuperiority);
				TextView txtWeakness = (TextView) convertView
						.findViewById(R.id.txtWeakness);
				TextView txtAuthor = (TextView) convertView
						.findViewById(R.id.txtAuthor);
				TextView txtGoal = (TextView) convertView
						.findViewById(R.id.txtGoal);
				TextView txtOtherInstitude = (TextView) convertView
						.findViewById(R.id.txtOtherInstitude);
				TextView txtType = (TextView) convertView
						.findViewById(R.id.txtType);
				TextView txtAssistMatter = (TextView) convertView
						.findViewById(R.id.txtAssistMatter);

				txtGradeYear.setText(XmlUtil.getElementText(mStudentElement,
						"GradeYear"));
				txtClassName.setText(XmlUtil.getElementText(mStudentElement,
						"ClassName"));
				txtStudentName.setText(XmlUtil.getElementText(mStudentElement,
						"StudentName"));

				txtNo.setText(XmlUtil.getElementText(element, "CodeName"));
				txtCategory.setText(quoteData(element, "CaseCategory",
						"CaseCategoryRemark"));
				txtOrigin.setText(quoteData(element, "CaseOrigin",
						"CaseOriginRemark"));
				txtSuperiority.setText(XmlUtil.getElementText(element,
						"Superiority"));
				txtWeakness
						.setText(XmlUtil.getElementText(element, "Weakness"));
				txtGoal.setText(XmlUtil.getElementText(element, "CounselGoal"));
				txtOtherInstitude.setText(XmlUtil.getElementText(element,
						"OtherInstitute"));
				txtType.setText(XmlUtil.getElementText(element, "CounselType"));
				txtAssistMatter.setText(XmlUtil.getElementText(element,
						"AssistedMatter"));
				txtDate.setText(XmlUtil.getElementText(element, "FileDate"));
				txtAuthor.setText(quoteData(element, "AuthorName", "AuthorID"));
			}
			return convertView;
		}

		@Override
		public boolean isEnabled(int position) {
		    return false;
		}
		
		private String appendString(Element e) {
			StringBuilder sb = new StringBuilder();

			for (Element item : XmlUtil.selectElements(e, "Item")) {
				String name = item.getAttribute("name");
				if (sb.length() > 0)
					sb.append("、");
				sb.append(name);
			}

			return sb.toString();
		}

		private String quoteData(Element e, String name, String remark) {
			String result = XmlUtil.getElementText(e, name);
			String memo = XmlUtil.getElementText(e, remark);
			if (!StringUtil.isNullOrWhitespace(memo)) {
				String tmp = "%s ( %s )";
				result = String.format(tmp, result, memo);
			}
			return result;
		}
	}
}
