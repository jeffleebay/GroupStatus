package luci.uci.edu.groupstatus;

import SoundMeter.SoundMeter;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaRecorder;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.text.format.Time;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.Toast;

public class SensorDataCollector extends Activity implements OnClickListener {
	
	//Vars for storing collected data
	Boolean DEVELOPER_MODE = false;
	int asyncTasksProgress = 0;
	HashMap<String, String> SensorResult = new HashMap<String, String>();
	String keys[] = { "status", "groupStatus", "wifiList", "noiseLevel", "location", "address" };

	// Vars for WiFi
	WifiManager wifi;
	BroadcastReceiver wifiBroadcastReceiver;
	ListView listViewForWiFiResults;
	ArrayList<HashMap<String, String>> arraylistForWiFiResult = new ArrayList<HashMap<String, String>>();
	SimpleAdapter adapterForWiFiResult;
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
	private Runnable updateProgressBarWiFi = new Runnable() {
		public void run() {
			if (mProgressStatusWiFi < 80) {
				mProgressStatusWiFi = mProgressStatusWiFi + progressValueWiFi / 2; //divided by 2 for smoothness
//				Log.i("WiFi", Integer.toString(mProgressStatusWiFi) + "%");

				mProgressWiFi.setProgress(mProgressStatusWiFi > 100 ? 100 : mProgressStatusWiFi);
				mHandlerWiFi.postDelayed(updateProgressBarWiFi, WIFI_POLL_INTERVAL / 2);
			} else {
				mHandlerWiFi.removeCallbacks(updateProgressBarWiFi);
			}
		}
	};

	// Vars for Noise

	private static final int NOISE_POLL_Time_Interval = 10; // seconds
	private static final int NOISE_POLL_INTERVAL = 300; // milliseconds
	private static final int NOISE_POLL_Times = 1000 * NOISE_POLL_Time_Interval / NOISE_POLL_INTERVAL;
	private SoundMeter mSensor;
	// private Handler mHandler = new Handler();
	ListView listViewForNoiseResult;
	ArrayList<HashMap<String, String>> arraylistForNoiseResult = new ArrayList<HashMap<String, String>>();
	SimpleAdapter adapterForNoiseResult;
	String NOISE_ITEM_KEY = "noise";

	//Vars for Noise progress bar

	private ProgressBar mProgressNoise;
	private int mProgressStatusNoise = 0;
	private Handler mHandlerNoise = new Handler();
	private int progressValueNoise = (int) ((int) 100 / (NOISE_POLL_Times * 1.5)); //There are some delay in the asynctask for noise
	private Runnable updateProgressBarNoise = new Runnable() {
		public void run() {
			if (mProgressStatusNoise < 80) {
				mProgressStatusNoise = mProgressStatusNoise + progressValueNoise;
//				Log.i("Noise", Integer.toString(mProgressStatusNoise) + "%");

				mProgressNoise.setProgress(mProgressStatusNoise > 100 ? 100 : mProgressStatusNoise);
				mHandlerNoise.postDelayed(updateProgressBarNoise, NOISE_POLL_INTERVAL);
			} else {
				mHandlerNoise.removeCallbacks(updateProgressBarNoise);
			}
		}
	};

	// Vars for Status
	TextView textViewReportButton;
	ImageView imageViewReportButton;

	// Vars for location
//	LocationManager locationManager;
//	LocationListener locationListener;

	//Vars for Location progress bar
	private ProgressBar mProgressLocation;
	private int mProgressStatusLocation = 0;
	private Handler mHandlerLocation = new Handler();
	private int progressValueLocation = 4;
	private Runnable updateProgressBarLocation = new Runnable() {
		public void run() {
			if (mProgressStatusLocation < 80) {
				mProgressStatusLocation = mProgressStatusLocation + progressValueLocation / 2; //divided by 2 for smoothness
//				Log.i("Location", Integer.toString(mProgressStatusLocation) + "%");

				mProgressLocation.setProgress(mProgressStatusLocation > 100 ? 100 : mProgressStatusLocation);
				mHandlerLocation.postDelayed(updateProgressBarLocation, 500 / 2); //divided by 2 for smoothness
			} else {
				mHandlerLocation.removeCallbacks(updateProgressBarLocation);
			}
		}
	};
	
