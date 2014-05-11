package luci.uci.edu.groupstatus.notification;

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
import android.view.Menu;
import android.view.MenuItem;

public class NotificationActivity extends Activity{

	 @Override
	    protected void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        setContentView(R.layout.result_reporter);
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
	 
//	    private void scheduleNotification(Notification notification, int delay) {
//	 
//	        Intent notificationIntent = new Intent(this, NotificationPublisher.class);
//	        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION_ID, 1);
//	        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION, notification);
//	        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
//	 
//	        long futureInMillis = SystemClock.elapsedRealtime() + delay;
//	        AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
//	        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, futureInMillis, pendingIntent);
//	    }
//	 
//	    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
//		private Notification getNotification(String content) {
//	        Notification.Builder builder = new Notification.Builder(this);
//	        builder.setContentTitle("Scheduled Notification");
//	        builder.setContentText(content);
//	        builder.setSmallIcon(R.drawable.ic_launcher);
//	        return builder.build();
//	    }
	    
	    private void createScheduledNotification(int days)
	    {
	    // Get new calendar object and set the date to now
	    Calendar calendar = Calendar.getInstance();
	    calendar.setTimeInMillis(System.currentTimeMillis());
	    // Add defined amount of days to the date
//	    calendar.add(Calendar.HOUR_OF_DAY, days * 24);
	    calendar.add(Calendar.SECOND, days);

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
