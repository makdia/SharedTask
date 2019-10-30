package com.example.computergallery.alarm;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Makdia Hussain on 2/6/2018.
 */
public class FriendListActivity extends AppCompatActivity {
    static ArrayList<FriendListModel> friendLists = new ArrayList<>();
    static RecyclerView recyclerView1;
    static FriendListAdapter friendListAdapter;

    static ArrayList<FriendRequestListModel> friendRequestLists = new ArrayList<>();
    static RecyclerView recyclerView2;
    FriendRequestListAdapter friendRequestListAdapter;

    DatabaseReference databaseReference_for_friend,databaseReference_for_friend_info;
    static TextView textView_add_friend;
    static LinearLayout hide_friend_list;
    SharedPreferences sharedPreferences_for_user_info;
    static String user_id,user_name,user_key,user_profile_pic;
    boolean isLoggedIn;
    int i=0;
    SharedPreferences sharedPreferences_for_dupication_remove;


    static ProgressBar progressBar;
    static FrameLayout linearLayout_progressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_list);

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
            //database reference object
            databaseReference_for_friend = FirebaseDatabase.getInstance().getReference("Friends").child(user_id);
            databaseReference_for_friend_info = FirebaseDatabase.getInstance().getReference("User");


            friendListAdapter = new FriendListAdapter(getApplicationContext(), friendLists);
            recyclerView1 = (RecyclerView) findViewById(R.id.recycleView1);
            recyclerView1.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
            recyclerView1.setAdapter(friendListAdapter);


            friendRequestListAdapter = new FriendRequestListAdapter(getApplicationContext(), friendRequestLists);
            recyclerView2 = (RecyclerView) findViewById(R.id.recycleView2);
            recyclerView2.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
            recyclerView2.setAdapter(friendRequestListAdapter);

            textView_add_friend = (TextView) findViewById(R.id.textvieww);
            hide_friend_list = (LinearLayout) findViewById(R.id.layout1);

            sharedPreferences_for_dupication_remove = getSharedPreferences("remove_duplicate_friend", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences_for_dupication_remove.edit();
            editor.putBoolean("remove_duplicate_friend", false);
            editor.commit();

            progressBar = (ProgressBar) findViewById(R.id.progressBar);
            linearLayout_progressBar = (FrameLayout) findViewById(R.id.layout2);


            //fetching all friend from firebase realtime database
            fetchingAllFriends();
        }
    }

     void fetchingAllFriends() {
        //attaching value event listener
         databaseReference_for_friend.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                //clearing the previous list
                friendLists.clear();
                friendRequestLists.clear();
                recyclerView1.removeAllViews();
                recyclerView2.removeAllViews();
                i++;
                if (i != 1) {
                    textView_add_friend.setVisibility(View.GONE);
                    Boolean check=sharedPreferences_for_dupication_remove.getBoolean("remove_duplicate_friend",false);
                    if(check.equals(false)) {
                        FriendListAdapter.searchLists.clear();
                        startActivity(new Intent(FriendListActivity.this, FriendListActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                        finish();
                    }
                } else if (i == 1) {
                    //iterating through all the nodes
                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        //adding friend to the list
                        String check_status = String.valueOf(postSnapshot.child("status").getValue());
                        final String friend_id = String.valueOf(postSnapshot.child("user_id").getValue());
                        if (i == 1) {
                            if (check_status.equals("Accepted")) {
                                final Query query1 = databaseReference_for_friend_info.orderByChild("user_id").equalTo(friend_id);
                                query1.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshotForFriend) {
                                        if (dataSnapshotForFriend.child(friend_id).exists()) {
                                            for (DataSnapshot postSnapshotForFriend1 : dataSnapshotForFriend.getChildren()) {
                                                FriendListModel friendListModel = postSnapshotForFriend1.getValue(FriendListModel.class);
                                                friendListModel.setUserId(String.valueOf(postSnapshotForFriend1.child("user_id").getValue()));
                                                friendListModel.setUserName(String.valueOf(postSnapshotForFriend1.child("user_name").getValue()));
                                                friendListModel.setKey(String.valueOf(postSnapshotForFriend1.child("key").getValue()));
                                                friendListModel.setUserTimezone(String.valueOf(postSnapshotForFriend1.child("user_timezone").getValue()));
                                                friendListModel.setStatus("Accepted");
                                                friendListModel.setUserProfilePic(String.valueOf(postSnapshotForFriend1.child("profile_pic").getValue()));
                                                friendLists.add(friendListModel);
                                                FriendListAdapter.searchLists.add(friendListModel);
                                                break;
                                            }
                                        }

                                        friendListAdapter.notifyDataSetChanged();
                                    }


                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                            } else if (check_status.equals("Pending_from_others")) {
                                textView_add_friend.setVisibility(View.VISIBLE);
                                Query query2 = databaseReference_for_friend_info.orderByChild("user_id").equalTo(friend_id);
                                query2.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshotForFriendInvitation) {
                                        if (dataSnapshotForFriendInvitation.child(friend_id).exists()) {
                                            for (DataSnapshot postSnapshotForFriendInvitation : dataSnapshotForFriendInvitation.getChildren()) {
                                                FriendRequestListModel friendRequestListModel = postSnapshotForFriendInvitation.getValue(FriendRequestListModel.class);
                                                friendRequestListModel.setUserId(String.valueOf(postSnapshotForFriendInvitation.child("user_id").getValue()));
                                                friendRequestListModel.setUserName(String.valueOf(postSnapshotForFriendInvitation.child("user_name").getValue()));
                                                friendRequestListModel.setKey(String.valueOf(postSnapshotForFriendInvitation.child("key").getValue()));
                                                friendRequestListModel.setStatus("Pending_from_others");
                                                friendRequestListModel.setUserProfilePic(String.valueOf(postSnapshotForFriendInvitation.child("profile_pic").getValue()));
                                                friendRequestLists.add(friendRequestListModel);
                                                break;
                                            }
                                        }

                                        friendRequestListAdapter.notifyDataSetChanged();

                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                            }
                            else {

                            }
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
        SharedPreferences.Editor editor = sharedPreferences_for_dupication_remove.edit();
        editor.putBoolean("remove_duplicate_friend", true);
        editor.commit();
        super.onBackPressed();
        startActivity(new Intent(this, HomeActivity.class));
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_friendlist_search, menu);
        //for search start
        MenuItem myActionMenuItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) myActionMenuItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                friendListAdapter.filter(newText);
                return false;
            }
        });
        //end
        return true;
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
        else if (id == R.id.action_add) {
            Intent intent = new Intent(getApplicationContext(), AddFriendActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

}
