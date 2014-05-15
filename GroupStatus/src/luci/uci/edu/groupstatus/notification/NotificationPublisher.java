package luci.uci.edu.groupstatus.notification;
import java.util.Calendar;

import luci.uci.edu.groupstatus.R;
import luci.uci.edu.groupstatus.WelcomePage;
import luci.uci.edu.groupstatus.R.drawable;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
 
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
	
	@SuppressWarnings("deprecation")
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
	 Intent intent = new Intent(context, WelcomePage.class);
	 
	 // Attach the intent to a pending intent
	 PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
	 
	 Calendar calendar = Calendar.getInstance();
	 calendar.setTimeInMillis(System.currentTimeMillis());
	 // Create the notification
	 Notification notification = new Notification(R.drawable.ic_launcher_notification, "Group Status", System.currentTimeMillis());
	 notification.setLatestEventInfo(context, "Group Status", "Time to update your status!",pendingIntent);
	 
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

}