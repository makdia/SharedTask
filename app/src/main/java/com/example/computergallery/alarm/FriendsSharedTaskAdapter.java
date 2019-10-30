package com.example.computergallery.alarm;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Makdia Hussain on 2/13/2018.
 */

public class FriendsSharedTaskAdapter extends RecyclerView.Adapter<FriendsSharedTaskAdapter.MyViewHolder>  {
    private ArrayList<FriendsSharedTaskModel> alarmLists;
    private Context context;
    String user_alarm_time;
    SharedPreferences sharedPreferences_for_user_info;
    String user_id;

    public FriendsSharedTaskAdapter(Context context, List<FriendsSharedTaskModel> alarmLists) {
        this.alarmLists = (ArrayList<FriendsSharedTaskModel>)alarmLists;
        this.context = context;
    }


    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.friends_shared_task_list,parent,false);
        MyViewHolder holder=new MyViewHolder(view,context,alarmLists);
        return holder;
    }


    @SuppressLint("ResourceAsColor")
    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {

        final FriendsSharedTaskModel newsModel=alarmLists.get(position);

        sharedPreferences_for_user_info=context.getSharedPreferences("login",context.MODE_PRIVATE);
        user_id=sharedPreferences_for_user_info.getString("unique_id","null");


        holder.tv_msg.setText(Html.fromHtml(newsModel.getTitle()));
        if (newsModel.getTaskImage()!=null && !newsModel.getTaskImage().equals("empty")) {
            Picasso.with(context).load(newsModel.getTaskImage()).placeholder(R.drawable.task_icon).into(holder.task_img);
        }

        //pick date and time in desired format
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat(ReminderEditActivity.DATE_TIME_FORMAT);
        try {
            Date date = format.parse(newsModel.getTime());
            cal.setTime(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        SimpleDateFormat timeFormat = new SimpleDateFormat(ReminderEditActivity.TIME_FORMAT);
        String timeForButton = timeFormat.format(cal.getTime());
        holder.tv_time.setText(Html.fromHtml(timeForButton));
        SimpleDateFormat dateFormat = new SimpleDateFormat(ReminderEditActivity.DATE_FORMAT);
        String dateForButton = dateFormat.format(cal.getTime());
        holder.tv_date.setText(Html.fromHtml(dateForButton));
        SimpleDateFormat dateTimeFormat = new SimpleDateFormat(ReminderEditActivity.DATE_TIME_FORMAT);
        user_alarm_time = dateTimeFormat.format(cal.getTime());
        //end
        if(newsModel.getTaskSeen().equals("Yes")){
            holder.img_seen.setImageResource(R.drawable.seen);
            holder.tv_seen.setTextColor(R.color.colorAccent);
            holder.tv_seen.setText("Delivered");
        }

        holder.btn_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ReminderEditActivity.class);
                intent.putExtra("alarm_id", newsModel.getId());
                intent.putExtra("alarm_title", newsModel.getTitle());
                intent.putExtra("alarm_time", newsModel.getTime());
                intent.putExtra("alarm_manager_id", newsModel.getAlarmManagerId());
                intent.putExtra("user_id", newsModel.getUserId());
                intent.putExtra("task_type", newsModel.getTaskType());
                intent.putExtra("task_owner_id", newsModel.getTaskOwnerId());
                intent.putExtra("task_image", newsModel.getTaskImage());
                intent.putExtra("friend_timezone", FriendsSharedTaskActivity.friend_timezone);
                intent.putExtra("save_task", "edit_friends_shared_task");
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        });


    }



    @Override
    public int getItemCount() {
        return alarmLists.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        TextView tv_msg,tv_time,tv_date;
        Button btn_edit;
        ImageView img_seen;
        TextView tv_seen;
        ImageView task_img;
        ArrayList<FriendsSharedTaskModel> alarmLists=new ArrayList<>();
        Context context;

        public MyViewHolder(View itemView, Context context, ArrayList<FriendsSharedTaskModel> alarmLists) {
            super(itemView);
            this.context=context;
            this.alarmLists=alarmLists;
            tv_msg=(TextView)itemView.findViewById(R.id.tv_msg);
            tv_time=(TextView)itemView.findViewById(R.id.tv_time);
            tv_date=(TextView)itemView.findViewById(R.id.tv_date);
            btn_edit=(Button) itemView.findViewById(R.id.btn_edit);
            img_seen=(ImageView) itemView.findViewById(R.id.img_seen);
            tv_seen=(TextView) itemView.findViewById(R.id.tv_seen);
            task_img=(ImageView) itemView.findViewById(R.id.task_img);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);

        }

        @Override
        public void onClick(View v) {

        }



        @Override
        public boolean onLongClick(View v) {
            int position=getAdapterPosition();
            final FriendsSharedTaskModel newsModel=this.alarmLists.get(position);

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(v.getRootView().getContext(),R.style.AppCompatAlertDialogStyle);
            alertDialogBuilder.setTitle("Are you sure?");
            alertDialogBuilder.setMessage("You'll lose both task and reminder!");
            alertDialogBuilder.setCancelable(false);
            // set positive button YES message
            alertDialogBuilder.setPositiveButton("Erase", new DialogInterface.OnClickListener(){
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    deleteAlarm(newsModel, newsModel.getId(), newsModel.getTitle(),  newsModel.getTime(), "Off", newsModel.getAlarmManagerId(), newsModel.getUserId(), newsModel.getTaskType(), newsModel.getTaskOwnerId(), newsModel.getTaskSeen(), newsModel.getTaskImage());
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




    public void deleteAlarm(final FriendsSharedTaskModel item, String alarm_id, String alarm_title,  String alarm_time, String state, String alarm_manager_id, String user_id, String task_type, String task_owner_id, String task_seen, String task_image) {
        //removing task reminder
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Task").child(user_id).child(alarm_id);
        Map<String, Object> value = new HashMap<>();
        value.put("id", alarm_id);
        value.put("title", alarm_title);
        value.put("time", alarm_time);
        value.put("state", "Off");
        value.put("alarmManagerId", alarm_manager_id);
        value.put("userId", user_id);
        value.put("taskType", "Deleted shared task");
        value.put("taskOwnerId", task_owner_id);
        value.put("taskSeen", task_seen);
        value.put("taskImage", task_image);
        databaseReference.setValue(value);
        alarmLists.remove(item);
        notifyDataSetChanged();

    }


}