	//Vars for Async Tasks
	ScanWiFiAccess scanWiFiAccess;
	DetectBackgroundNoise detectBackgroundNoise;
	GetAddressTask getAddressTask;
	CheckUpdatingProgress checkUpdatingProgress;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sensor_data_collector);
		
		if (!DEVELOPER_MODE) TurnOffDeveloperMode();

		// Vars for Status
		TextView textView = (TextView) findViewById(R.id.textViewTheStatus);
		if (getIntent().hasExtra("status")) {
			String status = getIntent().getExtras().getString("status");
			textView.setText(status);	
			SensorResult.put("status", status);
		}else{
			SensorResult.put(keys[0], "Coding in Vista del Campo");
		}
		
		if (getIntent().hasExtra("groupStatus")) {
			String groupStatus = getIntent().getExtras().getString("groupStatus");
			SensorResult.put("groupStatus", groupStatus);
		}else{
			SensorResult.put(keys[1], "No Guess");
		}

		// Vars for buttons
		textViewReportButton = (TextView) findViewById(R.id.textView_ReportButton);
		textViewReportButton.setVisibility(View.INVISIBLE);
		imageViewReportButton = (ImageView) findViewById(R.id.area_Button);
		imageViewReportButton.setVisibility(View.INVISIBLE);
		imageViewReportButton.setOnClickListener(this);

		// Vars for WiFi
		listViewForWiFiResults = (ListView) findViewById(R.id.list_WiFi);
		listViewForWiFiResults.setOnTouchListener(new ListView.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				int action = event.getAction();
				switch (action) {
				case MotionEvent.ACTION_DOWN: // Disallow ScrollView to intercept touch events.
					v.getParent().requestDisallowInterceptTouchEvent(true);
					break;

				case MotionEvent.ACTION_UP:   // Allow ScrollView to intercept touch events.
					v.getParent().requestDisallowInterceptTouchEvent(false);
					break;
				}
				v.onTouchEvent(event);		// Handle ListView touch events.
				return true;
			}
		});

		wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		if (wifi.isWifiEnabled() == false) {
			Toast.makeText(getApplicationContext(), "wifi is disabled..making it enabled", Toast.LENGTH_LONG).show();
			wifi.setWifiEnabled(true);
		}
		this.adapterForWiFiResult = new SimpleAdapter(SensorDataCollector.this, arraylistForWiFiResult, android.R.layout.simple_list_item_1,
				new String[] { WIFI_ITEM_KEY }, new int[] { android.R.id.text1 });
		listViewForWiFiResults.setAdapter(this.adapterForWiFiResult);

		wifiBroadcastReceiver = new BroadcastReceiver() 
		{
			@Override
			public void onReceive(Context c, Intent intent) {
				scannedWiFiResults = wifi.getScanResults();
				numberOfWiFiPointsFound = scannedWiFiResults.size();
			}
		}; 
		registerReceiver(wifiBroadcastReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

		// Vars for Noise
		listViewForNoiseResult = (ListView) findViewById(R.id.list_Noise);
		listViewForNoiseResult.setOnTouchListener(new ListView.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				int action = event.getAction();
				switch (action) {
				case MotionEvent.ACTION_DOWN: // Disallow ScrollView to intercept touch events.
					v.getParent().requestDisallowInterceptTouchEvent(true);
					break;

				case MotionEvent.ACTION_UP:   // Allow ScrollView to intercept touch events.
					v.getParent().requestDisallowInterceptTouchEvent(false);
					break;
				}
				v.onTouchEvent(event);		// Handle ListView touch events.
				return true;
			}
		});
		
		this.adapterForNoiseResult = new SimpleAdapter(SensorDataCollector.this, arraylistForNoiseResult, android.R.layout.simple_list_item_1,
				new String[] { NOISE_ITEM_KEY }, new int[] { android.R.id.text1 });
		listViewForNoiseResult.setAdapter(this.adapterForNoiseResult);

		mSensor = new SoundMeter();

		// Vars for location
		

		//Activate
		
		scanWiFiAccess = new ScanWiFiAccess(SensorDataCollector.this);
		scanWiFiAccess.execute(WIFI_POLL_Times);

		detectBackgroundNoise = new DetectBackgroundNoise(SensorDataCollector.this);
		detectBackgroundNoise.execute(NOISE_POLL_Times);

		getAddressTask = new GetAddressTask(SensorDataCollector.this);
		getAddressTask.execute(0);
		
		checkUpdatingProgress = new CheckUpdatingProgress();
		checkUpdatingProgress.execute(0);
		
		TextView textviewWiFi = (TextView) findViewById(R.id.textView_WiFi_status);
		textviewWiFi.startAnimation((Animation) AnimationUtils.loadAnimation(SensorDataCollector.this, R.anim.blink));

		TextView textviewNoise = (TextView) findViewById(R.id.textView_Noise_status);
		textviewNoise.startAnimation((Animation) AnimationUtils.loadAnimation(SensorDataCollector.this, R.anim.blink));

		TextView textviewLocation = (TextView) findViewById(R.id.textView_Location_status);
		textviewLocation.startAnimation((Animation) AnimationUtils.loadAnimation(SensorDataCollector.this, R.anim.blink));

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
	}

	@Override
	public void onResume() {
		super.onResume();
	}
	
	@Override
	public void onStop() {
		unregisterReceiver(wifiBroadcastReceiver);
		super.onStop();
	}
	
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		this.finish();
	}
	
	public void onClick(View view) {

		switch (view.getId()) {
		case R.id.area_Button:
//			Intent intent = new Intent(SensorDataCollector.this, ResultReporter.class);
//            intent.putExtra("results", SensorResult);
//            startActivity(intent);
			UploadToServer uploadToServer = new UploadToServer(SensorDataCollector.this);
			uploadToServer.execute(" ");	
			
			break;

		}
	}
	
	public void TurnOffDeveloperMode(){
//		TextView tv;
//		ImageView iv;
		findViewById(R.id.area_SnL).setVisibility(View.GONE);
		findViewById(R.id.area_shadow_buttom_SnL).setVisibility(View.GONE);
		findViewById(R.id.area_shadow_side_SnL).setVisibility(View.GONE);
//		findViewById(R.id.space_ListView).setVisibility(View.GONE);        //kept visible for margin bottom 
		findViewById(R.id.linearLayout_SnL_S).setVisibility(View.GONE);
		findViewById(R.id.divider_SnL).setVisibility(View.GONE);
		findViewById(R.id.linearLayout_SnL_L).setVisibility(View.GONE);
		
		findViewById(R.id.area_ListView).setVisibility(View.GONE);
		findViewById(R.id.area_shadow_buttom_ListView).setVisibility(View.GONE);
		findViewById(R.id.area_shadow_side_ListView).setVisibility(View.GONE);
		findViewById(R.id.space_ListView).setVisibility(View.GONE);
		findViewById(R.id.linearLayout_Listview).setVisibility(View.GONE);
		findViewById(R.id.space_ButtomOfThePage).setVisibility(View.GONE);
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
		}
		
		protected void onPreExecute() {

//			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, locationListener);
			locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, locationListener, null);
			location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
