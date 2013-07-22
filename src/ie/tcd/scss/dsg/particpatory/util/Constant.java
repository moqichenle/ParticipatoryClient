package ie.tcd.scss.dsg.particpatory.util;

import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.StrictMode;

public class Constant {
	public static String url ="http://192.168.1.10:8888";//10.6.33.177  192.168.1.10
	
	public static String getCategoryName(byte id){
		if(id==(byte)0){
			return "Traffic";
		}else if(id==(byte)1){
			return "Impression";
		}else if(id==(byte)2){
			return "Queue";
		}else{
			return "no category";
		}
	}
	
	public static JSONObject getLocationInfo(double lat, double lng) {

		HttpGet httpGet = new HttpGet(
				"http://maps.google.com/maps/api/geocode/json?latlng=" + lat
						+ "," + lng + "&sensor=true");
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
		} catch (Exception e) {
		}

		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject = new JSONObject(stringBuilder.toString());
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return jsonObject;
	}
	public static void setupConnectin(){
		if (android.os.Build.VERSION.SDK_INT > 9) {
			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
					.permitAll().build();
			StrictMode.setThreadPolicy(policy);
		}
	}
}
