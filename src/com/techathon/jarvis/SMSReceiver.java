package com.techathon.jarvis;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

public class SMSReceiver extends BroadcastReceiver implements OnInitListener{
	
	private TextToSpeech textToSpeech;
//	private boolean ttsEnabled;
	private AudioManager audioManager;
	StringBuffer sb = new StringBuffer();
	
	
	@Override
	public void onReceive(Context context, Intent intent) {

		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
		boolean isreadSMSEnabled = settings.getBoolean("pref_readSMS", true);
		if(isreadSMSEnabled){
//			audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
			Bundle bundle = intent.getExtras();
//			textToSpeech = new TextToSpeech(context, this);
			SmsMessage msgs[] = null;
			
			
//			Log.i("Outside ifSMSRCV", "Outside ifSMSRCV");
			if(bundle != null){
				Log.i("Inside ifSMSRCV", "Inside ifSMSRCV");
				Object[] pdus = (Object[])bundle.get("pdus");
				msgs =  new SmsMessage[pdus.length];
				for (int i = 0; i < msgs.length; i++) {
					msgs[i] = SmsMessage.createFromPdu((byte[])pdus[i]);
					sb.append("SMS from ").append(msgs[i].getOriginatingAddress());
					sb.append(":").append(msgs[i].getMessageBody().toString()).append("\n");
				}
				
//				speak(sb.toString());
				
				Toast.makeText(context, sb.toString(), Toast.LENGTH_LONG).show();
			}
		}
			
	}

	private void speak(String str){
		int normal = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
		int maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVol, 0);
		textToSpeech.speak(str, TextToSpeech.QUEUE_FLUSH, null);
		audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, normal, 0);
	}
	
	@Override
	public void onInit(int status) {
		if(status == TextToSpeech.SUCCESS){
//			ttsEnabled = true;
			speak(sb.toString());
		}
		
	}

}
