package com.example.computergallery.alarm;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

//import com.bumptech.glide.Glide;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Makdia Hussain on 2/16/2018.
 */

public class OldUserListAdapter extends RecyclerView.Adapter<OldUserListAdapter.MyViewHolder> {
    private ArrayList<OldUserListModel> oldUserLists;
    private Context context;

    public OldUserListAdapter(Context context, List<OldUserListModel> oldUserLists) {
        this.oldUserLists = (ArrayList<OldUserListModel>) oldUserLists;
        this.context = context;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.old_user_list, parent, false);
        MyViewHolder holder = new MyViewHolder(view, context, oldUserLists);
        return holder;
    }


    @SuppressLint("ResourceAsColor")
    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {

        final OldUserListModel oldUserListModel = oldUserLists.get(position);
        holder.tv_user_name.setText(Html.fromHtml(oldUserListModel.getUserName()));
        Picasso.with(context).load(oldUserListModel.getUserProfilePic()).placeholder(R.drawable.profile_pic2).into(holder.img_profile_pic);
        holder.btn_account_remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //creating a popup menu
                PopupMenu popup = new PopupMenu(context, holder.btn_account_remove);
                //inflating menu from xml resource
                popup.inflate(R.menu.menu_account_remove);
                //adding click listener
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.action_remove:
                                DatabaseReference dR = FirebaseDatabase.getInstance().getReference("User").child(oldUserListModel.getUserId());
                                dR.child("device_imei").setValue("");
                                oldUserLists.remove(oldUserListModel);
                                notifyDataSetChanged();
                                break;
                        }
                        return false;
                    }
                });
                //displaying the popup
                popup.show();
            }
        });

    }


    @Override
    public int getItemCount() {
        return oldUserLists.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView tv_user_name;
        Button btn_account_remove;
        ImageView img_profile_pic;
        ArrayList<OldUserListModel> oldUserLists = new ArrayList<>();
        Context context;

        public MyViewHolder(View itemView, Context context, ArrayList<OldUserListModel> oldUserLists) {
            super(itemView);
            this.context = context;
            this.oldUserLists = oldUserLists;
            tv_user_name = (TextView) itemView.findViewById(R.id.user_name);
            btn_account_remove = (Button) itemView.findViewById(R.id.btn_account_remove);
            img_profile_pic = (ImageView) itemView.findViewById(R.id.user_img);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int position=getAdapterPosition();
            final OldUserListModel oldUserListModel = this.oldUserLists.get(position);
            SharedPreferences.Editor editor = OldUserLoginOptionActivity.sharedPreferences_for_user_info.edit();
            editor.putBoolean("login", true);
            editor.putString("unique_id", oldUserListModel.getUserId());
            editor.putString("user_id", oldUserListModel.getUserId());
            editor.putString("user_name", oldUserListModel.getUserName());
            editor.putString("key", oldUserListModel.getKey());
            editor.putString("password", oldUserListModel.getPassword());
            editor.putString("device_imei", oldUserListModel.getDeviceImei());
            editor.putString("profile_pic", oldUserListModel.getUserProfilePic());
            editor.commit();
            Toast.makeText(context, "Login success!", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(context, HomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }

    }

}
