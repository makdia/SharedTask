package com.example.computergallery.alarm.services;


import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.computergallery.alarm.MyTaskActivity;
import com.example.computergallery.alarm.OnBootReceiver;
import com.example.computergallery.alarm.R;
import com.example.computergallery.alarm.SharedTaskActivity;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class MyFirebaseMessageReceiver extends FirebaseMessagingService {

    Random rand = new Random();
    Intent i;
    int n = rand.nextInt(50) + 1;
    SharedPreferences sharedPreferences_for_user_info;
    String user_id;

    boolean isLoggedIn;


    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        String shared_task = remoteMessage.getNotification().getTitle();
        String task_date = remoteMessage.getNotification().getBody();
        String task_user_id = remoteMessage.getNotification().getTitleLocalizationKey();

        sharedPreferences_for_user_info=getSharedPreferences("login",MODE_PRIVATE);
        isLoggedIn=sharedPreferences_for_user_info.getBoolean("login",false);
        user_id=sharedPreferences_for_user_info.getString("unique_id","null");

        if (remoteMessage == null)
            return;

        if (remoteMessage.getNotification() != null && isLoggedIn==true && task_user_id.equals(user_id)) {
                i = new Intent(this, SharedTaskActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
                NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                        .setAutoCancel(true)
                        .setContentTitle(getString(R.string.notify_new_task_title))
                        .setContentText(shared_task+" "+task_date)
                        .setSmallIcon(R.mipmap.notification)
                        .setContentIntent(pendingIntent)
                        .setSound(Settings.System.DEFAULT_NOTIFICATION_URI);
                NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                manager.notify(n, builder.build());
                updatingNotificationStatus(task_user_id,shared_task,task_date);
        }

        //if user get notification for task then set alarm manager
        if (remoteMessage.getData() != null && isLoggedIn==true && task_user_id.equals(user_id)) {
                Intent intent = new Intent(this, OnBootReceiver.class);
                new OnBootReceiver().onReceive(getApplicationContext(), intent);
        }
    }



    //requesting for updating  status of all delivered notification
    //start
    void updatingNotificationStatus(final String task_user_id, final String shared_task, final String task_date) {
        String url = "https://ityeard.com/Restaurant/shared_task/updateNotificationStatus.php";
        StringRequest sq = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("Error", "Response:  " + response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Error", "Error:  " + error);
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("user_id", user_id);
                params.put("task", shared_task);
                params.put("date", task_date);
                return params;
            }

        };

        sq.setRetryPolicy(new DefaultRetryPolicy(0, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(sq);
    }
    //end
}