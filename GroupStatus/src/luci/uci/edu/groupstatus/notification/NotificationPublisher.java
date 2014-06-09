package luci.uci.edu.groupstatus.notification;
import java.util.Calendar;

import luci.uci.edu.groupstatus.LoadingPage;
import luci.uci.edu.groupstatus.R;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
 
public class NotificationPublisher extends BroadcastReceiver {
 
//    public static String NOTIFICATION_ID = "notification-id";
//    public static String NOTIFICATION = "notification";
// 
//    public void onReceive(Context context, Intent intent) {
// 
//        NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
// 
//        Notification notification = intent.getParcelableExtra(NOTIFICATION);
//        int id = intent.getIntExtra(NOTIFICATION_ID, 0);
//        notificationManager.notify(id, notification);
// 
//    }
	
	@Override
	 public void onReceive(Context context, Intent paramIntent) {
		
		try {
		    Uri alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
			if(alert == null){															// if alert is null, using backup
			    alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
			    if(alert == null)    												//  just in case
			        alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);                
			}
		    Ringtone r = RingtoneManager.getRingtone(context, alert);
		    r.play();
		    Thread.sleep(1000);
		    r.stop();
		} catch (Exception e) {
		    e.printStackTrace();
		}

		// Request the notification manager
	 NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
	 
	 // Create a new intent which will be fired if you click on the notification
	 Intent intent = new Intent(context, LoadingPage.class);
	 
	 // Attach the intent to a pending intent
	 PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
	 
	 Calendar calendar = Calendar.getInstance();
	 calendar.setTimeInMillis(System.currentTimeMillis());
	 
	 // Create the notification
	 
//	 old API
//	 Notification notification = new Notification(R.drawable.ic_launcher_notification, "Group Status", System.currentTimeMillis());
//	 notification.setLatestEventInfo(context, "Group Status", "Time to update your status!",pendingIntent);
	 
	 Notification.Builder notificationBuilder = new Notification.Builder(context)
     .setContentTitle("Group Status")
     .setContentText("Time to update your status!")
     .setSmallIcon(R.drawable.ic_launcher_notification)
     .setContentIntent(pendingIntent)
     .setWhen(System.currentTimeMillis());
     //.build();
	 
	 Notification notification = null;
	 
	 //NotificationBuilder.build() requires API Level 16 or higher.
	 //Anything between API Level 11 & 15 you should use NotificationBuilder.getNotification().

	 if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
		 	Log.v("version", "16 up");
			notification = notificationBuilderForApi16AndUP(notificationBuilder);
		} else {
			Log.v("version", "11 to 15");
			notification = notificationBuilderForApi11to15(notificationBuilder);
		}

	 //only one notification in a minute at most
	 int notifyID = calendar.get(Calendar.DAY_OF_YEAR)*1000+calendar.get(Calendar.HOUR_OF_DAY)*60+calendar.get(Calendar.MINUTE);
	 // Fire the notification
	 notificationManager.notify(notifyID, notification);

	 
	 
//the popup dialog is quite annoying
	 
//	// Launch the alarm popup dialog
//     Intent alarmIntent = new Intent("android.intent.action.MAIN");
//
//     alarmIntent.setClass(context, AlarmDialogPopUp .class);
//     alarmIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//
//     // Pass on the alarm ID as extra data
//     alarmIntent.putExtra("AlarmID", paramIntent.getIntExtra("AlarmID", -1));
//
//     // Start the popup activity
//     context.startActivity(alarmIntent);
	 }
	
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	private Notification notificationBuilderForApi16AndUP(Notification.Builder notificationBuilder){
		return notificationBuilder.build();
	}
	
	@SuppressWarnings("deprecation")
	private Notification notificationBuilderForApi11to15(Notification.Builder notificationBuilder){
		return notificationBuilder.getNotification();
	}

}