package ie.tcd.scss.dsg.particpatory.util;

public class Calculation {
	public static float averageWalkSpeed(float before, float now){
		return (before+now)/2.0f;
	}
	public static float averageCycleSpeed(float before, float now){
		return (before+now)/2.0f;
	}
	public static float averageDriveSpeed(float before, float now){
		return (before+now)/2.0f;
	}
	public static float acceptance(int accepted, int overall){
		return accepted/overall*1.0f;
	}
}
