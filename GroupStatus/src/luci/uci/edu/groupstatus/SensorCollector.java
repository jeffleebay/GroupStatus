package luci.uci.edu.groupstatus;

import SoundMeter.SoundMeter;
import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

import java.io.IOException;
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
	ListView lv;
	int size = 0;
	List<ScanResult> results;
	String ITEM_KEY = "wifi";
	ArrayList<HashMap<String, String>> arraylist = new ArrayList<HashMap<String, String>>();
	SimpleAdapter adapter;

	// Vars for Noise
	Button buttonRecord;
	Button buttonStop;
	private static final int POLL_INTERVAL = 300;
	private SoundMeter mSensor;
	private Handler mHandler = new Handler();
	Double count=.0;
	Double sum=.0;
	private Runnable mPollTask = new Runnable() {
		public void run() {
			double amp = mSensor.getAmplitude();
			TextView textView1 = (TextView) findViewById(R.id.TextViewNoiseDegree);
			textView1.setText(String.valueOf(amp));
			
			count++;
			sum+=amp;
			TextView textView2 = (TextView) findViewById(R.id.TextViewNoiseDegreeSum);
			textView2.setText(String.valueOf(sum/count));

			mHandler.postDelayed(mPollTask, POLL_INTERVAL);
		}
	};

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
		lv = (ListView) findViewById(R.id.listWiFi);

		wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		if (wifi.isWifiEnabled() == false) {
			Toast.makeText(getApplicationContext(),
					"wifi is disabled..making it enabled", Toast.LENGTH_LONG)
					.show();
			wifi.setWifiEnabled(true);
		}
		this.adapter = new SimpleAdapter(SensorCollector.this, arraylist,
				android.R.layout.simple_list_item_1, new String[] { ITEM_KEY },
				new int[] { android.R.id.text1 });
		lv.setAdapter(this.adapter);

		registerReceiver(new BroadcastReceiver() {
			@Override
			public void onReceive(Context c, Intent intent) {
				results = wifi.getScanResults();
				size = results.size();
			}
		}, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

		// ScanWiFiAccess task = new ScanWiFiAccess(SensorCollector.this);
		// task.execute(5);

		// Vars for Noise
		mSensor = new SoundMeter();

		// Vars for location

	}

	public void onClick(View view) {

		// ScanWiFiAccess task = new ScanWiFiAccess(SensorCollector.this);
		// task.execute(10);

		switch (view.getId()) {
		case R.id.buttonRecord:
			mSensor.start();
			Log.i("mSensor", "start");
			mHandler.postDelayed(mPollTask, POLL_INTERVAL);
			break;
		case R.id.buttonStop:

			mHandler.removeCallbacks(mPollTask);

			double degree = .0;
			degree = mSensor.getAmplitude();
			Log.i("degree", String.valueOf(degree));
			// degree= 100*degree/32768;
			mSensor.stop();
			Log.i("mSensor", "stop");

			TextView textView = (TextView) findViewById(R.id.TextViewNoiseDegree);
			textView.setText(String.valueOf(degree));
			break;
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
			this.dialog.setMessage("Scanning WiFi Networks");
			this.dialog.show();
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		@Override
		protected Integer doInBackground(Integer... totalCounts) {
			int returnValue = 0;
			for (int counts : totalCounts) {
				returnValue = counts - 1;
				wifi.startScan();

				ListView tempLV = (ListView) findViewById(R.id.listWiFi);
				List<String> myList = new ArrayList<String>();

				int itemCount = tempLV.getCount();

				if (itemCount > 0) {
					Log.i("line", Integer.toString(143));
					while (itemCount > 0) {
						@SuppressWarnings("unchecked")
						HashMap<String, String> item = (HashMap<String, String>) tempLV
								.getItemAtPosition(itemCount - 1);
						myList.add(item.get("wifi"));
						// Log.i("WiFiPoint", myList.get(myList.size()-1));

						itemCount--;
					}
				}
				Log.i("size", "size=" + Integer.toString(size));

				try {
					size = size - 1;

					while (size >= 0) {
						// Log.i("line", Integer.toString(160));
						String wiFiAccessPoint = results.get(size).SSID + "  "
								+ results.get(size).capabilities;
						// String wiFiAccessPoint= ITEM_KEY,
						// results.get(size).BSSID);
						if (!myList.contains(wiFiAccessPoint)) {
							// Log.i("wifi", "update");
							HashMap<String, String> item = new HashMap<String, String>();
							item.put(ITEM_KEY, wiFiAccessPoint);
							arraylist.add(item);
						}
						size--;
					}

				} catch (Exception e) {
				}
			}
			return returnValue;
		}

		@Override
		protected void onPostExecute(Integer counts) {

			Log.i("counts", Integer.toString(counts));

			if (counts > 0) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				wifi.startScan();
				new ScanWiFiAccess(SensorCollector.this).execute(counts);
			}
			if (dialog.isShowing()) {
				dialog.dismiss();
			}
			adapter.notifyDataSetChanged();
		}
	}

}