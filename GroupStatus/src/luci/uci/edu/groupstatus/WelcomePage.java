package luci.uci.edu.groupstatus;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import luci.uci.edu.groupstatus.deprecated.StatusCollector;
import luci.uci.edu.groupstatus.notification.NotificationPublisher;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.EditorInfo;

public class WelcomePage extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.welcome_page);
		
		findViewById(R.id.welcomeBackground).startAnimation((Animation) AnimationUtils.loadAnimation(WelcomePage.this, R.anim.fade_out));
       	findViewById(R.id.linearLayoutForLogIn).startAnimation((Animation) AnimationUtils.loadAnimation(WelcomePage.this, R.anim.fade_in));
       	findViewById(R.id.loginArea).startAnimation((Animation) AnimationUtils.loadAnimation(WelcomePage.this, R.anim.fade_in));
		
		addListenerOnSoftKeyboar(); //listener whether the user presses done/enter
		addListenerOnTextView(); //act like a button

		LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		boolean enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

		if (!enabled) {
			showSettingsAlert(WelcomePage.this);
		}

	}
	
	@Override
	public void onResume() {
		super.onResume();
		findViewById(R.id.welcomeBackground).startAnimation((Animation) AnimationUtils.loadAnimation(WelcomePage.this, R.anim.fade_out));
       	findViewById(R.id.linearLayoutForLogIn).startAnimation((Animation) AnimationUtils.loadAnimation(WelcomePage.this, R.anim.fade_in));
       	findViewById(R.id.loginArea).startAnimation((Animation) AnimationUtils.loadAnimation(WelcomePage.this, R.anim.fade_in));
       	addListenerOnSoftKeyboar(); //listener whether the user presses done/enter
		addListenerOnTextView(); //act like a button
	}

	public void showSettingsAlert(final Context mContext) {
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);

		// Setting Dialog Title
		alertDialog.setTitle("Location service disabled");

		// Setting Dialog Message
		alertDialog.setMessage("GPS is not enabled. Please enable it in Settings menu.");

		// Setting Icon to Dialog
		//alertDialog.setIcon(R.drawable.delete);

		// on pressing cancel button
		alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});

		// On pressing Settings button
		alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
				mContext.startActivity(intent);
			}
		});

		// Showing Alert Message
		alertDialog.show();
	}

	public void addListenerOnTextView() {

		final TextView textView = (TextView) findViewById(R.id.loginTextView);
		textView.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				EditText userID = (EditText) findViewById(R.id.userID);
				EditText userPW = (EditText) findViewById(R.id.userPW);
				String userProfile = userID.getText().toString() + ";" + userPW.getText().toString();

				if (userID.getText().toString().isEmpty() || userPW.getText().toString().isEmpty()) {
					String text = "Login error. Invalid username or password.";
					Toast toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT);
					toast.setGravity(Gravity.CENTER, 0, 0);
					toast.show();
				} else {
					//	            	String text = "Logging to: " + userID.getText().toString() + " w/ " + userPW.getText().toString();	            	
					//	            	Toast toast = Toast.makeText(getApplicationContext(),text, Toast.LENGTH_LONG);
					//	            	toast.setGravity(Gravity.CENTER, 0, 0);
					//	            	toast.show();
					LogInToServer logInToServer = new LogInToServer(WelcomePage.this);
					logInToServer.execute(userProfile); //http://qs4task.appspot.com/socialanalysistaskqs?taskid=1001 //http://myfooserver.appspot.com/
				}
			}
		});
	}

	public void addListenerOnSoftKeyboar() {

		final TextView textView = (TextView) findViewById(R.id.loginTextView);
		final EditText editText = (EditText) findViewById(R.id.userPW);
		
		editText.setOnEditorActionListener(new OnEditorActionListener() {

	           @Override
	           public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
	               if (actionId == EditorInfo.IME_ACTION_DONE) {
	            	   textView.performClick();
	               }
	               return false;
	           }
	       });

	}

	private class LogInToServer extends AsyncTask<String, Void, String> {

		private ProgressDialog dialog;
		private Context context; // application context.
		String userID = "";
		String userPW = "";

		public LogInToServer(Activity activity) {
			context = activity;
			dialog = new ProgressDialog(context);
		}

		protected void onPreExecute() {
			this.dialog.setMessage("Logging to the server.");
			this.dialog.show();
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		@Override
		protected String doInBackground(String... userProfiles) {
			String response = "";
			for (String userProfile : userProfiles) {
				DefaultHttpClient client = new DefaultHttpClient();

				// Add user data
				userID = userProfile.toString().substring(0, userProfile.toString().indexOf(';'));
				userPW = userProfile.toString().substring(userProfile.toString().indexOf(';') + 1);
				HttpPost httppost = new HttpPost("http://group-status-376.appspot.com/groupstatus_server");
				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
				nameValuePairs.add(new BasicNameValuePair("function", "login"));
				nameValuePairs.add(new BasicNameValuePair("userID", userID));
				nameValuePairs.add(new BasicNameValuePair("userPW", userPW));

				try {
					httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
				} catch (UnsupportedEncodingException e1) {
					e1.printStackTrace();
				}

				try {
					HttpResponse execute = client.execute(httppost);
					InputStream content = execute.getEntity().getContent();

					BufferedReader buffer = new BufferedReader(new InputStreamReader(content));
					String s = "";
					while ((s = buffer.readLine()) != null) {
						response += s;
					}

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			return response;
		}

		@Override
		protected void onPostExecute(String result) {

			if (dialog.isShowing()) {
				dialog.dismiss();
			}

			if (result.startsWith("successfully logged in")) {

				String group = getParameter("group", result);
				String type = getParameter("type", result);
				String startingDate = getParameter("startingDate", result);
				String timeInterval = getParameter("timeInterval", result);

				Editor edit = PreferenceManager.getDefaultSharedPreferences(context).edit();
				edit.putString("userIDforGroupStatus", userID);
				edit.putString("userPWforGroupStatus", userPW);
				edit.putString("groupUserBelongedTo", group);
				edit.putString("typeOfTheParticipant", type);
				edit.clear(); //I know this is redundant... 
				edit.apply();

				if(type.equals("testing")){
					setNotificationsForTesting();
				}else if(type.equals("experiment")){
					int month = Integer.parseInt(startingDate.substring(0, 2));
					int date = Integer.parseInt(startingDate.substring(2));
					int days = Integer.parseInt(timeInterval);
					setNotificationsForExperiment(month, date, days);				
				}
				
				Intent i = new Intent(WelcomePage.this, CollectStatusAndSensorData.class);
				startActivity(i);

			} else {
				Toast toast = Toast.makeText(getApplicationContext(), "Login error. Invalid username or password.", Toast.LENGTH_SHORT);
				toast.setGravity(Gravity.CENTER, 0, 100);
				toast.show();
			}

		}
	}
	
	String getParameter(String parameter, String input){

		int indexOfParameter = input.indexOf(parameter);
		int indexOfValue = indexOfParameter + parameter.length() + 1;
		int indexOfNextSemicolon = input.indexOf(";", indexOfValue);
		if(indexOfParameter == -1) return "null value";
		if(indexOfNextSemicolon == -1) return input.substring(indexOfValue);
		
		return input.substring(indexOfValue, indexOfNextSemicolon);
		
	}

	public void setNotificationsForTesting() {

		int minutes = 5; //remind the user every 5 minutes

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

	public void setNotificationsForExperiment(int month, int date, int days) {

		int experimentHour[] = { 9, 12, 15, 18, 21 };

		getBaseContext();
		AlarmManager alarmManager = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(this, NotificationPublisher.class);

		month--; //January = 0 ...
		
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(System.currentTimeMillis());
		calendar.set(Calendar.MONTH, month);
		calendar.set(Calendar.DATE, date);

		for (int i = 0; i < experimentHour.length; i++) {
			calendar.set(Calendar.HOUR_OF_DAY, experimentHour[i]);
			for (int j = 0; j < days; j++) {
				calendar.set(Calendar.DAY_OF_YEAR, calendar.get(Calendar.DAY_OF_YEAR) + j);
				int currentTime = (int) System.currentTimeMillis(); //use the current time as id
				PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), currentTime, intent,PendingIntent.FLAG_UPDATE_CURRENT);
				alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
			}
		}
	}

}
