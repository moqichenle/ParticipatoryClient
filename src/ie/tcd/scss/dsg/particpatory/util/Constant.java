package ie.tcd.scss.dsg.particpatory.util;

public class Constant {
	public static String url ="http://192.168.1.10:8888";//10.6.33.177
	
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
}
