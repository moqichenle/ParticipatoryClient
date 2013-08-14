/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ie.tcd.scss.dsg.particpatory.activityrecognition;

import ie.tcd.scss.dsg.particpatory.AppContext;
import ie.tcd.scss.dsg.particpatory.util.Calculation;
import ie.tcd.scss.dsg.particpatory.util.Constant;
import ie.tcd.scss.dsg.particpatory.util.LocationUtil;
import ie.tcd.scss.dsg.particpatory.util.LocationUtil.LocationResult;
import android.app.AlertDialog;
import android.app.IntentService;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.Location;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

/**
 * Service that receives ActivityRecognition updates. It receives updates in the
 * background, even if the main Activity is not visible.
 */
public class ActivityRecognitionIntentService extends IntentService {

	private SharedPreferences mPrefs;
	public static final String RECOGNITION_RESULT = "result";
	private AppContext context;
	// private int activityDetectedTimes = 0;
	private float newSpeed;
	private String detectedMode;

	public ActivityRecognitionIntentService() {
		// Set the label for the service's background thread
		super("ActivityRecognitionIntentService");
	}

	/**
	 * Called when a new activity detection update is available.
	 */
	@Override
	protected void onHandleIntent(Intent intent) {

		Log.d("ActivityRecognitionIntentService", "start!");
		context = (AppContext) getApplicationContext();
		// Get a handle to the repository
		mPrefs = getSharedPreferences(AppContext.PREFS_NAME, MODE_PRIVATE);
		if (ActivityRecognitionResult.hasResult(intent)) {

			ActivityRecognitionResult result = ActivityRecognitionResult
					.extractResult(intent);
			DetectedActivity mostProbableActivity = result
					.getMostProbableActivity();

			int confidence = mostProbableActivity.getConfidence();
			int activityType = mostProbableActivity.getType();
			Log.d("ActivityRecognitionIntentService",
					getNameFromType(activityType) + "***" + confidence);
			detectedMode = getNameFromType(activityType);

			if (confidence >= 50 && !detectedMode.equals("UNKNOWN")
					&& !detectedMode.equals("unknown")) {
				Editor editor = mPrefs.edit();
				if (context.getLocation() != null) {
					// when the MainAcitvity detects current location.

					/*
					 * for test, assume the status is onfoot
					 * context.setMode("on_foot"); detectedMode = "on_foot";
					 */
					if (context.getMode() != null) {

						if (!context.getMode().equals(detectedMode)) {
							Log.d("ActivityRecognitionIntentService",
									"update when mode changed*!!!!!!!!!!"
											+ detectedMode);
							// only update when mode changed
							Handler h = new Handler(Looper.getMainLooper());
							h.post(new Runnable() {
								@Override
								public void run() {
									LocationResult locationResult = new LocationResult() {
										@Override
										public void gotLocation(
												Location location) {
											Log.d("ActivityRecognitionIntentService",
													"location:" + location);
											context.setLocation(location);
											newSpeed = location.getSpeed();
											doCalculations(detectedMode);
											Constant.updateUserInformation(
													location, context);
										}
									};
									LocationUtil updatedLocation = new LocationUtil();
									boolean flag = updatedLocation.getLocation(
											getApplicationContext(),
											locationResult);
									if (!flag) {
										// didnt open GPS or network.
										buildAlertMessageNoGps();
									}
								}
							});
							Constant.activityDetectedTimes = 0;
						} else if (!context.getMode().equals("still")) {
							// if the mode is not still, and didnt change for
							// every
							// mode
							// checking. every 60 seconds.
							Constant.activityDetectedTimes++;
							Handler h = new Handler(Looper.getMainLooper());
							h.post(new Runnable() {
								@Override
								public void run() {
									LocationResult locationResult = new LocationResult() {
										@Override
										public void gotLocation(
												Location location) {
											Log.d("ActivityRecognitionIntentService",
													"location:" + location);
											context.setLocation(location);
											newSpeed = location.getSpeed();
											if (context.getMode().equals(
													"on_foot")) {
												if (Constant.activityDetectedTimes == 15) {
													// walking , every 15 mins.
													doCalculations("on_foot");
													Constant.updateUserInformation(
															location, context);
													Constant.activityDetectedTimes = 0;
												}
											} else if (context.getMode()
													.equals("on_bicycle")) {
												if (Constant.activityDetectedTimes == 10) {
													// cycling , every 10 mins.
													doCalculations("on_bicycle");
													Constant.updateUserInformation(
															location, context);
													Constant.activityDetectedTimes = 0;
												}
											} else if (context.getMode()
													.equals("in_vehicle")) {
												if (Constant.activityDetectedTimes == 5) {
													// driving , every 5 mins.
													doCalculations("in_vehicle");
													Constant.updateUserInformation(
															location, context);
													Constant.activityDetectedTimes = 0;
												}
											}
										}
									};

									LocationUtil updatedLocation = new LocationUtil();
									boolean flag = updatedLocation.getLocation(
											getApplicationContext(),
											locationResult);
									if (!flag) {
										// didnt open GPS or network.
										buildAlertMessageNoGps();
									}
								}
							});
						}
					}
					context.setMode(detectedMode);
					Intent i = new Intent("ie.tcd.scss.dsg.particpatory.UPDATE");
					i.putExtra(RECOGNITION_RESULT, detectedMode);
					editor.putString("mode", detectedMode);
					editor.commit();
					editor.commit();
					sendBroadcast(i);
				}
			}
		}
	}

