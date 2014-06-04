package tw.com.ischool.dominator;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class NoDdsFragment extends Fragment {

	public static final String PARAM_AP_NAME = "AP_NAME";

	private TextView mTextView;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_no_dds, container,
				false);

		mTextView = (TextView)view.findViewById(R.id.txtMessage);
		
		return view;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		String apName = getArguments().getString(PARAM_AP_NAME);
		String message = getActivity().getString(R.string.nodds);
		message = String.format(message, apName);
		mTextView.setText(message);
	}
}
