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
		SetVisibility();
		final TextView tvNext = (TextView) findViewById(R.id.Button_Status_Next);
		tvNext.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {

				TextView tvStatus = (TextView) findViewById(R.id.EditText_Update_Status);
				reportedStatus = tvStatus.getText().toString();
				tvStatus.clearFocus();

				MoveLeftandFadeOutView();
				MoveLeftandFadeInView();

			}
		});

		final TextView tvUpdate = (TextView) findViewById(R.id.Button_GroupStatus_Update);
		tvUpdate.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {

				TextView tvGroupStatus = (TextView) findViewById(R.id.EditText_Update_Status);
				repostedGroupStatus = tvGroupStatus.getText().toString();
				
				Intent intent = new Intent(StatusCollector.this, SensorDataCollector.class);
	            intent.putExtra("status", reportedStatus);
	            intent.putExtra("groupStatus", repostedGroupStatus);
	            startActivity(intent);

			}
		});

	}

	public void SetVisibility() {
		findViewById(R.id.area_GroupStatus).setVisibility(View.GONE);
		findViewById(R.id.area_shadow_buttom_GroupStatus).setVisibility(View.GONE);
		findViewById(R.id.area_shadow_side_GroupStatus).setVisibility(View.GONE);
		findViewById(R.id.linearLayout_GroupStatus).setVisibility(View.GONE);
		findViewById(R.id.divider_GroupStatus).setVisibility(View.GONE);
		findViewById(R.id.EditText_Update_GroupStatus).setVisibility(View.GONE);
		findViewById(R.id.Button_GroupStatus_Update).setVisibility(View.GONE);
	}

	public void MoveLeftandFadeInView() {

		findViewById(R.id.area_GroupStatus).setVisibility(View.VISIBLE);
		findViewById(R.id.area_shadow_buttom_GroupStatus).setVisibility(View.VISIBLE);
		findViewById(R.id.area_shadow_side_GroupStatus).setVisibility(View.VISIBLE);
		findViewById(R.id.linearLayout_GroupStatus).setVisibility(View.VISIBLE);
		findViewById(R.id.divider_GroupStatus).setVisibility(View.VISIBLE);
		findViewById(R.id.Button_GroupStatus_Update).setVisibility(View.INVISIBLE);
		findViewById(R.id.EditText_Update_GroupStatus).setVisibility(View.VISIBLE);

		findViewById(R.id.EditText_Update_Status).setVisibility(View.GONE);
		findViewById(R.id.Button_Status_Next).setVisibility(View.GONE);

		findViewById(R.id.area_GroupStatus).startAnimation((Animation) AnimationUtils.loadAnimation(StatusCollector.this, R.anim.move_left_and_fade_in));
		findViewById(R.id.area_shadow_buttom_GroupStatus).startAnimation((Animation) AnimationUtils.loadAnimation(StatusCollector.this, R.anim.move_left_and_fade_in));
		findViewById(R.id.area_shadow_side_GroupStatus).startAnimation((Animation) AnimationUtils.loadAnimation(StatusCollector.this, R.anim.move_left_and_fade_in));
		findViewById(R.id.linearLayout_GroupStatus).startAnimation((Animation) AnimationUtils.loadAnimation(StatusCollector.this, R.anim.move_left_and_fade_in));
		findViewById(R.id.divider_GroupStatus).startAnimation((Animation) AnimationUtils.loadAnimation(StatusCollector.this, R.anim.move_left_and_fade_in));
		findViewById(R.id.EditText_Update_GroupStatus).startAnimation((Animation) AnimationUtils.loadAnimation(StatusCollector.this, R.anim.move_left_and_fade_in));
		findViewById(R.id.Button_GroupStatus_Update).startAnimation((Animation) AnimationUtils.loadAnimation(StatusCollector.this, R.anim.move_left_and_fade_in));
	}

	public void MoveLeftandFadeOutView() {
		findViewById(R.id.area_Status).startAnimation((Animation) AnimationUtils.loadAnimation(StatusCollector.this, R.anim.move_left_and_fade_out));
		findViewById(R.id.area_shadow_buttom_Status).startAnimation((Animation) AnimationUtils.loadAnimation(StatusCollector.this, R.anim.move_left_and_fade_out));
		findViewById(R.id.area_shadow_side_Status).startAnimation((Animation) AnimationUtils.loadAnimation(StatusCollector.this, R.anim.move_left_and_fade_out));
		findViewById(R.id.linearLayout_Status).startAnimation((Animation) AnimationUtils.loadAnimation(StatusCollector.this, R.anim.move_left_and_fade_out));
		findViewById(R.id.divider_Status).startAnimation((Animation) AnimationUtils.loadAnimation(StatusCollector.this, R.anim.move_left_and_fade_out));
		findViewById(R.id.EditText_Update_Status).startAnimation((Animation) AnimationUtils.loadAnimation(StatusCollector.this, R.anim.move_left_and_fade_out));
		findViewById(R.id.Button_Status_Next).startAnimation((Animation) AnimationUtils.loadAnimation(StatusCollector.this, R.anim.move_left_and_fade_out));
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
