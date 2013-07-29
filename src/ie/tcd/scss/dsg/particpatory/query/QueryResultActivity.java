package ie.tcd.scss.dsg.particpatory.query;

import ie.tcd.scss.dsg.particpatory.AppContext;
import ie.tcd.scss.dsg.particpatory.R;
import ie.tcd.scss.dsg.particpatory.util.Constant;
import ie.tcd.scss.dsg.po.ResultsToQuery;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;

public class QueryResultActivity extends Activity {
	private static final String TAG = "QueryResult";
	private AppContext context;
	// private ListFragment mFrag;
	private GoogleMap mMap;
	private String results;
	private Map<String, byte[]> ids = new HashMap<String, byte[]>();

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
		getActionBar().setDisplayHomeAsUpEnabled(true);
		mMap.setOnMarkerClickListener(new OnMarkerClickListener() {

			@Override
			public boolean onMarkerClick(Marker m) {
				String key = m.getId();
				Log.d(TAG, "----------------->" + key);
				if (ids.containsKey(m.getId())) {
					Log.d(TAG,
							"contain key..with picture/" + key + "/"
									+ m.getSnippet());
					customContent(m.getTitle(), m.getSnippet(), ids.get(key));
					m.showInfoWindow();
				} else {
					Log.d(TAG, "marker without picture");
					mMap.setInfoWindowAdapter(new InfoWindowAdapter() {

						@Override
						public View getInfoWindow(Marker arg0) {
							return null;
						}

						@Override
						public View getInfoContents(Marker arg0) {
							return null;
						}
					});
				}
				return false;
			}
		});
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			Intent intent = new Intent(context, QueryActivity.class);
			startActivity(intent);
			break;
		}
		return true;
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
				Marker m = mMap.addMarker(new MarkerOptions()
						.position(
								new LatLng(r[i].getLatitude(), r[i]
										.getLongitude()))
						.title(Constant.getCategoryName(r[i].getCategoryId()))
						.snippet(r[i].getContent()));
				ids.put(m.getId(), attach);
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

				if (r[i].getTaskComment() != null) {
					Marker m = mMap.addMarker(new MarkerOptions()
							.position(
									new LatLng(r[i].getLatitude(), r[i]
											.getLongitude()))
							.title(Constant.getCategoryName(r[i]
									.getCategoryId()))
							.snippet(r[i].getTaskComment()));
					ids.put(m.getId(), attach);
				} else {
					Marker m = mMap.addMarker(new MarkerOptions()
							.position(
									new LatLng(r[i].getLatitude(), r[i]
											.getLongitude()))
							.title(Constant.getCategoryName(r[i]
									.getCategoryId()))
							.snippet("no specific comment"));
					ids.put(m.getId(), attach);
				}

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

	private void customContent(final String titles, final String contents,
			final byte[] attach) {
		mMap.setInfoWindowAdapter(new InfoWindowAdapter() {

			// Use default InfoWindow frame
			@Override
			public View getInfoWindow(Marker arg0) {
				return null;
			}

			// Defines the contents of the InfoWindow
			@Override
			public View getInfoContents(Marker arg0) {

				// Getting view from the layout file info_window_layout
				View v = getLayoutInflater().inflate(
						R.layout.activity_map_content, null);

				TextView title = (TextView) v.findViewById(R.id.mc_1);

				TextView content = (TextView) v.findViewById(R.id.mc_2);

				title.setText(titles + "");
				content.setText(contents + "");

				ImageView imageToShow = (ImageView) v
						.findViewById(R.id.mc_image);

				BitmapFactory.Options options = new BitmapFactory.Options();
				Bitmap bMap = BitmapFactory.decodeByteArray(attach, 0,
						attach.length, options);
				imageToShow.setImageBitmap(bMap);

				// Returning the view containing InfoWindow contents
				return v;

			}
		});

	}
}
