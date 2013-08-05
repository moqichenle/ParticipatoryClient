package ie.tcd.scss.dsg.particpatory.query;

import ie.tcd.scss.dsg.particpatory.AppContext;
import ie.tcd.scss.dsg.particpatory.R;
import ie.tcd.scss.dsg.particpatory.SampleListFragment;
import ie.tcd.scss.dsg.particpatory.util.Constant;
import ie.tcd.scss.dsg.po.Query;

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
import android.os.StrictMode;
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

public class QueryActivity extends SlidingFragmentActivity{
	private static final String TAG = "QueryActivity";
	private AppContext context;
	private ListFragment mFrag;
	private ListView listView;
	private String results;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		context = (AppContext) getApplicationContext();
		setContentView(R.layout.activity_query);
		setupSlidingMenu(savedInstanceState);
		Log.d(TAG, "loaded");
		listView = (ListView) findViewById(R.id.queryList);
		listView.setOnItemClickListener(this.listViewListener);
		if (android.os.Build.VERSION.SDK_INT > 9) {
			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
					.permitAll().build();
			StrictMode.setThreadPolicy(policy);
		}
		new QueryListTask().execute(Constant.url + "/queryofuser");
	}

	private OnItemClickListener listViewListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View v, int pos, long id) {
			String itemValue = (String) listView.getItemAtPosition(pos);
			String[] split = itemValue.split("_");
			String queryId = split[0];
			context.setQueryId(queryId);
			Intent certainQuery = new Intent(context, QueryDetailActivity.class);
			startActivity(certainQuery);
		}

	};

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.actionbar_list_page, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			break;
		case R.id.menu_add:
			Intent newEntryIntent = new Intent(this, AddQueryActivity.class);
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

	private class QueryListTask extends AsyncTask<String, Void, HttpResponse> {
		@Override
		protected HttpResponse doInBackground(String... urls) {
			String url = urls[0];
			ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
			HttpPost request = new HttpPost(url);
			HttpClient httpClient = new DefaultHttpClient();
			HttpResponse response = null;
			String userId = context.getUserId();
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

					BufferedReader reader = new BufferedReader(
							new InputStreamReader(response.getEntity()
									.getContent(), "UTF-8"));
					results = reader.readLine();
					Gson gson = new Gson();
					if(results!=null){
						Query[] queries = gson.fromJson(results, Query[].class);
						String[] values = new String[queries.length];
						for (int i = 0; i < queries.length; i++) {
							Log.d(TAG, "get report +" + queries[i].getQueryId());
							values[i] = queries[i].getQueryId() + "_"
									+ queries[i].getContent();
						}
						if(queries.length==0){
							Toast.makeText(getApplicationContext(),
									"No Queries", Toast.LENGTH_LONG).show();
						}
						ArrayAdapter<String> adapter = new ArrayAdapter<String>(
								context, android.R.layout.simple_list_item_1,
								android.R.id.text1, values);
						listView.setAdapter(adapter);
					}
				} catch (ParseException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
