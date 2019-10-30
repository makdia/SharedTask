package com.example.computergallery.alarm;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;
/**
 * Created by Makdia Hussain on 2/7/2018.
 */
public class AddFriendActivity extends AppCompatActivity {
    EditText editText_friend_id,editText_key;
    CardView layout1,layout2;
    SharedPreferences sharedPreferences_for_user_info;
    String friend_id,friend_key,str_key,friend_name,friend_profile_pic,user_id,user_name,user_key,user_profile_pic;
    boolean isLoggedIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friend);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorAccent)));


        sharedPreferences_for_user_info=getSharedPreferences("login",MODE_PRIVATE);
        isLoggedIn=sharedPreferences_for_user_info.getBoolean("login",false);
        user_id=sharedPreferences_for_user_info.getString("unique_id","null");
        user_name=sharedPreferences_for_user_info.getString("user_name","null");
        user_key=sharedPreferences_for_user_info.getString("key","null");
        user_profile_pic=sharedPreferences_for_user_info.getString("profile_pic","null");


        if (isLoggedIn != true){
            Intent intent = new Intent(getApplicationContext(), OldUserLoginOptionActivity.class);
            startActivity(intent);
            finish();
        } else if (isLoggedIn == true) {
            editText_friend_id = (EditText) findViewById(R.id.edit_id);
            editText_key = (EditText) findViewById(R.id.edit_key);
            layout1 = (CardView) findViewById(R.id.layout1);
            layout2 = (CardView) findViewById(R.id.layout2);
            layout2.setVisibility(View.GONE);
        }

    }



    //search friend by id
    public void identifyFriendID(View view) {
        final Animation myAnim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.bounce);
        view.startAnimation(myAnim);
        friend_id = editText_friend_id.getText().toString();
        if (!friend_id.isEmpty()) {
            if (!friend_id.equals(user_id)) {
                DatabaseReference dRr = FirebaseDatabase.getInstance().getReference("User");
                Query query = dRr.orderByChild("user_id").equalTo(friend_id);
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(final DataSnapshot dataSnapshot) {
                        if ((dataSnapshot.child(friend_id).exists()) && (!friend_id.equals(user_id))) {
                            //iterating through all the nodes
                            DatabaseReference dR = FirebaseDatabase.getInstance().getReference("Friends").child(user_id);
                            Query query = dR.orderByChild("user_id").equalTo(friend_id);
                            query.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshotForFriend) {
                                    if (dataSnapshotForFriend.child(friend_id).exists()) {
                                        for (DataSnapshot postSnapshotForFriend : dataSnapshotForFriend.getChildren()) {
                                           String status  = String.valueOf(postSnapshotForFriend.child("status").getValue());
                                           if(status.equals("Accepted")){
                                               Toast.makeText(getApplicationContext(), friend_id+" already has your friend!\nPlease enter another friend id.", Toast.LENGTH_SHORT).show();
                                           }else{
                                               for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                                                   str_key = String.valueOf(postSnapshot.child("key").getValue());
                                                   layout1.setVisibility(View.GONE);
                                                   layout2.setVisibility(View.VISIBLE);
                                                   Toast.makeText(getApplicationContext(), "Your friend has been found!\nNow add your friend.", Toast.LENGTH_SHORT).show();
                                               }
                                           }
                                        }
                                    }
                                    else{
                                        for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                                            str_key = String.valueOf(postSnapshot.child("key").getValue());
                                            layout1.setVisibility(View.GONE);
                                            layout2.setVisibility(View.VISIBLE);
                                            Toast.makeText(getApplicationContext(), "Your friend has been found!\nNow add your friend.", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }
                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        } else {
                            Toast.makeText(getApplicationContext(), "Your friend has not been found!\nPlease enter a valid id!", Toast.LENGTH_SHORT).show();
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }else if (friend_id.equals(user_id)) {
                Toast.makeText(getApplicationContext(), "This is your id!\nPlease enter your friend id!", Toast.LENGTH_SHORT).show();
            }
        }else {
            Toast.makeText(getApplicationContext(), "Please Enter Your Friend ID", Toast.LENGTH_SHORT).show();
        }
    }



    //identify friend key and then add friend
    public void identifyFriendKey(View view) {
        final Animation myAnim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.bounce);
        view.startAnimation(myAnim);
        friend_key = editText_key.getText().toString();
        if (!friend_key.isEmpty()) {
                        layout1.setVisibility(View.GONE);
                        layout2.setVisibility(View.VISIBLE);
                        if(friend_key.equals(str_key)){
                            Toast.makeText(getApplicationContext(), "Your friend has been added successfully!", Toast.LENGTH_SHORT).show();
                            addFriend();
                        }
                        else{
                            Toast.makeText(getApplicationContext(), "You entered a wrong key!\nPlease enter a valid key!", Toast.LENGTH_SHORT).show();
                        }
        } else {
            Toast.makeText(getApplicationContext(), "Please Enter Your Friend Key", Toast.LENGTH_LONG).show();
        }
    }




    //add friend
    public void addFriend() {
        //inserting friend in my friend list
        DatabaseReference dR = FirebaseDatabase.getInstance().getReference("Friends").child(user_id).child(friend_id);
        Map<String, Object> value1 = new HashMap<>();
        value1.put("user_id", friend_id);
        value1.put("key", str_key);
        value1.put("status", "Accepted");
        dR.setValue(value1);

        //inserting me as friend in other friend list
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Friends").child(friend_id).child(user_id);
        Map<String, Object> value2 = new HashMap<>();
        value2.put("user_id", user_id);
        value2.put("key", user_key);
        value2.put("status", "Accepted");
        databaseReference.setValue(value2);

        Intent intent = new Intent(getApplicationContext(), FriendListActivity.class);
        startActivity(intent);
        finish();

    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(this, FriendListActivity.class));
        finish();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}

