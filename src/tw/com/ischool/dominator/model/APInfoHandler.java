package tw.com.ischool.dominator.model;

import ischool.dsa.client.ContractConnection;
import ischool.dsa.client.target.dsns.DSNSTargetURLProvider;
import ischool.dsa.utility.DSRequest;
import ischool.dsa.utility.DSResponse;
import ischool.dsa.utility.XmlUtil;
import ischool.dsa.utility.http.Cancelable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.w3c.dom.Element;

import tw.com.ischool.oauth2signin.APInfo;
import tw.com.ischool.oauth2signin.User;
import android.content.Context;

public class APInfoHandler {
	private ArrayList<APInfoEx> mApList;
	private int mCurrentCount = 0;

	public APInfoHandler(User user) {
		mApList = new ArrayList<APInfoHandler.APInfoEx>();
		for (APInfo ap : user.getApplications()) {
			mApList.add(new APInfoEx(ap));
		}
	}

	public void checkAPContracts(Context context,
			final OnCompletedListener<Void> listener,
			final Cancelable cancelable) {
		mCurrentCount = 0;
		for (final APInfoEx ap : mApList) {
			DSATask<Void> task = new DSATask<Void>() {
				@Override
				protected DSResponse doInBackground(Void... params) {
					try {
						DSNSTargetURLProvider provider = new DSNSTargetURLProvider(
								ap.getApName());
						ContractConnection cc = new ContractConnection(
								provider, "info");
						DSRequest req = new DSRequest();
						return cc.sendRequest("ListContract", req, cancelable);
					} catch (Exception ex) {
						this.onExceptionOccured(ex);
					}
					return null;
				}
			};

			task.setOnReceiveListener(new OnReceiveListener<DSResponse>() {

				@Override
				public void onReceive(DSResponse result) {
					Element content = result.getContent();
					for (Element e : XmlUtil
							.selectElements(content, "Contract")) {
						String contractName = e.getAttribute("Name");
						ap.addContract(contractName);
						checkCompleted(listener);
					}
				}

				@Override
				public void onError(Exception e) {
					checkCompleted(listener);
				}
			});
			task.execute();
		}
	}

	public List<APInfo> listApplications() {
		ArrayList<APInfo> list = new ArrayList<APInfo>();
		for (final APInfoEx ap : mApList) {
			list.add(ap);
		}
		return list;
	}

	public void containContract(String apName, final String contractName,
			final OnCompletedListener<Boolean> listener,
			final Cancelable cancelable) {
		for (final APInfoEx ap : mApList) {
			if (ap.getApName().equals(apName)) {
				// return ap.getContracts().contains(contractName);

				DSATask<Void> task = new DSATask<Void>() {
					@Override
					protected DSResponse doInBackground(Void... params) {
						try {
							DSNSTargetURLProvider provider = new DSNSTargetURLProvider(
									ap.getApName());
							ContractConnection cc = new ContractConnection(
									provider, "info");
							DSRequest req = new DSRequest();
							return cc.sendRequest("ListContract", req,
									cancelable);
						} catch (Exception ex) {
							this.onExceptionOccured(ex);
						}
						return null;
					}
				};

				task.setOnReceiveListener(new OnReceiveListener<DSResponse>() {

					@Override
					public void onReceive(DSResponse result) {
						Element content = result.getContent();
						boolean contain = false;
						for (Element e : XmlUtil.selectElements(content,
								"Contract")) {
							String cname = e.getAttribute("Name");
							if (cname.equals(contractName)) {
								contain = true;
								break;
							}
						}

						listener.onCompleted(contain);
					}

					@Override
					public void onError(Exception e) {
						listener.onCompleted(false);
					}
				});
				task.execute();
			}
		}
	}

	private void checkCompleted(OnCompletedListener<Void> listener) {
		mCurrentCount++;

		if (mCurrentCount == mApList.size() && listener != null)
			listener.onCompleted(null);
	}

	public class APInfoEx extends APInfo {
		private HashSet<String> _contracts;

		public APInfoEx(APInfo ap) {
			super(ap.getID(), ap.getApName(), ap.getFullName(), ap.getOrigin());

			_contracts = new HashSet<String>();
		}

		public void addContract(String contract) {
			_contracts.add(contract);
		}

		public Set<String> getContracts() {
			return _contracts;
		}
	}
}
