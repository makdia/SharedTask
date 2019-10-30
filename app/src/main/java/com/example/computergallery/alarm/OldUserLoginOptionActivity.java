package com.example.computergallery.alarm;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class OldUserLoginOptionActivity extends AppCompatActivity {


    public static ArrayList<OldUserListModel> oldUserLists = new ArrayList<>();
    RecyclerView recyclerView;
    OldUserListAdapter oldUserListAdapter;


    private static final int MY_PERMISSIONS_REQUEST_READ_PHONE_STATE = 0;
    public static SharedPreferences sharedPreferences_for_user_info;
    boolean isLoggedIn;
    public  static String IMEINumber;
    DatabaseReference databaseReference_for_user;
    TextView tv_login;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_old_user_login_option);

        sharedPreferences_for_user_info = getSharedPreferences("login", MODE_PRIVATE);
        isLoggedIn = sharedPreferences_for_user_info.getBoolean("login", false);

        if (isLoggedIn == true) {
            Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
            startActivity(intent);
            finish();
        }

        oldUserListAdapter = new OldUserListAdapter(getApplicationContext(), oldUserLists);
        recyclerView = (RecyclerView) findViewById(R.id.recycleView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        recyclerView.setAdapter(oldUserListAdapter);
        tv_login=(TextView) findViewById(R.id.tv_login);
        tv_login.setVisibility(View.GONE);
        databaseReference_for_user = FirebaseDatabase.getInstance().getReference("User");
        loadIMEI();
    }



    void fetchingAllUsers() {
        //attaching value event listener
        databaseReference_for_user.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //clearing the previous list
                oldUserLists.clear();
                //iterating through all the nodes
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    //adding user to the list
                    String check_imei = String.valueOf(postSnapshot.child("device_imei").getValue());
                    if (check_imei.equals(IMEINumber)) {
                        tv_login.setVisibility(View.VISIBLE);
                        OldUserListModel oldUserListModel = postSnapshot.getValue(OldUserListModel.class);
                        oldUserListModel.setUserId(String.valueOf(postSnapshot.child("user_id").getValue()));
                        oldUserListModel.setUserName(String.valueOf(postSnapshot.child("user_name").getValue()));
                        oldUserListModel.setKey(String.valueOf(postSnapshot.child("key").getValue()));
                        oldUserListModel.setPassword(String.valueOf(postSnapshot.child("password").getValue()));
                        oldUserListModel.setDeviceImei(String.valueOf(postSnapshot.child("device_imei").getValue()));
                        oldUserListModel.setUserProfilePic(String.valueOf(postSnapshot.child("profile_pic").getValue()));
                        oldUserLists.add(oldUserListModel);
                        oldUserListAdapter.notifyDataSetChanged();
                    }
                    else {

                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }


    //login
    public void login(View view) {
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(intent);
        finish();
    }


    //registration
    public void registration(View view) {
        Intent intent = new Intent(getApplicationContext(), RegistrationActivity.class);
        startActivity(intent);
        finish();
    }



    // Called when the 'loadIMEI' function is triggered.
    public void loadIMEI() {
        // Check if the READ_PHONE_STATE permission is already available.
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {
            // READ_PHONE_STATE permission has not been granted.
            requestReadPhoneStatePermission();
        } else {
            // READ_PHONE_STATE permission is already been granted.
            doPermissionGrantedStuffs();
        }
    }
    //Requests the READ_PHONE_STATE permission.If the permission has been denied previously, a dialog will prompt the user to grant the permission, otherwise it is requested directly.
    private void requestReadPhoneStatePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                android.Manifest.permission.READ_PHONE_STATE)) {
            //Re-request if the permission was not granted.If the user has previously denied the permission.
            ActivityCompat.requestPermissions(OldUserLoginOptionActivity.this, new String[]{android.Manifest.permission.READ_PHONE_STATE},
                    MY_PERMISSIONS_REQUEST_READ_PHONE_STATE);
        } else {
            // READ_PHONE_STATE permission has not been granted yet. Request it directly.
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_PHONE_STATE},
                    MY_PERMISSIONS_REQUEST_READ_PHONE_STATE);
        }
    }
    // Callback received when a permissions request has been completed.
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        if (requestCode == MY_PERMISSIONS_REQUEST_READ_PHONE_STATE) {
            // Check if the only required permission has been granted
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // READ_PHONE_STATE permission has been granted, proceed with displaying IMEI Number
                doPermissionGrantedStuffs();
            } else {
                new AlertDialog.Builder(OldUserLoginOptionActivity.this)
                        .setTitle("Permission Request")
                        .setMessage("If you don't give permission,then you'll lose all data after uninstalling this app!")
                        .setCancelable(false)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                loadIMEI();
                            }
                        })
                        .setIcon(R.drawable.ic_launcher_foreground)
                        .show();
            }
        }
    }
    //taking IMEI Number of Phone
    @SuppressLint("MissingPermission")
    public void doPermissionGrantedStuffs() {
        TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        IMEINumber = tm.getDeviceId();
        //fetching all users who are already logged in by using this device from firebase realtime database
        fetchingAllUsers();
    }


    @Override
    public void onRestart() {
        super.onRestart();  // Always call the superclass method first
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

}
