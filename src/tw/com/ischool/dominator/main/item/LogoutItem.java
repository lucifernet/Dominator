package tw.com.ischool.dominator.main.item;

import tw.com.ischool.dominator.R;
import tw.com.ischool.oauth2signin.SignInActivity;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;

public class LogoutItem extends ActionItem {
	public LogoutItem() {
		super.init(R.string.item_logout, R.drawable.ic_logout,
				DisplayStatus.LOGINED, new PaddingTask() {

					@Override
					public void invoke(final Activity activity) {

						AlertDialog.Builder dialog = new AlertDialog.Builder(
								activity);
						dialog.setTitle(R.string.logout_title);
						dialog.setMessage(R.string.logout_message);
						dialog.setIcon(android.R.drawable.ic_dialog_alert);
						dialog.setCancelable(false);
						dialog.setPositiveButton(R.string.confirm,
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int which) {
										Intent i = new Intent(activity,
												SignInActivity.class);
										i.putExtra(
												SignInActivity.ACTION_TYPE,
												SignInActivity.ACTION_TYPE_SIGNOUT);
										activity.startActivityForResult(
												i,
												SignInActivity.ACTION_REQUESTCODE);
									}
								});
						dialog.setNegativeButton(R.string.cancel,
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int which) {
										dialog.dismiss();
									}
								});
						dialog.show();
					}
				});
	}
}
