package com.techathon.jarvis;

import java.util.ArrayList;
import java.util.List;

import com.techathon.jarvis.data.DatabaseHelper;
import com.techathon.jarvis.data.LocationData;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class DrivingLogsActivity extends Activity {

	private ListView drivingLogs;
	ArrayList<LocationData> drivingLogsList;
	private DrivingLogsAdapter drivingLogsAdapter;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_driving_logs);
		Context context =  this;
		// look if logs are enabled 
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
		boolean isLogEnabled = settings.getBoolean("pref_logs", true);
		if(isLogEnabled){
			// get logs
			DatabaseHelper db =  new DatabaseHelper(context);
			db.getReadableDatabase();
			drivingLogsList = db.getAllLogs();
			if(drivingLogsList == null || drivingLogsList.size() == 0){
				Toast.makeText(context, "Unable to retrive logs.", Toast.LENGTH_SHORT).show();
			}else{
				drivingLogs = (ListView)findViewById(R.id.drivingLogs);
//				drivingLogsList = DBHelper.getAllData();
				drivingLogsAdapter = new DrivingLogsAdapter();
				drivingLogs.setAdapter(drivingLogsAdapter);
			}
			
//			Toast.makeText(this,"LOGS ENABLED",Toast.LENGTH_SHORT).show();
//			return;
		}else{
			Toast.makeText(this,"Please enable logs in Settings",Toast.LENGTH_SHORT).show();
			return;
		}
		
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
//		getMenuInflater().inflate(R.menu.driving_logs, menu);
		return true;
	}
	
	class DrivingLogsAdapter extends BaseAdapter{
		
		private class ViewHolder{
			TextView location;
			TextView dateTime;
			TextView speed;
		}		
		
		
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return drivingLogsList.size();
		}

		@Override
		public Object getItem(int position) {
			return drivingLogsList.get(position);
			
		}

		@Override
		public long getItemId(int arg0) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			
			ViewHolder holder;
			// TODO Auto-generated method stub
			if(convertView == null){
				holder = new ViewHolder();
				convertView = getLayoutInflater().inflate(R.layout.driving_logs_list_item, parent, false);
				holder.dateTime = (TextView)convertView.findViewById(R.id.dateTime);
				holder.location = (TextView)convertView.findViewById(R.id.location);
				holder.speed = (TextView)convertView.findViewById(R.id.speed);
				convertView.setTag(holder);
			}else{
				holder = (ViewHolder)convertView.getTag();
			}
			
			LocationData logData = drivingLogsList.get(position);
			
			holder.dateTime.setText(logData.date+"");
			holder.speed.setText(logData.speed+"");
			holder.location.setText(logData.str_locality);
			
			return convertView;
		}
		
	}

}
