package com.techathon.jarvis;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.telephony.TelephonyManager;
import android.view.KeyEvent;
import android.widget.Toast;

import com.android.internal.telephony.ITelephony;

public class ProximityService extends Service implements SensorEventListener, OnInitListener{

	private SensorManager sensorManager;
	private AudioManager audioManager;
	private Sensor mSensor;
	private long startTime,endTime;
	private boolean gvsStarted;
	private TextToSpeech textToSpeech;
	private boolean ttsEnabled;
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		
		sensorManager = (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
		audioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
		mSensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
		
		sensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
		
		textToSpeech = new TextToSpeech(this, this);
	}
	
	
	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		sensorManager.unregisterListener(this);
	}

	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSensorChanged(SensorEvent arg0) {
		// TODO Auto-generated method stub
		gvsStarted = false;
		if(arg0.values[0] == 0){
//			showToast("close");
			startTime = System.currentTimeMillis();
			checkif1SecLapsed();
			
		}else{
//			showToast("away");
			endTime = System.currentTimeMillis();
			long temp = endTime - startTime;
			if((temp/1000) < 1){
				if(Holder.isPhoneRinging){
					showToast("Call autoanswer");
					autoAnswerCall();
				}else{
					gvsStarted = true;
					showToast("Initializing GVS...");
					startGVS();
				}
			}
		}
	}
	
	private void checkif1SecLapsed() {
		// TODO Auto-generated method stub
		new CountDownTimer(1000,10) {
			
			@Override
			public void onTick(long arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onFinish() {
				// TODO Auto-generated method stub
				if(!gvsStarted){
//					showToast("held for 1 sec");
					if(Holder.isPhoneRinging){
						//Code for auto decline call
						showToast("Declining Call");
						declineCall();
					}else{
						playPauseMusic();
					}
				}
			}

		}.start();
	}
	
	private void declineCall() {
		// TODO Auto-generated method stub
		speak("Declining Call");
		Holder.isPhoneRinging = false;
		// called when someone is ringing to this phone
		TelephonyManager manager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
    	Class c=null;
    	try {
    		c = Class.forName(manager.getClass().getName());
    	} catch (ClassNotFoundException e) {
    		// TODO Auto-generated catch block
    		e.printStackTrace();
    	}
    	Method m=null;
    	try {
    		m = c.getDeclaredMethod("getITelephony");
    	} catch (SecurityException e) {
    		// TODO Auto-generated catch block
    		e.printStackTrace();
    	} catch (NoSuchMethodException e) {
    		// TODO Auto-generated catch block
    		e.printStackTrace();
    	}
    		m.setAccessible(true);
		ITelephony telephonyService=null;
		try {
			telephonyService = (ITelephony) m.invoke(manager);
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//Bundle bundle = intent.getExtras();
		telephonyService.endCall();
	}
	
	private void speak(String str){
		int normal = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
		int maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVol, 0);
		textToSpeech.speak(str, TextToSpeech.QUEUE_FLUSH, null);
		audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, normal, 0);
	}
	private void playPauseMusic() {
		// TODO Auto-generated method stub
		Intent i = new Intent("com.android.music.musicservicecommand");
		if(audioManager.isMusicActive()){
			showToast("pause music");
			i.putExtra("command", "pause");
			sendBroadcast(i);
		}else{
			showToast("play music");
			i.putExtra("command", "play");
			sendBroadcast(i);
		}
	}

	private void startGVS() {
		// TODO Auto-generated method stub
		Intent i = new Intent(RecognizerIntent.ACTION_WEB_SEARCH);
		i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		i.putExtra(RecognizerIntent.EXTRA_PROMPT, "Jarvis Voice Command");
		startActivity(i);
	}

	private void autoAnswerCall() {
//		speak("Auto Answer");
		Holder.isPhoneRinging = false;
		// TODO Auto-generated method stub
		Intent i = new Intent(Intent.ACTION_MEDIA_BUTTON);
		i.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_UP,KeyEvent.KEYCODE_HEADSETHOOK));
		this.sendOrderedBroadcast(i, "android.permission.CALL_PRIVILEGED");
		
	}

	private void showToast(String message){
		Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onInit(int status) {
		// TODO Auto-generated method stub
		if(status == TextToSpeech.SUCCESS){
			ttsEnabled = true;
		}
	}

	
}
