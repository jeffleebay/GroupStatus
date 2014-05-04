package luci.uci.edu.groupstatus;

import SoundMeter.SoundMeter;
import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaRecorder;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

public class SensorCollector extends Activity implements OnClickListener {

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

	// Vars for Noise
	Button buttonRecord;
	Button buttonStop;
	private static final int NOISE_POLL_Time_Interval = 10; //seconds
	private static final int NOISE_POLL_INTERVAL = 600; //milliseconds
	private static final int NOISE_POLL_Time = 1000*NOISE_POLL_Time_Interval/NOISE_POLL_INTERVAL;
	private SoundMeter mSensor;
	// private Handler mHandler = new Handler();
	ListView listViewForNoiseResult;
	ArrayList<HashMap<String, String>> arraylistForNoiseResult = new ArrayList<HashMap<String, String>>();
	SimpleAdapter adapterForNoiseResult;
	String NOISE_ITEM_KEY = "noise";

	// private Runnable mPollTask = new Runnable() {
	// public void run() {
	// double amp = mSensor.getAmplitude();
	// amp = Double.parseDouble(decimalFormat.format(amp));
	// TextView textView1 = (TextView) findViewById(R.id.TextViewNoiseDegree);
	// textView1.setText(String.valueOf(amp));
	//
	// soundLevelList.add(amp);
	// HashMap<String, String> item = new HashMap<String, String>();
	// item.put(ITEM_KEY, String.valueOf(amp));
	// arraylistForNoiseResult.add(item);
	// adapterForNoiseResult.notifyDataSetChanged();
	//
	// count++;
	// sum += amp;
	// TextView textView2 = (TextView)
	// findViewById(R.id.TextViewNoiseDegreeSum);
	// textView2.setText(String.valueOf(Double.parseDouble(decimalFormat.format(sum
	// / count))));
	//
	// mHandler.postDelayed(mPollTask, POLL_INTERVAL);
	// }
	// };

