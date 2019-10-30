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
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
/**
 * Created by Makdia Hussain on 1/27/2018.
 */
public class MyTaskActivity extends AppCompatActivity{

    public static List<MyTaskModel> alarmLists = new ArrayList<>();
    RecyclerView recyclerView;
    MyTaskAdapter alarmAdapter;
    DatabaseReference databaseReference;
    SharedPreferences sharedPreferences_for_user_info;
    String user_id;
    boolean isLoggedIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_task);


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
            //database reference object
            databaseReference = FirebaseDatabase.getInstance().getReference("Task").child(user_id);
            //databaseReference.keepSynced(true);


            alarmAdapter = new MyTaskAdapter(getApplicationContext(), alarmLists);
            recyclerView = (RecyclerView) findViewById(R.id.recycleView);
            recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
            recyclerView.setAdapter(alarmAdapter);


            //fetching all task from firebase realtime database
            gettingAllAlarm();


            final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.add);
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final Animation myAnim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.bounce);
                    fab.startAnimation(myAnim);
                    Intent intent = new Intent(getApplicationContext(), ReminderEditActivity.class);
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


    void gettingAllAlarm() {
        //attaching value event listener
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //clearing the previous list
                //recyclerView.removeAllViews();
                alarmLists.clear();
                //iterating through all the nodes
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    //getting all task
                    MyTaskModel alarmModel = postSnapshot.getValue(MyTaskModel.class);
                    if (alarmModel.getTaskType().equals("Own task")) {
                        //adding task to the list
                        alarmLists.add(alarmModel);
                    }
                    alarmAdapter.notifyDataSetChanged();
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
        startActivity(new Intent(this, HomeActivity.class));
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