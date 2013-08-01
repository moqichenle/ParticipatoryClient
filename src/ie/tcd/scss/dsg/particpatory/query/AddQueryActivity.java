package ie.tcd.scss.dsg.particpatory.query;

import ie.tcd.scss.dsg.particpatory.AppContext;
import ie.tcd.scss.dsg.particpatory.R;
import ie.tcd.scss.dsg.particpatory.SampleListFragment;
import ie.tcd.scss.dsg.particpatory.util.Calculation;
import ie.tcd.scss.dsg.particpatory.util.Constant;
import ie.tcd.scss.dsg.po.Query;
import ie.tcd.scss.dsg.po.User;

import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;

public class AddQueryActivity extends SlidingFragmentActivity {
	private static final String TAG = "AddQueryActivity";
	private AppContext context;
	private ListFragment mFrag;
	private Location local;
	private String formatted_address;
	private byte categoryId = 0;
	private ImageView finding;
	private AutoCompleteTextView location_of_interest;
	private double lat;
	private double lon;
	private User currentUser;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		context = (AppContext) getApplicationContext();
		setContentView(R.layout.activity_add_query);

		setupSlidingMenu(savedInstanceState);
		Spinner category = (Spinner) findViewById(R.id.query_spinner);
		ArrayAdapter<CharSequence> categoryAdapter = ArrayAdapter
				.createFromResource(this, R.array.category_array,
						android.R.layout.simple_spinner_item);
		categoryAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		category.setAdapter(categoryAdapter);

		category.setOnItemSelectedListener(this.selectCategory);

