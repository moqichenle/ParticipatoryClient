package ie.tcd.scss.dsg.particpatory;

import ie.tcd.scss.dsg.po.User;
import ie.tcd.scss.dsg.po.UserLocation;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import com.google.android.gcm.GCMRegistrar;
import com.google.gson.Gson;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

@TargetApi(Build.VERSION_CODES.GINGERBREAD)
public class UserRegister extends Activity {
	private static final String TAG = "UserRegisterActivity";
	private AppContext context;
	private Location local;
	private LocationManager locationManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		context = (AppContext) getApplicationContext();
		// load content of the activity
		setContentView(R.layout.activity_user_register);
		Button userRegisterButton = (Button) findViewById(R.id.user_register_button);
		userRegisterButton.setOnClickListener(this.userRegisterButtonListener);

		// Acquire a reference to the system Location Manager
		locationManager = (LocationManager) this
				.getSystemService(Context.LOCATION_SERVICE);
		if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			buildAlertMessageNoGps();
		}

		// Register the listener with the Location Manager to receive location
		// updates
		local = locationManager
				.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
				500, 5, locationListener);
	}

	private LocationListener locationListener = new LocationListener() {
		public void onLocationChanged(Location location) {
			// Called when a new location is found by the network location
			// provider.
			// makeUseOfNewLocation(location);
			Log.d(TAG, "get new location updated");
			local = location;
		}

		public void onStatusChanged(String provider, int status, Bundle extras) {
		}

		public void onProviderEnabled(String provider) {
		}

		public void onProviderDisabled(String provider) {
		}
	};

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

	@SuppressLint("NewApi")
	private OnClickListener userRegisterButtonListener = new OnClickListener() {

		@TargetApi(Build.VERSION_CODES.GINGERBREAD)
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			if ((local != null) && local.hasAccuracy()
					&& (local.getAccuracy() <= 200)) {
				Log.d(TAG, "get location good enough");
				Log.d(TAG, local.getAccuracy() + "");
				Log.d(TAG, local.getLatitude() + "=latitude");

				String url = "http://10.6.33.177:8888/newuser";
				if (android.os.Build.VERSION.SDK_INT > 9) {
					StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
							.permitAll().build();
					StrictMode.setThreadPolicy(policy);
				}
				try {
					Log.d(TAG, "set url");
					HttpPost request = new HttpPost(url);
					User user= createNewUser(local);
					Gson gson = new Gson();
					String json = gson.toJson(user);
					StringEntity entity = new StringEntity(json, "UTF-8");
					entity.setContentType("application/json");
					request.setEntity(entity);
					HttpClient httpClient = new DefaultHttpClient();
					HttpResponse response = httpClient.execute(request);
					if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
						Log.d(TAG, "request successfully");
						String result = EntityUtils.toString(response
								.getEntity());
						EditText userName = (EditText) findViewById(R.id.userName);
						userName.setText(result);
					} else {
						Log.d(TAG, "failed");
					}
				} catch (ClientProtocolException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				locationManager.removeUpdates(locationListener);
			} else {
				Log.d(TAG, "get location is not good enough");
				buildAlertMessageNoLocation();
			}

		}
		
		/**
		 * get the userInformation
		 */
		private User createNewUser(Location location) {
			User u =  new User();
			u.setAcceptPercent(0);
			u.setAverCycleSpeed(0);
			u.setAverDriveSpeed(0);
			u.setAverWalkSpeed(0);
			u.setHasSensor((byte)48);
			UserLocation userLocation = new UserLocation();
			userLocation.setAccuracy(location.getAccuracy());
			userLocation.setBearing(location.getBearing());
			userLocation.setLatitude(location.getLatitude());
			userLocation.setLongitude(location.getLongitude());
			userLocation.setSpeed(location.getSpeed());
			u.setLocation(userLocation);
			u.setMode("Still");
			u.setRegisterId(GCMRegistrar.getRegistrationId(getApplicationContext()));
			u.setStreetName("Pearse Street");
			u.setUpdatedTime(System.currentTimeMillis());
			return u;
		}

	};

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
}

// private class NetworkTask extends AsyncTask<String, Void, HttpResponse>
// {//put inside the main class
// @Override
// protected HttpResponse doInBackground(String... urls) {
// String url = urls[0];
// HttpGet request = new HttpGet(url);
// HttpClient httpClient = new DefaultHttpClient();
// HttpResponse response = null;
// try {
// response = httpClient.execute(request);
// if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
// Log.d(TAG, "request successfully");
// Log.d(TAG, EntityUtils.toString(response.getEntity()));
// } else {
// Log.d(TAG, "failed");
// }
// } catch (ClientProtocolException e) {
// // TODO Auto-generated catch block
// e.printStackTrace();
// } catch (IOException e) {
// // TODO Auto-generated catch block
// e.printStackTrace();
// }
// return response;
// }
//
//
// @Override
// protected void onPostExecute(HttpResponse result) {
// // Do something with result
// if (result != null)
// result.getEntity().toString();
// }
// }