	// Vars for Status
	// Vars for location

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.collect_sensor);

		// Vars for Status
		String status = getIntent().getExtras().getString("status");
		TextView textView = (TextView) findViewById(R.id.textViewTheStatus);
		textView.setText(status);

		// Vars for Noise
		buttonRecord = (Button) findViewById(R.id.buttonRecord);
		buttonRecord.setOnClickListener(this);
		buttonStop = (Button) findViewById(R.id.buttonStop);
		buttonStop.setOnClickListener(this);

		// Vars for WiFi
		listViewForWiFiResults = (ListView) findViewById(R.id.listWiFi);

		wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		if (wifi.isWifiEnabled() == false) {
			Toast.makeText(getApplicationContext(), "wifi is disabled..making it enabled", Toast.LENGTH_LONG).show();
			wifi.setWifiEnabled(true);
		}
		this.adapterForWiFiResult = new SimpleAdapter(SensorCollector.this, arraylistForWiFiResult, android.R.layout.simple_list_item_1,
				new String[] { WIFI_ITEM_KEY }, new int[] { android.R.id.text1 });
		listViewForWiFiResults.setAdapter(this.adapterForWiFiResult);

		registerReceiver(new BroadcastReceiver() {
			@Override
			public void onReceive(Context c, Intent intent) {
				scannedWiFiResults = wifi.getScanResults();
				numberOfWiFiPointsFound = scannedWiFiResults.size();
			}
		}, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

		// ScanWiFiAccess task = new ScanWiFiAccess(SensorCollector.this);
		// task.execute(WIFI_POLL_Times);

		// Vars for Noise
		mSensor = new SoundMeter();

		listViewForNoiseResult = (ListView) findViewById(R.id.listNoise);
		this.adapterForNoiseResult = new SimpleAdapter(SensorCollector.this, arraylistForNoiseResult, android.R.layout.simple_list_item_1,
				new String[] { NOISE_ITEM_KEY }, new int[] { android.R.id.text1 });
		listViewForNoiseResult.setAdapter(this.adapterForNoiseResult);

		// buttonRecord.performClick();

		DetectBackgroundNoise task = new DetectBackgroundNoise(SensorCollector.this);
		task.execute(NOISE_POLL_Time);
		// Vars for location

	}

	public void onClick(View view) {

		// switch (view.getId()) {
		// case R.id.buttonRecord:
		// mSensor.start();
		// Log.i("mSensor", "start");
		// mHandler.postDelayed(mPollTask, POLL_INTERVAL);
		// break;
		// case R.id.buttonStop:
		//
		// mHandler.removeCallbacks(mPollTask);
		//
		// double degree = .0;
		// degree = mSensor.getAmplitude();
		// Log.i("degree", String.valueOf(degree));
		// // degree= 100*degree/32768;
		// mSensor.stop();
		// Log.i("mSensor", "stop");
		//
		// TextView textView = (TextView)
		// findViewById(R.id.TextViewNoiseDegree);
		// textView.setText(String.valueOf(degree));
		// break;
		// }
	}

	private class ScanWiFiAccess extends AsyncTask<Integer, Void, Integer> {

		private ProgressDialog dialog;
		private Context context; // application context.

		public ScanWiFiAccess(Activity activity) {
			context = activity;
			dialog = new ProgressDialog(context);
		}

		protected void onPreExecute() {
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
					Log.i("counts", Integer.toString(counts));
					counts--;
					wifi.startScan();

					ListView tempLV = (ListView) findViewById(R.id.listWiFi);
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
							String wiFiAccessPoint = scannedWiFiResults.get(numberOfWiFiPointsFound).SSID + "  "
									+ scannedWiFiResults.get(numberOfWiFiPointsFound).capabilities;
							// String wiFiAccessPoint=
							// ITEM_KEY,scannedWiFiResults.get(numberOfWiFiPointsFound).BSSID);

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
		// will
		// drop trailing zeroes
		Double count = .0;
		Double sum = .0;
		List<Double> soundLevelList = new ArrayList<Double>();

		public DetectBackgroundNoise(Activity activity) {
			context = activity;
			dialog = new ProgressDialog(context);
		}

		protected void onPreExecute() {
			mSensor.start();
			Log.i("mSensor", "start");

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

			// double redundantAmp = mSensor.getAmplitude();
			// redundantAmp =
			// Double.parseDouble(decimalFormat.format(redundantAmp));
			//
			// soundLevelList.add(redundantAmp);
			// HashMap<String, String> redundantItem = new HashMap<String,
			// String>();
			// redundantItem.put(NOISE_ITEM_KEY, String.valueOf(redundantAmp));
			// arraylistForNoiseResult.add(redundantItem);

			int counts = totalCounts[0];
			while (counts > 0) {
				Log.i("counts", Integer.toString(counts));
				counts--;
				double amp = mSensor.getAmplitude();
				amp = Double.parseDouble(decimalFormat.format(amp));

				soundLevelList.add(amp);
				HashMap<String, String> item = new HashMap<String, String>();
				item.put(NOISE_ITEM_KEY, String.valueOf(amp));
				arraylistForNoiseResult.add(item);

				count++;
				sum += amp;

				try {
					Thread.sleep(NOISE_POLL_INTERVAL);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			arraylistForNoiseResult.remove(0); //drop the first one whose result is always zero for some reasons
			return 0;

		}

		@Override
		protected void onPostExecute(Integer counts) {

			double degree = .0;
			degree = mSensor.getAmplitude();
			degree = Double.parseDouble(decimalFormat.format(degree));
			Log.i("degree", String.valueOf(degree));
			// degree= 100*degree/32768;
			mSensor.stop();
			Log.i("mSensor", "stop");

			TextView textView2 = (TextView) findViewById(R.id.TextViewNoiseDegreeSum);
			textView2.setText(String.valueOf(Double.parseDouble(decimalFormat.format(sum / count))));

			TextView textView = (TextView) findViewById(R.id.TextViewNoiseDegree);
			textView.setText(String.valueOf(degree));

			adapterForNoiseResult.notifyDataSetChanged();

			// if (dialog.isShowing()) {
			// dialog.dismiss();
			// }
		}
	}

}