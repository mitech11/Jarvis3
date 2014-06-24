package com.techathon.jarvis.data;

public class LocationData {
	public String str_locality;
	public String str_city;
	public double latitude;
	public double longitude;
	public String date;
	public String time;
	public float speed;
	
	public LocationData(String place,String city,double lat, double lng,String dt,String time,float speed) {
		this.str_locality = place;
		this.str_city = city;
		this.latitude = lat;
		this.longitude = lng;
		this.date = dt;
		this.time = time;
		this.speed = speed;
	}

	public String toString(){
		return "You were at "+str_locality +" on "+ date +" at "+ time +" driving at "+ speed; 
	}

}
