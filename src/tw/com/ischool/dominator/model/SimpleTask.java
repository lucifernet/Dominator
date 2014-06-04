package tw.com.ischool.dominator.model;

import android.os.AsyncTask;

public abstract class SimpleTask<Params, Progress, Result> extends
		AsyncTask<Params, Progress, Result> {
	protected OnReceiveListener<Result> mListener;
	protected Exception mException;

	@Override
	protected void onPostExecute(Result result) {
		if (mListener == null)
			return;

		if(isCancelled())
			mListener.onCanceled();
		
		if (mException != null)
			mListener.onError(mException);
		else
			mListener.onReceive(result);
	}

	protected void onExceptionOccured(Exception exception) {
		mException = exception;
	}
	
	protected void setOnReceiveListener(
			OnReceiveListener<Result> listener) {
		mListener = listener;
	}
}
