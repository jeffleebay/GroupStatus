package luci.uci.edu.groupstatus;

import java.util.Calendar;
import luci.uci.edu.groupstatus.datastore.StatusDataSource;
import luci.uci.edu.groupstatus.notification.NotificationPublisher;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.view.View;

public class Initialization extends Activity {

	final int NOTIFICATION_MODE = 2; // 0 for Testing, 1 for Experiment
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.initialization);
		
		final EditText startingDate = (EditText) findViewById(R.id.startingDate);
		final TextView setupTextViewExp = (TextView) findViewById(R.id.setupTextViewExp);
		setupTextViewExp.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				int month = Integer.parseInt(startingDate.getText().toString().substring(0, 2));
				int date = Integer.parseInt(startingDate.getText().toString().substring(2));
				setNotificationsForExperiment(month, date);
				Intent i = new Intent(Initialization.this, CollectStatusAndSensorData.class);
				startActivity(i);
			}
		});
		final TextView setupTextViewTest = (TextView) findViewById(R.id.setupTextViewTest);
		setupTextViewTest.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				setNotificationsForTesting();
				Intent i = new Intent(Initialization.this, CollectStatusAndSensorData.class);
				startActivity(i);
			}
		});
		final TextView setupTextViewSkip = (TextView) findViewById(R.id.setupTextViewSkip);
		setupTextViewSkip.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent i = new Intent(Initialization.this, CollectStatusAndSensorData.class);
				startActivity(i);
			}
		});


	}


	public void setNotificationsForTesting() {

		int minutes = 1; //remind the user every 5 minutes

		getBaseContext();
		AlarmManager alarmManager = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(this, NotificationPublisher.class);
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(System.currentTimeMillis());

		for (int i = 1; i <= 5; i++) {
			calendar.add(Calendar.MINUTE, minutes * i);
			int currentTime = (int) System.currentTimeMillis(); //use the current time as id
			PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), currentTime, intent, PendingIntent.FLAG_UPDATE_CURRENT);
			alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
		}
	}

	public void setNotificationsForExperiment(int month, int date) {

		int experimentHour[] = { 9, 12, 15, 18, 21 };

		getBaseContext();
		AlarmManager alarmManager = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(this, NotificationPublisher.class);

		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(System.currentTimeMillis());
		calendar.set(Calendar.MONTH, month);
		calendar.set(Calendar.DATE, date);

		for (int i = 0; i < 5; i++) {
			calendar.set(Calendar.HOUR_OF_DAY, experimentHour[0]);
			for (int j = 0; j < 6; j++) {
				calendar.set(Calendar.DAY_OF_YEAR, calendar.get(Calendar.DAY_OF_YEAR) + j);
				int currentTime = (int) System.currentTimeMillis(); //use the current time as id
				PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), currentTime, intent,
						PendingIntent.FLAG_UPDATE_CURRENT);
				alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
			}
		}
	}

}
