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
import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
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
		// Get a handle to the repository
		mPrefs = getSharedPreferences(AppContext.PREFS_NAME, MODE_PRIVATE);


		// If the intent contains an update
		if (ActivityRecognitionResult.hasResult(intent)) {

			// Get the update
			ActivityRecognitionResult result = ActivityRecognitionResult
					.extractResult(intent);

			// Get the most probable activity from the list of activities in the
			// update
			DetectedActivity mostProbableActivity = result
					.getMostProbableActivity();

			// Get the confidence percentage for the most probable activity
			int confidence = mostProbableActivity.getConfidence();
			// Get the type of activity
			int activityType = mostProbableActivity.getType();
			Log.d("ActivityRecognitionIntentService", getNameFromType(activityType)+"***"+confidence);
			if(confidence>=80&&!getNameFromType(activityType).equals("UNKNOWN")){
				Editor editor = mPrefs.edit();
				editor.putString("mode", getNameFromType(activityType));
				editor.commit();
				Intent i = new Intent("ie.tcd.scss.dsg.particpatory.UPDATE");
				i.putExtra(RECOGNITION_RESULT, getNameFromType(activityType));
				sendBroadcast(i);
			}
		}
	}


//	/**
//	 * Determine if an activity means that the user is moving.
//	 * 
//	 * @param type
//	 *            The type of activity the user is doing (see DetectedActivity
//	 *            constants)
//	 * @return true if the user seems to be moving from one location to another,
//	 *         otherwise false
//	 */
//	private boolean isMoving(int type) {
//		switch (type) {
//		// These types mean that the user is probably not moving
//		case DetectedActivity.STILL:
//		case DetectedActivity.TILTING:
//		case DetectedActivity.UNKNOWN:
//			return false;
//		default:
//			return true;
//		}
//	}


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
//		case DetectedActivity.TILTING:
//			return "tilting";
		}
		return "UNKNOWN";
	}
}
