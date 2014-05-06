package luci.uci.edu.groupstatus;

import SoundMeter.SoundMeter;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.widget.TextView;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaRecorder;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.util.Log;
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

	// Vars for WiFi
	WifiManager wifi;
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
				Log.i("WiFi", Integer.toString(mProgressStatusWiFi) + "%");

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
				Log.i("Noise", Integer.toString(mProgressStatusNoise) + "%");

				mProgressNoise.setProgress(mProgressStatusNoise > 100 ? 100 : mProgressStatusNoise);
				mHandlerNoise.postDelayed(updateProgressBarNoise, NOISE_POLL_INTERVAL);
			} else {
				mHandlerNoise.removeCallbacks(updateProgressBarNoise);
			}
		}
	};

	// Vars for Status
	Button buttonWiFi;
	Button buttonNoise;

	// Vars for location
	LocationManager locationManager;
	LocationListener locationListener;

	//Vars for Location progress bar
	private ProgressBar mProgressLocation;
	private int mProgressStatusLocation = 0;
	private Handler mHandlerLocation = new Handler();
	private int progressValueLocation = 4;
	private Runnable updateProgressBarLocation = new Runnable() {
		public void run() {
			if (mProgressStatusLocation < 80) {
				mProgressStatusLocation = mProgressStatusLocation + progressValueLocation / 2; //divided by 2 for smoothness
				Log.i("Location", Integer.toString(mProgressStatusLocation) + "%");

				mProgressLocation.setProgress(mProgressStatusLocation > 100 ? 100 : mProgressStatusLocation);
				mHandlerLocation.postDelayed(updateProgressBarLocation, 500 / 2); //divided by 2 for smoothness
			} else {
				mHandlerLocation.removeCallbacks(updateProgressBarLocation);
			}
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sensor_data_collector);

		// Vars for Status
		TextView textView = (TextView) findViewById(R.id.textViewTheStatus);
		if (getIntent().hasExtra("status")) {
			String status = getIntent().getExtras().getString("status");
			textView.setText(status);
		}

		// Vars for buttons
		//		buttonWiFi = (Button) findViewById(R.id.buttonWiFi);
		//		buttonWiFi.setOnClickListener(this);
		//		buttonNoise = (Button) findViewById(R.id.buttonNoise);
		//		buttonNoise.setOnClickListener(this);

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

		registerReceiver(new BroadcastReceiver() {
			@Override
			public void onReceive(Context c, Intent intent) {
				scannedWiFiResults = wifi.getScanResults();
				numberOfWiFiPointsFound = scannedWiFiResults.size();
			}
		}, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

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
		LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		LocationListener locationListener = new MyLocationListener();

		//Activate
		ScanWiFiAccess scanWiFiAccess = new ScanWiFiAccess(SensorDataCollector.this);
		scanWiFiAccess.execute(WIFI_POLL_Times);

		DetectBackgroundNoise detectBackgroundNoise = new DetectBackgroundNoise(SensorDataCollector.this);
		detectBackgroundNoise.execute(NOISE_POLL_Times);

		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, locationListener);

		Animation animation = AnimationUtils.loadAnimation(SensorDataCollector.this, R.anim.blink);

		TextView textviewWiFi = (TextView) findViewById(R.id.textView_WiFi_status);
		textviewWiFi.startAnimation(animation);

		TextView textviewNoise = (TextView) findViewById(R.id.textView_Noise_status);
		textviewNoise.startAnimation(animation);

		TextView textviewLocation = (TextView) findViewById(R.id.textView_Location_status);
		textviewLocation.startAnimation(animation);

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
		//        locationManager.removeUpdates(locationListener);
	}

	//reactivates listener when app is resumed
	@Override
	public void onResume() {
		super.onResume();
	}

	private class MyLocationListener implements LocationListener {

		@Override
		public void onLocationChanged(Location loc) {

			String latitude = Double.toString(loc.getLatitude());
			String longitude = Double.toString(loc.getLongitude());
			Log.i("location", latitude + "," + longitude);

			String stringForTextView = latitude.substring(0, 4) + " , " + longitude.substring(0, 7);

			GetAddressTask getAddressTask = new GetAddressTask(SensorDataCollector.this);
			getAddressTask.execute(loc);
		}

		@Override
		public void onProviderDisabled(String provider) {
		}

		@Override
		public void onProviderEnabled(String provider) {
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
		}
	}

	public void onClick(View view) {

		//		switch (view.getId()) {
		//		case R.id.buttonWiFi:
		//			mProgressWiFi = (ProgressBar) findViewById(R.id.progressBar_WiFi);
		//			mHandlerWiFi.post(updateProgressBarWiFi);
		//			break;
		//
		//		case R.id.buttonNoise:
		//			mProgressNoise = (ProgressBar) findViewById(R.id.progressBar_Noise);
		//			mHandlerNoise.post(updateProgressBarNoise);
		//			break;
		//		}
	}

	private class GetAddressTask extends AsyncTask<Location, Void, String> {
		Context mContext;

		public GetAddressTask(Context context) {
			mContext = context;
		}

		@Override
		protected String doInBackground(Location... params) {
			Geocoder gcd = new Geocoder(getBaseContext(), Locale.getDefault());
			Location loc = params[0];
			List<Address> addresses = null;
			String addressText = "";
			try {
				addresses = gcd.getFromLocation(loc.getLatitude(), loc.getLongitude(), 1);
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

		}

	}

	private class ScanWiFiAccess extends AsyncTask<Integer, Void, Integer> {

		private ProgressDialog dialog;
		private Context context; // application context.

		public ScanWiFiAccess(Activity activity) {
			context = activity;
			dialog = new ProgressDialog(context);
		}

		protected void onPreExecute() {

			Log.i("Wifi", "start");

			// this.dialog.setMessage("Scanning WiFi Networks");
			// this.dialog.show();
			// try {
			// Thread.sleep(100);
			// } catch (InterruptedException e) {
			// e.printStackTrace();
			// }
		}

		@Override
		protected Integer doInBackground(Integer... totalCounts) {

			int returnValue = 0;
			for (int counts : totalCounts) {
				while (counts > 0) {
					Log.i("Wifi", Integer.toString(counts));
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
					Log.i("numberOfWiFiPointsFound", "numberOfWiFiPointsFound=" + Integer.toString(numberOfWiFiPointsFound));
					try {
						numberOfWiFiPointsFound = numberOfWiFiPointsFound - 1;

						while (numberOfWiFiPointsFound >= 0) {
							// String wiFiAccessPoint =
							// scannedWiFiResults.get(numberOfWiFiPointsFound).SSID
							// + "  "
							// +
							// scannedWiFiResults.get(numberOfWiFiPointsFound).capabilities;
							String wiFiAccessPoint = scannedWiFiResults.get(numberOfWiFiPointsFound).SSID + ";"
									+ scannedWiFiResults.get(numberOfWiFiPointsFound).BSSID + ";"
									+ scannedWiFiResults.get(numberOfWiFiPointsFound).level;

							if (!myList.contains(wiFiAccessPoint)) {
								HashMap<String, String> item = new HashMap<String, String>();
								item.put(WIFI_ITEM_KEY, wiFiAccessPoint);
								arraylistForWiFiResult.add(item);
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

			Log.i("Wifi", "stop");

			TextView textviewWiFi = (TextView) findViewById(R.id.textView_WiFi_status);
			textviewWiFi.clearAnimation();
			textviewWiFi.setText("Updated");

			ImageView imageViewWiFi = (ImageView) findViewById(R.id.progressFinishedIcon_WiFi);
			imageViewWiFi.setVisibility(View.VISIBLE);

			// if (dialog.isShowing()) {
			// dialog.dismiss();
			// }
		}
	}

	private class DetectBackgroundNoise extends AsyncTask<Integer, Void, Integer> {

		private ProgressDialog dialog;
		private Context context; // application context.

		DecimalFormat decimalFormat = new DecimalFormat("#.00");
		// 00 means exactly two decimal places; # means "optional" digit and it
		// will drop trailing zeroes
		// Double count = .0;
		// Double sum = .0;
		List<Double> soundLevelList = new ArrayList<Double>();

		public DetectBackgroundNoise(Activity activity) {
			context = activity;
			dialog = new ProgressDialog(context);
		}

		protected void onPreExecute() {
			mSensor.start();
			Log.i("Noise", "start");

			// this.dialog.setMessage("Scanning WiFi Networks");
			// this.dialog.show();
			// try {
			// Thread.sleep(100);
			// } catch (InterruptedException e) {
			// e.printStackTrace();
			// }
		}

		@Override
		protected Integer doInBackground(Integer... totalCounts) {

			int counts = totalCounts[0];
			while (counts > 0) {
				Log.i("Noise", Integer.toString(counts));
				counts--;
				double amp = mSensor.getAmplitude();
				amp = Double.parseDouble(decimalFormat.format(amp));

				soundLevelList.add(amp);
				HashMap<String, String> item = new HashMap<String, String>();
				item.put(NOISE_ITEM_KEY, String.valueOf(amp));
				arraylistForNoiseResult.add(item);

				// count++;
				// sum += amp;

				try {
					Thread.sleep(NOISE_POLL_INTERVAL);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			arraylistForNoiseResult.remove(0); // drop the first one whose
												// result is always zero for
												// some reasons
			return 0;

		}

		@Override
		protected void onPostExecute(Integer counts) {

			mSensor.stop();

			adapterForNoiseResult.notifyDataSetChanged();
			mProgressStatusNoise = 100;
			mProgressNoise.setProgress(100);

			Log.i("Noise", "stop");

			TextView textviewNoise = (TextView) findViewById(R.id.textView_Noise_status);
			textviewNoise.clearAnimation();
			textviewNoise.setText("Updated");
			ImageView imageViewNoise = (ImageView) findViewById(R.id.progressFinishedIcon_Noise);
			imageViewNoise.setVisibility(View.VISIBLE);

			// if (dialog.isShowing()) {
			// dialog.dismiss();
			// }
		}
	}

}