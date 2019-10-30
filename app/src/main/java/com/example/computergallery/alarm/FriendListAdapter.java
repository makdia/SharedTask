package com.example.computergallery.alarm;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

//import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Makdia Hussain on 2/6/2018.
 */

public class FriendListAdapter extends RecyclerView.Adapter<FriendListAdapter.MyViewHolder> {
    private ArrayList<FriendListModel> friendLists;
    private Context context;
    public static ArrayList<FriendListModel> searchLists;


    Handler handler;
    Timer t;

    public FriendListAdapter(Context context, ArrayList<FriendListModel> friendLists) {
        this.friendLists = friendLists;
        this.searchLists = new ArrayList<FriendListModel>();
        //this.searchLists.addAll(FriendListActivity.friendLists );
        this.context = context;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.friend_list, parent, false);
        MyViewHolder holder = new MyViewHolder(view, context, friendLists);
        return holder;
    }


    @SuppressLint("ResourceAsColor")
    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        final FriendListModel frienddListModel = friendLists.get(position);

        //holder.tv_friend_key.setText(Html.fromHtml("Key : "+frienddListModel.getKey()));
        holder.tv_friend_id.setText(Html.fromHtml("(ID:" + frienddListModel.getUserId() + ")"));
        holder.tv_friend_name.setText(Html.fromHtml(frienddListModel.getUserName()));
        Picasso.with(context).load(frienddListModel.getUserProfilePic()).placeholder(R.drawable.friend_img2).into(holder.img_profile_pic);
        holder.btn_add_task.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, FriendsSharedTaskActivity.class);
                intent.putExtra("friend_id", frienddListModel.getUserId());
                intent.putExtra("friend_timezone", frienddListModel.getUserTimezone());
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        });

    }


    @Override
    public int getItemCount() {
        return friendLists.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        TextView tv_friend_name, tv_friend_id, tv_friend_key;
        Button btn_add_task;
        ImageView img_profile_pic;
        ArrayList<FriendListModel> friendLists = new ArrayList<>();
        Context context;

        public MyViewHolder(View itemView, Context context, ArrayList<FriendListModel> friendLists) {
            super(itemView);
            this.context = context;
            this.friendLists = friendLists;
            tv_friend_name = (TextView) itemView.findViewById(R.id.friend_name);
            tv_friend_id = (TextView) itemView.findViewById(R.id.friend_id);
            //tv_friend_key = (TextView) itemView.findViewById(R.id.friend_key);
            btn_add_task = (Button) itemView.findViewById(R.id.btn_add_task);
            img_profile_pic = (ImageView) itemView.findViewById(R.id.friend_img);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }


        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            final FriendListModel newsModel = this.friendLists.get(position);
            Intent intent = new Intent(context, FriendsSharedTaskActivity.class);
            intent.putExtra("friend_id", newsModel.getUserId());
            intent.putExtra("friend_timezone", newsModel.getUserTimezone());
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }


        @Override
        public boolean onLongClick(final View v) {
            final int position = getAdapterPosition();
            final FriendListModel newsModel = this.friendLists.get(position);

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(v.getRootView().getContext(), R.style.AppCompatAlertDialogStyle);
            alertDialogBuilder.setTitle("Are you sure?");
            alertDialogBuilder.setMessage("Do you want to remove your friend?");
            alertDialogBuilder.setCancelable(false);
            // set positive button YES message
            alertDialogBuilder.setPositiveButton("Remove", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(final DialogInterface dialog, int which) {
                    dialog.dismiss();
                    FriendListActivity.linearLayout_progressBar.setVisibility(View.VISIBLE);
                    FriendListActivity.hide_friend_list.setVisibility(View.GONE);
                    new deletingFriendAndTask().execute(FriendListActivity.user_id, newsModel.getUserId());


                    handler = new Handler();
                    t = new Timer();
                    t.schedule(new TimerTask() {
                        public void run() {

                            handler.post(new Runnable() {
                                public void run() {
                                    //THIS ACTIONS WILL WILL EXECUTE AFTER 5 SECONDS...
                                    Intent intent = new Intent(context, FriendListActivity.class);
                                    context.startActivity(intent);
                                }
                            });
                        }
                    }, 5000);

                    /*Thread timer;
                    timer = new Thread() {
                        public void run() {
                            //Display for 1 minute
                            new deletingFriendAndTask().execute(FriendListActivity.user_id, newsModel.getUserId());
                            try {
                                sleep(5000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            } finally {
                                //Go back to Activity
                                Intent intent = new Intent(context, FriendListActivity.class);
                                context.startActivity(intent);
                            }
                        }
                    };
                    timer.start();*/

                   /* new AsyncTask<Void, Integer, Long>() {
                        @Override
                        protected void onPreExecute() {
                            FriendListActivity.linearLayout_progressBar.setVisibility(View.VISIBLE);
                            FriendListActivity.hide_friend_list.setVisibility(View.GONE);
                        }

                        protected Long doInBackground(Void... voids) {
                            SharedPreferences sharedPreferences_for_dupication_remove = context.getSharedPreferences("remove_duplicate_friend", context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences_for_dupication_remove.edit();
                            editor.putBoolean("remove_duplicate_friend", true);
                            editor.commit();
                            long totalSize = 2;
                            int i = 0;
                            for (i = 0; i < 2; i++) {
                                final int finalI = i;
                                Thread background = new Thread(new Runnable() {
                                    public void run() {
                                        try {
                                            if (finalI == 0) {
                                                // Do some work here
                                                //removing friend from my friend list
                                                long timeout = 2000;
                                                while (timeout > 0) {
                                                    timeout = timeout - 1000;
                                                    try {
                                                        Thread.sleep(100);
                                                    } catch (InterruptedException e) {
                                                    }
                                                }
                                                DatabaseReference dR = FirebaseDatabase.getInstance().getReference("Friends").child(FriendListActivity.user_id).child(newsModel.getUserId());
                                                dR.removeValue();
                                            } else if (finalI == 1) {
                                                //removing friend from other friend list
                                                long timeout = 2000;
                                                while (timeout > 0) {
                                                    timeout = timeout - 1000;
                                                    try {
                                                        Thread.sleep(100);
                                                    } catch (InterruptedException e) {
                                                    }
                                                }
                                                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Friends").child(newsModel.getUserId()).child(FriendListActivity.user_id);
                                                databaseReference.removeValue();
                                            }
                                              /*  if(finalI==2) {

                                                }
                                                else if(finalI==3) {
                                                    Intent intent = new Intent(context, FriendListActivity.class);
                                                    context.startActivity(intent);
                                                }
                                                */
                                       /* } catch (Throwable t) {
                                            Log.i("Animation", "Thread  exception " + t);
                                        } finally {

                                        }
                                    }
                                });
                                background.start();
                                // Escape early if cancel() is called
                                if (isCancelled()) break;
                            }
                            return totalSize;
                        }

                        protected void onProgressUpdate(Integer... progress) {
                            // setProgressPercent(progress[0]);
                        }

                        protected void onPostExecute(Long result) {
                            Intent intent = new Intent(context, FriendListActivity.class);
                            context.startActivity(intent);
                        }
                    }.execute();
*/

                }
            });
            // set neutral button OK
            alertDialogBuilder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // Dismiss the dialog
                    dialog.dismiss();
                }
            });
            AlertDialog alertDialog = alertDialogBuilder.create();
            // show alert
            alertDialog.show();
            return false;
        }
    }


    //for search start Filter method
    public void filter(String charText) {
        charText = charText.toLowerCase(Locale.getDefault());
        FriendListActivity.friendLists.clear();
        if (charText.length() == 0) {
            FriendListActivity.friendLists.addAll(searchLists);
        } else {
            for (FriendListModel wp : searchLists) {
                if (wp.getUserName().toLowerCase(Locale.getDefault()).contains(charText)) {
                    FriendListActivity.friendLists.add(wp);
                }
            }
        }
        notifyDataSetChanged();
    }


    class deletingFriendAndTask extends AsyncTask<String, Void, Boolean> {

        protected void onPreExecute() {

        }
        @Override
        protected Boolean doInBackground(String... strings) {
            SharedPreferences sharedPreferences_for_dupication_remove = context.getSharedPreferences("remove_duplicate_friend", context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences_for_dupication_remove.edit();
            editor.putBoolean("remove_duplicate_friend", true);
            editor.commit();
            DatabaseReference dR = FirebaseDatabase.getInstance().getReference("Friends").child(strings[0]).child(strings[1]);
            dR.removeValue();
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Friends").child(strings[1]).child(strings[0]);
            databaseReference.removeValue();
            return doGetJson(strings[0], strings[1]);
        }
        protected void onProgressUpdate(Integer... progress) {

        }
        protected void onPostExecute(final Boolean success) {

            if (success) {

            }
        }

        public boolean doGetJson(final String s1, final String s2) {

            //removing my all task from my shared task list which are shared by my friend
            final DatabaseReference databaseReference_for_task2 = FirebaseDatabase.getInstance().getReference("Task").child(s1);
            //attaching value event listener
            databaseReference_for_task2.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    //iterating through all the nodes
                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        String taskOwnerId = String.valueOf(postSnapshot.child("taskOwnerId").getValue());
                        String id = String.valueOf(postSnapshot.child("id").getValue());
                        if (taskOwnerId.equals(s2)) {
                            Long intent_id = Long.valueOf(String.valueOf(postSnapshot.child("alarmManagerId").getValue()).substring(7, 12));
                            new ReminderManager(context).setReminder(id, intent_id, "", Calendar.getInstance(), "Off", "", "", "", "", "");
                            databaseReference_for_task2.child(id).removeValue();
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });


            //removing friends all shared task from friends shared task list which are shared by me
            final DatabaseReference databaseReference_for_task1 = FirebaseDatabase.getInstance().getReference("Task").child(s2);
            //attaching value event listener
            databaseReference_for_task1.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    //iterating through all the nodes
                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        String taskOwnerId = String.valueOf(postSnapshot.child("taskOwnerId").getValue());
                        String id = String.valueOf(postSnapshot.child("id").getValue());
                        if (taskOwnerId.equals(s1)) {
                            databaseReference_for_task1.child(id).child("state").setValue("Off");
                            if (taskOwnerId.equals(s1)) {
                                databaseReference_for_task1.child(id).child("taskType").setValue("Deleted shared task");
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
            return true;
        }
    }

}