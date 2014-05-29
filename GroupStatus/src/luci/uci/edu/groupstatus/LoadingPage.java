package luci.uci.edu.groupstatus;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;

public class LoadingPage extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.welcome_page);
		
		findViewById(R.id.linearLayoutForLogIn).setVisibility(View.GONE);
       	findViewById(R.id.loginArea).setVisibility(View.GONE);
		
	}
	@Override
	public void onResume() {
		super.onResume();
		
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		String userID = settings.getString("userIDforGroupStatus", "n/a");
		String userPW = settings.getString("userPWforGroupStatus", "n/a");

		final EditText userID_EditText = (EditText) findViewById(R.id.userID);
		final EditText userPW_EditText = (EditText) findViewById(R.id.userPW);

		if (!userID.equals("n/a") && !userPW.equals("n/a")) {
			userID_EditText.setText(userID);
			userPW_EditText.setText(userPW);

			//once the user is already logged in
			Intent i = new Intent(LoadingPage.this, CollectStatusAndSensorData.class);
			startActivity(i);
		} else {
			Intent i = new Intent(LoadingPage.this, WelcomePage.class);
			startActivity(i);
		}
	}

}