		local = context.getLocation();
		finding = (ImageView) findViewById(R.id.query_locationing);
		TextView textView = (TextView) findViewById(R.id.textView1);
		formatted_address = Constant.getLocationInfo(local.getLatitude(),
				local.getLongitude());
		textView.setText(formatted_address);
		if(local.hasAccuracy()&&local.getAccuracy()<=50){
			finding.setImageResource(R.drawable.accept);
		}
		location_of_interest = (AutoCompleteTextView) findViewById(R.id.query_location_input);
		location_of_interest.addTextChangedListener(this.searchLocation);
		// location_of_interest.setThreshold(3);
		Constant.setupConnectin();
	}

	private TextWatcher searchLocation = new TextWatcher() {

		@Override
		public void afterTextChanged(Editable input) {
			String street = input.toString().replaceAll(" ", "");
			new AutoCompleteTask().execute(street);
		}

		@Override
		public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
				int arg3) {

		}

		@Override
		public void onTextChanged(CharSequence arg0, int arg1, int arg2,
				int arg3) {

		}

	};

	private OnItemSelectedListener selectCategory = new OnItemSelectedListener() {

		@Override
		public void onItemSelected(AdapterView<?> parent, View view, int pos,
				long id) {
			String category = (String) parent.getItemAtPosition(pos);
			if (category.equals("Select Category")) {
				categoryId = -1;
			} else if (category.equals("Traffic")) {
				categoryId = 0;
			} else {
				categoryId = 1;
			}
		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {
		}

	};

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.actionbar_add_page, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			break;
		case R.id.menu_submit:
			if (local != null && local.hasAccuracy()
					&& local.getAccuracy() <= 50) {
				Query newQuery = new Query();
				if (categoryId == -1) {
					Toast.makeText(context, "please select a category",
							Toast.LENGTH_LONG).show();
				} else {
					if (android.os.Build.VERSION.SDK_INT > 9) {
						StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
								.permitAll().build();
						StrictMode.setThreadPolicy(policy);
					}
					newQuery.setCategoryId(categoryId);
					String content = "would like to know the "
							+ Constant.getCategoryName(categoryId) + "at "
							+ location_of_interest.getText().toString();
					newQuery.setContent(content);
					getLocationFromStreetName(location_of_interest.getText()
							.toString().replaceAll(" ", ""));
					// getLocationFromStreetName("42PearseStreetDublinIreland");
					newQuery.setLatitude(lat);
					newQuery.setLongitude(lon);
					newQuery.setQueryTime(System.currentTimeMillis());
					newQuery.setStreetName(location_of_interest.getText()
							.toString());
					newQuery.setUserId(Long.valueOf(context.getUserId()));
					boolean flag = submitQuery(newQuery);
					currentUser = new User();

					currentUser.setAccuracy(local.getAccuracy());
					currentUser.setBearing(local.getBearing());
					currentUser.setLatitude(local.getLatitude());
					currentUser.setLongitude(local.getLongitude());
					currentUser.setSpeed(local.getSpeed());
					calculateAverage();
					currentUser.setAcceptPercent(context.getAcceptPercent());
					currentUser.setMode(context.getMode());
					currentUser.setStreetName(formatted_address);
					currentUser.setStreetName(formatted_address);
					currentUser.setUserId(Long.valueOf(context.getUserId()));
					if (flag) {
						updateUserContext(currentUser);
					}
				}
			} else {
				buildAlertMessageNoLocation();
			}
			break;
		}
		return true;
	}

	private void calculateAverage() {
		SharedPreferences shared = getSharedPreferences(AppContext.PREFS_NAME,
				MODE_PRIVATE);
		float newSpeed = local.getSpeed();
		float walkSpeed = context.getAverWalkSpeed();
		float cycleSpeed = context.getAverCycleSpeed();
		float driveSpeed = context.getAverDriveSpeed();
		if (context.getMode().equals("on_foot")) {
			walkSpeed = Calculation.averageWalkSpeed(walkSpeed, newSpeed);
			currentUser.setAverWalkSpeed(walkSpeed);
		} else if (context.getMode().equals("on_bicycle")) {
			cycleSpeed = Calculation.averageCycleSpeed(cycleSpeed, newSpeed);
			currentUser.setAverCycleSpeed(cycleSpeed);
		} else if (context.getMode().equals("in_vehicle")) {
			driveSpeed = Calculation.averageDriveSpeed(driveSpeed, newSpeed);
			currentUser.setAverDriveSpeed(driveSpeed);
		}else{
			currentUser.setAverWalkSpeed(context.getAverWalkSpeed());
			currentUser.setAverDriveSpeed( context.getAverDriveSpeed());
			currentUser.setAverCycleSpeed(context.getAverCycleSpeed());
		}

		Editor editor = shared.edit();
		editor.putFloat("cycle", cycleSpeed);
		editor.putFloat("drive", driveSpeed);
		editor.putFloat("walk", walkSpeed);
		editor.commit();
	}

	private boolean submitQuery(Query query) {
		Log.d(TAG, "set url");
		String url = Constant.url + "/newquery";
		HttpPost request = new HttpPost(url);
		Gson gson = new Gson();
		String json = gson.toJson(query);
		StringEntity entity;
		try {
			entity = new StringEntity(json, "UTF-8");
			entity.setContentType("application/json");
			request.setEntity(entity);
			HttpClient httpClient = new DefaultHttpClient();
			HttpResponse response = httpClient.execute(request);
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				Log.d(TAG, "request successfully");
				return true;
			} else {
				Log.d(TAG, "failed");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	private void updateUserContext(User user) {

		Log.d(TAG, "set url");
		String url = Constant.url + "/updateuser";
		HttpPost request = new HttpPost(url);
		Gson gson = new Gson();
		String json = gson.toJson(user);
		StringEntity entity;
		try {
			entity = new StringEntity(json, "UTF-8");
			entity.setContentType("application/json");
			request.setEntity(entity);
			HttpClient httpClient = new DefaultHttpClient();
			HttpResponse response = httpClient.execute(request);
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				Log.d(TAG, "request successfully");
				Intent newIntent = new Intent(getApplicationContext(),
						QueryActivity.class);
				startActivity(newIntent);
			} else {
				Log.d(TAG, "failed");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void getLocationFromStreetName(String streetname) {
		HttpGet httpGet = new HttpGet(
				"http://maps.googleapis.com/maps/api/geocode/json?address="
						+ streetname + "&sensor=false");
		HttpClient client = new DefaultHttpClient();
		HttpResponse response;
		StringBuilder stringBuilder = new StringBuilder();

		try {
			response = client.execute(httpGet);
			HttpEntity entity = response.getEntity();
			InputStream stream = entity.getContent();
			int b;
			while ((b = stream.read()) != -1) {
				stringBuilder.append((char) b);
			}

			JSONObject jsonObject = new JSONObject(stringBuilder.toString());
			JSONObject convert = new JSONObject();
			convert = jsonObject.getJSONArray("results").getJSONObject(0);
			convert = convert.getJSONObject("geometry").getJSONObject(
					"location");

			lat = Double.valueOf(convert.getDouble("lat"));
			lon = Double.valueOf(convert.getDouble("lng"));
		} catch (Exception e) {
			e.printStackTrace();
		}
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

		// getActionBar().setDisplayHomeAsUpEnabled(true);
	}

	@Override
	protected void onPause() {
		super.onPause();

	}

	/**
	 * didnt get good enough location
	 */
	private void buildAlertMessageNoLocation() {
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(
				"Your location is not good enough, please wait for several seconds.")
				.setCancelable(false)
				.setNegativeButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(final DialogInterface dialog,
							final int id) {
						dialog.cancel();
					}
				});
		final AlertDialog alert = builder.create();
		alert.show();
	}

	private class AutoCompleteTask extends
			AsyncTask<String, Void, HttpResponse> {
		@Override
		protected HttpResponse doInBackground(String... urls) {
			String street = urls[0];
			HttpGet httpGet = new HttpGet(
					"http://maps.googleapis.com/maps/api/geocode/json?address="
							+ street + "&sensor=false");
			HttpClient client = new DefaultHttpClient();
			HttpResponse response = null;
			try {
				response = client.execute(httpGet);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			return response;
		}

		@Override
		protected void onPostExecute(HttpResponse response) {
			// Do something with result
			if (response != null) {
				HttpEntity entity = response.getEntity();
				InputStream stream;
				try {
					stream = entity.getContent();
					int b;
					StringBuilder stringBuilder = new StringBuilder();
					while ((b = stream.read()) != -1) {
						stringBuilder.append((char) b);
					}
					JSONObject object = new JSONObject(stringBuilder.toString());
					JSONArray array = object.getJSONArray("results");
					String[] addresses = new String[array.length()];
					for (int i = 0; i < array.length(); i++) {
						addresses[i] = array.getJSONObject(i).getString(
								"formatted_address");
						Log.d(TAG, addresses[i]);
					}
					ArrayAdapter<String> adapter = new ArrayAdapter<String>(
							context, android.R.layout.simple_list_item_1,
							addresses);
					location_of_interest.setAdapter(adapter);
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		}
	}
}
