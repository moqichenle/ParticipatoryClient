package ie.tcd.scss.dsg.particpatory.report;

import ie.tcd.scss.dsg.particpatory.AppContext;
import ie.tcd.scss.dsg.particpatory.R;
import ie.tcd.scss.dsg.particpatory.SampleListFragment;
import ie.tcd.scss.dsg.particpatory.util.Constant;
import ie.tcd.scss.dsg.po.ReportFromApp;
import ie.tcd.scss.dsg.po.User;
import ie.tcd.scss.dsg.po.UserLocation;

import java.nio.ByteBuffer;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.gson.Gson;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;

public class AddReportActivity extends SlidingFragmentActivity {
	private static final String TAG = "AddReportActivity";
	private AppContext context;
	private ListFragment mFrag;
	private Spinner keywords;
	private byte categoryId = 0;
	private ArrayAdapter<CharSequence> keywordsAdapter;
	private Location local;
	private LocationManager locationManager;
	private ImageView finding;
	private Timer timer;
	private String contend;
	private ImageView attach;
	private ImageView imageView;
	private String formatted_address;
	private User currentUser = new User();
	private ReportFromApp report = new ReportFromApp();
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		context = (AppContext) getApplicationContext();
		setContentView(R.layout.activity_add_report);
		setupSlidingMenu(savedInstanceState);
		Log.d(TAG, "loaded");
		Spinner category = (Spinner) findViewById(R.id.category);
		ArrayAdapter<CharSequence> categoryAdapter = ArrayAdapter
				.createFromResource(this, R.array.category_array,
						android.R.layout.simple_spinner_item);
		categoryAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		category.setAdapter(categoryAdapter);

		keywords = (Spinner) findViewById(R.id.hintword);
		keywordsAdapter = ArrayAdapter.createFromResource(this,
				R.array.keyword_array, android.R.layout.simple_spinner_item);
		keywordsAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		keywords.setAdapter(keywordsAdapter);

		keywords.setOnItemSelectedListener(this.selectKeyword);
		category.setOnItemSelectedListener(this.selectCategory);

		finding = (ImageView) findViewById(R.id.locationIcon);

		// positioning
		locationManager = (LocationManager) this
				.getSystemService(Context.LOCATION_SERVICE);
		Log.d(TAG, "try to get last known location from GPS");
		local = locationManager
				.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		
		if(local==null){
			Log.d(TAG, "NO last known location from GPS,try to get from network");
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

		attach = (ImageView) findViewById(R.id.addAttachment);
		attach.setOnClickListener(this.addAttachment);
		imageView = (ImageView) findViewById(R.id.showpics);
	}

	private OnClickListener addAttachment = new OnClickListener() {

		@Override
		public void onClick(View v) {
			Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			startActivityForResult(takePicture, 0);// zero can be replced with
													// any action code
		}

	};

	@Override
	protected void onActivityResult(int requestCode, int resultCode,
			Intent imageReturnedIntent) {
		super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
		if(requestCode==0){
			if(imageReturnedIntent!=null){
				Bitmap photo = (Bitmap) imageReturnedIntent.getExtras().get("data");
				photo = Bitmap.createBitmap(photo);
	            imageView.setImageBitmap(photo);
	            imageView.setVisibility(View.VISIBLE);
	            int bytes = photo.getByteCount();
	          //or we can calculate bytes this way. Use a different value than 4 if you don't use 32bit images.
	          //int bytes = b.getWidth()*b.getHeight()*4; 

	          ByteBuffer buffer = ByteBuffer.allocate(bytes); //Create a new buffer
	          photo.copyPixelsToBuffer(buffer); //Move the byte data to the buffer
	          byte[] array = buffer.array(); 
	          Log.d(TAG, "@@@@@@@@@@@@@@@@@picture's size=="+array.length);
	          report.setAttachment(array);
			}
		}
	}

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
			Log.d(TAG, "submit report");
			if (local != null && local.hasAccuracy()
					&& local.getAccuracy() <= 50) {
				finding.setImageResource(R.drawable.accept);
				Log.d(TAG, "" + local.getAccuracy() + "*" + local.getLatitude());
			} else {
				buildAlertMessageNoLocation();
			}

			UserLocation currLocation = new UserLocation();
			currLocation.setAccuracy(local.getAccuracy());
			currLocation.setBearing(local.getBearing());
			currLocation.setLatitude(local.getLatitude());
			currLocation.setLongitude(local.getLongitude());
			currLocation.setSpeed(local.getSpeed());
			currentUser.setLocation(currLocation);

			
			report.setCategoryId(categoryId);
			report.setContend(contend);
			report.setUserId(Long.valueOf(context.getUserId()));

			// TODO value below need to be changed
			currentUser.setAverCycleSpeed(0);
			currentUser.setAverDriveSpeed(0);
			currentUser.setAverWalkSpeed(0);
			currentUser.setMode("Walking");

			// TODO streetsname using formatted_address
			currentUser.setStreetName(formatted_address);
			currentUser.setUserId(Long.valueOf(context.getUserId()));
			report.setUser(currentUser);

			if (android.os.Build.VERSION.SDK_INT > 9) {
				StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
						.permitAll().build();
				StrictMode.setThreadPolicy(policy);
			}
			Log.d(TAG, "set url");
			String url = Constant.url + "/newreport";
			HttpPost request = new HttpPost(url);
			Gson gson = new Gson();
			String json = gson.toJson(report);
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
							ReportActivity.class);
					startActivity(newIntent);
				} else {
					Log.d(TAG, "failed");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			break;
		}
		return true;
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

	private LocationListener locationListener = new LocationListener() {
		public void onLocationChanged(Location location) {
			Log.d(TAG, "get new location updated");
			local = location;
			if (local != null && local.hasAccuracy()&& local.getAccuracy() <= 50) {
				finding.setImageResource(R.drawable.accept);
				Log.d(TAG, "get location good enough");
				TextView textView = (TextView) findViewById(R.id.textView1);
				JSONObject ret = Constant.getLocationInfo(local.getLatitude(),local.getLongitude());
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
				keywords.setAdapter(keywordsAdapter);
			} else if (category.equals("Traffic")) {
				ArrayAdapter<CharSequence> tra_key = ArrayAdapter
						.createFromResource(context, R.array.tra_keyword_array,
								android.R.layout.simple_spinner_item);
				keywords.setAdapter(tra_key);
				categoryId = 0;
			} else {
				ArrayAdapter<CharSequence> imp_key = ArrayAdapter
						.createFromResource(context, R.array.imp_keyword_array,
								android.R.layout.simple_spinner_item);
				keywords.setAdapter(imp_key);
				categoryId = 1;
			}
		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {
		}

	};
	private OnItemSelectedListener selectKeyword = new OnItemSelectedListener() {

		@Override
		public void onItemSelected(AdapterView<?> parent, View v, int pos,
				long id) {
			String keyword = (String) parent.getItemAtPosition(pos);
			contend = keyword;
		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {
		}

	};

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
