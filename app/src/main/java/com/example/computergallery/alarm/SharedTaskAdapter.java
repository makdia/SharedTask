package com.example.computergallery.alarm;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
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
import java.util.List;

/**
 * Created by Makdia Hussain on 1/28/2018.
 */

public class SharedTaskAdapter extends RecyclerView.Adapter<SharedTaskAdapter.MyViewHolder>  {
    private ArrayList<SharedTaskModel> alarmLists;
    private Context context;
    String user_alarm_time;
    SharedPreferences sharedPreferences_for_user_info;
    String user_id;


    private int lastPosition = -1;


    public SharedTaskAdapter(Context context, List<SharedTaskModel> alarmLists) {
        this.alarmLists = (ArrayList<SharedTaskModel>)alarmLists;
        this.context = context;
    }


    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.my_task_list,parent,false);
        MyViewHolder holder=new MyViewHolder(view,context,alarmLists);
        return holder;
    }


    @SuppressLint("ResourceAsColor")
    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {

        final SharedTaskModel newsModel=alarmLists.get(position);

        sharedPreferences_for_user_info=context.getSharedPreferences("login",context.MODE_PRIVATE);
        user_id=sharedPreferences_for_user_info.getString("unique_id","null");

        holder.tv_msg.setText(Html.fromHtml(newsModel.getTitle()));
        if (newsModel.getTaskImage()!=null && !newsModel.getTaskImage().isEmpty()) {
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




        if(newsModel.getState().equals("On")){
            holder.btn_state.setBackgroundResource(R.drawable.alarmon);
        } else if (newsModel.getState().equals("Off")) {
            holder.btn_state.setBackgroundResource(R.drawable.alarmoff);
        }
        //setAnimation(holder.itemView, position);

        /*if(newsModel.getTaskSeen().equals("No")){
            DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("Task").child(user_id);
            mDatabase.child(newsModel.getId()).child("taskSeen").setValue("Yes");
        }*/

        final String[] state = {newsModel.getState()};
        holder.btn_state.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Date old_date = null,current_date = null;
                Calendar cal = Calendar.getInstance();
                SimpleDateFormat dateTimeFormat = new SimpleDateFormat(ReminderEditActivity.DATE_TIME_FORMAT);
                String reminderDateTime = null;

                //take current date
                String currentDate= dateTimeFormat.format(cal.getTime());
                try {
                    current_date = new SimpleDateFormat(ReminderEditActivity.DATE_TIME_FORMAT).parse(currentDate);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                //take user old date
                try {
                    old_date = new SimpleDateFormat(ReminderEditActivity.DATE_TIME_FORMAT).parse(newsModel.getTime());
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                if(state[0].equals("On")){
                    cal.setTime(old_date);
                    reminderDateTime = dateTimeFormat.format(cal.getTime());
                    holder.btn_state.setBackgroundResource(R.drawable.alarmoff);
                    //setting task reminder state off
                    DatabaseReference dR = FirebaseDatabase.getInstance().getReference("Task").child(user_id).child(newsModel.getId());
                    SharedTaskModel alarmModel = new SharedTaskModel(newsModel.getId(), newsModel.getTitle(),  reminderDateTime, "Off", newsModel.getAlarmManagerId(), newsModel.getUserId(), newsModel.getTaskType(), newsModel.getTaskOwnerId(), "Yes", newsModel.getTaskImage());
                    dR.setValue(alarmModel);
                    Toast.makeText(context,"Task Reminder Off",Toast.LENGTH_SHORT).show();
                    state[0] ="Off";
                } else if (state[0].equals("Off")) {
                    if(current_date.after(old_date)){
                        cal.setTime(old_date);
                        int hour = cal.get(Calendar.HOUR_OF_DAY);
                        int minute = cal.get(Calendar.MINUTE);
                        int second = cal.get(Calendar.SECOND);
                        cal.setTime(current_date);
                        int year = cal.get(Calendar.YEAR);
                        int month = cal.get(Calendar.MONTH);
                        int day = cal.get(Calendar.DAY_OF_MONTH);
                        cal.set(Calendar.YEAR, year);
                        cal.set(Calendar.MONTH, month);
                        cal.set(Calendar.DAY_OF_MONTH, day);
                        cal.set(Calendar.HOUR_OF_DAY, hour);
                        cal.set(Calendar.MINUTE, minute);
                        cal.add(cal.DATE,1);
                        reminderDateTime = dateTimeFormat.format(cal.getTime());
                    }else if(current_date.before(old_date)){
                        cal.setTime(old_date);
                        reminderDateTime = dateTimeFormat.format(cal.getTime());
                    }
                    else if(current_date.equals(old_date)){
                        cal.setTime(old_date);
                        cal.add(cal.MINUTE, 1);
                        reminderDateTime = dateTimeFormat.format(cal.getTime());
                    }

                    holder.btn_state.setBackgroundResource(R.drawable.alarmon);
                    //setting task reminder state on
                    DatabaseReference dR = FirebaseDatabase.getInstance().getReference("Task").child(user_id).child(newsModel.getId());
                    SharedTaskModel alarmModel = new SharedTaskModel(newsModel.getId(), newsModel.getTitle(),  reminderDateTime, "On", newsModel.getAlarmManagerId(),newsModel.getUserId(), newsModel.getTaskType(), newsModel.getTaskOwnerId(), "Yes", newsModel.getTaskImage());
                    dR.setValue(alarmModel);
                    Toast.makeText(context,"Task Reminder On",Toast.LENGTH_SHORT).show();
                    state[0] ="On";
                }

                Long alarm_intent_id= Long.valueOf(newsModel.getAlarmManagerId().substring(7,12));
                //Toast.makeText(context,newsModel.getId()+alarm_intent_id+newsModel.getTitle()+newsModel.getBody()+cal+newsModel.getState()+newsModel.getAlarmManagerId()+newsModel.getUserId()+newsModel.getTaskType(),Toast.LENGTH_SHORT).show();
                new ReminderManager(context).setReminder(newsModel.getId(),alarm_intent_id, newsModel.getTitle(),  cal, newsModel.getState(), newsModel.getAlarmManagerId(), newsModel.getUserId(), newsModel.getTaskType(), newsModel.getTaskOwnerId(), newsModel.getTaskImage());
            }
        });




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
                intent.putExtra("save_task", "edit_shared_task");
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
        Button btn_state,btn_delete,btn_edit;
        ImageView task_img;
        ArrayList<SharedTaskModel> alarmLists=new ArrayList<>();
        Context context;

        public MyViewHolder(View itemView, Context context, ArrayList<SharedTaskModel> alarmLists) {
            super(itemView);
            this.context=context;
            this.alarmLists=alarmLists;
            tv_msg=(TextView)itemView.findViewById(R.id.tv_msg);
            tv_time=(TextView)itemView.findViewById(R.id.tv_time);
            tv_date=(TextView)itemView.findViewById(R.id.tv_date);
            btn_state=(Button) itemView.findViewById(R.id.btn_state);
            btn_edit=(Button) itemView.findViewById(R.id.btn_edit);
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
            final SharedTaskModel newsModel=this.alarmLists.get(position);

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(v.getRootView().getContext(),R.style.AppCompatAlertDialogStyle);
            alertDialogBuilder.setTitle("Are you sure?");
            alertDialogBuilder.setMessage("You'll lose both task and reminder!");
            alertDialogBuilder.setCancelable(false);
            // set positive button YES message
            alertDialogBuilder.setPositiveButton("Erase", new DialogInterface.OnClickListener(){
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    deleteAlarm(newsModel, newsModel.getId(), newsModel.getTitle(),  newsModel.getTime(), "Off", newsModel.getAlarmManagerId(), newsModel.getUserId(), newsModel.getTaskType(), newsModel.getTaskOwnerId(), newsModel.getTaskImage());
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




    public void deleteAlarm(final SharedTaskModel item, String alarm_id, String alarm_title,  String alarm_time, String state, String alarm_manager_id, String user_id, String task_type, String task_owner_id, String task_image) {
                Calendar cal = Calendar.getInstance();
                SimpleDateFormat format = new SimpleDateFormat(ReminderEditActivity.DATE_TIME_FORMAT);
                try {
                    Date date = format.parse(alarm_time);
                    cal.setTime(date);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                //calling remove task reminder method
                boolean return_value=deleteDataFromFirebase(alarm_id);

                Long alarm_intent_id= Long.valueOf(alarm_manager_id.substring(7,12));

                if(return_value==true) {
                    new ReminderManager(context).setReminder(alarm_id, alarm_intent_id, alarm_title,  cal, "Off", alarm_manager_id, user_id, task_type, task_owner_id, task_image);
                    alarmLists.remove(item);
                    notifyDataSetChanged();
                }
        }


    private boolean deleteDataFromFirebase(String id) {
        //removing task reminder
        DatabaseReference dR = FirebaseDatabase.getInstance().getReference("Task").child(user_id).child(id);
        dR.removeValue();
        return true;
    }

    private void setAnimation(View viewToAnimate, int position)
    {
        // If the bound view wasn't previously displayed on screen, it's animated
        if (position > lastPosition)
        {
            Animation animation = AnimationUtils.loadAnimation(context, R.anim.recycleview_bounce);
            viewToAnimate.startAnimation(animation);
            lastPosition = position;
        }
    }
}
