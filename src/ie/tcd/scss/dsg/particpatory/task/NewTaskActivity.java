package ie.tcd.scss.dsg.particpatory.task;

import ie.tcd.scss.dsg.particpatory.AppContext;
import ie.tcd.scss.dsg.particpatory.R;
import ie.tcd.scss.dsg.particpatory.SampleListFragment;
import ie.tcd.scss.dsg.particpatory.util.Calculation;
import ie.tcd.scss.dsg.particpatory.util.Constant;
import ie.tcd.scss.dsg.particpatory.util.LocationUtil;
import ie.tcd.scss.dsg.particpatory.util.LocationUtil.LocationResult;
import ie.tcd.scss.dsg.po.TaskModel;
import ie.tcd.scss.dsg.po.User;

import java.io.ByteArrayOutputStream;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;

public class NewTaskActivity extends SlidingFragmentActivity {
	private static final String TAG = "NewTaskActivity";
	private AppContext context;
	private ListFragment mFrag;
	private String taskId;
	private Location local;
	private EditText userInput;
	private ImageView imageView;
	private TaskModel task = new TaskModel();
	private String formatted_address;
	private User currentUser = new User();
	private int acceptedNumber;
	private SharedPreferences shared;
	private TextView description;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		context = (AppContext) getApplicationContext();
		setContentView(R.layout.activity_show_task);
		setupSlidingMenu(savedInstanceState);
		shared = getSharedPreferences(AppContext.PREFS_NAME, MODE_PRIVATE);
		
		String taskInfo = context.getTaskInfo();
		description = (TextView) findViewById(R.id.task_desc);
		
		// taskInfo = description+expirePeriod+taskId(sub)
		if (context.getTaskdescription() != null) {
			taskId = context.getTaskId();
			description.setText(context.getTaskdescription());
		} else {
			String[] subMsg = taskInfo.split("_");
			description.setText(subMsg[0]);
			// int expireTime = Integer.valueOf(subMsg[1]);
			// String assignId = subMsg[3];
			taskId = subMsg[2];
		}

		LocationResult locationResult = new LocationResult() {
			@Override
			public void gotLocation(Location location) {
				// Got the location!
				Log.d(TAG, "get location:" + location);
				local = location;
				formatted_address = Constant.getLocationInfo(local.getLatitude(),
						local.getLongitude());
			}
		};
		
		LocationUtil updatedLocation = new LocationUtil();
		updatedLocation.getLocation(this, locationResult);
		userInput = (EditText) findViewById(R.id.input);
		ImageView addPicture = (ImageView) findViewById(R.id.addPic);
		addPicture.setOnClickListener(this.addNewPictureListener);
		imageView = (ImageView) findViewById(R.id.showImg);
	}

	private OnClickListener addNewPictureListener = new OnClickListener() {

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
		if (requestCode == 0) {
			if (imageReturnedIntent != null) {
				Bitmap photo = (Bitmap) imageReturnedIntent.getExtras().get(
						"data");
				photo = Bitmap.createBitmap(photo);
				Log.d(TAG, "photo="+photo);
				imageView.setImageBitmap(photo);
				imageView.setVisibility(View.VISIBLE);

				int bytes = photo.getByteCount();
				ByteArrayOutputStream array = new ByteArrayOutputStream(bytes);
				photo.compress(Bitmap.CompressFormat.PNG, 100, array);
				task.setPicture(array.toByteArray());
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
			String comment = userInput.getText().toString();
			task.setComment(comment);
			task.setTaskId(Long.valueOf(taskId));
			currentUser.setAccuracy(local.getAccuracy());
			currentUser.setBearing(local.getBearing());
			currentUser.setLatitude(local.getLatitude());
			currentUser.setLongitude(local.getLongitude());
			currentUser.setSpeed(local.getSpeed());
			currentUser.setMode(context.getMode());
			boolean flag = false;
			flag = submitTask(task);
			if (flag) {
				acceptedNumber = shared.getInt("acceptAmount", 0);
				acceptedNumber += 1;
				calculateAverage();
				currentUser.setStreetName(formatted_address);
				currentUser.setUserId(Long.valueOf(context.getUserId()));
				updateUserContext(currentUser);
			}
			break;
		}
		return true;
	}

	private void calculateAverage() {
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
		int overallNumber = shared.getInt("overall", 0);
		float acceptance = 0.f;
		if (overallNumber != 0) {
			acceptance = Calculation.acceptance(acceptedNumber, overallNumber);
		}
		currentUser.setAcceptPercent(acceptance);
		Editor editor = shared.edit();
		editor.putFloat("accept", acceptance);
		editor.putFloat("cycle", cycleSpeed);
		editor.putFloat("drive", driveSpeed);
		editor.putFloat("walk", walkSpeed);
		editor.commit();
	}

	private boolean submitTask(TaskModel task) {
		Log.d(TAG, "set url");
		String url = Constant.url + "/finishtask";
		HttpPost request = new HttpPost(url);
		Gson gson = new Gson();
		String json = gson.toJson(task);
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
						TaskActivity.class);
				startActivity(newIntent);
			} else {
				Log.d(TAG, "failed");
			}
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

		SlidingMenu sm = getSlidingMenu();
		sm.setShadowWidthRes(R.dimen.shadow_width);
		sm.setShadowDrawable(R.drawable.shadow);
		sm.setBehindOffsetRes(R.dimen.slidingmenu_offset);
		sm.setFadeDegree(0.35f);
		sm.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
	}
}
