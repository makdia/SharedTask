package com.example.computergallery.alarm;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Makdia Hussain on 2/5/2018.
 */
public class HomeActivity extends AppCompatActivity {

    DatabaseReference databaseReference;
    SharedPreferences sharedPreferences_for_user_info;
    boolean isLoggedIn;
    private static boolean isPersistenceEnabled = false;
    public static String user_id,token;
    TextView tv_friends;
    Button tv_friend_invitations;
    int total_friend=0,total_friend_request=0;
    String IMEINumber;

    TextView tv_my_task,tv_shared_task;
    int my_total_task=0,my_total_shared_task=0;


    SharedPreferences sharedPreferences_for_first_run;
    boolean isFirstRun;
    boolean connected=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!isPersistenceEnabled) {
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
            isPersistenceEnabled = true;
        }
        setContentView(R.layout.activity_home2);

        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorAccent)));

        sharedPreferences_for_user_info=getSharedPreferences("login",MODE_PRIVATE);
        isLoggedIn=sharedPreferences_for_user_info.getBoolean("login",false);
        token = sharedPreferences_for_user_info.getString("token", "null");
        user_id=sharedPreferences_for_user_info.getString("unique_id","null");
        IMEINumber=sharedPreferences_for_user_info.getString("device_imei","null");

        //checking session is already expired or not
        logout();

        if (isLoggedIn != true){
            Intent intent = new Intent(getApplicationContext(), OldUserLoginOptionActivity.class);
            startActivity(intent);
            finish();
        }else if (isLoggedIn == true) {

            databaseReference = FirebaseDatabase.getInstance().getReference("Friends").child(user_id);
            TextView tv = (TextView) this.findViewById(R.id.message);
            tv.setSelected(true);
            tv_friends = (TextView) this.findViewById(R.id.total_friends);
            //tv_friend_invitations = (Button) this.findViewById(R.id.total_friend_invitations);
          //  tv_friend_invitations.setVisibility(View.GONE);
            tv_my_task = (TextView) this.findViewById(R.id.my_task);
            tv_shared_task = (TextView) this.findViewById(R.id.shared_task);


            //counting total friends from firebase realtime database
            contingTotalFriends();
            //counting total friends request from firebase realtime database
            countingTotalFriendsInvitation();

            //check internet connection
            ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
            if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                    connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
                if(activeNetwork != null && activeNetwork.isConnectedOrConnecting()) {
                    //we are connected to a network
                    connected = true;
                    if(isFirstRun==false && connected==true) {
                        requestingForPendingNotification(token, user_id);
                    }
                }
            } else {
                connected = false;
            }
            sharedPreferences_for_first_run = PreferenceManager.getDefaultSharedPreferences(this);
            isFirstRun = sharedPreferences_for_first_run.getBoolean("FIRSTRUN", true);
            //calling function for storing device token
            if (isFirstRun==true && connected==true && !token.isEmpty() && !user_id.isEmpty()) {
                storeToken(token, "1", user_id);
            }

            Intent intent = new Intent(this, OnBootReceiver.class);
            new OnBootReceiver().onReceive(getApplicationContext(), intent);

        }

    }



    public void myTask(View view) {
        LinearLayout l1 = (LinearLayout) findViewById(R.id.layout_my_task);
        final Animation myAnim = AnimationUtils.loadAnimation(this, R.anim.bounce);
        l1.startAnimation(myAnim);
        Intent intent = new Intent(this, MyTaskActivity.class);
        startActivity(intent);
        finish();
    }

    public void mySharedTask(View view) {
        LinearLayout l1 = (LinearLayout) findViewById(R.id.layout_my_shared_task);
        final Animation myAnim = AnimationUtils.loadAnimation(this, R.anim.bounce);
        l1.startAnimation(myAnim);
        Intent intent = new Intent(this, SharedTaskActivity.class);
        startActivity(intent);
        finish();
    }

    public void friends(View view) {
        LinearLayout l1 = (LinearLayout) findViewById(R.id.layout_friends);
        final Animation myAnim = AnimationUtils.loadAnimation(this, R.anim.bounce);
        l1.startAnimation(myAnim);
        Intent intent = new Intent(this, FriendListActivity.class);
        startActivity(intent);
        finish();
    }

    public void inviteFriends(View view) {
        LinearLayout l1 = (LinearLayout) findViewById(R.id.layout_invite_friends);
        final Animation myAnim = AnimationUtils.loadAnimation(this, R.anim.bounce);
        l1.startAnimation(myAnim);
        Intent intent = new Intent(this, InviteFriendActivity.class);
        startActivity(intent);
        finish();

    }


    void contingTotalFriends() {
        //attaching value event listener
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                //counting friends
                if(dataSnapshot.getChildrenCount()!=0){
                    total_friend=0;
                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        String check_status = String.valueOf(postSnapshot.child("status").getValue());
                        if (check_status.equals("Accepted")) {
                            total_friend++;
                        }
                        }
                        if(total_friend!=0){
                            tv_friends.setText("Friends("+total_friend+")");
                        }else{
                            tv_friends.setText("Friends");
                        }
                }else{
                    tv_friends.setText("Friends");
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    void countingTotalFriendsInvitation() {
        //attaching value event listener
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                    //counting no of invitations
                    if(dataSnapshot.getChildrenCount()!=0){
                        total_friend_request=0;
                        for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                            String check_status = String.valueOf(postSnapshot.child("status").getValue());
                            if (check_status.equals("Pending_from_others")) {
                                total_friend_request++;
                            }
                        }
                        if(total_friend_request!=0) {
                         //   tv_friend_invitations.setVisibility(View.VISIBLE);
                         //   tv_friend_invitations.setText(String.valueOf(total_friend_request));
                        }else{
                          //  tv_friend_invitations.setVisibility(View.GONE);
                        }
                    }else{
                      //  tv_friend_invitations.setVisibility(View.GONE);
                    }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }




    //requesting for all pending notification
    //start
    void requestingForPendingNotification(final String token, final String user_id) {
        String url = "https://ityeard.com/Restaurant/shared_task/pendingNotification.php";
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
                params.put("token", token);
                params.put("user_id", user_id);
                return params;
            }

        };

        sq.setRetryPolicy(new DefaultRetryPolicy(0, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(sq);
    }
    //end



    //storing device token
    //start
    void storeToken(final String token, final String status, final String user_id) {
        String url = "https://ityeard.com/Restaurant/shared_task/store_token.php";
        StringRequest sq = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if(response.equals("successful")){
                    SharedPreferences.Editor editor = sharedPreferences_for_first_run.edit();
                    editor.putBoolean("FIRSTRUN", false);
                    editor.commit();
                }else{

                }
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
                params.put("status",status);
                params.put("token", token);
                params.put("user_id", user_id);
                return params;
            }

        };

        sq.setRetryPolicy(new DefaultRetryPolicy(0, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(sq);
    }
    //end




    //session expired because another user is logged in by using this account
    public void logout() {
            DatabaseReference dR = FirebaseDatabase.getInstance().getReference("User");
            Query query=dR.orderByChild("user_id").equalTo(user_id);
            query.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.child(user_id).exists()) {
                        for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                            String device_imei= String.valueOf(postSnapshot.child("device_imei").getValue());
                            if(!device_imei.isEmpty() && !device_imei.equals(IMEINumber)) {
                                SharedPreferences.Editor editor=sharedPreferences_for_user_info.edit();
                                editor.putBoolean("login",false);
                                editor.putString("unique_id", null);
                                editor.commit();
                                Toast.makeText(getApplicationContext(), "Your session is expired! Because another user is logged in by using this account!", Toast.LENGTH_LONG).show();
                                Intent intent = new Intent(getApplicationContext(), OldUserLoginOptionActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        }
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

    }


    @Override
    public void onRestart() {
        super.onRestart();  // Always call the superclass method first
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_page, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_update_profile) {
            Intent intent = new Intent(getApplicationContext(), UpdateInfoActivity.class);
            intent.putExtra("update", "update_user_info");
            startActivity(intent);
            finish();
            return true;
        }
        if (id == R.id.action_change_password) {
            Intent intent = new Intent(getApplicationContext(), ChangePasswordActivity.class);
            intent.putExtra("update", "update_password");
            startActivity(intent);
            finish();
            return true;
        }
        if (id == R.id.action_logout) {
            SharedPreferences.Editor editor=sharedPreferences_for_user_info.edit();
            editor.putBoolean("login",false);
            editor.putString("unique_id", null);
            editor.commit();
            Intent intent = new Intent(getApplicationContext(), OldUserLoginOptionActivity.class);
            startActivity(intent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
