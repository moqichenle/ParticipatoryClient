package ie.tcd.scss.dsg.particpatory;

import ie.tcd.scss.dsg.particpatory.user.UserProfile;
import ie.tcd.scss.dsg.particpatory.user.UserRegister;
import ie.tcd.scss.dsg.particpatory.user.Welcome;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gcm.GCMRegistrar;

/**
 * The Main Activity.
 * 
 * This activity starts up the RegisterActivity immediately, which communicates
 * with your App Engine backend using Cloud Endpoints. It also receives push
 * notifications from backend via Google Cloud Messaging (GCM).
 * 
 * Check out RegisterActivity.java for more details.
 */
public class MainActivity extends Activity {
	AppContext context;
	private static final String TAG = "MainActivity";

	
	@SuppressLint("CommitPrefEdits")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		context = (AppContext) getApplicationContext();

		
		Log.d(TAG, (context.getNickName()) + "hi");
		if (context.getNickName() == null) {
			if (GCMRegistrar.getRegistrationId(this.getApplicationContext()) != "") {
				Log.d(TAG, "user registered before");
				String registerId = GCMRegistrar.getRegistrationId(this
						.getApplicationContext());
				context.setRegisteredId(registerId);
				SharedPreferences shared = getSharedPreferences(
						AppContext.PREFS_NAME, MODE_PRIVATE);
				Editor edit = shared.edit();
				edit.putString("registeredId", registerId);
				edit.commit();
				Log.d(TAG, "store the registeredId in the app");
				Intent intent = new Intent(this, UserRegister.class);
				startActivity(intent);
			} else {
				// Start up RegisterActivity right away
				Intent intent = new Intent(this, Welcome.class);
				startActivity(intent);
				// Since this is just a wrapper to start the main activity,
				// finish it after launching RegisterActivity
			}
		} else {
			Intent intent = new Intent(this, UserProfile.class);
			startActivity(intent);
		}
		finish();

	}


}
