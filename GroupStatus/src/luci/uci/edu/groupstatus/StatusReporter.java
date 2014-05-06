package luci.uci.edu.groupstatus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class StatusReporter extends Activity {

	ListView listView1;
	ListView listView2;
	ListView listView3;
	String statusList[] = { "Meeting", "Exercise", "Relaxing", "LUCI", "Car",
			"With", "Mike", "In", "On", "Lunch", "Office", "By Myself",
			"Campus", "Jeff", "Coffee", "Feeling", "Happy", "Tired", "Working",
			"Reading", "DBH", "Irvine", "Class", "Discussing" };
	String statusList1[] = { "Meeting", "Exercise", "Relaxing",
			"Feeling happy", "Feeling tired", "Feeling hungry", "Working",
			"Reading", "Discussing", "Waiting", "Taking a bath", "Sleeping",
			"Packing", "Revising", "Drinking hot chocolate", "Taking a break",
			"Booking tickets", "Leaving for", "Waiting for the ceremony",
			"Proofreading", "Writing", "Browsing", "Twittering",
			"Making Coffee", "Grading", "Jogging", "Having breakfast",
			"Wasting time" };
	String statusList2[] = { "LUCI", "at LUCI", "Irvine", "in the car",
			"on the bus", "at office", "on campus", "at DBH", "at ARC",
			"at Verano Place", "at Humanity Hall", "at student center",
			"at home", "at great park", "at Irvine spectrum", "at ICS",
			"at Newport Beach", "at University Town Center", "in seminar",
			"at University Club", "at Aldrich park", "at Yogurtland",
			"at fishbowl", "at the rooftop" };
	String statusList3[] = { "With", "At", "In", "On", "Mike", "Sharon",
			"Lunch", "Dinner", "By Myself", "Jeff", "Amy", "Jane", "Oscar",
			"Java city", "Coffee", "Class", "Infomatics people", "Seminar",
			"Happy hour", "Breeze", "Happy birthday to", "Exploding",
			"Awesome boba", "9gag", "Facebook", "Accepted", "Refected" };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.report_status);

		// set the view

		listView1 = (ListView) findViewById(R.id.statuslistView1);
		listView2 = (ListView) findViewById(R.id.statuslistView2);
		listView3 = (ListView) findViewById(R.id.statuslistView3);

		setViewAndOnItemClickListener(listView1, 1);
		setViewAndOnItemClickListener(listView2, 2);
		setViewAndOnItemClickListener(listView3, 3);

		final TextView textView = (TextView) findViewById(R.id.reportTextView);
		final EditText editText = (EditText) findViewById(R.id.edit);
		textView.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				
				//hide keyboard
				InputMethodManager imm = (InputMethodManager)getSystemService(
					      Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
				
				
//				Toast toast = Toast.makeText(getApplicationContext(),editText.getText().toString(), Toast.LENGTH_SHORT);
//            	toast.setGravity(Gravity.CENTER, 0, 50);
//            	toast.show();
            	
	            Intent intent = new Intent(StatusReporter.this, SensorDataCollector.class);
	            intent.putExtra("status", editText.getText().toString());
	            startActivity(intent);
			}

		});
		
		editText.setText("Coding in Vista del Campo ");	

	}

	public void setViewAndOnItemClickListener(ListView listView, int listNum) {

		final ArrayList<String> list = new ArrayList<String>();

		switch (listNum) {
		case 1:
			for (int i = 0; i < statusList1.length; i++)
				list.add("\"" + statusList1[i] + "\"");
			break;
		case 2:
			for (int i = 0; i < statusList2.length; i++)
				list.add("\"" + statusList2[i] + "\"");
			break;
		case 3:
			for (int i = 0; i < statusList3.length; i++)
				list.add("\"" + statusList3[i] + "\"");
			break;
		}
		
		final StableArrayAdapter adapter = new StableArrayAdapter(this,
				android.R.layout.simple_list_item_1, list);
		
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@SuppressLint("NewApi")
			@Override
			public void onItemClick(AdapterView<?> parent, final View view,
					int position, long id) {
				final String item = (String) parent.getItemAtPosition(position);
				view.animate().setDuration(500).alpha(0)
						.withEndAction(new Runnable() {
							@Override
							public void run() {
								list.remove(item);
								adapter.notifyDataSetChanged();
								view.setAlpha(1);
								EditText editText = (EditText) findViewById(R.id.edit);
								String tempTxt = editText.getText().toString();
								tempTxt += item.substring(1, item.length() - 1)
										+ " ";
								editText.setText(tempTxt.toLowerCase(),
										TextView.BufferType.EDITABLE);
								editText.setSelection(editText.getText()
										.toString().length() - 1);
							}
						});
			}

		});

	}

	private class StableArrayAdapter extends ArrayAdapter<String> {

		HashMap<String, Integer> mIdMap = new HashMap<String, Integer>();

		public StableArrayAdapter(Context context, int textViewResourceId,
				List<String> objects) {
			super(context, textViewResourceId, objects);
			for (int i = 0; i < objects.size(); ++i) {
				mIdMap.put(objects.get(i), i);
			}
		}

		@Override
		public long getItemId(int position) {
			String item = getItem(position);
			return mIdMap.get(item);
		}

		@Override
		public boolean hasStableIds() {
			return true;
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_FrndMngr:
			Intent intentI = new Intent(this, FriendManager.class);
			startActivity(intentI);
			break;
		}

		return true;
	}

}