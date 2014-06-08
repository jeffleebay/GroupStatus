package luci.uci.edu.groupstatus;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import luci.uci.edu.groupstatus.datastore.StatusDataSource;
import luci.uci.edu.groupstatus.datastore.StatusObject;
import SoundMeter.SoundMeter;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.format.Time;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class CollectStatusAndSensorData extends Activity implements OnClickListener {

	//Vars for storing collected data
	int asyncTasksProgress = 0;
	HashMap<String, String> SensorResult = new HashMap<String, String>();
	String keys[] = { "status", "groupStatus", "wifiList", "noiseLevel", "location", "address" };

	// Vars for WiFi
	WifiManager wifi;
	BroadcastReceiver wifiBroadcastReceiver;
	ArrayList<HashMap<String, String>> arraylistForWiFiResult = new ArrayList<HashMap<String, String>>();
	String WIFI_ITEM_KEY = "wifi";
	List<ScanResult> scannedWiFiResults;
	int numberOfWiFiPointsFound = 0;
	private static final int WIFI_POLL_INTERVAL = 1000;
	private static final int WIFI_POLL_Times = 5;

	//Vars for WiFi progress bar
	private ProgressBar mProgressWiFi;
	private int mProgressStatusWiFi = 0;
	private Handler mHandlerWiFi = new Handler();
	private int progressValueWiFi = (int) 100 / WIFI_POLL_Times;
	private Runnable updateProgressBarWiFi;

	// Vars for Noise
	private static final int NOISE_POLL_Time_Interval = 10; // seconds
	private static final int NOISE_POLL_INTERVAL = 300; // milliseconds
	private static final int NOISE_POLL_Times = 1000 * NOISE_POLL_Time_Interval / NOISE_POLL_INTERVAL;
	private SoundMeter mSensor;
	ArrayList<HashMap<String, String>> arraylistForNoiseResult = new ArrayList<HashMap<String, String>>();
	String NOISE_ITEM_KEY = "noise";

	//Vars for Noise progress bar
	private ProgressBar mProgressNoise;
	private int mProgressStatusNoise = 0;
	private Handler mHandlerNoise = new Handler();
	private int progressValueNoise = (int) ((int) 100 / (NOISE_POLL_Times * 1.5)); //There are some delay in the asynctask for noise
	private Runnable updateProgressBarNoise;

	// Vars for Status

	// Vars for location

	//Vars for Location progress bar
	private ProgressBar mProgressLocation;
	private int mProgressStatusLocation = 0;
	private Handler mHandlerLocation = new Handler();
	private int progressValueLocation = 4;
	private Runnable updateProgressBarLocation;
	
	//Vars for Async Tasks
	ScanWiFiAccess scanWiFiAccess;
	DetectBackgroundNoise detectBackgroundNoise;
	GetAddressTask getAddressTask;
	CheckUpdatingProgress checkUpdatingProgress;
	
	//Vars for database
	private StatusDataSource statusDataSource;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.status_and_sensor);

		//Set Visibility
		findViewById(R.id.groupStatus_blocks).setVisibility(View.GONE);
		findViewById(R.id.visualizationOfSensorData).setVisibility(View.GONE);
		findViewById(R.id.textView_ReportButton).setVisibility(View.INVISIBLE);
		findViewById(R.id.Button_Sensor_Upload).setVisibility(View.INVISIBLE);
		
		//Set hint Text
		EditText etStatus = (EditText) findViewById(R.id.EditText_Update_Status);
		etStatus.setHint("What are you doing now?\nEx. Taking a bus to buy groceries in Trader Joes with roommates #lifeexpense #dinner #toohot #universitytowncenter");
		EditText etGroupStatus = (EditText) findViewById(R.id.EditText_Update_GroupStatus);
		etGroupStatus.setHint("Guess what others are doing now?\nLeave it blank and click Update if you don't want to.");
		
		//Set Buttons
		final TextView tvNext = (TextView) findViewById(R.id.Button_Status_Next);
		tvNext.setOnClickListener(this);
		final TextView tvUpdate = (TextView) findViewById(R.id.Button_GroupStatus_Update);
		tvUpdate.setOnClickListener(this);
		final ImageView ivUpload = (ImageView) findViewById(R.id.Button_Sensor_Upload);
		ivUpload.setOnClickListener(this);
		
		//Set Threads for Progress Bars
		updateProgressBarWiFi = new Runnable() {
			public void run() {
				if (mProgressStatusWiFi < 80) {
					mProgressStatusWiFi = mProgressStatusWiFi + progressValueWiFi / 2; //divided by 2 for smoothness
//					Log.i("WiFi", Integer.toString(mProgressStatusWiFi) + "%");

					mProgressWiFi.setProgress(mProgressStatusWiFi > 100 ? 100 : mProgressStatusWiFi);
					mHandlerWiFi.postDelayed(updateProgressBarWiFi, WIFI_POLL_INTERVAL / 2);
				} else {
					mHandlerWiFi.removeCallbacks(updateProgressBarWiFi);
				}
			}
		};
		updateProgressBarNoise = new Runnable() {
			public void run() {
				if (mProgressStatusNoise < 80) {
					mProgressStatusNoise = mProgressStatusNoise + progressValueNoise;
//					Log.i("Noise", Integer.toString(mProgressStatusNoise) + "%");

					mProgressNoise.setProgress(mProgressStatusNoise > 100 ? 100 : mProgressStatusNoise);
					mHandlerNoise.postDelayed(updateProgressBarNoise, NOISE_POLL_INTERVAL);
				} else {
					mHandlerNoise.removeCallbacks(updateProgressBarNoise);
				}
			}
		};
		updateProgressBarLocation = new Runnable() {
			public void run() {
				if (mProgressStatusLocation < 80) {
					mProgressStatusLocation = mProgressStatusLocation + progressValueLocation / 2; //divided by 2 for smoothness
//					Log.i("Location", Integer.toString(mProgressStatusLocation) + "%");

					mProgressLocation.setProgress(mProgressStatusLocation > 100 ? 100 : mProgressStatusLocation);
					mHandlerLocation.postDelayed(updateProgressBarLocation, 500 / 2); //divided by 2 for smoothness
				} else {
					mHandlerLocation.removeCallbacks(updateProgressBarLocation);
				}
			}
		};
		
		// Vars for WiFi
		wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
