package ie.tcd.scss.dsg.particpatory.query;

import ie.tcd.scss.dsg.particpatory.AppContext;
import ie.tcd.scss.dsg.particpatory.R;
import ie.tcd.scss.dsg.particpatory.util.Constant;
import ie.tcd.scss.dsg.po.ResultsToQuery;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;

public class QueryResultActivity extends Activity {
	private static final String TAG = "QueryResult";
	private AppContext context;
//	private ListFragment mFrag;
	private GoogleMap mMap;
	private String results;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map);
		Log.d(TAG, "loaded");
		context = (AppContext) getApplicationContext();
		String queryId = context.getQueryId();
		// setupSlidingMenu(savedInstanceState);
		setUpMapIfNeeded();
		mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
		mMap.setMyLocationEnabled(true);
		UiSettings settings = mMap.getUiSettings();
		settings.setCompassEnabled(true);
		settings.setMyLocationButtonEnabled(true);
		settings.setScrollGesturesEnabled(true);
		settings.setZoomControlsEnabled(true);
		settings.setZoomGesturesEnabled(true);
		// create an overlay that shows our current location
		new ResultListTask().execute(Constant.url + "/queryresults", queryId);
	}

	private void setUpMapIfNeeded() {
		// Do a null check to confirm that we have not already instantiated the
		// map.
		if (mMap == null) {
			mMap = ((MapFragment) getFragmentManager().findFragmentById(
					R.id.map)).getMap();
			// Check if we were successful in obtaining the map.
			if (mMap == null) {
				// The Map isnt verified.
				Toast.makeText(getApplicationContext(),
						"The Map is not verified", Toast.LENGTH_LONG).show();
			}
		}
	}

	// private void setupSlidingMenu(Bundle savedInstanceState) {
	//
	// setBehindContentView(R.layout.menu_frame);
	// if (savedInstanceState == null) {
	// FragmentTransaction t = this.getSupportFragmentManager()
	// .beginTransaction();
	// mFrag = new SampleListFragment();
	// t.replace(R.id.menu_frame, mFrag);
	// t.commit();
	// } else {
	// mFrag = (ListFragment) this.getSupportFragmentManager()
	// .findFragmentById(R.id.menu_frame);
	// }
	//
	// // customize the SlidingMenu
	// SlidingMenu sm = getSlidingMenu();
	// sm.setShadowWidthRes(R.dimen.shadow_width);
	// sm.setShadowDrawable(R.drawable.shadow);
	// sm.setBehindOffsetRes(R.dimen.slidingmenu_offset);
	// sm.setFadeDegree(0.35f);
	// sm.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
	//
	// // getActionBar().setDisplayHomeAsUpEnabled(true);
	// }

	private class ResultListTask extends AsyncTask<String, Void, HttpResponse> {
		@Override
		protected HttpResponse doInBackground(String... urls) {
			String url = urls[0];
			String queryId = urls[1];

			ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
			HttpPost request = new HttpPost(url);
			HttpClient httpClient = new DefaultHttpClient();
			HttpResponse response = null;
			postParameters.add(new BasicNameValuePair("queryId", queryId));

			Log.d(TAG, "before requesting");
			try {
				request.setEntity(new UrlEncodedFormEntity(postParameters));
				response = httpClient.execute(request);
				if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
					Log.d(TAG, "request successfully");
				} else {
					Log.d(TAG, "failed");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return response;
		}

		@Override
		protected void onPostExecute(HttpResponse response) {
			// Do something with result
			if (response != null) {
				try {

					BufferedReader reader = new BufferedReader(
							new InputStreamReader(response.getEntity()
									.getContent(), "UTF-8"));
					results = reader.readLine();
					Gson gson = new Gson();
					if (results != null) {
						System.out.println(results + "**********");
						ResultsToQuery[] r = gson.fromJson(results,
								ResultsToQuery[].class);
						if (r.length == 0) {
							Toast.makeText(getApplicationContext(),
									"No Results", Toast.LENGTH_LONG).show();
						} else {
							AddMarkers(r);
						}
					}
				} catch (ParseException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private void AddMarkers(ResultsToQuery[] r) {
		for (int i = 0; i < r.length; i++) {
			if (r[i].getReportId() != null && r[i].getResultImage() != null) {
				byte[] attach = r[i].getResultImage();
				BitmapFactory.Options options = new BitmapFactory.Options();
				Bitmap bMap = BitmapFactory.decodeByteArray(attach, 0,
						attach.length, options);
				mMap.addMarker(new MarkerOptions()
						.position(
								new LatLng(r[i].getLatitude(), r[i]
										.getLongitude()))
						.title(Constant.getCategoryName(r[i].getCategoryId()))
						.snippet(r[i].getContent())
						.icon(BitmapDescriptorFactory.fromBitmap(bMap)));
			} else if (r[i].getReportId() != null
					&& r[i].getResultImage() == null) {
				mMap.addMarker(new MarkerOptions()
						.position(
								new LatLng(r[i].getLatitude(), r[i]
										.getLongitude()))
						.title(Constant.getCategoryName(r[i].getCategoryId()))
						.snippet(r[i].getContent()));
			} else if (r[i].getTaskId() != null
					&& r[i].getResultImage() != null) {
				// image
				byte[] attach = r[i].getResultImage();
				BitmapFactory.Options options = new BitmapFactory.Options();
				Bitmap bMap = BitmapFactory.decodeByteArray(attach, 0,
						attach.length, options);
				mMap.addMarker(new MarkerOptions()
						.position(
								new LatLng(r[i].getLatitude(), r[i]
										.getLongitude()))
						.title(Constant.getCategoryName(r[i].getCategoryId()))
						.icon(BitmapDescriptorFactory.fromBitmap(bMap)));
			} else if (r[i].getTaskId() != null
					&& r[i].getResultImage() == null) {
				// normal questioned tasks
				mMap.addMarker(new MarkerOptions()
						.position(
								new LatLng(r[i].getLatitude(), r[i]
										.getLongitude()))
						.title(Constant.getCategoryName(r[i].getCategoryId()))
						.snippet(r[i].getTaskComment()));
			}
		}
	}
}
