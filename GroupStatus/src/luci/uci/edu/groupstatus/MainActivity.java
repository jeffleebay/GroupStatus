package luci.uci.edu.groupstatus;

import java.util.ArrayList;

import java.util.HashMap;
import java.util.List;

//import com.groupstatusbackend.RegisterActivity;

import luci.uci.edu.groupstatus.R;
import android.os.Bundle;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
//import android.view.Gravity;
//import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
//import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
//import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
//import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;


public class MainActivity extends Activity {

	//FriendAdapter adapter = null;
	ListView listView1;
	ListView listView2;
	ListView listView3;
	String statusList[]={"Meeting", "Exercise", "Relaxing", "LUCI", "Car", "With", 
			             "Mike", "In", "On", "Lunch", "Office", "By Myself", 
			             "Campus", "Jeff", "Coffee", "Feeling", "Happy", "Tired",
			             "Working", "Reading", "DBH", "Irvine", "Class", "Discussing"};
	
	String statusList1[]={"Meeting", "Exercise", "Relaxing", "Feeling happy", "Feeling tired", "Feeling hungry",
            "Working", "Reading", "Discussing", "Waiting", "Taking a bath", "Sleeping", "Packing", "Revising",
            "Drinking hot chocolate", "Taking a break", "Booking tickets", "Leaving for",
            "Waiting for the ceremony", "Proofreading", "Writing", "Browsing", "Twittering", "Making Coffee",
            "Grading", "Jogging", "Having breakfast", "Wasting time"};
	
	String statusList2[]={"LUCI", "at LUCI", "Irvine", "in the car", "on the bus", "at office", "on campus", "at DBH",
			"at ARC", "at Verano Place", "at Humanity Hall", "at student center", "at home", "at great park","at Irvine spectrum",
			"at ICS", "at Newport Beach", "at University Town Center", "in seminar", "at University Club", "at Aldrich park",
			"at Yogurtland", "at fishbowl", "at the rooftop"};
	
	String statusList3[]={"With", "At", "In", "On", "Mike", "Sharon", "Lunch", "Dinner", "By Myself", 
            "Jeff", "Amy", "Jane", "Oscar", "Java city", "Coffee",  "Class", "Infomatics people", "Seminar", "Happy hour",
            "Breeze", "Happy birthday to", "Exploding","Awesome boba", "9gag", "Facebook", "Accepted", "Refected"};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

//		Intent intent = new Intent(this, RegisterActivity.class);
//        startActivity(intent);
		
		// set the view
		
		listView1 = (ListView) findViewById(R.id.listView1);
		listView2 = (ListView) findViewById(R.id.listView2);
		listView3 = (ListView) findViewById(R.id.listView3);
		
		
		final ArrayList<String> list1 = new ArrayList<String>();
		final ArrayList<String> list2 = new ArrayList<String>();
		final ArrayList<String> list3 = new ArrayList<String>();
	    for (int i = 0; i < statusList1.length; i++) list1.add("\"" + statusList1[i] + "\"");
	    for (int i = 0; i < statusList2.length; i++) list2.add("\"" + statusList2[i] + "\"");
	    for (int i = 0; i < statusList3.length; i++) list3.add("\"" + statusList3[i] + "\"");

	    final StableArrayAdapter adapter1 = new StableArrayAdapter(this,android.R.layout.simple_list_item_1, list1);
	    final StableArrayAdapter adapter2 = new StableArrayAdapter(this,android.R.layout.simple_list_item_1, list2);
	    final StableArrayAdapter adapter3 = new StableArrayAdapter(this,android.R.layout.simple_list_item_1, list3);
	    listView1.setAdapter(adapter1);
	    listView2.setAdapter(adapter2);
	    listView3.setAdapter(adapter3);
	    listView1.setOnItemClickListener(new AdapterView.OnItemClickListener() {

	        @SuppressLint("NewApi")
			@Override
	        public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
	          final String item = (String) parent.getItemAtPosition(position);
	          view.animate().setDuration(500).alpha(0).withEndAction(new Runnable() {
	                @Override
	                public void run() {
	                  list1.remove(item);
	                  adapter1.notifyDataSetChanged();
	                  view.setAlpha(1);
	                  EditText editText = (EditText)findViewById(R.id.edit);
	                  String tempTxt = editText.getText().toString();
	                  tempTxt += item.substring(1, item.length()-1) + "  ";
	                  editText.setText(tempTxt, TextView.BufferType.EDITABLE);
	                }
	              });
	        }

	      });
	    listView2.setOnItemClickListener(new AdapterView.OnItemClickListener() {

	        @SuppressLint("NewApi")
			@Override
	        public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
	          final String item = (String) parent.getItemAtPosition(position);
	          view.animate().setDuration(500).alpha(0).withEndAction(new Runnable() {
	                @Override
	                public void run() {
	                  list2.remove(item);
	                  adapter2.notifyDataSetChanged();
	                  view.setAlpha(1);
	                  EditText editText = (EditText)findViewById(R.id.edit);
	                  String tempTxt = editText.getText().toString();
	                  tempTxt += item.substring(1, item.length()-1) + "  ";
	                  editText.setText(tempTxt, TextView.BufferType.EDITABLE);
	                }
	              });
	        }

	      });
	    listView3.setOnItemClickListener(new AdapterView.OnItemClickListener() {

	        @SuppressLint("NewApi")
			@Override
	        public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
	          final String item = (String) parent.getItemAtPosition(position);
	          view.animate().setDuration(500).alpha(0).withEndAction(new Runnable() {
	                @Override
	                public void run() {
	                  list3.remove(item);
	                  adapter3.notifyDataSetChanged();
	                  view.setAlpha(1);
	                  EditText editText = (EditText)findViewById(R.id.edit);
	                  String tempTxt = editText.getText().toString();
	                  tempTxt += item.substring(1, item.length()-1) + "  ";
	                  editText.setText(tempTxt, TextView.BufferType.EDITABLE);
	                }
	              });
	        }

	      });
//		adapter = new FriendAdapter(this);
//		friendlistView.setAdapter(adapter);
		
		final Button button = (Button) findViewById(R.id.button1);
        button.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
            	EditText editText = (EditText)findViewById(R.id.edit);
            	Toast.makeText(getApplicationContext(),editText.getText().toString(), Toast.LENGTH_LONG).show();
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
/*
	public class FriendAdapter extends BaseAdapter {
		private LayoutInflater myInflater;
		private int divisor = 3;
		
		
		public FriendAdapter(Context c) {
			myInflater = LayoutInflater.from(c);
		}

		@Override
		public int getCount() {
			return statusList.length/divisor;
		}

		@Override
		public Object getItem(int position) {
			return statusList[position*divisor];
		}

		@Override
		public long getItemId(int position) {
			return position*divisor;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			convertView = myInflater.inflate(R.layout.friend_template, null);

			TextView txt1 = ((TextView) convertView.findViewById(R.id.txt1));
			TextView txt2 = ((TextView) convertView.findViewById(R.id.txt2));
			TextView txt3 = ((TextView) convertView.findViewById(R.id.txt3));

			txt1.setText(statusList[divisor*position]);
			txt2.setText(statusList[divisor*position+1]);
			txt3.setText(statusList[divisor*position+2]);
			return convertView;
		}

	}
*/
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
