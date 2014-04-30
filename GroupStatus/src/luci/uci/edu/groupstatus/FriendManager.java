package luci.uci.edu.groupstatus;

//import java.util.List;
//import java.util.Random;

import luci.uci.edu.groupstatus.R;
import android.annotation.SuppressLint;
import android.app.ListActivity;
//import android.content.Intent;
import android.os.Bundle;
import android.util.SparseArray;
//import android.view.Menu;
//import android.view.MenuItem;
//import android.view.View;
//import android.widget.ArrayAdapter;
import android.widget.ExpandableListView;

@SuppressLint("SimpleDateFormat")
public class FriendManager extends ListActivity {
	
	// more efficient than HashMap for mapping integers to objects
	  SparseArray<Group> groups = new SparseArray<Group>();

	  @Override
	  protected void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.friend_template);
	    createData();
	    ExpandableListView listView = (ExpandableListView) findViewById(R.id.listView);
	    MyExpandableListAdapter adapter = new MyExpandableListAdapter(this,
	        groups);
	    listView.setAdapter(adapter);
	  }

	  public void createData() {
	    for (int j = 0; j < 5; j++) {
	      Group group = new Group("Test " + j);
	      for (int i = 0; i < 5; i++) {
	        group.children.add("Sub Item" + i);
	      }
	      groups.append(j, group);
	    }
	  }


}