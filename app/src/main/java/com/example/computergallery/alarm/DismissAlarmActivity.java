package com.example.computergallery.alarm;

import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.Vibrator;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import android.app.Activity;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Makdia Hussain on 1/29/2018.
 */
public class DismissAlarmActivity extends Activity{

   MediaPlayer mp=null ;
   Vibrator v=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_dismiss_alarm);

        Button btn_stopAlarm = (Button) findViewById(R.id.btn_dismiss);
        TextView tv_title = (TextView) findViewById(R.id.tv_msg);
        TextView tv_time = (TextView) findViewById(R.id.tv_time);
        ImageView imageView_task_image = (ImageView) findViewById(R.id.task_img);


        mp = MediaPlayer.create(getBaseContext(), getAlarmUri());
        v = (Vibrator) getSystemService(this.VIBRATOR_SERVICE);

        String title=getIntent().getStringExtra("title");
        String time=getIntent().getStringExtra("time");
        String image=getIntent().getStringExtra("image");

        //converting date time format to only time format
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat(ReminderEditActivity.DATE_TIME_FORMAT);
        try {
            Date date = format.parse(time);
            cal.setTime(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        SimpleDateFormat timeFormat = new SimpleDateFormat(ReminderEditActivity.TIME_FORMAT);
        String pick_time_format = timeFormat.format(cal.getTime());


        tv_title.setText(title);
        tv_time.setText(pick_time_format);
        if(image!=null && !image.equals("empty")) {
            Picasso.with(this).load(image).placeholder(R.drawable.task_icon).into(imageView_task_image);
        }

        btn_stopAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onDestroy();
            }
        });


        btn_stopAlarm.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {
                // TODO Auto-generated method stub
                onDestroy();
                return false;
            }
        });

       playSound(this, getAlarmUri());
    }



   private void playSound(final Context context, Uri alert) {
        Thread background = new Thread(new Runnable() {
            public void run() {
                try {
                    for (long i = 0; i < 10; ++i) {
                        mp.start();
                        v.vibrate(3000);
                        long timeout = 5000;
                        while (mp.isPlaying() && (timeout > 0)) {
                            timeout = timeout - 100;
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                            }
                        }
                    }
                    //Thread.sleep(8000);
                } catch (Throwable t) {
                    Log.i("Animation", "Thread  exception "+t);
                }finally {
                    finish();
                    WakeLocker.release();
                }
            }
        });
        background.start();

    }


    //stop alarm manager
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mp.stop();
        v.cancel();
        new ReminderService(this).cancelNotification();
        finish();
    }



    //Get an alarm sound. Try for an alarm. If none set, take ringtone.
    private Uri getAlarmUri() {
        Uri alert = RingtoneManager
                .getDefaultUri(RingtoneManager.TYPE_ALARM);
        if (alert == null) {
            alert = RingtoneManager
                    .getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            if (alert == null) {
                alert = RingtoneManager
                        .getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            }
        }
        return alert;
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        onDestroy();
    }

}