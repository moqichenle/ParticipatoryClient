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

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;

public class ReportActivity extends SlidingFragmentActivity {
	private static final String TAG = "ReportActivity";
	private AppContext context;
	private ListFragment mFrag;
	private ListView listView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		context = (AppContext) getApplicationContext();
		setContentView(R.layout.activity_report);
		setupSlidingMenu(savedInstanceState);
		Log.d(TAG, "loaded");
		listView = (ListView) findViewById(R.id.reportlist);
		listView.setOnItemClickListener(this.listViewListener);
		new NetworkTask().execute(Constant.url+"/reportlistofuser");
	}

	private OnItemClickListener listViewListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View v, int pos, long id) {
			String itemValue = (String) listView.getItemAtPosition(pos);
			Toast.makeText(getApplicationContext(),
					"Position :" + pos + "  ListItem : " + itemValue,
					Toast.LENGTH_LONG).show();
		}

	};

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.actionbar_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			break;
		case R.id.menu_add:
			Intent newEntryIntent = new Intent(this, AddReportActivity.class);
			startActivity(newEntryIntent);
			break;
		}
		return true;
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

	private class NetworkTask extends AsyncTask<String, Void, HttpResponse> {
		@Override
		protected HttpResponse doInBackground(String... urls) {
			String url = urls[0];
			HttpPost request = new HttpPost(url);
			HttpClient httpClient = new DefaultHttpClient();
			HttpResponse response = null;
			String userId = context.getUserId();
			ArrayList<NameValuePair> postParameters= new ArrayList<NameValuePair>();
			postParameters.add(new BasicNameValuePair("userId", userId));
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
			// Do something with result
			if (response != null) {
				try {
					
					BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
					String results = reader.readLine();
					Log.d(TAG, "**"+results);
					Gson gson = new Gson();
					Report[] reports =  gson.fromJson(results, Report[].class);
					String[] values =new String[reports.length];
					for(int i=0;i<reports.length;i++){
						Log.d(TAG, "get report +"+reports[i].getReportId());
						values[i]=reports[i].getReportId()+"_"+reports[i].getContend();
					}
					ArrayAdapter<String> adapter = new ArrayAdapter<String>(context,
							android.R.layout.simple_list_item_1, android.R.id.text1, values);
					listView.setAdapter(adapter);
				} catch (ParseException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		}
	}
}
