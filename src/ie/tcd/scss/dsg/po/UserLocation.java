package ie.tcd.scss.dsg.po;

/**
 * 
 * @author Lele
 * 
 *         used to store the information about a location. including the
 *         longitude, altitude, accuracy; speed and bearing are used to
 *         calculate the next likely location.
 * 
 */
public class UserLocation {

	private double longitude;
	private double latitude;
	private float bearing;
	private float speed;
	private float accuracy;

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public float getBearing() {
		return bearing;
	}

	public void setBearing(float bearing) {
		this.bearing = bearing;
	}

	public float getSpeed() {
		return speed;
	}

	public void setSpeed(float speed) {
		this.speed = speed;
	}

	public float getAccuracy() {
		return accuracy;
	}

	public void setAccuracy(float accuracy) {
		this.accuracy = accuracy;
	}

}