//			Log.i("location", location.getLatitude() + "," + location.getLongitude());
			SensorResult.put("location", location.getLatitude() + "," + location.getLongitude());
		}

		@Override
		protected String doInBackground(Integer... params) {
			Geocoder gcd = new Geocoder(getBaseContext(), Locale.getDefault());
			int par = params[0];
			List<Address> addresses = null;
			String addressText = "";
			try {
				addresses = gcd.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (addresses.size() > 0) {

				Address address = addresses.get(0);
				addressText = String.format("%s: %s, %s, %s, %s", address.getFeatureName(),
						address.getMaxAddressLineIndex() > 0 ? address.getAddressLine(0) : "", // Add it if there's a street address
						address.getLocality(), // Locality is usually a city
						address.getCountryName(), // The country of the address
						address.getPostalCode());
			} else {
				addressText = "; No address found";
			}
			return addressText;
		}

		@Override
		protected void onPostExecute(String address) {

			TextView textViewTheLocation = (TextView) findViewById(R.id.TextView_TheLocation);
			textViewTheLocation.setText(address);

			mProgressStatusLocation = 100;
			mProgressLocation.setProgress(100);

			TextView textviewLocation = (TextView) findViewById(R.id.textView_Location_status);
			textviewLocation.clearAnimation();
			textviewLocation.setText("Updated");
			ImageView imageViewLocation = (ImageView) findViewById(R.id.progressFinishedIcon_Location);
			imageViewLocation.setVisibility(View.VISIBLE);
			asyncTasksProgress++;
			
			SensorResult.put("address", address);

//			Log.i("address", address);
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

					ListView tempLV = (ListView) findViewById(R.id.list_WiFi);
					List<String> myList = new ArrayList<String>();

					int itemCount = tempLV.getCount();

					if (itemCount > 0) {
						// Log.i("line", Integer.toString(204));
						while (itemCount > 0) {
							@SuppressWarnings("unchecked")
							HashMap<String, String> item = (HashMap<String, String>) tempLV.getItemAtPosition(itemCount - 1);
							myList.add(item.get("wifi"));

							itemCount--;
						}
					}
//					Log.i("numberOfWiFiPointsFound", "numberOfWiFiPointsFound=" + Integer.toString(numberOfWiFiPointsFound));
					try {
						numberOfWiFiPointsFound = numberOfWiFiPointsFound - 1;

						while (numberOfWiFiPointsFound >= 0) {
							 String wiFiAccessPoint =  scannedWiFiResults.get(numberOfWiFiPointsFound).BSSID;
							// + "  "  +  scannedWiFiResults.get(numberOfWiFiPointsFound).capabilities;
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

			adapterForWiFiResult.notifyDataSetChanged();
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

			adapterForNoiseResult.notifyDataSetChanged();
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

			imageViewReportButton.setVisibility(View.VISIBLE);
			imageViewReportButton.startAnimation((Animation) AnimationUtils.loadAnimation(SensorDataCollector.this, R.anim.fade_in));
			try {
				Thread.sleep(500);
			} catch (Exception e) {
				e.printStackTrace();
			}
			textViewReportButton.setVisibility(View.VISIBLE);
			textViewReportButton.startAnimation((Animation) AnimationUtils.loadAnimation(SensorDataCollector.this, R.anim.fade_in));

		}
	}
	
	private class UploadToServer extends AsyncTask<String, Void, String> {
		
	    private ProgressDialog dialog;
        private Context context; // application context.
        private ProgressBar progressBar_spinner_Upload;

        
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
	    	for (String userProfile : userProfiles) {
		        DefaultHttpClient client = new DefaultHttpClient();
		        
		        // Add sensor data       
		        HttpPost httppost = new HttpPost("http://group-status-376.appspot.com/groupstatus_server");		        	        
		        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		        
		        nameValuePairs.add(new BasicNameValuePair("function", "upload"));
		        for (int i = 0; i < keys.length; i++){
		        	nameValuePairs.add(new BasicNameValuePair(keys[i], SensorResult.get(keys[i])));
					Log.i(keys[i], SensorResult.get(keys[i]).toString());
		        }
		        
		        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
				String userID = settings.getString("userIDforGroupStatus", "n/a");
				String group = settings.getString("groupUserBelondedTo", "n/a");

				nameValuePairs.add(new BasicNameValuePair("userID", userID));
				nameValuePairs.add(new BasicNameValuePair("group", group));

				Time today = new Time(Time.getCurrentTimezone());
				today.setToNow(); 
				
				String currentTime = today.month + "/" + today.monthDay + "," + today.format("%k:%M:%S");

				nameValuePairs.add(new BasicNameValuePair("timestamp", currentTime));
			
				
//		        nameValuePairs.add(new BasicNameValuePair("groupStatus", "I don't know what others are doing!"));
		        
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
	    	
	    	progressBar_spinner_Upload.setVisibility(View.INVISIBLE);
	    	
	    	if(result.endsWith("success")){	    			    	
//	    		final Toast toast = Toast.makeText(getApplicationContext(),"Successfully Uploaded", Toast.LENGTH_SHORT);
//		    	toast.setGravity(Gravity.CENTER, 0, 100);
//		    	toast.show();
//		    	
//		    	//The count down timer is used for controlling display time more specifically -> for aesthetics 
//		    	
//		    	new CountDownTimer(50, 500) {						//duration = 100
//			        public void onTick(long millisUntilFinished) {
//			            toast.show();
//			        }
//			        public void onFinish() {
//			            toast.cancel();
//			        }
//			    }.start();
			    
	    		
			    ImageView imageView_checked_Upload = (ImageView) findViewById(R.id.imageView_checked_Upload);
			    imageView_checked_Upload.setVisibility(View.VISIBLE);
			    
		    	
	    	}else{
	    		Toast toast = Toast.makeText(getApplicationContext(),"Upload error. Invalid username or password.", Toast.LENGTH_SHORT);
	    		toast.setGravity(Gravity.CENTER, 0, 100);
		    	toast.show();
	    	}
		    	
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
			Editor edit = PreferenceManager.getDefaultSharedPreferences(SensorDataCollector.this).edit();
			edit.clear();
			edit.apply();
			Intent intentI = new Intent(this, WelcomePage.class);
			startActivity(intentI);
			break;
		}
		return true;
	}
}