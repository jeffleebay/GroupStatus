package luci.uci.edu.groupstatus.deprecated;

import java.util.Calendar;

import luci.uci.edu.groupstatus.R;
import luci.uci.edu.groupstatus.R.id;
import luci.uci.edu.groupstatus.R.layout;
import luci.uci.edu.groupstatus.R.menu;
import luci.uci.edu.groupstatus.notification.NotificationPublisher;
import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.app.Activity;
import android.os.SystemClock;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

public class NotificationActivity extends Activity{

	 @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.result_reporter);

		final ImageView button = (ImageView) findViewById(R.id.area_Button_Upload);
		button.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				setNotificationsForTesting();
//				setNotificationsForExperiment();
			}
		});

	}

	public void setNotificationsForExperiment() {

		int experimentHour[] = { 9, 12, 15, 18, 21 };

		getBaseContext();
		AlarmManager alarmManager = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(this, NotificationPublisher.class);

		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(System.currentTimeMillis());
		calendar.set(Calendar.MINUTE, calendar.get(Calendar.MINUTE)+1);

		for (int i = 0; i < 5; i++) {
			calendar.set(Calendar.HOUR_OF_DAY, experimentHour[0]);
			int currentTime = (int) System.currentTimeMillis();				//use the current time as id
			PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), currentTime, intent, PendingIntent.FLAG_UPDATE_CURRENT);
			alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
		}
	}
	
	 public void setNotificationsForTesting(){
		 
		 int minutes=5;	//remind the user every 5 minutes
		 
		 getBaseContext();
		 AlarmManager alarmManager = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
		 Intent intent = new Intent(this, NotificationPublisher.class);
		 Calendar calendar = Calendar.getInstance();
		 calendar.setTimeInMillis(System.currentTimeMillis());

		 for(int i=1;i<=5;i++){			 
			 calendar.add(Calendar.MINUTE, minutes*i);
			 int currentTime = (int) System.currentTimeMillis();				//use the current time as id
			 PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), currentTime, intent, PendingIntent.FLAG_UPDATE_CURRENT);
			 alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
		 }
	 }

	 
	    @Override
	    public boolean onCreateOptionsMenu(Menu menu) {
	        // Inflate the menu; this adds items to the action bar if it is present.
	        getMenuInflater().inflate(R.menu.notification, menu);
	        return true;
	    }
	 
	    @Override
	    public boolean onOptionsItemSelected(MenuItem item) {
	        switch (item.getItemId()) {
	            case R.id.action_5:
//	                scheduleNotification(getNotification("5 second delay"), 5000);
	            	createScheduledNotification(5);
	                return true;
	            case R.id.action_10:
//	                scheduleNotification(getNotification("10 second delay"), 10000);
	                return true;
	            case R.id.action_30:
//	                scheduleNotification(getNotification("30 second delay"), 30000);
	                return true;
	            default:
	                return super.onOptionsItemSelected(item);
	        }
	    }
	    
	    private void createScheduledNotification(int seconds)
	    {
	    // Get new calendar object and set the date to now
	    Calendar calendar = Calendar.getInstance();
	    calendar.setTimeInMillis(System.currentTimeMillis());
	    // Add defined amount of days to the date
//	    calendar.add(Calendar.HOUR_OF_DAY, days * 24);
	    calendar.add(Calendar.SECOND, seconds);

	    // Retrieve alarm manager from the system
	    AlarmManager alarmManager = (AlarmManager) getApplicationContext().getSystemService(getBaseContext().ALARM_SERVICE);
	    // Every scheduled intent needs a different ID, else it is just executed once
	    int id = (int) System.currentTimeMillis();

	    // Prepare the intent which should be launched at the date
	    Intent intent = new Intent(this, NotificationPublisher.class);

	    // Prepare the pending intent
	    PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), id, intent, PendingIntent.FLAG_UPDATE_CURRENT);

	    // Register the alert in the system. You have the option to define if the device has to wake up on the alert or not
	    alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
	    }
}
