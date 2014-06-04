package tw.com.ischool.dominator.model;

import ischool.dsa.utility.DSRequest;
import ischool.dsa.utility.DSResponse;

import org.w3c.dom.Element;

import tw.com.ischool.dominator.R;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.AsyncTask;

public class RequestInterface {

	private ProgressDialog mDialog;
	private Context mContext;
	private OnReceiveListener<DSResponse> mListener;
	private AsyncTask<DSRequest, Integer, DSResponse> mTask;

	private RequestInterface(Context context, int titleId, int messageId,
			String serviceName, Element content) {
		mDialog = new ProgressDialog(mContext);
		mDialog.setTitle(mContext.getString(titleId));
		mDialog.setMessage(mContext.getString(messageId));
		mDialog.setCancelable(false);
		mDialog.setButton(DialogInterface.BUTTON_NEGATIVE,
				mContext.getString(R.string.cancel), new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();

						if (mTask != null)
							mTask.cancel(true);

						if (mListener != null)
							mListener.onCanceled();
					}
				});

	}

	public void setListener(OnReceiveListener<DSResponse> listener) {
		mListener = listener;
	}

	public void start() {
		mDialog.show();

	}

	public void cancel() {

	}

//	public static RequestInterface create(Context context, int titleId,
//			int messageId, String serviceName, Element content) {
//		final ProgressDialog dialog = new ProgressDialog(context);
//		dialog.setTitle(context.getString(titleId));
//		dialog.setMessage(context.getString(messageId));
//		dialog.setCancelable(false);
//
//		final DSATask<DSRequest> task = DSS.sendRequest(context, serviceName,
//				content, new OnReceiveListener<DSResponse>() {
//
//					@Override
//					public void onReceive(DSResponse result) {
//						bindStudents(result);
//
//						TextView emptyText = (TextView) mActivity
//								.findViewById(R.id.emptyResults);
//						mStudentGridView.setEmptyView(emptyText);
//
//						dialog.dismiss();
//					}
//
//					@Override
//					public void onError(Exception e) {
//						showLog("Error occured when getting students: "
//								+ e.toString());
//						TextView emptyText = (TextView) context
//								.findViewById(R.id.emptyResults);
//						mStudentGridView.setEmptyView(emptyText);
//
//						dialog.dismiss();
//					}
//				});
//	}
}
