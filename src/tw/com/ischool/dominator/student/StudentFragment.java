package tw.com.ischool.dominator.student;

import ischool.dsa.client.ContractConnection;
import ischool.dsa.utility.Converter;
import ischool.dsa.utility.DSRequest;
import ischool.dsa.utility.DSResponse;
import ischool.dsa.utility.XmlHelper;
import ischool.dsa.utility.XmlUtil;
import ischool.dsa.utility.http.Cancelable;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import org.w3c.dom.Element;

import tw.com.ischool.dominator.R;
import tw.com.ischool.dominator.main.MainActivity;
import tw.com.ischool.dominator.model.DSATask;
import tw.com.ischool.dominator.model.DSS;
import tw.com.ischool.dominator.model.OnReceiveListener;
import tw.com.ischool.dominator.util.CacheHelper;
import tw.com.ischool.dominator.util.StringUtil;
import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

public class StudentFragment extends Fragment {

	public static final String STATE_GRADE = "gradeyear";
	public static final String STATE_CLASS = "class";
	public static final String STATE_CLASS_CONTENT = "class_content";

	private Element mClassContent;
	private String mClassSelectedName;
	private String mGradeSelectedName;
//	private PullToRefreshLayout mPullToRefreshLayout;
	private Activity mActivity;
	private ArrayList<String> mGradeList = new ArrayList<String>();
	private List<Element> mAllClassList = new ArrayList<Element>();
	private ArrayList<String> mClassList = new ArrayList<String>();
	private ArrayList<Element> mClassElements = new ArrayList<Element>();
	private ArrayList<Element> mStudentList = new ArrayList<Element>();
	private Spinner mGradeSpinner;
	private Spinner mClassSpinner;
	private GridView mStudentGridView;
	private ArrayAdapter<String> mGradeAdapter;
	private ArrayAdapter<String> mClassAdapter;
	private ArrayAdapter<Element> mStudentAdapter;
	private HashMap<String, Bitmap> mStudentPhotos = new HashMap<String, Bitmap>();
	private Bitmap mPhotoNotFound;

	private OnItemSelectedListener mGradeSelectedListener = new OnItemSelectedListener() {

		@Override
		public void onItemSelected(AdapterView<?> parent, View view,
				int position, long itemId) {

			mClassElements.clear();
			mClassList.clear();

			if (position == 0) {
				mClassElements.addAll(mAllClassList);
			} else {
				String grade = mGradeSpinner.getSelectedItem().toString();

				mClassElements.clear();
				for (Element e : mAllClassList) {
					String g = XmlUtil.getElementText(e, "GradeYear");
					if (grade.equals(g)) {
						mClassElements.add(e);
					}
				}
			}

			for (Element e : mClassElements) {
				String className = XmlUtil.getElementText(e, "ClassName");
				mClassList.add(className);
			}

			if (mClassElements.size() > 0) {
				setClassAdapter();
				mClassAdapter.notifyDataSetChanged();

				boolean found = false;
				int index = 0;
				if (!StringUtil.isNullOrWhitespace(mClassSelectedName)) {
					for (String className : mClassList) {
						if (className.equals(mClassSelectedName)) {
							found = true;
							break;
						}
						index++;
					}
				}

				if (!found)
					index = 0;

				mClassSpinner.setSelection(index);
			}
			// if(mClassElements.size() > 0) {
			// Element classElement = mClassElements.get(0);
			// String classID = XmlUtil.getElementText(classElement, "ClassID");
			// loadStudents(classID);
			// }
		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {

		}
	};

	private OnItemSelectedListener mClassSelectedListener = new OnItemSelectedListener() {

		@Override
		public void onItemSelected(AdapterView<?> parent, View view,
				int position, long itemId) {
			Element classElement = mClassElements.get(position);
			String classID = XmlUtil.getElementText(classElement, "ClassID");
			mClassSelectedName = XmlUtil.getElementText(classElement,
					"ClassName");

			loadStudents(classID);
		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {

		}
	};

	private OnItemClickListener mStudentSelectedListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long itemId) {
			StudentViewHolder holder = (StudentViewHolder) view.getTag();
			Bitmap photo = ((BitmapDrawable) holder.imgPhoto.getDrawable())
					.getBitmap();

			Intent intent = new Intent(mActivity, StudentActivity.class);
			intent.putExtra(StudentActivity.PARAM_STUDENT,
					XmlHelper.convertToString(holder.element));
			intent.putExtra(StudentActivity.PARAM_PHOTO, photo);
			startActivity(intent);
		}
	};

//	private OnRefreshListener mRefreshListener = new OnRefreshListener() {
//
//		@Override
//		public void onRefreshStarted(View view) {
//			DSS.sendRequest(mActivity, "student.GetClassList",
//					new OnReceiveListener<DSResponse>() {
//
//						@Override
//						public void onReceive(DSResponse result) {
//							mClassContent = result.getBody();
//
//							bindClass();
//							mPullToRefreshLayout.setRefreshComplete();
//						}
//
//						@Override
//						public void onError(Exception e) {
//							showLog("Error occured : " + e.toString());
//							mPullToRefreshLayout.setRefreshComplete();
//						}
//					}, new Cancelable());
//		}
//	};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.fragment_student, container,
				false);

		// Retrieve the PullToRefreshLayout from the content view
