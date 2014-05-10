package luci.uci.edu.groupstatus;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

public class AlarmDialogPopUp extends Activity {

	private int m_alarmId;

	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Get the alarm ID from the intent extra data
		Intent intent = getIntent();
		Bundle extras = intent.getExtras();

		if (extras != null) {
			m_alarmId = extras.getInt("AlarmID", -1);
		} else {
			m_alarmId = -1;
		}

		// Show the pop up dialog
		showDialog(0);
	}

	@SuppressWarnings("deprecation")
	@Override
	protected Dialog onCreateDialog(int id) {
		super.onCreateDialog(id);

		// Build the dialog
		AlertDialog.Builder alert = new AlertDialog.Builder(this);

		alert.setTitle("Group Status");
		alert.setMessage("Its time for the alarm ");
		alert.setCancelable(true);

		alert.setPositiveButton("Report", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				AlarmDialogPopUp.this.finish();
				Intent intent = new Intent(AlarmDialogPopUp.this, WelcomePage.class);
				startActivity(intent);
			}
		});
		alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				AlarmDialogPopUp.this.finish();
			}
		});

		// Create and return the dialog
		AlertDialog dlg = alert.create();

		return dlg;
	}
}