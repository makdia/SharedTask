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
 * Created by Makdia Hussain on 2/8/2018.
 */

public class InviteFriendListAdapter extends RecyclerView.Adapter<InviteFriendListAdapter.MyViewHolder> {
    private ArrayList<InviteFriendListModel> inviteFriendLists;
    private Context context;
    public static ArrayList<InviteFriendListModel> searchLists;


    public InviteFriendListAdapter(Context context, ArrayList<InviteFriendListModel> inviteFriendLists) {
        this.inviteFriendLists = inviteFriendLists;
        this.searchLists = new ArrayList<InviteFriendListModel>();
        //this.searchLists.addAll(InviteFriendActivity.inviteFriendLists);
        this.context = context;
    }


    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.friend_invitation_list, parent, false);
        MyViewHolder holder = new MyViewHolder(view, context, inviteFriendLists);
        return holder;

    }

    @SuppressLint("ResourceAsColor")
    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {

        final InviteFriendListModel friendInvListModel = inviteFriendLists.get(position);
        //holder.tv_friend_key.setText(Html.fromHtml("Key : "+friendListModel.getKey()));
        holder.tv_friend_id.setText(Html.fromHtml("(ID:"+friendInvListModel.getUserId()+")"));
        holder.tv_friend_name.setText(Html.fromHtml(friendInvListModel.getUserName()));
        if(friendInvListModel.getStatus().equals("Cancel Invitation")){
            holder.img_invitation_confirmation.setImageResource(R.drawable.invite_friend_correct);
        }else{
            holder.img_invitation_confirmation.setImageResource(R.drawable.invite_friend_plus);
        }
        Picasso.with(context).load(friendInvListModel.getUserProfilePic()).placeholder(R.drawable.friend_img2).into(holder.img_profile_pic);
        holder.lay_invite_friend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //send friend request
                if(friendInvListModel.getStatus().equals("Invite Friend")) {
                    holder.img_invitation_confirmation.setImageResource(R.drawable.invite_friend_correct);

                    DatabaseReference dR1 = FirebaseDatabase.getInstance().getReference("Friends").child(friendInvListModel.getUserId()).child(InviteFriendActivity.user_id);
                    Map<String, Object> value1 = new HashMap<>();
                    value1.put("user_id", InviteFriendActivity.user_id);
                    value1.put("key", InviteFriendActivity.user_key);
                    value1.put("status", "Pending_from_others");
                    dR1.setValue(value1);

                    DatabaseReference dR2 = FirebaseDatabase.getInstance().getReference("Friends").child(InviteFriendActivity.user_id).child(friendInvListModel.getUserId());
                    Map<String, Object> value2 = new HashMap<>();
                    value2.put("user_id", friendInvListModel.getUserId());
                    value2.put("key", friendInvListModel.getKey());
                    value2.put("status", "Pending_from_me");
                    dR2.setValue(value2);
                    friendInvListModel.setStatus("Cancel Invitation");
                    Toast.makeText(context, "Your invitation has been sent successfully!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(context, InviteFriendActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                }
                //reject friend request
                else if(friendInvListModel.getStatus().equals("Cancel Invitation")) {
                    holder.img_invitation_confirmation.setImageResource(R.drawable.invite_friend_plus);

                    DatabaseReference dR1 = FirebaseDatabase.getInstance().getReference("Friends").child(friendInvListModel.getUserId()).child(InviteFriendActivity.user_id);
                    dR1.removeValue();
                    DatabaseReference dR2 = FirebaseDatabase.getInstance().getReference("Friends").child(InviteFriendActivity.user_id).child(friendInvListModel.getUserId());
                    dR2.removeValue();
                    friendInvListModel.setStatus("Invite Friend");
                    Toast.makeText(context, "Your invitation has been rejected successfully!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(context, InviteFriendActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                }
            }
        });
    }


    @Override
    public int getItemCount() {
        return inviteFriendLists.size();
    }


    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView tv_friend_name, tv_friend_id, tv_friend_key;
        LinearLayout lay_invite_friend;
        ImageView img_profile_pic,img_invitation_confirmation;
        ArrayList<InviteFriendListModel> inviteFriendLists = new ArrayList<>();
        Context context;

        public MyViewHolder(View itemView, Context context, ArrayList<InviteFriendListModel> inviteFriendLists) {
            super(itemView);
            this.context = context;
            this.inviteFriendLists = inviteFriendLists;
            tv_friend_name = (TextView) itemView.findViewById(R.id.friend_name);
            tv_friend_id = (TextView) itemView.findViewById(R.id.friend_id);
            //tv_friend_key = (TextView) itemView.findViewById(R.id.friend_key);
            lay_invite_friend = (LinearLayout) itemView.findViewById(R.id.lay_invite_friend);
            img_profile_pic = (ImageView) itemView.findViewById(R.id.friend_img);
            img_invitation_confirmation = (ImageView) itemView.findViewById(R.id.img_invitation_confirmation);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
        }

    }


        //for search start Filter method
        public void filter(String charText) {
            charText = charText.toLowerCase(Locale.getDefault());
            InviteFriendActivity.inviteFriendLists.clear();
            if (charText.length() == 0) {
                InviteFriendActivity.inviteFriendLists.addAll(searchLists);
            } else {
                for (InviteFriendListModel wp : searchLists) {
                    if (wp.getUserName().toLowerCase(Locale.getDefault()).contains(charText)) {
                        InviteFriendActivity.inviteFriendLists.add(wp);
                    }
                }
            }
            notifyDataSetChanged();
    }
}
