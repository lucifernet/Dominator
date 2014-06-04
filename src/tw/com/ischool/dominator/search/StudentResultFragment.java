package tw.com.ischool.dominator.search;

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
import tw.com.ischool.dominator.student.StudentActivity;
import tw.com.ischool.dominator.util.CacheHelper;
import tw.com.ischool.dominator.util.StringUtil;
import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

public class StudentResultFragment extends Fragment {

	private static final String KEYWORD = "Keyword";
	private static final String RESULT = "Result";

//	private PullToRefreshLayout mPullToRefreshLayout;
	private GridView mStudentGridView;
	private TextView mTxtStudentCount;
	private TextView mTxtSearchTitle;
	private Activity mActivity;
	private String mKeyword;
	private ArrayAdapter<Element> mStudentAdapter;
	private ArrayList<Element> mStudentList = new ArrayList<Element>();
	private HashMap<String, Bitmap> mStudentPhotos = new HashMap<String, Bitmap>();
	private Element mSearchResult;

//	private OnRefreshListener mRefreshListener = new OnRefreshListener() {
//
//		@Override
//		public void onRefreshStarted(View view) {
//			Element content = XmlUtil.createElement("Request");
//			Element orElement = XmlUtil.addElement(content, "Or");
//			XmlUtil.addElement(orElement, "StudentName", mKeyword);
//			XmlUtil.addElement(orElement, "StudentNumber", mKeyword);
//
//			DSS.sendRequest(mActivity, "student.LookupStudent",
//					new OnReceiveListener<DSResponse>() {
//
//						@Override
//						public void onReceive(DSResponse result) {
//							bindStudents(result);
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

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_student_result,
				container, false);

		// Retrieve the PullToRefreshLayout from the content view
//		mPullToRefreshLayout = (PullToRefreshLayout) view
//				.findViewById(R.id.ptr_search_student);
		mStudentGridView = (GridView) view.findViewById(R.id.search_container);
		mTxtSearchTitle = (TextView) view.findViewById(R.id.search_title);
		mTxtStudentCount = (TextView) view.findViewById(R.id.search_count);

		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		mActivity = getActivity();
		mKeyword = getArguments().getString(SearchManager.QUERY);

		mStudentAdapter = new StudentAdapter(mActivity,
				R.layout.item_grid_student, mStudentList);
		mStudentGridView.setAdapter(mStudentAdapter);
		mStudentGridView.setOnItemClickListener(mStudentSelectedListener);

//		ActionBarPullToRefresh.from(this.getActivity())
//				.allChildrenArePullable().listener(mRefreshListener)
//				.setup(mPullToRefreshLayout);

		if (savedInstanceState != null) {
			mKeyword = savedInstanceState.getString(KEYWORD);
			String resultString = savedInstanceState.getString(RESULT);

			if (!StringUtil.isNullOrWhitespace(resultString))
				mSearchResult = XmlHelper.parseXml(resultString);
		}

		if (mSearchResult == null) {
			reload();
		} else {
			bindStudents(mSearchResult);
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putString(KEYWORD, mKeyword);

		if (mSearchResult != null) {
			outState.putString(RESULT, XmlHelper.convertToString(mSearchResult));
		}
		super.onSaveInstanceState(outState);
	}

	private void reload() {
		final ProgressDialog dialog = new ProgressDialog(mActivity);
		dialog.setTitle(mActivity.getString(R.string.progress_title));
		dialog.setMessage(mActivity.getString(R.string.search_progress_dialog));
		dialog.setCancelable(false);

		// final ProgressDialog dialog = ProgressDialog.show(mActivity,
		// mActivity.getString(R.string.progress_title),
		// mActivity.getString(R.string.search_progress_dialog));

		Element content = XmlUtil.createElement("Request");
		Element orElement = XmlUtil.addElement(content, "Or");
		XmlUtil.addElement(orElement, "StudentName", mKeyword);
		XmlUtil.addElement(orElement, "StudentNumber", mKeyword);

		final Cancelable cancelable = new Cancelable();

		final DSATask<DSRequest> task = DSS.sendRequest(mActivity,
				"student.LookupStudent", content,
				new OnReceiveListener<DSResponse>() {

					@Override
					public void onReceive(DSResponse result) {
						bindStudents(result);
						dialog.dismiss();
					}

					@Override
					public void onError(Exception e) {
						showLog("Error occured when getting students: "
								+ e.toString());
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
		bindStudents(response.getContent());
	}

	private void bindStudents(Element result) {
		mStudentList.clear();
		mStudentPhotos.clear();

		mSearchResult = result;
		mStudentList.addAll(XmlUtil.selectElements(mSearchResult, "Student"));

		Collections.sort(mStudentList, new Comparator<Element>() {

			@Override
			public int compare(Element lhs, Element rhs) {
				int lgrade = Converter.toInteger(XmlUtil.getElementText(lhs, "GradeYear"), 0);
				int rgrade = Converter.toInteger(XmlUtil.getElementText(rhs, "GradeYear"), 0);
				int gradeCompare = Integer.compare(lgrade, rgrade);
				if(gradeCompare != 0) return gradeCompare;
				
				String lname = XmlUtil.getElementText(lhs, "ClassName");
				String rname = XmlUtil.getElementText(rhs, "ClassName");
				int nameCompare = lname.compareTo(rname);
				if(nameCompare != 0) 
					return nameCompare;
				
				int lno = Converter.toInteger(XmlUtil.getElementText(lhs, "SeatNo"), Integer.MAX_VALUE);
				int rno = Converter.toInteger(XmlUtil.getElementText(rhs, "SeatNo"), Integer.MAX_VALUE);
				return Integer.compare(lno, rno);
			}
		});
		
		String searchCount = mActivity.getString(R.string.search_result_count);
		searchCount = String.format(searchCount,
				String.valueOf(mStudentList.size()));
		mTxtStudentCount.setText(searchCount);

		String searchTitle = mActivity
				.getString(R.string.search_student_result);
		searchTitle = String.format(searchTitle, mKeyword);
		mTxtSearchTitle.setText(searchTitle);

		mStudentAdapter.notifyDataSetChanged();
	}

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

			ImageDownloaderTask task = new ImageDownloaderTask(holder.imgPhoto,
					position);
			task.execute(studentId);

			return convertView;
		}
	}

	private class StudentViewHolder {
		public ImageView imgPhoto;
		public TextView txtStudentName;
		public TextView txtClassName;
		public Element element;
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
			DSResponse response = cc.sendRequest("student.GetPhoto", request, new Cancelable());
			Element body = response.getBody();
			String photoString = XmlUtil.getElementText(body, "PhotoString");

			if (!StringUtil.isNullOrWhitespace(photoString)) {
				Bitmap decodedByte = StringUtil.toBitmap(photoString);

				mStudentPhotos.put(studentId, decodedByte);
				CacheHelper.cacheImage(mActivity, studentId, decodedByte);

				return decodedByte;
			} else {
				return null;
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
						imageView.setImageDrawable(imageView.getContext()
								.getResources()
								.getDrawable(R.drawable.ic_student));
					}
				}
			}
		}
	}
}
