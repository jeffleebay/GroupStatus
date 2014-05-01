package luci.uci.edu.groupstatus;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class SensorCollector extends Activity implements OnClickListener {

	WifiManager wifi;
	ListView lv;
	Button buttonScan;
	int size = 0;
	List<ScanResult> results;

	String ITEM_KEY = "wifi";
	ArrayList<HashMap<String, String>> arraylist = new ArrayList<HashMap<String, String>>();
	SimpleAdapter adapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.collect_sensor);

		String status = getIntent().getExtras().getString("status");
		TextView textView = (TextView) findViewById(R.id.textViewTheStatus);
		textView.setText(status);

		buttonScan = (Button) findViewById(R.id.buttonScan);
		buttonScan.setOnClickListener(this);
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

//		for (int i = 0; i < 10; i++) {
//			buttonScan.performClick();
//			try {
//				Thread.sleep(500);
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}

	}

	public void onClick(View view) {
		// arraylist.clear();
		wifi.startScan();

		Toast.makeText(this, "Scanning...." + size, Toast.LENGTH_SHORT).show();

		ListView tempLV = (ListView) findViewById(R.id.listWiFi);
		List<String> myList = new ArrayList<String>();

		int itemCount = tempLV.getCount();

		if (itemCount > 0) {
			while (itemCount > 0) {
				@SuppressWarnings("unchecked")
				HashMap<String, String> item = (HashMap<String, String>) tempLV
						.getItemAtPosition(itemCount - 1);
				myList.add(item.get("wifi"));
				// Log.i("WiFiPoint", myList.get(myList.size()-1));

				itemCount--;
			}
		}

		try {
			size = size - 1;

			while (size >= 0) {

				String wiFiAccessPoint = results.get(size).SSID + "  "
						+ results.get(size).capabilities;
				// String wiFiAccessPoint= ITEM_KEY, results.get(size).BSSID);
				if (!myList.contains(wiFiAccessPoint)) {

					HashMap<String, String> item = new HashMap<String, String>();
					item.put(ITEM_KEY, wiFiAccessPoint);
					arraylist.add(item);
					adapter.notifyDataSetChanged();
				}
				size--;
			}
		} catch (Exception e) {
		}
	}
}