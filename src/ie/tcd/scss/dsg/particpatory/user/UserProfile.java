package ie.tcd.scss.dsg.particpatory.user;

import ie.tcd.scss.dsg.particpatory.AppContext;
import ie.tcd.scss.dsg.particpatory.R;
import ie.tcd.scss.dsg.particpatory.SampleListFragment;
import ie.tcd.scss.dsg.particpatory.activityrecognition.ActivityRecognitionIntentService;
import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.ActivityRecognitionClient;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;

public class UserProfile extends SlidingFragmentActivity implements
		ConnectionCallbacks, OnConnectionFailedListener {
	public static final int MILLISECONDS_PER_SECOND = 1000;
	public static final int DETECTION_INTERVAL_SECONDS = 20;
	public static final int DETECTION_INTERVAL_MILLISECONDS = MILLISECONDS_PER_SECOND
			* DETECTION_INTERVAL_SECONDS;
	private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
	private PendingIntent mActivityRecognitionPendingIntent;
	private ActivityRecognitionClient mActivityRecognitionClient;
	private boolean mInProgress;
	private boolean mReceiverRegistered;
	private static final String TAG = "UserProfileActivity";
	private AppContext context;
	private ListFragment mFrag;
	private TextView mode;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "load the page");

		setContentView(R.layout.activity_user_profile);
		context = (AppContext) getApplicationContext();
		setupSlidingMenu(savedInstanceState);

		mInProgress = false;

		mActivityRecognitionClient = new ActivityRecognitionClient(context,
				this, this);
		Intent intent = new Intent(context,
				ActivityRecognitionIntentService.class);
		mActivityRecognitionPendingIntent = PendingIntent.getService(context,
				0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

		startUpdates();
		registerReceiver(mUpdateReceiver, new IntentFilter(
				"ie.tcd.scss.dsg.particpatory.UPDATE"));
		mReceiverRegistered = true;
		Log.d(TAG, "get User");
		String nickName = context.getNickName();
		TextView userName = (TextView) findViewById(R.id.userName);
		userName.setText(nickName);
		mode = (TextView) findViewById(R.id.mode);
		mode.setText(context.getMode());
		TextView walk = (TextView) findViewById(R.id.walk);
		TextView cycle = (TextView) findViewById(R.id.cycle);
		TextView drive = (TextView) findViewById(R.id.drive);
		TextView rate = (TextView) findViewById(R.id.rate);
		// mode.setText(context.getMode());
		walk.setText(context.getAverWalkSpeed() + "");
		cycle.setText(context.getAverCycleSpeed() + "");
		drive.setText(context.getAverDriveSpeed() + "");
		rate.setText(context.getAcceptPercent() + "");
	}

	private BroadcastReceiver mUpdateReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context con, Intent intent) {
			String result = intent
					.getStringExtra(ActivityRecognitionIntentService.RECOGNITION_RESULT);
			mode.setText(result);
			context.setMode(result);
		}
	};

	/*
	 * Handle results returned to the FragmentActivity by Google Play services
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		switch (requestCode) {
		case CONNECTION_FAILURE_RESOLUTION_REQUEST:
			switch (resultCode) {
			case Activity.RESULT_OK:
				/*
				 * Try the request again
				 */
				break;
			}
		}
	}

	@Override
	public void onConnectionFailed(ConnectionResult arg0) {
		mInProgress = false;
		Toast.makeText(context, "CONNECTION FAILED!!", Toast.LENGTH_LONG)
				.show();
	}

	@Override
	public void onConnected(Bundle arg0) {
		mActivityRecognitionClient.requestActivityUpdates(
				DETECTION_INTERVAL_MILLISECONDS,
				mActivityRecognitionPendingIntent);
		mInProgress = false;
		mActivityRecognitionClient.disconnect();
	}

	@Override
	public void onDisconnected() {
		mInProgress = false;
		mActivityRecognitionClient = null;
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (mReceiverRegistered) {
			unregisterReceiver(mUpdateReceiver);
			mReceiverRegistered = false;
		}
	}

	public static class ErrorDialogFragment extends DialogFragment {

		private Dialog mDialog;

		public ErrorDialogFragment() {
			super();
			mDialog = null;
		}

		public void setDialog(Dialog dialog) {
			mDialog = dialog;
		}

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			return mDialog;
		}
	}

	public void startUpdates() {
		if (!servicesConnected()) {
			return;
		}
		if (!mInProgress) {
			mInProgress = true;
			mActivityRecognitionClient.connect();
		} else {
			Toast.makeText(this, "Already a request in progess",
					Toast.LENGTH_SHORT).show();
		}
	}

	private boolean servicesConnected() {
		int resultCode = GooglePlayServicesUtil
				.isGooglePlayServicesAvailable(this);

		if (ConnectionResult.SUCCESS == resultCode) {
			Log.d("Activity Recognition", "Google Play services is available.");
			return true;
		} else {
			Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(
					resultCode, this, CONNECTION_FAILURE_RESOLUTION_REQUEST);

			if (errorDialog != null) {
				ErrorDialogFragment errorFragment = new ErrorDialogFragment();
				errorFragment.setDialog(errorDialog);
				errorFragment
						.show(getFragmentManager(), "Activity Recognition");
			}
			return false;
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
}
