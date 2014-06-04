package tw.com.ischool.dominator.model;

import ischool.dsa.client.ContractConnection;
import ischool.dsa.utility.DSRequest;
import ischool.dsa.utility.DSResponse;
import ischool.dsa.utility.http.Cancelable;

import org.w3c.dom.Element;

import tw.com.ischool.oauth2signin.APInfo;
import android.content.Context;

public class DSS {

	public static final String CONTRACT = "ischool.dss";

	private static APInfo DSNS;

	public static void setDSNS(APInfo dsns) {
		DSNS = dsns;
	}

	public static DSATask<DSRequest> sendRequest(Context context,
			String targetService, Element content,
			OnReceiveListener<DSResponse> listener, Cancelable cancelable) {

		if (DSNS == null)
			return null;

		return DSATask.sendRequest(context, DSNS.getApName(), CONTRACT,
				targetService, content, listener, cancelable);
	}

	public static DSATask<DSRequest> sendRequest(Context context,
			String targetService, OnReceiveListener<DSResponse> listener,
			Cancelable cancelable) {

		if (DSNS == null)
			return null;

		return DSATask.sendRequest(context, DSNS.getApName(), CONTRACT,
				targetService, listener, cancelable);
	}
	
	public static ContractConnection getLastConnection() {
		return DSATask.LAST_CONNECTION;
	}	
}
