package luci.uci.edu.groupstatus;


import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.view.Gravity;
import android.view.View;
 
public class WelcomePage extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.welcome_page);
		
		addListenerOnText();
		
	}

	public void addListenerOnText() {
		 
		final TextView  textView  = (TextView) findViewById(R.id.loginTextView);
		textView.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
            	EditText userID = (EditText)findViewById(R.id.userID);
            	EditText userPW = (EditText)findViewById(R.id.userPW);
            	String userProfile = userID.getText().toString() + ";" + userPW.getText().toString();
            	
            	if(userID.getText().toString().isEmpty() || userPW.getText().toString().isEmpty()){
            		String text = "Login error. Invalid username or password.";
            		Toast toast = Toast.makeText(getApplicationContext(),text, Toast.LENGTH_SHORT);
                	toast.setGravity(Gravity.CENTER, 0, 0);
                	toast.show();
            	} else {
//	            	String text = "Logging to: " + userID.getText().toString() + " w/ " + userPW.getText().toString();	            	
//	            	Toast toast = Toast.makeText(getApplicationContext(),text, Toast.LENGTH_LONG);
//	            	toast.setGravity(Gravity.CENTER, 0, 0);
//	            	toast.show();
            	LogInToServer task = new LogInToServer(WelcomePage.this);
            	task.execute(userProfile); //http://qs4task.appspot.com/socialanalysistaskqs?taskid=1001 //http://myfooserver.appspot.com/
            	}            	
            }
        });
	}
	
	private class LogInToServer extends AsyncTask<String, Void, String> {
	
	    private ProgressDialog dialog;
        private Context context; // application context.
        
		public LogInToServer(Activity activity) {
            context = activity;
            dialog = new ProgressDialog(context);
        }
	    
	    protected void onPreExecute() {
	        this.dialog.setMessage("Connecting to the server");
	        this.dialog.show();
	        try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
	    }
		
	    @Override
	    protected String doInBackground(String... userProfiles) {
	    	String response = "";
	    	for (String userProfile : userProfiles) {
		        DefaultHttpClient client = new DefaultHttpClient();
		        
		        // Add user data
		        String userID = userProfile.toString().substring(0, userProfile.toString().indexOf(';')); 
		        String userPW = userProfile.toString().substring(userProfile.toString().indexOf(';')+1); 	        
		        HttpPost httppost = new HttpPost("http://group-status-376.appspot.com/groupstatus_server");		        	        
		        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		        nameValuePairs.add(new BasicNameValuePair("userID", userID));
		        nameValuePairs.add(new BasicNameValuePair("userPW", userPW));
		        
		        try {
					httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
				} catch (UnsupportedEncodingException e1) {
					e1.printStackTrace();
				}
		        
		        try {
		          HttpResponse execute = client.execute(httppost);
		          InputStream content = execute.getEntity().getContent();
	
		          BufferedReader buffer = new BufferedReader(new InputStreamReader(content));
		          String s = "";
		          while ((s = buffer.readLine()) != null) {
		            response += s;
		          }
	
		        } catch (Exception e) {
		          e.printStackTrace();
		        }
		    }
			return response;
	    }
	    

		@Override
	    protected void onPostExecute(String result) {
	    	
	    	if (dialog.isShowing()) {
	            dialog.dismiss();
	        }
	    	
	    	if(result.endsWith("success")){	    			    	
	    		final Toast toast = Toast.makeText(getApplicationContext(),"Successfully logged in", Toast.LENGTH_SHORT);
		    	toast.setGravity(Gravity.CENTER, 0, 100);
		    	toast.show();
		    	
		    	//The count down timer is used for controlling display time more specifically -> for aesthetics 
		    	
		    	new CountDownTimer(50, 500) {						//duration = 100
			        public void onTick(long millisUntilFinished) {
			            toast.show();
			        }
			        public void onFinish() {
			            toast.cancel();
			        }
			
			    }.start();
		    	
		    	Intent i = new Intent(WelcomePage.this, MainActivity.class);		    	
		    	startActivity(i); 
	    	}else{
	    		Toast toast = Toast.makeText(getApplicationContext(),"Login error. Invalid username or password.", Toast.LENGTH_SHORT);
	    		toast.setGravity(Gravity.CENTER, 0, 100);
		    	toast.show();
	    	}
		    	
	    }
	  }	
 
}
