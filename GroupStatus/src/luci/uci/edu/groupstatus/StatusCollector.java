package luci.uci.edu.groupstatus;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class StatusCollector extends Activity {

	String reportedStatus = "";
	String repostedGroupStatus = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.status_collector);
		
		//Set Visibility
		findViewById(R.id.groupStatus_blocks).setVisibility(View.GONE);

		final TextView tvNext = (TextView) findViewById(R.id.Button_Status_Next);
		tvNext.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {

				TextView tvStatus = (TextView) findViewById(R.id.EditText_Update_Status);
				reportedStatus = tvStatus.getText().toString();
				tvStatus.clearFocus();

				//Move Left and Fade Out View
				findViewById(R.id.status_blocks).startAnimation((Animation) AnimationUtils.loadAnimation(StatusCollector.this, R.anim.move_left_and_fade_out));

				//Move Left and Fade In View
				findViewById(R.id.groupStatus_blocks).setVisibility(View.VISIBLE);
				findViewById(R.id.groupStatus_blocks).startAnimation((Animation) AnimationUtils.loadAnimation(StatusCollector.this, R.anim.move_left_and_fade_in));
				
				TextView tvGroupStatus = (TextView) findViewById(R.id.EditText_Update_GroupStatus);
				tvGroupStatus.requestFocus();
			}
		});

		final TextView tvUpdate = (TextView) findViewById(R.id.Button_GroupStatus_Update);
		tvUpdate.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {

				TextView tvGroupStatus = (TextView) findViewById(R.id.EditText_Update_GroupStatus);
				repostedGroupStatus = tvGroupStatus.getText().toString();
				
				Intent intent = new Intent(StatusCollector.this, SensorDataCollector.class);
	            intent.putExtra("status", reportedStatus);
	            intent.putExtra("groupStatus", repostedGroupStatus);
	            startActivity(intent);

			}
		});

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
			Editor edit = PreferenceManager.getDefaultSharedPreferences(StatusCollector.this).edit();
			edit.clear();
			edit.apply();
			Intent intentI = new Intent(this, WelcomePage.class);
			startActivity(intentI);
			break;
		}
		return true;
	}
}
