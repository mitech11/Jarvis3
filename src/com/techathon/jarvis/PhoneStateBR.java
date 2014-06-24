package com.techathon.jarvis;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;

public class PhoneStateBR extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		if(intent.getAction().equals(Intent.ACTION_NEW_OUTGOING_CALL)){
			Holder.isPhoneRinging = false;
		}else{
			TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
			switch (telephonyManager.getCallState()) {
			case TelephonyManager.CALL_STATE_RINGING:
				Holder.isPhoneRinging = true;
				break;
			case TelephonyManager.CALL_STATE_OFFHOOK:
				Holder.isPhoneRinging = false;
				break;
			default:
				break;
			}
		}
	}

	
}