	private void doCalculations(String activity) {
		Editor editor = mPrefs.edit();
		// float newSpeed = local.getSpeed();
		Log.d("caclulations::::::::::newSpeed", newSpeed + "");
		float walkSpeed = context.getAverWalkSpeed();
		float cycleSpeed = context.getAverCycleSpeed();
		float driveSpeed = context.getAverDriveSpeed();
		if (activity.equals("on_foot")) {
			walkSpeed = Calculation.averageWalkSpeed(walkSpeed, newSpeed);
			context.setAverWalkSpeed(walkSpeed);
			editor.putFloat("walk", walkSpeed);
			Log.d("doCalculations", "newWalkSpeed=" + walkSpeed);
		} else if (activity.equals("on_bicycle")) {
			cycleSpeed = Calculation.averageCycleSpeed(cycleSpeed, newSpeed);
			context.setAverCycleSpeed(cycleSpeed);
			editor.putFloat("cycle", cycleSpeed);
		} else if (activity.equals("in_vehicle")) {
			driveSpeed = Calculation.averageDriveSpeed(driveSpeed, newSpeed);
			context.setAverDriveSpeed(driveSpeed);
			editor.putFloat("drive", driveSpeed);
		}
		editor.commit();
	}

	// private void checkLocation() {
	// Handler h = new Handler(Looper.getMainLooper());
	// h.post(new Runnable() {
	// @Override
	// public void run() {
	// LocationResult locationResult = new LocationResult() {
	// @Override
	// public void gotLocation(Location location) {
	// Log.d("ActivityRecognitionIntentService", "location:"
	// + location);
	// context.setLocation(location);
	// }
	// };
	//
	// LocationUtil updatedLocation = new LocationUtil();
	// boolean flag = updatedLocation.getLocation(
	// getApplicationContext(), locationResult);
	// if (!flag) {
	// // didnt open GPS or network.
	// buildAlertMessageNoGps();
	// }
	// }
	// });
	// }

	/**
	 * to check if the GPS is open
	 */
	private void buildAlertMessageNoGps() {
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(
				"Your GPS seems to be disabled and you didn't open wireless network. To continue, you should enable location services.")
				.setCancelable(false)
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(final DialogInterface dialog,
							final int id) {
						startActivity(new Intent(
								android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
					}
				});
		final AlertDialog alert = builder.create();
		alert.show();
	}

	// /**
	// * Determine if an activity means that the user is moving.
	// *
	// * @param type
	// * The type of activity the user is doing (see DetectedActivity
	// * constants)
	// * @return true if the user seems to be moving from one location to
	// another,
	// * otherwise false
	// */
	// private boolean isMoving(int type) {
	// switch (type) {
	// // These types mean that the user is probably not moving
	// case DetectedActivity.STILL:
	// case DetectedActivity.TILTING:
	// case DetectedActivity.UNKNOWN:
	// return false;
	// default:
	// return true;
	// }
	// }

	/**
	 * Map detected activity types to strings
	 * 
	 * @param activityType
	 *            The detected activity type
	 * @return A user-readable name for the type
	 */
	private String getNameFromType(int activityType) {
		switch (activityType) {
		case DetectedActivity.IN_VEHICLE:
			return "in_vehicle";
		case DetectedActivity.ON_BICYCLE:
			return "on_bicycle";
		case DetectedActivity.ON_FOOT:
			return "on_foot";
		case DetectedActivity.STILL:
			return "still";
		case DetectedActivity.UNKNOWN:
			return "unknown";
			// case DetectedActivity.TILTING:
			// return "tilting";
		}
		return "UNKNOWN";
	}
}
