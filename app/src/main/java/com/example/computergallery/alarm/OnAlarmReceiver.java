package com.example.computergallery.alarm;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ComponentInfo;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;
/**
 * Created by Makdia Hussain on 1/28/2018.
 */
public class OnAlarmReceiver extends BroadcastReceiver  {

	private static final String TAG = ComponentInfo.class.getCanonicalName();

	@Override	
	public void onReceive(Context context, Intent intent) {
		WakeLocker.acquire(context);

		Log.d(TAG, "Received wake up from alarm manager.");

		String alarm_id=intent.getExtras().getString("alarm_id");
		long alarm_intent_id = intent.getExtras().getLong("intent_id");
		String alarm_title=intent.getExtras().getString("title");
		String alarm_time=intent.getExtras().getString("reminderDateTime");
		String alarm_state=intent.getExtras().getString("state");
		String alarm_manager_id=intent.getExtras().getString("alarm_manager_id");
		String user_id=intent.getExtras().getString("user_id");
		String task_type=intent.getExtras().getString("task_type");
		String task_owner_id=intent.getExtras().getString("task_owner_id");
		String task_image=intent.getExtras().getString("task_image");

		new ReminderService(context).doReminderWork(alarm_id, alarm_intent_id, alarm_title, alarm_time, alarm_state,alarm_manager_id, user_id, task_type, task_owner_id, task_image);

	}
}
