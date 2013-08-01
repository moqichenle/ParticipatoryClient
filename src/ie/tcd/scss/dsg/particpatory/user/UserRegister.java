package ie.tcd.scss.dsg.particpatory.user;

import ie.tcd.scss.dsg.particpatory.AppContext;
import ie.tcd.scss.dsg.particpatory.R;
import ie.tcd.scss.dsg.particpatory.util.Constant;
import ie.tcd.scss.dsg.po.User;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gcm.GCMRegistrar;
import com.google.gson.Gson;

@TargetApi(Build.VERSION_CODES.GINGERBREAD)
public class UserRegister extends Activity {
	private static final String TAG = "UserRegisterActivity";
	private AppContext context;
	private Location local;
//	private LocationManager locationManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		context = (AppContext) getApplicationContext();
		setContentView(R.layout.activity_user_register);
		Button userRegisterButton = (Button) findViewById(R.id.user_register_button);
		userRegisterButton.setOnClickListener(this.userRegisterButtonListener);
		Constant.setupConnectin();
		local = context.getLocation();
		// locationManager = (LocationManager) this
		// .getSystemService(Context.LOCATION_SERVICE);
		// if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
		// {
		// buildAlertMessageNoGps();
		// }
		//
		// local = locationManager
		// .getLastKnownLocation(LocationManager.GPS_PROVIDER);
		// locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
		// 500, 5, locationListener);
	}

	// private LocationListener locationListener = new LocationListener() {
	// public void onLocationChanged(Location location) {
	// Log.d(TAG, "get new location updated");
	// local = location;
	// }
	//
	// public void onStatusChanged(String provider, int status, Bundle extras) {
	// }
	//
	// public void onProviderEnabled(String provider) {
	// }
	//
	// public void onProviderDisabled(String provider) {
	// }
	// };

	@SuppressLint("NewApi")
	private OnClickListener userRegisterButtonListener = new OnClickListener() {

		@SuppressLint("CommitPrefEdits")
		@TargetApi(Build.VERSION_CODES.GINGERBREAD)
		@Override
		public void onClick(View v) {
			EditText nickName = (EditText) findViewById(R.id.userName);
			SharedPreferences shared = getSharedPreferences(
					AppContext.PREFS_NAME, MODE_PRIVATE);

			Log.d(TAG, "nickName:" + nickName);
			if ((local != null) && local.hasAccuracy()
					&& (local.getAccuracy() <= 50)) {
				Log.d(TAG, "get location good enough");
				Log.d(TAG, local.getAccuracy() + "");

				String url = Constant.url + "/newuser";
				try {
					Log.d(TAG, "set url");
					HttpPost request = new HttpPost(url);
					User user = createNewUser(local);
					Gson gson = new Gson();
					String json = gson.toJson(user);
					// put into context
					context.setNickName(nickName.getText().toString());
					context.setAcceptPercent(user.getAcceptPercent());
					context.setAverCycleSpeed(user.getAverCycleSpeed());
					context.setAverDriveSpeed(user.getAverDriveSpeed());
					context.setAverWalkSpeed(user.getAverWalkSpeed());
					context.setMode(user.getMode());

					// save into sharedPreference
					Editor editor = shared.edit();
					editor.putString("nickName", nickName.getText().toString());
					editor.putFloat("accept", user.getAcceptPercent());
					editor.putFloat("cycle", user.getAverCycleSpeed());
					editor.putFloat("drive", user.getAverDriveSpeed());
					editor.putFloat("walk", user.getAverWalkSpeed());
					editor.putString("mode", user.getMode());

					StringEntity entity = new StringEntity(json, "UTF-8");
					entity.setContentType("application/json");
					request.setEntity(entity);
					HttpClient httpClient = new DefaultHttpClient();
					HttpResponse response = httpClient.execute(request);
					if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
						Log.d(TAG, "request successfully");
						String result = EntityUtils.toString(response
								.getEntity());
						Log.d(TAG, "get results back");
						context.setUserId(result);
						editor.putString("userId", result);
						editor.commit();

						// jump into UserProfile
						Intent userProfile = new Intent(
								getApplicationContext(), UserProfile.class);
						startActivity(userProfile);
					} else {
						Log.d(TAG, "failed");
					}
				} catch (ClientProtocolException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}

				// locationManager.removeUpdates(locationListener);
			} else {
				Log.d(TAG, "get location is not good enough");
				buildAlertMessageNoLocation();
			}

		}

		/**
		 * get the userInformation
		 */
		private User createNewUser(Location location) {
			User u = new User();
			u.setAcceptPercent(0);
			u.setAverCycleSpeed(0);
			u.setAverDriveSpeed(0);
			u.setAverWalkSpeed(0);
			u.setHasSensor((byte) 48);

			u.setAccuracy(location.getAccuracy());
			u.setBearing(location.getBearing());
			u.setLatitude(location.getLatitude());
			u.setLongitude(location.getLongitude());
			u.setSpeed(location.getSpeed());

			u.setMode("Still");
			u.setRegisterId(GCMRegistrar
					.getRegistrationId(getApplicationContext()));
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

	@Override
	protected void onPause() {
		// locationManager.removeUpdates(locationListener);
		super.onPause();
	}

}
