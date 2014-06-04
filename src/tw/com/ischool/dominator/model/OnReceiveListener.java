package tw.com.ischool.dominator.model;

public abstract class OnReceiveListener<Result> {
	private Object _sender;

	protected Object getSender() {
		return _sender;
	}

	protected OnReceiveListener() {
	}
	
	protected OnReceiveListener(Object sender) {
		_sender = sender;
	}
	
	protected abstract void onReceive(Result result);

	protected abstract void onError(Exception e);	
	
	protected void onCanceled(){};
}