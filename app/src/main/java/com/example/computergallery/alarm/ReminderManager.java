package com.example.computergallery.alarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;
/**
 * Created by Makdia Hussain on 1/28/2018.
 */
public class ReminderManager{

	private Context mContext;
	private AlarmManager mAlarmManager;
	PendingIntent pi;


	public ReminderManager(Context context) {
		mContext = context;
		mAlarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
	}


	public void setReminder(String alarm_id ,Long intent_id, String title, Calendar when, String state, String alarm_manager_id,  String user_id, String task_type, String task_owner_id, String task_image) {

		SimpleDateFormat dateTimeFormat = new SimpleDateFormat(ReminderEditActivity.DATE_TIME_FORMAT);
		String reminderDateTime = dateTimeFormat.format(when.getTime());


		Intent i = new Intent(mContext, OnAlarmReceiver.class);
		i.putExtra("alarm_id", alarm_id);
		i.putExtra("intent_id", (long) intent_id);
		i.putExtra("title", title);
		i.putExtra("reminderDateTime", reminderDateTime);
		i.putExtra("state", state);
		i.putExtra("alarm_manager_id", alarm_manager_id);
		i.putExtra("user_id", user_id);
		i.putExtra("task_type", task_type);
		i.putExtra("task_owner_id", task_owner_id);
		i.putExtra("task_image", task_image);
		final int _id = Integer.parseInt(String.valueOf(intent_id));

		pi = PendingIntent.getBroadcast(mContext, _id , i, PendingIntent.FLAG_UPDATE_CURRENT);

		if(state.equals("On")) {
			mAlarmManager.set(AlarmManager.RTC_WAKEUP, when.getTimeInMillis(), pi);
		}else if(state.equals("Off")) {
			if (mAlarmManager!= null) {
				mAlarmManager.cancel(pi);
			}

		}
	}

}