//		mPullToRefreshLayout = (PullToRefreshLayout) view
//				.findViewById(R.id.carddemo_extra_ptr_layout);

		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		mActivity = getActivity();

		mPhotoNotFound = BitmapFactory.decodeResource(mActivity.getResources(),
				R.drawable.ic_student);

		mGradeSpinner = (Spinner) mActivity.findViewById(R.id.spinnerGrade);
		mClassSpinner = (Spinner) mActivity.findViewById(R.id.spinnerClass);
		mStudentGridView = (GridView) mActivity.findViewById(R.id.gridStudent);

		mGradeAdapter = new ArrayAdapter<String>(mActivity,
				android.R.layout.simple_spinner_item, android.R.id.text1,
				mGradeList);
		mGradeAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mGradeSpinner.setOnItemSelectedListener(mGradeSelectedListener);
		mGradeSpinner.setAdapter(mGradeAdapter);

		// setClassAdapter();
		// mClassAdapter = new ArrayAdapter<String>(mActivity,
		// android.R.layout.simple_spinner_item, android.R.id.text1,
		// mClassList);
		// mClassAdapter
		// .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// mClassSpinner.setOnItemSelectedListener(mClassSelectedListener);
		// mClassSpinner.setAdapter(mClassAdapter);

		mStudentAdapter = new StudentAdapter(mActivity,
				R.layout.item_grid_student, mStudentList);
		mStudentGridView.setAdapter(mStudentAdapter);
		mStudentGridView.setOnItemClickListener(mStudentSelectedListener);

		if (savedInstanceState != null) {
			mGradeSelectedName = savedInstanceState.getString(STATE_GRADE);
			mClassSelectedName = savedInstanceState.getString(STATE_CLASS);
			mClassContent = XmlHelper.parseXml(savedInstanceState
					.getString(STATE_CLASS_CONTENT));

			bindClass();

			// if (!StringUtil.isNullOrWhitespace(mGradeSelectedName)) {
			// int index = findItemIndex(mGradeList, mGradeSelectedName);
			// mGradeSpinner.setSelection(index);
			// }
			// if (!StringUtil.isNullOrWhitespace(mClassSelectedName)) {
			// int index = findItemIndex(mClassList, mClassSelectedName);
			// mClassSpinner.setSelection(index);
			// }
		} else {
			reload();
		}

