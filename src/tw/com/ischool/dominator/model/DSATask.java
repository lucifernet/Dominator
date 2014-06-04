package tw.com.ischool.dominator.model;

import ischool.dsa.client.ContractConnection;
import ischool.dsa.utility.DSRequest;
import ischool.dsa.utility.DSResponse;
import ischool.dsa.utility.http.Cancelable;

import org.w3c.dom.Element;

import tw.com.ischool.dominator.main.MainActivity;
import tw.com.ischool.oauth2signin.AccessToken;
import tw.com.ischool.oauth2signin.util.DSConnectionPooling;
import tw.com.ischool.oauth2signin.util.DSConnectionPooling.GetConnectionListener;
import android.content.Context;
import android.util.Log;

public abstract class DSATask<Params> extends
		SimpleTask<Params, Void, DSResponse> {

	protected ContractConnection mConnection;

	protected void setConnection(ContractConnection connection) {
		mConnection = connection;
	}

	protected DSResponse sendRequest(String targetService, DSRequest request,
			Cancelable cancelable) {
		return mConnection.sendRequest(targetService, request, cancelable);
	}

	protected DSResponse sendRequest(String targetService, Cancelable cancelable) {
		return mConnection.sendRequest(targetService, new DSRequest(),
				cancelable);
	}

	public static ContractConnection LAST_CONNECTION;

	public static <Params> void sendRequest(Context context, String dsns,
			String contract, final DSATask<Params> task,
			final OnReceiveListener<DSResponse> listener,
			final Params... params) {

		DSConnectionPooling pool = DSConnectionPooling.get();
		pool.getConnection(dsns, contract, AccessToken.getCurrentAccessToken(),
				context, new GetConnectionListener() {

					@Override
					public void onSuccess(ContractConnection cn) {

						Log.d(MainActivity.TAG, "Get connection succeed");
						LAST_CONNECTION = cn;

						task.setConnection(cn);
						task.setOnReceiveListener(listener);
						task.execute(params);
					}

					@Override
					public void onFail(Exception ex) {
						Log.e(MainActivity.TAG,
								"Get connection fail : " + ex.toString());
						if (listener != null)
							listener.onError(ex);
					}
				});
	}

	public static DSATask<DSRequest> sendRequest(Context context, String dsns,
			String targetContract, final String targetService, Element content,
			OnReceiveListener<DSResponse> listener, final Cancelable cancelable) {

		DSRequest request = new DSRequest();
		if (content != null)
			request.setContent(content);

		DSATask<DSRequest> task = new DSATask<DSRequest>() {

			@Override
			protected DSResponse doInBackground(DSRequest... params) {

				try {
					DSRequest req;
					if (params.length > 0)
						req = params[0];
					else
						req = new DSRequest();

					return this.sendRequest(targetService, req, cancelable);

				} catch (Exception ex) {
					this.onExceptionOccured(ex);
				}
				return null;
			}
		};

		sendRequest(context, dsns, targetContract, task, listener, request);

		return task;
	}

	public static DSATask<DSRequest> sendRequest(Context context, String dsns,
			String targetContract, final String targetService,
			final OnReceiveListener<DSResponse> listener, Cancelable cancelable) {

		return sendRequest(context, dsns, targetContract, targetService, null,
				listener, cancelable);
	}
}
