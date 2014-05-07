package luci.uci.edu.groupstatus;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class ResultReporter extends Activity implements OnClickListener {

	HashMap<String, String> SensorResult = new HashMap<String, String>();
	String keys[] = { "status", "wifi", "noise", "location", "address" };
	ImageView iv;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.result_reporter);
		
		iv = (ImageView) findViewById(R.id.area_Button_Upload);
		iv.setOnClickListener(this);

	}

	@SuppressWarnings("unchecked")
	@Override
	public void onClick(View v) {
		
		
		
		if (getIntent().hasExtra("results")) {
			SensorResult = (HashMap<String, String>) getIntent().getSerializableExtra("results");
		for (int i = 0; i < keys.length; i++)
			Log.i(keys[i], SensorResult.get(keys[i]));
		}else{
			SensorResult.put(keys[0], "Coding in Vista del Campo");
			SensorResult.put(keys[1], "48:f8:b3:43:a0:5a,-85;28:c6:8e:a8:d4:0f,-86;14:d6:4d:30:85:aa,-82;08:86:3b:1a:36:d4,-55;");
			SensorResult.put(keys[2], "0.01;0.11;0.01;0.02;0.01;0.01;0.24;0.02;0.01;0.11;0.03;0.02;0.02;0.07;0.01;0.04;0.03;0.02;0.66;0.01;");
			SensorResult.put(keys[3], "33.64394436,-117.823742");
			SensorResult.put(keys[4], "462-510: 462-510 Arroyo Dr, Irvine, United States, 92617");
		}
		UploadToServer uploadToServer = new UploadToServer(ResultReporter.this);
		uploadToServer.execute(" ");	
		
	}
	
	private class UploadToServer extends AsyncTask<String, Void, String> {
		
	    private ProgressDialog dialog;
        private Context context; // application context.
        private ProgressBar progressBar_Upload;

        
		public UploadToServer(Activity activity) {
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
	        
	        TextView textView_ReportButton_Upload = (TextView) findViewById(R.id.textView_ReportButton_Upload);
			textView_ReportButton_Upload.setVisibility(View.INVISIBLE);
			progressBar_Upload = (ProgressBar) findViewById(R.id.progressBar_Upload);
			progressBar_Upload.setVisibility(View.VISIBLE);
	    }
		
	    @Override
	    protected String doInBackground(String... userProfiles) {
	    	String response = "";
	    	for (String userProfile : userProfiles) {
		        DefaultHttpClient client = new DefaultHttpClient();
		        
		        // Add sensor data       
		        HttpPost httppost = new HttpPost("http://group-status-376.appspot.com/groupstatus_server");		        	        
		        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		        
		        nameValuePairs.add(new BasicNameValuePair("function", "upload"));
		        for (int i = 0; i < keys.length; i++)
		        	nameValuePairs.add(new BasicNameValuePair(keys[i], SensorResult.get(keys[i])));
		        
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
	    		final Toast toast = Toast.makeText(getApplicationContext(),"Successfully Uploaded", Toast.LENGTH_SHORT);
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
			    progressBar_Upload.setVisibility(View.INVISIBLE);
			    ImageView checked_Upload = (ImageView) findViewById(R.id.checked_Upload);
			    checked_Upload.setVisibility(View.VISIBLE);
			    
		    	
	    	}else{
	    		Toast toast = Toast.makeText(getApplicationContext(),"Upload error. Invalid username or password.", Toast.LENGTH_SHORT);
	    		toast.setGravity(Gravity.CENTER, 0, 100);
		    	toast.show();
	    	}
		    	
	    }
	  }	

}