//		ActionBarPullToRefresh.from(mActivity).allChildrenArePullable()
//				.listener(mRefreshListener).setup(mPullToRefreshLayout);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		String className = StringUtil.EMPTY;
		if (mClassSpinner.getSelectedItem() != null)
			className = mClassSpinner.getSelectedItem().toString();

		String gradeName = StringUtil.EMPTY;
		if (mGradeSpinner.getSelectedItem() != null)
			gradeName = mGradeSpinner.getSelectedItem().toString();

		outState.putString(STATE_CLASS, className);
		outState.putString(STATE_GRADE, gradeName);
		outState.putString(STATE_CLASS_CONTENT,
				XmlHelper.convertToString(mClassContent));

		super.onSaveInstanceState(outState);
	}

	private void reload() {
		// final ProgressDialog dialog = ProgressDialog.show(mActivity,
		// mActivity.getString(R.string.progress_title),
		// mActivity.getString(R.string.student_loading_class));

		final ProgressDialog dialog = new ProgressDialog(mActivity);
		dialog.setTitle(mActivity.getString(R.string.progress_title));
		dialog.setMessage(mActivity.getString(R.string.student_loading_class));
		dialog.setCancelable(false);
		final Cancelable cancelable = new Cancelable();

		final DSATask<DSRequest> task = DSS.sendRequest(mActivity,
				"student.GetClassList", new OnReceiveListener<DSResponse>() {

					@Override
					public void onReceive(DSResponse result) {
						mClassContent = result.getBody();						
						bindClass();
						dialog.dismiss();
					}

					@Override
					public void onError(Exception e) {
						showLog("Error occured : " + e.toString());
						dialog.dismiss();
					}
				}, cancelable);
		dialog.setButton(DialogInterface.BUTTON_NEGATIVE,
				getString(R.string.cancel), new OnClickListener() {

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

	private void setClassAdapter() {
		mClassAdapter = new ArrayAdapter<String>(mActivity,
				android.R.layout.simple_spinner_item, android.R.id.text1,
				mClassList);
		mClassAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mClassSpinner.setOnItemSelectedListener(mClassSelectedListener);
		mClassSpinner.setAdapter(mClassAdapter);
	}

	private void loadStudents(String classid) {
		final ProgressDialog dialog = new ProgressDialog(mActivity);
		dialog.setTitle(mActivity.getString(R.string.progress_title));
		dialog.setMessage(mActivity.getString(R.string.student_loading_student));
		dialog.setCancelable(false);

		Element content = XmlUtil.createElement("Request");
		XmlUtil.addElement(content, "ClassID", classid);
		XmlUtil.addElement(content, "RecordCount", "200");

		final Cancelable cancelable = new Cancelable();

		final DSATask<DSRequest> task = DSS.sendRequest(mActivity,
				"student.LookupStudent", content,
				new OnReceiveListener<DSResponse>() {

					@Override
					public void onReceive(DSResponse result) {
						bindStudents(result);

						TextView emptyText = (TextView) mActivity
								.findViewById(R.id.emptyResults);
						mStudentGridView.setEmptyView(emptyText);

						dialog.dismiss();
					}

					@Override
					public void onError(Exception e) {
						showLog("Error occured when getting students: "
								+ e.toString());
						TextView emptyText = (TextView) mActivity
								.findViewById(R.id.emptyResults);
						mStudentGridView.setEmptyView(emptyText);

						dialog.dismiss();
					}
				}, cancelable);

		dialog.setButton(DialogInterface.BUTTON_NEGATIVE,
				getString(R.string.cancel), new OnClickListener() {

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

	private void bindStudents(DSResponse response) {
		// TODO bind student
		mStudentList.clear();
		mStudentPhotos.clear();

		Element content = response.getContent();
		mStudentList.addAll(XmlUtil.selectElements(content, "Student"));

		Collections.sort(mStudentList, new Comparator<Element>() {

			@Override
			public int compare(Element lhs, Element rhs) {
				int lno = Converter.toInteger(XmlUtil.getElementText(lhs, "SeatNo"), Integer.MAX_VALUE);
				int rno = Converter.toInteger(XmlUtil.getElementText(rhs, "SeatNo"), Integer.MAX_VALUE);
				return Integer.compare(lno, rno);
			}
		});
		
		mStudentAdapter.notifyDataSetChanged();
	}

	private void bindClass() {
		mClassList.clear();
		mGradeList.clear();

		mGradeList.add(mActivity.getString(R.string.student_all_grade));

		mAllClassList = XmlUtil.selectElements(mClassContent, "Class");

		Collections.sort(mAllClassList, new Comparator<Element>() {

			@Override
			public int compare(Element lhs, Element rhs) {
				// TODO Auto-generated method stub

				String lhstring = XmlHelper.convertToString(lhs, true);
				Log.d(MainActivity.TAG, lhstring);

				String lgrade = XmlUtil.getElementText(lhs, "GradeYear");
				String rgrade = XmlUtil.getElementText(rhs, "GradeYear");

				int gradeCompareResult = lgrade.compareTo(rgrade);
				if (gradeCompareResult != 0)
					return gradeCompareResult;

				int lorder = Converter.toInteger(
						XmlUtil.getElementText(lhs, "display_order"), 0);
				int rorder = Converter.toInteger(
						XmlUtil.getElementText(rhs, "display_order"), 0);
				
				if(lorder != rorder){
					return Integer.compare(lorder, rorder);
				}
				
				String lname = XmlUtil.getElementText(lhs, "ClassName");
				String rname = XmlUtil.getElementText(rhs, "ClassName");
				return lname.compareTo(rname);				
			}
		});

		for (Element e : mAllClassList) {
			String grade = XmlUtil.getElementText(e, "GradeYear");

			if (!mGradeList.contains(grade))
				mGradeList.add(grade);

			String className = XmlUtil.getElementText(e, "ClassName");
			mClassList.add(className);
		}

		mGradeAdapter.notifyDataSetChanged();

		if (!StringUtil.isNullOrWhitespace(mGradeSelectedName)) {
			int index = 0;
			for (String grade : mGradeList) {
				if (grade.equals(mGradeSelectedName)) {
					mGradeSpinner.setSelection(index);
					break;
				}
				index++;
			}
		}
		// mClassAdapter.notifyDataSetChanged();
	}

	// private int findItemIndex(ArrayList<String> list, String itemName) {
	// for (int i = 0; i < list.size(); i++) {
	// String str = list.get(i);
	// if (str.equals(itemName))
	// return i;
	// }
	// return 0;
	// }

	private void showLog(String msg) {
		Log.d(MainActivity.TAG, msg);
	}

	private class StudentAdapter extends ArrayAdapter<Element> {
		private int _resource;
		private LayoutInflater _inflator;

		public StudentAdapter(Context context, int resource,
				List<Element> objects) {
			super(context, resource, objects);

			_resource = resource;
			_inflator = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			StudentViewHolder holder;
			if (convertView == null) {
				convertView = _inflator.inflate(_resource, null);
				holder = new StudentViewHolder();
				holder.txtStudentName = (TextView) convertView
						.findViewById(R.id.txtName);
				holder.txtClassName = (TextView) convertView
						.findViewById(R.id.txtClass);
				holder.imgPhoto = (ImageView) convertView
						.findViewById(R.id.imgPhoto);

				convertView.setTag(holder);
			} else {
				holder = (StudentViewHolder) convertView.getTag();
			}

			holder.element = this.getItem(position);
						
			String studentName = XmlUtil.getElementText(holder.element,
					"StudentName");
			String className = XmlUtil.getElementText(holder.element,
					"ClassName");
			
			String seatNo = XmlUtil.getElementText(holder.element, "SeatNo");
			if(!StringUtil.isNullOrWhitespace(seatNo)){
				className = className + " ( " + seatNo + " )";
			}
			
			final String studentId = XmlUtil.getElementText(holder.element,
					"StudentID");

			holder.txtStudentName.setText(studentName);
			holder.txtClassName.setText(className);

			// TODO loading student photo
			if (mStudentPhotos.containsKey(studentId)) {
				holder.imgPhoto.setImageBitmap(mStudentPhotos.get(studentId));
			} else {
				ImageDownloaderTask task = new ImageDownloaderTask(
						holder.imgPhoto, position);
				task.execute(studentId);
			}
			return convertView;
		}
	}

	private class ImageDownloaderTask extends AsyncTask<String, Void, Bitmap> {
		private final WeakReference<ImageView> _imageViewReference;
		private int _position;

		public ImageDownloaderTask(ImageView imageView, int position) {
			_imageViewReference = new WeakReference<ImageView>(imageView);
			_position = position;
		}

		@Override
		protected void onPreExecute() {
			if (_imageViewReference != null) {
				ImageView imageView = _imageViewReference.get();
				if (imageView != null) {
					imageView.setImageBitmap(null);
				}
			}
		}

		@Override
		// Actual download method, run in the task thread
		protected Bitmap doInBackground(String... params) {
			String studentId = params[0];

			if (mStudentPhotos.containsKey(studentId))
				return mStudentPhotos.get(studentId);

			Bitmap cacheImage = CacheHelper.loadImage(mActivity, studentId);
			if (cacheImage != null) {
				mStudentPhotos.put(studentId, cacheImage);
				return cacheImage;
			}

			Element req = XmlUtil.createElement("Request");
			XmlUtil.addElement(req, "StudentID", studentId);
			DSRequest request = new DSRequest();
			request.setContent(req);

			ContractConnection cc = DSS.getLastConnection();
			DSResponse response = cc.sendRequest("student.GetPhoto", request,
					new Cancelable());
			Element body = response.getBody();
			String photoString = XmlUtil.getElementText(body, "PhotoString");

			if (!StringUtil.isNullOrWhitespace(photoString)) {
				Bitmap decodedByte = StringUtil.toBitmap(photoString);

				mStudentPhotos.put(studentId, decodedByte);
				CacheHelper.cacheImage(mActivity, studentId, decodedByte);

				return decodedByte;
			} else {
				mStudentPhotos.put(studentId, mPhotoNotFound);
				return mPhotoNotFound;
			}
		}

		@Override
		// Once the image is downloaded, associates it to the imageView
		protected void onPostExecute(Bitmap bitmap) {
			if (isCancelled()) {
				bitmap = null;
			}

			int start = mStudentGridView.getFirstVisiblePosition();
			int end = mStudentGridView.getLastVisiblePosition();
			if (_position < start || _position > end) {
				bitmap = null;
			}

			if (_imageViewReference != null) {
				ImageView imageView = _imageViewReference.get();
				if (imageView != null) {

					if (bitmap != null) {
						imageView.setImageBitmap(bitmap);
					} else {
						imageView.setImageBitmap(mPhotoNotFound);
					}
				}
			}
		}
	}

	private class StudentViewHolder {
		public ImageView imgPhoto;
		public TextView txtStudentName;
		public TextView txtClassName;
		public Element element;
	}
}
