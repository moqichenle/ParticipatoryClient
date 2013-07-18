package ie.tcd.scss.dsg.particpatory.query;

import ie.tcd.scss.dsg.particpatory.AppContext;
import ie.tcd.scss.dsg.particpatory.R;
import ie.tcd.scss.dsg.particpatory.SampleListFragment;
import ie.tcd.scss.dsg.particpatory.util.Constant;
import ie.tcd.scss.dsg.po.Query;
import ie.tcd.scss.dsg.po.User;
import ie.tcd.scss.dsg.po.UserLocation;

import java.io.InputStream;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
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
	private LocationManager locationManager;
	private Timer timer;
	private String formatted_address;
	private byte categoryId = 0;
	private ImageView finding;
	private AutoCompleteTextView location_of_interest;
	private double lat;
	private double lon;

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

		// positioning
		locationManager = (LocationManager) this
				.getSystemService(Context.LOCATION_SERVICE);
		Log.d(TAG, "try to get last known location from GPS");
		local = locationManager
				.getLastKnownLocation(LocationManager.GPS_PROVIDER);

		if (local == null) {
			Log.d(TAG,
					"NO last known location from GPS,try to get from network");
			local = locationManager
					.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		}
		// GPS_PROVIDER
		if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			buildAlertMessageNoGps();
		}

		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
				500, 5, locationListener);
		startTimer();

		finding = (ImageView) findViewById(R.id.query_locationing);
		location_of_interest = (AutoCompleteTextView) findViewById(R.id.query_location_input);

		location_of_interest.addTextChangedListener(this.searchLocation);
		location_of_interest.setThreshold(3);
		// location_of_interest.setOnClickListener(new OnClickListener(){
		// @Override
		// public void onClick(View arg0) {
		// location_of_interest.setDropDownHeight(LayoutParams.WRAP_CONTENT);
		// }
		// });
		// location_of_interest.setOnItemClickListener(new
		// OnItemClickListener(){
		// @Override
		// public void onItemClick(AdapterView<?> listView, View arg1, int
		// position, long arg3) {
		// try
		// {
		// String venue = (String)listView.getItemAtPosition(position);
		// location_of_interest.setText(venue);
		// location_of_interest.dismissDropDown();
		// location_of_interest.setDropDownHeight(0);
		// }
		// catch(Exception e)
		// {
		// Log.v("erros", e.toString());
		// }
		// }
		// });
	}

	private TextWatcher searchLocation = new TextWatcher() {

		@Override
		public void afterTextChanged(Editable input) {
			if (android.os.Build.VERSION.SDK_INT > 9) {
				StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
						.permitAll().build();
				StrictMode.setThreadPolicy(policy);
			}
			String street = input.toString().replaceAll(" ", "");
			Log.d(TAG, "###" + input);
			Log.d(TAG, "**" + street);
			HttpGet httpGet = new HttpGet(
					"http://maps.googleapis.com/maps/api/geocode/json?address="
							+ street + "&sensor=false");
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

				JSONObject object = new JSONObject(stringBuilder.toString());
				JSONArray array = object.getJSONArray("results");
				String[] addresses = new String[array.length()];
				for (int i = 0; i < array.length(); i++) {
					addresses[i] = array.getJSONObject(i).getString(
							"formatted_address");
					Log.d(TAG, addresses[i]);
					if (i == 2)
						break;
				}
				ArrayAdapter<String> adapter = new ArrayAdapter<String>(
						context, android.R.layout.simple_list_item_1, addresses);
				location_of_interest.setAdapter(adapter);
			} catch (Exception e1) {
				e1.printStackTrace();
			}

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
	private LocationListener locationListener = new LocationListener() {
		public void onLocationChanged(Location location) {
			Log.d(TAG, "get new location updated");
			local = location;
			if (local != null && local.hasAccuracy()
					&& local.getAccuracy() <= 50) {
				finding.setImageResource(R.drawable.accept);
				Log.d(TAG, "get location good enough");
				TextView textView = (TextView) findViewById(R.id.textView1);
				JSONObject ret = Constant.getLocationInfo(local.getLatitude(),
						local.getLongitude());
				JSONObject address;
				try {
					address = ret.getJSONArray("results").getJSONObject(0);
					formatted_address = address.getString("formatted_address");
					textView.setText(formatted_address);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}

		public void onStatusChanged(String provider, int status, Bundle extras) {
		}

		public void onProviderEnabled(String provider) {
		}

		public void onProviderDisabled(String provider) {
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
//					getLocationFromStreetName(location_of_interest.getText().toString().replaceAll(" ", ""));
					getLocationFromStreetName("42PearseStreetDublinIreland");
					newQuery.setLatitude(lat);
					newQuery.setLongitude(lon);
					newQuery.setQueryTime(System.currentTimeMillis());
					newQuery.setStreetName(formatted_address);
					newQuery.setUserId(Long.valueOf(context.getUserId()));
					boolean flag = submitQuery(newQuery);
					User currentUser = new User();

					UserLocation currLocation = new UserLocation();
					currLocation.setAccuracy(local.getAccuracy());
					currLocation.setBearing(local.getBearing());
					currLocation.setLatitude(local.getLatitude());
					currLocation.setLongitude(local.getLongitude());
					currLocation.setSpeed(local.getSpeed());

					currentUser.setLocation(currLocation);
					// TODO average speed and mode.
					currentUser.setAcceptPercent(context.getAcceptPercent());
					currentUser.setAverCycleSpeed(context.getAverCycleSpeed());
					currentUser.setAverDriveSpeed(context.getAverDriveSpeed());
					currentUser.setAverWalkSpeed(context.getAverWalkSpeed());
					currentUser.setMode(context.getMode());
					currentUser.setStreetName(formatted_address);
					currentUser.setUpdatedTime(System.currentTimeMillis());
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
			convert = convert.getJSONObject("geometry").getJSONObject("location");
			
			lat = Double.valueOf(convert.getDouble("lat"));
			lon = Double.valueOf(convert.getDouble("lng"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void startTimer() {
		this.timer = new Timer();
		Log.d(TAG, "timer starts.");
		this.timer.schedule(new TimeTask(), 10000, 10000);
	}

	class TimeTask extends TimerTask {
		@Override
		public void run() {
			Log.d(TAG, "load network provider");
			local = locationManager
					.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
			locationManager.requestLocationUpdates(
					LocationManager.NETWORK_PROVIDER, 500, 5, locationListener);
			timer.cancel();
			timer.purge();
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
		locationManager.removeUpdates(locationListener);

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

	/**
	 * to check if the GPS is open
	 */
	private void buildAlertMessageNoGps() {
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(
				"Your GPS seems to be disabled, do you want to enable it?")
				.setCancelable(false)
				.setPositiveButton("Yes",
						new DialogInterface.OnClickListener() {
							public void onClick(final DialogInterface dialog,
									final int id) {
								startActivity(new Intent(
										android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
							}
						})
				.setNegativeButton("No", new DialogInterface.OnClickListener() {
					public void onClick(final DialogInterface dialog,
							final int id) {
						dialog.cancel();
					}
				});
		final AlertDialog alert = builder.create();
		alert.show();
	}
}
