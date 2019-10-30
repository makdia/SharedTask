package com.example.computergallery.alarm;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by Makdia Hussain on 2/8/2018.
 */
public class InviteFriendActivity extends AppCompatActivity {
    static ArrayList<InviteFriendListModel> inviteFriendLists = new ArrayList<>();
    RecyclerView recyclerView;
    InviteFriendListAdapter inviteFriendListAdapter;

    DatabaseReference databaseReference_for_user,databaseReference_for_invitefriend;
    SharedPreferences sharedPreferences_for_user_info;
    static String user_id,user_name,user_key,user_profile_pic;
    boolean isLoggedIn;
    int i=0;
    SharedPreferences sharedPreferences_for_dupication_remove;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite_friend);

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
            databaseReference_for_user = FirebaseDatabase.getInstance().getReference("User");
            databaseReference_for_invitefriend = FirebaseDatabase.getInstance().getReference("Friends");

            inviteFriendListAdapter = new InviteFriendListAdapter(getApplicationContext(), inviteFriendLists);
            recyclerView = (RecyclerView) findViewById(R.id.recycleView);
            recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
            recyclerView.setAdapter(inviteFriendListAdapter);

            sharedPreferences_for_dupication_remove = getSharedPreferences("remove_duplicate_invitation", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences_for_dupication_remove.edit();
            editor.putBoolean("remove_duplicate_invitation", false);
            editor.commit();
            //fetching all unfriend from firebase realtime database
            fetchingUnfriend();
        }
    }

    void fetchingUnfriend() {
        //attaching value event listener
        databaseReference_for_user.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                //clearing the previous list
                recyclerView.removeAllViews();
                inviteFriendLists.clear();
                InviteFriendListAdapter.searchLists.clear();
                //iterating through all the nodes
                for (final DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                     //each time pick one user & then comparing user with friend list and then pick unfriends
                    final String friend_id=String.valueOf(postSnapshot.child("user_id").getValue());
                    //Query query1 = databaseReference_for_invitefriend.child(user_id).child(friend_id);
                    databaseReference_for_invitefriend.child(user_id).child(friend_id).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot snapshot) {
                            i += 1;
                            if (i - 1 == dataSnapshot.getChildrenCount()) {
                                Boolean check = sharedPreferences_for_dupication_remove.getBoolean("remove_duplicate_invitation", false);
                                if (check.equals(false)) {
                                    startActivity(new Intent(InviteFriendActivity.this, InviteFriendActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                                    finish();
                                }
                            } else {
                                if (snapshot.getChildrenCount() == 0 && (!friend_id.equals(user_id))) {
                                    InviteFriendListModel inviteFriendListModel = postSnapshot.getValue(InviteFriendListModel.class);
                                    inviteFriendListModel.setUserId(String.valueOf(postSnapshot.child("user_id").getValue()));
                                    inviteFriendListModel.setUserName(String.valueOf(postSnapshot.child("user_name").getValue()));
                                    inviteFriendListModel.setKey(String.valueOf(postSnapshot.child("key").getValue()));
                                    inviteFriendListModel.setUserProfilePic(String.valueOf(postSnapshot.child("profile_pic").getValue()));
                                    inviteFriendListModel.setStatus("Invite Friend");
                                    inviteFriendLists.add(inviteFriendListModel);
                                    InviteFriendListAdapter.searchLists.add(inviteFriendListModel);
                                    inviteFriendListAdapter.notifyDataSetChanged();
                                } else if (snapshot.getChildrenCount() != 0 && (!friend_id.equals(user_id)) && (String.valueOf(snapshot.child("status").getValue()).equals("Pending_from_me"))) {
                                    InviteFriendListModel inviteFriendListModel = postSnapshot.getValue(InviteFriendListModel.class);
                                    inviteFriendListModel.setUserId(String.valueOf(postSnapshot.child("user_id").getValue()));
                                    inviteFriendListModel.setUserName(String.valueOf(postSnapshot.child("user_name").getValue()));
                                    inviteFriendListModel.setKey(String.valueOf(postSnapshot.child("key").getValue()));
                                    inviteFriendListModel.setUserProfilePic(String.valueOf(postSnapshot.child("profile_pic").getValue()));
                                    inviteFriendListModel.setStatus("Cancel Invitation");
                                    inviteFriendLists.add(inviteFriendListModel);
                                    InviteFriendListAdapter.searchLists.add(inviteFriendListModel);
                                    inviteFriendListAdapter.notifyDataSetChanged();
                                }

                            }
                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

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
        editor.putBoolean("remove_duplicate_invitation", true);
        editor.commit();
        super.onBackPressed();
        startActivity(new Intent(this, HomeActivity.class));
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_friendinvitationlist_search, menu);
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
                inviteFriendListAdapter.filter(newText);
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

        return super.onOptionsItemSelected(item);
    }
}
