package com.techathon.jarvis;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class ChargerBR extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		Log.i("", "Charger connected...");
		Intent i = new Intent(context, ProximityService.class);
		context.startService(i);
		showJarvisNotification(context);
//		if (intent.getAction() == Intent.ACTION_DOCK_EVENT) {
//			int dockState = intent.getIntExtra(Intent.EXTRA_DOCK_STATE, -1);
//			boolean isCarDocked = dockState == Intent.EXTRA_DOCK_STATE_DESK || 
//	                 dockState == Intent.EXTRA_DOCK_STATE_LE_DESK ||
//	                 dockState == Intent.EXTRA_DOCK_STATE_HE_DESK;
//			if (isCarDocked) {
//				context.startService(i);
//				showJarvisNotification(context);
//			}
//		} else {
//			closeNotification(context);
//			context.stopService(i);
//		}
	}

	private void closeNotification(Context context) {
		NotificationManager mNotificationManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.cancel(1234);
	}

	private void showJarvisNotification(Context context) {
		NotificationManager notificationManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		Intent intent = new Intent(context, Home.class);
		PendingIntent pendingIntent = PendingIntent.getActivity(
				context.getApplicationContext(), 0, intent,
				Notification.FLAG_ONGOING_EVENT);
		NotificationCompat.Builder builder = new NotificationCompat.Builder(
				context.getApplicationContext());
		builder.setContentTitle("Jarvis");
		builder.setContentIntent(pendingIntent);
		builder.setContentText("Jarvis is listening...");
		builder.setSmallIcon(R.drawable.ic_launcher);
		notificationManager.notify(1234, builder.build());
	}
}