//		if (wifi.isWifiEnabled() == false) {
//			Toast.makeText(getApplicationContext(), "Wifi was disabled. Enabling now...", Toast.LENGTH_LONG).show();
//			wifi.setWifiEnabled(true);
//		}

		wifiBroadcastReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context c, Intent intent) {
				scannedWiFiResults = wifi.getScanResults();
				numberOfWiFiPointsFound = scannedWiFiResults.size();
			}
		};
		registerReceiver(wifiBroadcastReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

		// Vars for Noise
		mSensor = new SoundMeter();

		// Vars for database
		statusDataSource = new StatusDataSource(getApplicationContext());

		//Activate

		scanWiFiAccess = new ScanWiFiAccess(CollectStatusAndSensorData.this);
		scanWiFiAccess.execute(WIFI_POLL_Times);

		detectBackgroundNoise = new DetectBackgroundNoise(CollectStatusAndSensorData.this);
		detectBackgroundNoise.execute(NOISE_POLL_Times);

		getAddressTask = new GetAddressTask(CollectStatusAndSensorData.this);
		getAddressTask.execute(0);

		checkUpdatingProgress = new CheckUpdatingProgress();
		checkUpdatingProgress.execute(0);

		TextView textviewWiFi = (TextView) findViewById(R.id.textView_WiFi_status);
		textviewWiFi.startAnimation((Animation) AnimationUtils.loadAnimation(CollectStatusAndSensorData.this, R.anim.blink));

		TextView textviewNoise = (TextView) findViewById(R.id.textView_Noise_status);
		textviewNoise.startAnimation((Animation) AnimationUtils.loadAnimation(CollectStatusAndSensorData.this, R.anim.blink));

		TextView textviewLocation = (TextView) findViewById(R.id.textView_Location_status);
		textviewLocation.startAnimation((Animation) AnimationUtils.loadAnimation(CollectStatusAndSensorData.this, R.anim.blink));

		mProgressWiFi = (ProgressBar) findViewById(R.id.progressBar_WiFi);
		mHandlerWiFi.post(updateProgressBarWiFi);

		mProgressNoise = (ProgressBar) findViewById(R.id.progressBar_Noise);
		mHandlerNoise.post(updateProgressBarNoise);

		mProgressLocation = (ProgressBar) findViewById(R.id.progressBar_Location);
		mHandlerLocation.post(updateProgressBarLocation);

	}
	
	@Override
	public void onPause() {
		super.onPause();
		mSensor.stop();
	}

	@Override
	public void onResume() {
		super.onResume();
	}
	
	@Override
	public void onStop() {
		super.onStop();
		
		//if the status is uploaded
		TextView textView_Upload_Button = (TextView) findViewById(R.id.textView_ReportButton);
		if(textView_Upload_Button.getAlpha()== (float) 0.98){

			//			finish();	//It might go back to log in page when you finish this activity right after you log in 			
			Intent i = new Intent(CollectStatusAndSensorData.this, LoadingPage.class);
			startActivity(i);
		}
	}
	
	@Override
	public void onDestroy() {
		unregisterReceiver(wifiBroadcastReceiver);
		super.onDestroy();
	}
	
	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}

	public void onClick(View view) {
		
		EditText tvStatus = (EditText) findViewById(R.id.EditText_Update_Status);
		EditText tvGroupStatus = (EditText) findViewById(R.id.EditText_Update_GroupStatus);
		
		switch (view.getId()) {
		case R.id.Button_Status_Next:
			
			String reportedStatus = tvStatus.getText().toString();
			if(reportedStatus.isEmpty()){
				final Toast toast = Toast.makeText(getApplicationContext(),"Please write down your what you are doing now.", Toast.LENGTH_SHORT);
				toast.setGravity(Gravity.CENTER, 0, 100);
				toast.show();
			}else{
				SensorResult.put(keys[0], reportedStatus);
			
				tvStatus.clearFocus();
				tvGroupStatus.requestFocus();
	
				//Move Left and Fade Out View
				findViewById(R.id.status_blocks).startAnimation((Animation) AnimationUtils.loadAnimation(CollectStatusAndSensorData.this, R.anim.move_left_and_fade_out));
				findViewById(R.id.status_blocks).setVisibility(View.GONE);
	
				//Move Left and Fade In View
				findViewById(R.id.groupStatus_blocks).setVisibility(View.VISIBLE);
				findViewById(R.id.groupStatus_blocks).startAnimation((Animation) AnimationUtils.loadAnimation(CollectStatusAndSensorData.this, R.anim.move_left_and_fade_in));
			}
			break;
			
		case R.id.Button_GroupStatus_Update:
			
			String reportedGroupStatus = tvGroupStatus.getText().toString();
			if(reportedGroupStatus.isEmpty()){
				SensorResult.put(keys[1], "No Guess");
			}else{
				SensorResult.put(keys[1], reportedGroupStatus);
			}


			//Hide the soft keyboard
			InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(tvGroupStatus.getWindowToken(), 0);

			//Move Left and Fade Out View
			findViewById(R.id.groupStatus_blocks).startAnimation((Animation) AnimationUtils.loadAnimation(CollectStatusAndSensorData.this, R.anim.move_left_and_fade_out));
			findViewById(R.id.groupStatus_blocks).setVisibility(View.GONE);

			//Move Left and Fade In View
			findViewById(R.id.visualizationOfSensorData).setVisibility(View.VISIBLE);
			findViewById(R.id.visualizationOfSensorData).startAnimation((Animation) AnimationUtils.loadAnimation(CollectStatusAndSensorData.this, R.anim.move_left_and_fade_in));
			
//			Intent intent = new Intent(CollectStatusAndSensorData.this, SensorDataCollector.class);
//            intent.putExtra("status", reportedStatus);
//            intent.putExtra("groupStatus", repostedGroupStatus);
//            startActivity(intent);
			
			break;
		case R.id.Button_Sensor_Upload:
			
			UploadToServer uploadToServer = new UploadToServer(CollectStatusAndSensorData.this);
			uploadToServer.execute(" ");
			
			break;
		}
	}
	
	private class MyLocationListener implements LocationListener {

		@Override
		public void onLocationChanged(Location loc) {}

		@Override
		public void onProviderDisabled(String provider) {}

		@Override
		public void onProviderEnabled(String provider) {}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {}
	}

	private class GetAddressTask extends AsyncTask<Integer, Void, String> {
		Context mContext;
		Location location;
		LocationManager locationManager;
		LocationListener locationListener;

		public GetAddressTask(Context context) {
			mContext = context;
			locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
			locationListener = new MyLocationListener();
			
			// getting GPS status
			Boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

			// getting network status
			Boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

			if (!isGPSEnabled && !isNetworkEnabled) {
				// no network provider is enabled
				Log.i("Network", "No network provider is enabled");
			} else {
				if (isNetworkEnabled) {
					locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 10, locationListener);
					Log.i("Network", "Network Enabled");
					if (locationManager != null) {
						location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
					}
				}
				if (isGPSEnabled) {
					if (location == null) {
						locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 10, locationListener);
						Log.i("GPS", "GPS Enabled");
						if (locationManager != null) {
							location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
						}
					}
				}
			}
			
		}

		@Override
		protected String doInBackground(Integer... params) {
			
			if(location == null) {
				SensorResult.put("location", "location service disabled");
				return "null";
			}
			
			SensorResult.put("location", location.getLatitude() + "," + location.getLongitude());
			
			Geocoder gcd = new Geocoder(getBaseContext(), Locale.getDefault());
			int par = params[0];
			List<Address> addresses = null;
			String addressText = "";
			try {
				addresses = gcd.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			if(addresses==null){
				return "No wifi/network connection";
			}
			
			if (addresses.size() > 0) {

				Address address = addresses.get(0);
				addressText = String.format("%s: %s, %s, %s, %s", address.getFeatureName(),
						address.getMaxAddressLineIndex() > 0 ? address.getAddressLine(0) : "", // Add it if there's a street address
						address.getLocality(), // Locality is usually a city
						address.getCountryName(), // The country of the address
						address.getPostalCode());
			} else {
				addressText = "No address found";
			}
			return addressText;
		}

		@Override
		protected void onPostExecute(String address) {

			mProgressStatusLocation = 100;
			mProgressLocation.setProgress(100);

			TextView textviewLocation = (TextView) findViewById(R.id.textView_Location_status);
			textviewLocation.clearAnimation();
			textviewLocation.setText("Updated");
			ImageView imageViewLocation = (ImageView) findViewById(R.id.progressFinishedIcon_Location);
			imageViewLocation.setVisibility(View.VISIBLE);
			asyncTasksProgress++;
			
			SensorResult.put("address", address);
		}

	}

	private class ScanWiFiAccess extends AsyncTask<Integer, Void, Integer> {

		private ProgressDialog dialog;
		private Context context; // application context.
		private String combinedScannedWiFiResult = "";

		public ScanWiFiAccess(Activity activity) {
			context = activity;
			dialog = new ProgressDialog(context);
		}

		protected void onPreExecute() {
//			Log.i("Wifi", "start");
		}

		@Override
		protected Integer doInBackground(Integer... totalCounts) {

			int returnValue = 0;
			for (int counts : totalCounts) {
				while (counts > 0) {
//					Log.i("Wifi", Integer.toString(counts));
					counts--;
					wifi.startScan();

					List<String> myList = new ArrayList<String>();

//					Log.i("numberOfWiFiPointsFound", "numberOfWiFiPointsFound=" + Integer.toString(numberOfWiFiPointsFound));
					try {
						numberOfWiFiPointsFound = numberOfWiFiPointsFound - 1;

						while (numberOfWiFiPointsFound >= 0) {
							 String wiFiAccessPoint =  scannedWiFiResults.get(numberOfWiFiPointsFound).BSSID;
//							 + "  "  +  scannedWiFiResults.get(numberOfWiFiPointsFound).capabilities;
//							String wiFiAccessPoint = scannedWiFiResults.get(numberOfWiFiPointsFound).SSID + ";"
//									+ scannedWiFiResults.get(numberOfWiFiPointsFound).BSSID + ";"
//									+ scannedWiFiResults.get(numberOfWiFiPointsFound).level;

							if (!myList.contains(wiFiAccessPoint)) {
								HashMap<String, String> item = new HashMap<String, String>();
								item.put(WIFI_ITEM_KEY, wiFiAccessPoint);
								arraylistForWiFiResult.add(item);
								wiFiAccessPoint += "," + scannedWiFiResults.get(numberOfWiFiPointsFound).level;
								combinedScannedWiFiResult += wiFiAccessPoint + ";"; 
							}
							numberOfWiFiPointsFound--;
						}

						Thread.sleep(WIFI_POLL_INTERVAL);

					} catch (Exception e) {
						e.printStackTrace();
					}
					wifi.startScan();
				}
			}
			return returnValue;

		}

		@Override
		protected void onPostExecute(Integer counts) {

			mProgressStatusWiFi = 100;
			mProgressWiFi.setProgress(100);

//			Log.i("Wifi", "stop");

			TextView textviewWiFi = (TextView) findViewById(R.id.textView_WiFi_status);
			textviewWiFi.clearAnimation();
			textviewWiFi.setText("Updated");
			ImageView imageViewWiFi = (ImageView) findViewById(R.id.progressFinishedIcon_WiFi);
			imageViewWiFi.setVisibility(View.VISIBLE);
			asyncTasksProgress++;
			
			SensorResult.put("wifiList", combinedScannedWiFiResult);

		}
	}

	private class DetectBackgroundNoise extends AsyncTask<Integer, Void, Integer> {

		private ProgressDialog dialog;
		private Context context; // application context.
		private String combinedRecordedNoiseResult = "";

		DecimalFormat decimalFormat = new DecimalFormat("#.00");
		// 00 means exactly two decimal places; # means "optional" digit and it
		// will drop trailing zeroes

		public DetectBackgroundNoise(Activity activity) {
			context = activity;
			dialog = new ProgressDialog(context);
		}

		protected void onPreExecute() {
			mSensor.start();
//			Log.i("Noise", "start");
		}

		@Override
		protected Integer doInBackground(Integer... totalCounts) {

			int counts = totalCounts[0];
			while (counts > 0) {
//				Log.i("Noise", Integer.toString(counts));
				counts--;
				double amp = mSensor.getAmplitude();
				amp = Double.parseDouble(decimalFormat.format(amp));
				
				combinedRecordedNoiseResult += String.valueOf(amp) + ";";

				HashMap<String, String> item = new HashMap<String, String>();
				item.put(NOISE_ITEM_KEY, String.valueOf(amp));
				arraylistForNoiseResult.add(item);

				try {
					Thread.sleep(NOISE_POLL_INTERVAL);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			// drop the first one whose result is always zero for some reasons
			arraylistForNoiseResult.remove(0); 
			combinedRecordedNoiseResult = combinedRecordedNoiseResult.substring(combinedRecordedNoiseResult.indexOf(";")+1);
			
			return 0;

		}

		@Override
		protected void onPostExecute(Integer counts) {

			mSensor.stop();

			mProgressStatusNoise = 100;
			mProgressNoise.setProgress(100);

//			Log.i("Noise", "stop");

			TextView textviewNoise = (TextView) findViewById(R.id.textView_Noise_status);
			textviewNoise.clearAnimation();
			textviewNoise.setText("Updated");
			ImageView imageViewNoise = (ImageView) findViewById(R.id.progressFinishedIcon_Noise);
			imageViewNoise.setVisibility(View.VISIBLE);
			asyncTasksProgress++;
			
			SensorResult.put("noiseLevel", combinedRecordedNoiseResult);

		}
	}
	
	private class CheckUpdatingProgress extends AsyncTask<Integer, Void, Integer> {

		@Override
		protected Integer doInBackground(Integer... totalCounts) {

			int counts = totalCounts[0];

			
			while (true){
//				Log.i("checkValue",Integer.toString(asyncTasksProgress));
				if(asyncTasksProgress==3) {
					break;
				}else{
					try {
						Thread.sleep(500);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
//			Log.i("checked", "done");

			return 0;

		}

		@Override
		protected void onPostExecute(Integer counts) {

			final TextView textViewReportButton = (TextView) findViewById(R.id.textView_ReportButton);
			final ImageView imageViewReportButton = (ImageView) findViewById(R.id.Button_Sensor_Upload);
			
			imageViewReportButton.setVisibility(View.VISIBLE);
			imageViewReportButton.startAnimation((Animation) AnimationUtils.loadAnimation(CollectStatusAndSensorData.this, R.anim.fade_in));
			try {
				Thread.sleep(500);
			} catch (Exception e) {
				e.printStackTrace();
			}
			textViewReportButton.setVisibility(View.VISIBLE);
			textViewReportButton.startAnimation((Animation) AnimationUtils.loadAnimation(CollectStatusAndSensorData.this, R.anim.fade_in));

		}
	}
	
	private class UploadToServer extends AsyncTask<String, Void, String> {
		
	    ProgressDialog dialog;
        Context context; // application context.
        ProgressBar progressBar_spinner_Upload;
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

        
		public UploadToServer(Activity activity) {
            context = activity;
            dialog = new ProgressDialog(context);
        }
	    
	    protected void onPreExecute() {
	        this.dialog.setMessage("Uploading to the server");
	        this.dialog.show();
	        try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
	        
			progressBar_spinner_Upload = (ProgressBar) findViewById(R.id.progressBar_spinner_Upload);
			progressBar_spinner_Upload.setVisibility(View.VISIBLE);
	    }
		
	    @SuppressLint("SimpleDateFormat")
		@Override
		protected String doInBackground(String... userProfiles) {
			String response = "";
			String userProfile[] = userProfiles;

			DefaultHttpClient client = new DefaultHttpClient();
			HttpPost httppost = new HttpPost("http://group-status-376.appspot.com/groupstatus_server");

			//Add function
			nameValuePairs.add(new BasicNameValuePair("function", "upload"));

			//Add user information
			SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
			String userID = settings.getString("userIDforGroupStatus", "n/a");
			String group = settings.getString("groupUserBelongedTo", "n/a");

			nameValuePairs.add(new BasicNameValuePair("userID", userID));
			nameValuePairs.add(new BasicNameValuePair("group", group));
			
			//Add time stamp
			Time today = new Time(Time.getCurrentTimezone());
			today.setToNow();
			String currentTime = Integer.toString(today.month + 1) + "/" + Integer.toString(today.monthDay) + "," + today.format("%k:%M:%S");
			nameValuePairs.add(new BasicNameValuePair("timestamp", currentTime));

			// Add sensor data       
			for (int i = 0; i < keys.length; i++) {
				nameValuePairs.add(new BasicNameValuePair(keys[i], SensorResult.get(keys[i])));
//				Log.i(keys[i], SensorResult.get(keys[i]).toString());
			}

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

			return response;
		}

		@Override
	    protected void onPostExecute(String result) {
	    	
	    	if (dialog.isShowing()) {
	            dialog.dismiss();
	        }

	    	progressBar_spinner_Upload.setVisibility(View.INVISIBLE);
	    	
	    	statusDataSource.open();
	    	
	    	if(result.endsWith("success")){	 
	    		
				
				List<StatusObject> listOfNotUploadedYetStatusObjects = statusDataSource.getAllNotUploadedYetStatusObjects();
	    		
				//check if there is any stored in the database
				if (!listOfNotUploadedYetStatusObjects.isEmpty()) {
					int numberOfNotUploadedYetObject = listOfNotUploadedYetStatusObjects.size();
					for (int i = 0; i < numberOfNotUploadedYetObject; i++) {
						StatusObject statusObject = listOfNotUploadedYetStatusObjects.get(i);
						UploadFromDBToServer uploadFromDBToServer = new UploadFromDBToServer(CollectStatusAndSensorData.this, i+1, numberOfNotUploadedYetObject);
						uploadFromDBToServer.execute(statusObject);
						
						//the get method would block the UI thread but I don't have time to implement the solution to avoid the issue that
						//the database would be opened by different parallel aysnctasks at the same time if  
						
						Boolean uploadFlag = false;
						
						try {
							uploadFlag=uploadFromDBToServer.get().endsWith("success"); 
						} catch (InterruptedException e) {
							e.printStackTrace();
						} catch (ExecutionException e) {
							e.printStackTrace();
						} 
						
						if(uploadFlag){
							statusDataSource.updateAJustUploadedStatusObject(statusObject);
							Log.i("database", "updated");
							final Toast toast = Toast.makeText(getApplicationContext(),"Successfully upload all statuses.", Toast.LENGTH_SHORT);
							toast.show();
						}
						
					}
				}else{					
					
					final Toast toast = Toast.makeText(getApplicationContext(),"Successfully upload the status.", Toast.LENGTH_SHORT);
					toast.show();
				}
				
				//Toast Information
				LayoutInflater inflater = getLayoutInflater();
				View layout = inflater.inflate(R.layout.toast_layout,(ViewGroup) findViewById(R.id.toast_layout_root));

				TextView text1 = (TextView) layout.findViewById(R.id.text1);
				text1.setText(" Thanks for updating ");
				TextView text2 = (TextView) layout.findViewById(R.id.text2);
				text2.setText(" Click here to leave ");

				Toast toast = new Toast(getApplicationContext());
				toast.setGravity(Gravity.BOTTOM, 0, 0);
				toast.setDuration(Toast.LENGTH_SHORT);
				toast.setView(layout);
				toast.show();
				
				//Change UI for showing "successfully uploaded"
				
				ImageView imageView_checked_Upload = (ImageView) findViewById(R.id.imageView_checked_Upload);
				imageView_checked_Upload.setVisibility(View.VISIBLE);
				
				statusDataSource.createAStatusObject(
						nameValuePairs.get(1).getValue(),
						nameValuePairs.get(2).getValue(),
						nameValuePairs.get(3).getValue(),
						nameValuePairs.get(4).getValue(),
						nameValuePairs.get(5).getValue(),
						nameValuePairs.get(6).getValue(),
						nameValuePairs.get(7).getValue(),
						nameValuePairs.get(8).getValue(),
						nameValuePairs.get(9).getValue(), 1);	//1 = true = uploaded
		    	
	    	}else{
	    		Toast toast = Toast.makeText(getApplicationContext(),"Upload error. The status and data are stored to the local database.", Toast.LENGTH_SHORT);
		    	toast.show();
		    	

				statusDataSource.createAStatusObject(
						nameValuePairs.get(1).getValue(),
						nameValuePairs.get(2).getValue(),
						nameValuePairs.get(3).getValue(),
						nameValuePairs.get(4).getValue(),
						nameValuePairs.get(5).getValue(),
						nameValuePairs.get(6).getValue(),
						nameValuePairs.get(7).getValue(),
						nameValuePairs.get(8).getValue(),
						nameValuePairs.get(9).getValue(), 0);  //0 = false = not uploaded yet
	    	}
	    	
	    	//Change the UI to prevent re-upload
	    	ImageView imageView_Upload_Button = (ImageView) findViewById(R.id.Button_Sensor_Upload);
			imageView_Upload_Button.setEnabled(false); 
	    	TextView textView_Upload_Button = (TextView) findViewById(R.id.textView_ReportButton);
			textView_Upload_Button.setTextColor(Color.GRAY);

			//Change the alpha so the activity could restart after the status is uploaded  
			textView_Upload_Button.setAlpha((float) 0.98);
	    	
	    	statusDataSource.close();
	    }
	  }	
	
	private class UploadFromDBToServer extends AsyncTask<StatusObject, Void, String> {
		
		ProgressDialog dialog;
		Context context; // application context.
		ProgressBar progressBar_spinner_Upload;
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		int theOrderOfThisObject = 0;
		int totalNotUploadedYetObjects = 0;
		
		
		public UploadFromDBToServer(Activity activity, int theOrderOfThisObject, int totalNotUploadedYetObjects) {
			context = activity;
			dialog = new ProgressDialog(context);
			this.theOrderOfThisObject = theOrderOfThisObject;
			this.totalNotUploadedYetObjects = totalNotUploadedYetObjects;
		}
		
		protected void onPreExecute() {
			String dialogText = "Uploading previous collected statuses to the server" + Integer.toString(theOrderOfThisObject) + " of " + Integer.toString(totalNotUploadedYetObjects); 
//			String dialogText = "Uploading previous collected statuses to the server"; 
			this.dialog.setMessage(dialogText);
			this.dialog.setIndeterminate(true);
			this.dialog.setCancelable(false);
			this.dialog.show();
			
			progressBar_spinner_Upload = (ProgressBar) findViewById(R.id.progressBar_spinner_Upload);
			progressBar_spinner_Upload.setVisibility(View.VISIBLE);
		}
		
		@SuppressLint("SimpleDateFormat")
		@Override
		protected String doInBackground(StatusObject... statusObjectParameter) {
			String response = "";
			StatusObject statusObject = statusObjectParameter[0];
			
			DefaultHttpClient client = new DefaultHttpClient();
			HttpPost httppost = new HttpPost("http://group-status-376.appspot.com/groupstatus_server");

			nameValuePairs.add(new BasicNameValuePair("function", "upload"));
			nameValuePairs.add(new BasicNameValuePair("userID", statusObject.getUserID()));
			nameValuePairs.add(new BasicNameValuePair("group", statusObject.getGroup()));
			nameValuePairs.add(new BasicNameValuePair("timestamp", statusObject.getTimestamp()));
			nameValuePairs.add(new BasicNameValuePair("status", statusObject.getStatus()));
			nameValuePairs.add(new BasicNameValuePair("groupStatus", statusObject.getGroupStatus()));
			nameValuePairs.add(new BasicNameValuePair("wifiList", statusObject.getWifiList()));
			nameValuePairs.add(new BasicNameValuePair("noiseLevel", statusObject.getNoiseLevel()));
			nameValuePairs.add(new BasicNameValuePair("location", statusObject.getLocation()));
			nameValuePairs.add(new BasicNameValuePair("address", statusObject.getAddress()));
			
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
			
			return response;
		}
		
		@Override
		protected void onPostExecute(String result) {
			
			if (dialog.isShowing()) {
				dialog.dismiss();
			}
			
			progressBar_spinner_Upload.setVisibility(View.INVISIBLE);
			
		}
	}	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.log_out:
			Editor edit = PreferenceManager.getDefaultSharedPreferences(CollectStatusAndSensorData.this).edit();
			edit.clear();
			edit.apply();
			Intent intentI = new Intent(this, WelcomePage.class);
			startActivity(intentI);
			break;
		}
		return true;
	}
}
