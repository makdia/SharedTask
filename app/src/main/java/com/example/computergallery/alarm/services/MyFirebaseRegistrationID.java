package com.example.computergallery.alarm.services;

import android.content.SharedPreferences;
import android.util.Log;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;



public class MyFirebaseRegistrationID extends FirebaseInstanceIdService {

    String TAG = MyFirebaseRegistrationID.this.getClass().getName();


    @Override
    public  void onTokenRefresh() {
        super.onTokenRefresh();

        String token = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "onTokenRefresh:  " + token);

        SharedPreferences sharedPreferences_for_user_info=getSharedPreferences("login",MODE_PRIVATE);
        SharedPreferences.Editor editor=sharedPreferences_for_user_info.edit();
        editor.putString("token",token);
        editor.commit();

    }

}
