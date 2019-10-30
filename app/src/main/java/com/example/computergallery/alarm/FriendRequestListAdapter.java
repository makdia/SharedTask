package com.example.computergallery.alarm;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

//import com.bumptech.glide.Glide;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Created by Makdia Hussain on 2/10/2018.
 */

public class FriendRequestListAdapter extends RecyclerView.Adapter<FriendRequestListAdapter.MyViewHolder> {
    private ArrayList<FriendRequestListModel> friendRequestLists;
    private Context context;

    public FriendRequestListAdapter(Context context, List<FriendRequestListModel> friendRequestLists) {
        this.friendRequestLists = (ArrayList<FriendRequestListModel>) friendRequestLists;
        this.context = context;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.friend_request_list, parent, false);
        MyViewHolder holder = new MyViewHolder(view, context, friendRequestLists);
        return holder;
    }


    @SuppressLint("ResourceAsColor")
    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {

        final FriendRequestListModel friendReqListModel = friendRequestLists.get(position);

        //holder.tv_friend_key.setText(Html.fromHtml("Key : "+friendReqListModel.getKey()));
        holder.tv_friend_id.setText(Html.fromHtml("ID : "+friendReqListModel.getUserId()));
        holder.tv_friend_name.setText(Html.fromHtml("Name : "+friendReqListModel.getUserName()));
        Picasso.with(context).load(friendReqListModel.getUserProfilePic()).placeholder(R.drawable.friend_img2).into(holder.img_profile_pic);
        holder.lay_accept_friend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //accept friend request and inserting friend in my friend list
                DatabaseReference databaseReference1 = FirebaseDatabase.getInstance().getReference("Friends").child(FriendListActivity.user_id).child(friendReqListModel.getUserId());
                Map<String, Object> value1 = new HashMap<>();
                value1.put("user_id", friendReqListModel.getUserId());
                value1.put("key", friendReqListModel.getKey());
                value1.put("status", "Accepted");
                databaseReference1.setValue(value1);

                //inserting me as friend in other friend list
                DatabaseReference databaseReference2 = FirebaseDatabase.getInstance().getReference("Friends").child(friendReqListModel.getUserId()).child(FriendListActivity.user_id);
                Map<String, Object> value2 = new HashMap<>();
                value2.put("user_id", FriendListActivity.user_id);
                value2.put("key", FriendListActivity.user_key);
                value2.put("status", "Accepted");
                databaseReference2.setValue(value2);
                Toast.makeText(context,"Friend invitation accepted!",Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(context, FriendListActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        });


        holder.lay_reject_friend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //reject friend request
                DatabaseReference dR1 = FirebaseDatabase.getInstance().getReference("Friends").child(FriendListActivity.user_id).child(friendReqListModel.getUserId());
                dR1.removeValue();
                DatabaseReference dR2 = FirebaseDatabase.getInstance().getReference("Friends").child(friendReqListModel.getUserId()).child(FriendListActivity.user_id);
                dR2.removeValue();
                Toast.makeText(context,"Friend invitation rejected!",Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(context, FriendListActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        });

    }


    @Override
    public int getItemCount() {
        return friendRequestLists.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView tv_friend_name, tv_friend_id, tv_friend_key;
        RelativeLayout lay_accept_friend,lay_reject_friend;
        ImageView img_profile_pic;
        ArrayList<FriendRequestListModel> friendRequestLists = new ArrayList<>();
        Context context;

        public MyViewHolder(View itemView, Context context, ArrayList<FriendRequestListModel> friendRequestLists) {
            super(itemView);
            this.context = context;
            this.friendRequestLists = friendRequestLists;
            tv_friend_name = (TextView) itemView.findViewById(R.id.friend_name);
            tv_friend_id = (TextView) itemView.findViewById(R.id.friend_id);
            //tv_friend_key = (TextView) itemView.findViewById(R.id.friend_key);
            lay_accept_friend = (RelativeLayout) itemView.findViewById(R.id.lay_accept_friend);
            lay_reject_friend = (RelativeLayout) itemView.findViewById(R.id.lay_reject_friend);
            img_profile_pic = (ImageView) itemView.findViewById(R.id.friend_img);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
        }

    }


}
