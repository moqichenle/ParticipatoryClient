package ie.tcd.scss.dsg.particpatory.util;

import ie.tcd.scss.dsg.particpatory.AppContext;
import ie.tcd.scss.dsg.po.User;

import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import android.location.Location;
import android.os.StrictMode;

import com.google.gson.Gson;

public class Constant {
	public static String url = "http://participatorysensing.appspot.com";// 10.6.33.177
															// 192.168.1.10
	public static int activityDetectedTimes = 0;

	public static String getCategoryName(byte id) {
		if (id == (byte) 0) {
			return "Traffic";
		} else if (id == (byte) 1) {
			return "Impression";
		} else if (id == (byte) 2) {
			return "Queue";
		} else {
			return "no category";
		}
	}

	/**
	 * according longtitude and latitude , get the location's name
	 * 
	 * @param lat
	 * @param lng
	 * @return
	 */
	public static String getLocationInfo(double lat, double lng) {
		setupConnectin();
		HttpGet httpGet = new HttpGet(
				"http://maps.googleapis.com/maps/api/geocode/json?latlng="
						+ lat + "," + lng + "&sensor=true");
		HttpClient client = new DefaultHttpClient();
		HttpResponse response;
		StringBuilder stringBuilder = new StringBuilder();

		try {
			response = client.execute(httpGet);
			HttpEntity entity = response.getEntity();
			InputStream stream = entity.getContent();
			int b;
			while ((b = stream.read()) != -1) {
				stringBuilder.append((char) b);
			}

			JSONObject jsonObject = new JSONObject();
			jsonObject = new JSONObject(stringBuilder.toString());
			JSONObject address;
			address = jsonObject.getJSONArray("results").getJSONObject(0);
			String formatted_address = address.getString("formatted_address");
			System.out.println("get formatted_address!!!" + formatted_address);
			return formatted_address;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static void setupConnectin() {
		if (android.os.Build.VERSION.SDK_INT > 9) {
			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
					.permitAll().build();
			StrictMode.setThreadPolicy(policy);
		}
	}

	public static void updateUserInformation(Location local, AppContext context) {
		Constant.setupConnectin();
		System.out.println("++++++++++++++++++++++++++++" + local.getLatitude()
				+ "/" + local.getLongitude());
		User user = new User();
		user.setUserId(Long.valueOf(context.getUserId()));
		user.setAccuracy(local.getAccuracy());
		user.setBearing(local.getBearing());
		user.setLatitude(local.getLatitude());
		user.setLongitude(local.getLongitude());
		user.setSpeed(local.getSpeed());
		user.setAcceptPercent(context.getAcceptPercent());
		user.setAverCycleSpeed(context.getAverCycleSpeed());
		user.setAverDriveSpeed(context.getAverDriveSpeed());
		user.setAverWalkSpeed(context.getAverWalkSpeed());
		user.setMode(context.getMode());
		user.setStreetName(getLocationInfo(local.getLatitude(),
				local.getLongitude()));
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
				System.out.println("update user's information.");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
