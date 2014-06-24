package com.techathon.jarvis;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMyLocationChangeListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CameraPosition.Builder;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class ShowParkedCar extends Activity implements OnMyLocationChangeListener{

	private GoogleMap googleMap;
	private SharedPreferences preferences;
	private double mLatPrefs,mLongPrefs,mLat,mLong;
	private LocationManager locationManager;
	private TextView parkingText;
	private Button parkingButton;
	private boolean saveMode;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_show_parked_car);
		
		preferences = this.getSharedPreferences(Holder.SHARED_PREFS_NAME, Context.MODE_PRIVATE);
		parkingText = (TextView) this.findViewById(R.id.parkingTextV);
		parkingButton = (Button) this.findViewById(R.id.parkingButton);
		
		/*locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		boolean isGPS = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if(!isGPS){
        	showMyDialog();
        }
		try {
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10 * 60 * 60 * 1000, 10, this);
			try {
				locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10 * 60 * 60 * 1000, 10, this);
			} catch (Exception e) {
				// TODO: handle exception
			}
		} catch (Exception e) {
			// TODO: handle exception
		}*/
		
		
		MapFragment map = (MapFragment) this.getFragmentManager().findFragmentById(R.id.showCarParked);
		googleMap = map.getMap();
		googleMap.setMyLocationEnabled(true);
		googleMap.setOnMyLocationChangeListener(this);
//		LatLng location = new LatLng(18.554427, 73.892508);
		mLatPrefs = Double.parseDouble(preferences.getString(Holder.LATTITUDE, "0"));
		mLongPrefs = Double.parseDouble(preferences.getString(Holder.LONGITUDE, "0"));
		
		LatLng location = null;
		Builder builder = new CameraPosition.Builder();
		if(mLatPrefs != 0 && mLongPrefs != 0){
			saveMode = false;
			parkingText.setVisibility(TextView.VISIBLE);
			parkingButton.setText("Reset Parking Location");
			
			try {
				getParkingAddress(mLatPrefs,mLongPrefs);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				parkingText.setText("Location Name Unavailable");
			}
			
			location = new LatLng(mLatPrefs,mLongPrefs);
			placeMarker(mLatPrefs,mLongPrefs);
		
			builder.zoom(16);
			builder.target(location);
			CameraPosition position = builder.build();
			
			googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(position));
			
		}else{
			saveMode = true;
			parkingButton.setText("Save Parking Location");
			googleMap.clear();
			parkingText.setVisibility(TextView.GONE);
			
		}	
	}
	
	private void placeMarker(double latitude, double longitude) {
		// TODO Auto-generated method stub
		MarkerOptions markerOptions = new MarkerOptions();
		markerOptions.position(new LatLng(latitude, longitude));
		markerOptions.title("Parking Location");
		googleMap.addMarker(markerOptions);
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
//		mLat = (long) googleMap.getMyLocation().getLatitude();
//		mLong = (long) googleMap.getMyLocation().getLongitude();
	}
	
	public void saveReset(View view) {
		SharedPreferences.Editor editor = preferences.edit();
		if (saveMode) {
			editor.putString(Holder.LATTITUDE, mLat + "");
			editor.putString(Holder.LONGITUDE, mLong + "");
			editor.commit();
			showToast("Parking location saved!!!  @ Lattitude = " + mLat + " and Longitude = " +  mLong);
			
			parkingButton.setText("Reset Parking Location");
			parkingText.setVisibility(TextView.VISIBLE);
			
			placeMarker(mLat, mLong);
			try {
				getParkingAddress(mLat,mLong);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				parkingText.setText("Location Unavailable");
			}
			
			saveMode = false;
		} else {
			editor.clear();
			editor.commit();
			showToast("Parking Location Reset!!!");
			googleMap.clear();
			parkingButton.setText("Save Parking Location");
			parkingText.setVisibility(TextView.GONE);
			saveMode = true;
		}
	}
	
	private void getParkingAddress(double latti, double longi) throws IOException {
		// TODO Auto-generated method stub
		if(Geocoder.isPresent()){
			Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
			List<Address> addresses = geocoder.getFromLocation(latti,longi, 1);
			if(addresses.size() > 0){
				Address address = addresses.get(0);
				parkingText.setText(address.getLocality() +", " + address.getSubLocality());
			}
		}else{
			parkingText.setText("Location Name Unavailable");
		}
	}

	private void showToast(String message){
		Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
//		getMenuInflater().inflate(R.menu.show_parked_car, menu);
		return true;
	}

	
	
    private void showMyDialog() {
		// TODO Auto-generated method stub
		AlertDialog.Builder  dialog = new AlertDialog.Builder(ShowParkedCar.this);
		dialog.setTitle("Enable GPS");
		dialog.setMessage("Please turn on your GPS");
		
		
		dialog.setPositiveButton("OK", new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
				startActivity(intent);
				arg0.dismiss();
			}
		});
		
		dialog.setNegativeButton("Cancel", new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				// TODO Auto-generated method stub
				arg0.dismiss();
			}
		});
		
		dialog.show();
	}

	@Override
	public void onMyLocationChange(Location loc) {
		// TODO Auto-generated method stub
//		if(loc.getAccuracy() < 1000.){
			mLat = loc.getLatitude();
			mLong = loc.getLongitude();
//		}
		LatLng lng = new LatLng(loc.getLatitude(), loc.getLongitude());
		googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(lng, 16));
		
	}

/*	@Override
	public void onLocationChanged(Location loc) {
		// TODO Auto-generated method stub
		mLat = (long) loc.getLatitude();
		mLong = (long) loc.getLongitude();
		
		LatLng location = new LatLng(mLat,mLong);
		Builder builder = new CameraPosition.Builder();
		builder.zoom(16);
		builder.target(location);
		CameraPosition position = builder.build();
		googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(position));
	}

	@Override
	public void onProviderDisabled(String arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderEnabled(String arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
		// TODO Auto-generated method stub
		
	}*/
}