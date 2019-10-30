package com.example.computergallery.alarm;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
/**
 * Created by Makdia Hussain on 1/28/2018.
 */
public class ReminderService  {
	Context ct;
	NotificationManager mgr;
	Intent notificationIntent;
	PendingIntent pi;
	NotificationCompat.Builder builder;
	int id;
	SharedPreferences sharedPreferences_for_user_info;
	boolean isLoggedIn;
	String user_id;

	public ReminderService(Context context) {
		ct = context;
		mgr = (NotificationManager) ct.getSystemService(Context.NOTIFICATION_SERVICE);
	}


	//start notification
	void doReminderWork(final String alarm_id, Long alarm_intent_id, String title, String time, String state, String alarm_manager_id, String task_user_id, String task_type, String task_owner_id, String task_image) {

		Log.d("ReminderService", "Doing work.");

		notificationIntent = new Intent(ct, MyTaskActivity.class);

		notificationIntent.putExtra("alarm_id", alarm_id);
		notificationIntent.putExtra("title",title);
		notificationIntent.putExtra("time",time);

		pi = PendingIntent.getActivity(ct, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

		// Create Notification using NotificationCompat.Builder
		builder = new NotificationCompat.Builder(ct)
				// Set Icon
				.setSmallIcon(android.R.drawable.stat_sys_warning)
				// Set Ticker Message
				.setTicker(ct.getString(R.string.notify_new_task_message))
				// Set Title
				.setContentTitle(ct.getString(R.string.notify_new_task_title))
				// Set Text
				.setContentText(ct.getString(R.string.notify_new_task_message))
				// Add an Action Button below Notification
				// Set PendingIntent into Notification
				.setContentIntent(pi)
				// Dismiss Notification
				.setAutoCancel(true);

		id = (int) ((long) alarm_intent_id);


		sharedPreferences_for_user_info=ct.getSharedPreferences("login",ct.MODE_PRIVATE);
		isLoggedIn=sharedPreferences_for_user_info.getBoolean("login",false);
		user_id=sharedPreferences_for_user_info.getString("unique_id","null");

		if(state.equals("On") && isLoggedIn == true && task_user_id.equals(user_id)) {
			Log.d("Error", state);
			mgr.notify(id, builder.build());
			//setting task reminder state off after giving alarm
			DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("Task").child(task_user_id);
			mDatabase.child(alarm_id).child("state").setValue("Off");
			if(task_type.equals("Shared task")) {
				mDatabase.child(alarm_id).child("taskSeen").setValue("Yes");
			}
			Intent i = new Intent(ct, DismissAlarmActivity.class);
			i.putExtra("title",title);
			i.putExtra("time",time);
			i.putExtra("image",task_image);
			i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			ct.startActivity(i);
		}else if(state.equals("Off") || isLoggedIn != true || !task_user_id.equals(user_id)){
			Log.d("Error", state);
			mgr.cancelAll();
			DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("Task").child(task_user_id);
			mDatabase.child(alarm_id).child("state").setValue("Off");
			if(task_type.equals("Shared task")) {
				mDatabase.child(alarm_id).child("taskSeen").setValue("Yes");
			}
		}
	}

	//cancel notification from dismiss activity
	public void cancelNotification(){
		mgr.cancelAll();
	}
}
