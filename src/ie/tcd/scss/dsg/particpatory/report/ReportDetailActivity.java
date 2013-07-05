package ie.tcd.scss.dsg.particpatory.report;

import ie.tcd.scss.dsg.particpatory.AppContext;
import ie.tcd.scss.dsg.particpatory.R;
import ie.tcd.scss.dsg.particpatory.SampleListFragment;
import ie.tcd.scss.dsg.particpatory.util.Constant;
import ie.tcd.scss.dsg.po.Report;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.widget.TextView;

import com.google.gson.Gson;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;

public class ReportDetailActivity extends SlidingFragmentActivity {
	private static final String TAG = "DetailedReport";
	private AppContext context;
	private ListFragment mFrag;
	private String results;
	private TextView report_category;
	private TextView report_content;
	private TextView report_location;
	private TextView report_time;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		context = (AppContext) getApplicationContext();
		setContentView(R.layout.activity_report_detail);
		setupSlidingMenu(savedInstanceState);
		Log.d(TAG, "loaded");
		String reportId = context.getReportId();
		report_category = (TextView) findViewById(R.id.r_1);
		report_content= (TextView) findViewById(R.id.r_2);
		report_location = (TextView) findViewById(R.id.r_3);
		report_time = (TextView) findViewById(R.id.r_4);
		
		if (android.os.Build.VERSION.SDK_INT > 9) {
			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
					.permitAll().build();
			StrictMode.setThreadPolicy(policy);
		}
		new ReportDetailTask().execute(Constant.url+"/reportdetail", reportId); 
	}

	private void setupSlidingMenu(Bundle savedInstanceState) {

		setBehindContentView(R.layout.menu_frame);
		if (savedInstanceState == null) {
			FragmentTransaction t = this.getSupportFragmentManager()
					.beginTransaction();
			mFrag = new SampleListFragment();
			t.replace(R.id.menu_frame, mFrag);
			t.commit();
		} else {
			mFrag = (ListFragment) this.getSupportFragmentManager()
					.findFragmentById(R.id.menu_frame);
		}

		// customize the SlidingMenu
		SlidingMenu sm = getSlidingMenu();
		sm.setShadowWidthRes(R.dimen.shadow_width);
		sm.setShadowDrawable(R.drawable.shadow);
		sm.setBehindOffsetRes(R.dimen.slidingmenu_offset);
		sm.setFadeDegree(0.35f);
		sm.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);

		// getActionBar().setDisplayHomeAsUpEnabled(true); // the logo of the
		// app can be clicked
	}
	private class ReportDetailTask extends AsyncTask<String, Void, HttpResponse> {
		@Override
		protected HttpResponse doInBackground(String... urls) {
			String url = urls[0];
			ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
			HttpPost request = new HttpPost(url);
			HttpClient httpClient = new DefaultHttpClient();
			HttpResponse response = null;
			postParameters.add(new BasicNameValuePair("reportId", urls[1]));

			Log.d(TAG, "before requesting");
			try {
				request.setEntity(new UrlEncodedFormEntity(postParameters));
				response = httpClient.execute(request);
				if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
					Log.d(TAG, "request successfully");
				} else {
					Log.d(TAG, "failed");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return response;
		}

		@Override
		protected void onPostExecute(HttpResponse response) {
			if (response != null) {
				try {
					BufferedReader reader = new BufferedReader(
							new InputStreamReader(response.getEntity()
									.getContent(), "UTF-8"));
					results = reader.readLine();
					Gson gson = new Gson();
					Report report = new Report();
					report = gson.fromJson(results, Report.class);
					Log.d(TAG, results);
					report_category.setText(Constant.getCategoryName(report.getCategoryId()));
					report_content.setText(report.getContend());
					report_location.setText(report.getLatitude()+";"+report.getLongitude());
					Date myDate = new Date(report.getReportTime());
					report_time.setText(myDate.toString());
				} catch (ParseException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
