package com.example.computergallery.alarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ComponentInfo;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
/**
 * Created by Makdia Hussain on 1/28/2018.
 */
public class OnBootReceiver extends BroadcastReceiver {
	
	private static final String TAG = ComponentInfo.class.getCanonicalName();
	SharedPreferences sharedPreferences_for_user_info;
	String user_id;


	@Override
	public void onReceive(Context context, Intent intent) {
		WakeLocker.acquire(context);

		final ReminderManager reminderMgr = new ReminderManager(context);

		sharedPreferences_for_user_info=context.getSharedPreferences("login",context.MODE_PRIVATE);
		user_id=sharedPreferences_for_user_info.getString("unique_id","null");
		//database reference object
		DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Task").child(user_id);
		//attaching value event listener
		databaseReference.addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(DataSnapshot dataSnapshot) {
				//iterating through all the nodes
				for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
						//getting task
						String alarm_id = String.valueOf(postSnapshot.child("id").getValue());
						String alarm_time = String.valueOf(postSnapshot.child("time").getValue());
						String alarm_state = String.valueOf(postSnapshot.child("state").getValue());
						String alarm_manager_id = String.valueOf(postSnapshot.child("alarmManagerId").getValue());
						String alarm_title = String.valueOf(postSnapshot.child("title").getValue());
						String task_user_id = String.valueOf(postSnapshot.child("userId").getValue());
						String task_type = String.valueOf(postSnapshot.child("taskType").getValue());
						String task_owner_id = String.valueOf(postSnapshot.child("taskOwnerId").getValue());
						String task_image = String.valueOf(postSnapshot.child("taskImage").getValue());

						if (task_type.equals("Deleted shared task")) {
							DatabaseReference dR = FirebaseDatabase.getInstance().getReference("Task").child(task_user_id).child(alarm_id);
							dR.removeValue();
						}
						Long intent_id = Long.valueOf(alarm_manager_id.substring(7, 12));
						Calendar cal = Calendar.getInstance();
						SimpleDateFormat format = new SimpleDateFormat(ReminderEditActivity.DATE_TIME_FORMAT);
						try {
							java.util.Date date = format.parse(alarm_time);
							cal.setTime(date);
							reminderMgr.setReminder(alarm_id, intent_id, alarm_title, cal, alarm_state, alarm_manager_id, task_user_id, task_type, task_owner_id, task_image);
						} catch (java.text.ParseException e) {
							Log.e("OnBootReceiver", e.getMessage(), e);
						}

					}

			}

			@Override
			public void onCancelled(DatabaseError databaseError) {

			}
		});

	}

}

