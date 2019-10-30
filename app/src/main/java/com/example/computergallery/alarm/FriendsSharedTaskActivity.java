package com.example.computergallery.alarm;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class FriendsSharedTaskActivity extends AppCompatActivity {
    public static List<FriendsSharedTaskModel> alarmLists = new ArrayList<>();
    RecyclerView recyclerView;
    FriendsSharedTaskAdapter alarmAdapter;
    DatabaseReference databaseReference;
    SharedPreferences sharedPreferences_for_user_info;
    String user_id,friend_id;
    boolean isLoggedIn;
    static String friend_timezone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends_shared_task);


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorAccent)));

        sharedPreferences_for_user_info=getSharedPreferences("login",MODE_PRIVATE);
        isLoggedIn=sharedPreferences_for_user_info.getBoolean("login",false);
        user_id=sharedPreferences_for_user_info.getString("unique_id","null");

        if (isLoggedIn != true){
            Intent intent = new Intent(getApplicationContext(), OldUserLoginOptionActivity.class);
            startActivity(intent);
            finish();
        } else if (isLoggedIn == true) {
            friend_id = getIntent().getStringExtra("friend_id");
            friend_timezone = getIntent().getStringExtra("friend_timezone");
            //database reference object
            databaseReference = FirebaseDatabase.getInstance().getReference("Task").child(friend_id);


            alarmAdapter = new FriendsSharedTaskAdapter(getApplicationContext(), alarmLists);
            recyclerView = (RecyclerView) findViewById(R.id.recycleView);
            recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
            recyclerView.setAdapter(alarmAdapter);


            //fetching my friends all shared task from firebase realtime database
            gettingFriendsAllSharedTask();


            final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.add);
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final Animation myAnim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.bounce);
                    fab.startAnimation(myAnim);
                    Intent intent = new Intent(getApplicationContext(), ReminderEditActivity.class);
                    intent.putExtra("friend_id", friend_id);
                    intent.putExtra("friend_timezone", friend_timezone);
                    intent.putExtra("save_task", "shared_task");
                    startActivity(intent);
                }
            });


            //floating button hide and unhide
            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    if (dy > 0 && fab.getVisibility() == View.VISIBLE) {
                        fab.hide();
                    } else if (dy < 0 && fab.getVisibility() != View.VISIBLE) {
                        fab.show();
                    }
                }
            });
        }
    }

    void gettingFriendsAllSharedTask() {
        //attaching value event listener
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //clearing the previous list
                recyclerView.removeAllViews();
                alarmLists.clear();
                //iterating through all the nodes
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    String pick_my_user_id=String.valueOf(postSnapshot.child("taskOwnerId").getValue());
                    String task_type = String.valueOf(postSnapshot.child("taskType").getValue());
                    if (task_type.equals("Shared task") && user_id.equals(pick_my_user_id)) {
                        FriendsSharedTaskModel alarmModel = postSnapshot.getValue(FriendsSharedTaskModel.class);
                        alarmLists.add(alarmModel);
                        alarmAdapter.notifyDataSetChanged();
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