package com.example.computergallery.alarm;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

/**
 * Created by Makdia Hussain on 1/28/2018.
 */
public class ReminderEditActivity extends AppCompatActivity {

    // Dialog Constants for date and time
    private static final int DATE_PICKER_DIALOG = 0;
    private static final int TIME_PICKER_DIALOG = 1;
    public static final String DATE_FORMAT = "d MMM yyyy";
    public static final String TIME_FORMAT = "h:mm a";
    public static final String DATE_TIME_FORMAT = "d MMM yyyy h:mm a";

    private EditText mTitleText;
    private Button mDateButton;
    private Button mTimeButton;
    private Button mConfirmButton;
    private Long mRowId;
    private String alarm_id,alarm_title,alarm_time,alarm_manager_id;
    private Calendar mCalendar;
    String userDate,currentDate;
    Date current_date,user_date;
    String save_task;
    String share_user_id,own_user_id,user_id,task_type,task_owner_id;

    //database reference object
    DatabaseReference databaseReference;
    SharedPreferences sharedPreferences_for_user_info;
    boolean isLoggedIn;

    //for task image
    String task_img="empty";
    private Button btnChoose;
    private ImageView imageView_task_img;
    private Uri filePath;
    private int PICK_IMAGE_REQUEST = 0;
    //Firebase
    FirebaseStorage storage;
    StorageReference storageReference;
    ProgressDialog progressDialog;
    private static final int MY_PERMISSIONS_REQUEST_READ_PHONE_STATE = 0;
    //for timezone
    String timezone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminder_edit);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorAccent)));


        sharedPreferences_for_user_info=getSharedPreferences("login",MODE_PRIVATE);
        isLoggedIn=sharedPreferences_for_user_info.getBoolean("login",false);
        if (isLoggedIn != true){
            Intent intent = new Intent(getApplicationContext(), OldUserLoginOptionActivity.class);
            startActivity(intent);
            finish();
        }


        Intent intent = getIntent();
        save_task = intent.getStringExtra("save_task");


        mCalendar = Calendar.getInstance();
        mTitleText = (EditText) findViewById(R.id.title);
        mDateButton = (Button) findViewById(R.id.reminder_date);
        mTimeButton = (Button) findViewById(R.id.reminder_time);
        mConfirmButton = (Button) findViewById(R.id.confirm);

        if(save_task!=null){
            if (mRowId == null && (save_task.equals("edit_my_task") || save_task.equals("edit_shared_task"))) {
                getSupportActionBar().setTitle(R.string.edit_reminder_title);
                alarm_id=intent.getStringExtra("alarm_id");
                alarm_title=intent.getStringExtra("alarm_title");
                alarm_time=intent.getStringExtra("alarm_time");
                alarm_manager_id=intent.getStringExtra("alarm_manager_id");
                share_user_id = intent.getStringExtra("user_id");
                task_type = intent.getStringExtra("task_type");
                task_owner_id = intent.getStringExtra("task_owner_id");
                task_img = intent.getStringExtra("task_image");
                mRowId =Long.valueOf(alarm_manager_id.substring(7,12));
                user_id=share_user_id;
                TimeZone timeZone = TimeZone.getDefault();
                timezone = timeZone.getID();  // "It takes user device time zone"
                setTimezone(timezone);
            }
            else if (mRowId == null && (save_task.equals("edit_friends_shared_task"))) {
                getSupportActionBar().setTitle(R.string.edit_reminder_title);
                alarm_id=intent.getStringExtra("alarm_id");
                alarm_title=intent.getStringExtra("alarm_title");
                alarm_time=intent.getStringExtra("alarm_time");
                alarm_manager_id=intent.getStringExtra("alarm_manager_id");
                share_user_id = intent.getStringExtra("user_id");
                task_type = intent.getStringExtra("task_type");
                task_owner_id = intent.getStringExtra("task_owner_id");
                task_img = intent.getStringExtra("task_image");
                mRowId =Long.valueOf(alarm_manager_id.substring(7,12));
                user_id=share_user_id;
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ReminderEditActivity.this, R.style.AppCompatAlertDialogStyle);
                alertDialogBuilder.setMessage("Which Timezone you want to set for this task? Choose Timezone.");
                alertDialogBuilder.setCancelable(false);
                // set positive button YES message
                alertDialogBuilder.setPositiveButton("Friend Timezone", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        timezone = getIntent().getStringExtra("friend_timezone");
                        Toast.makeText(getApplicationContext(), "You select your friend TimeZone", Toast.LENGTH_SHORT).show();
                        setTimezone(timezone);
                    }
                });
                // set neutral button OK
                alertDialogBuilder.setNeutralButton("My Timezone", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Dismiss the dialog
                        dialog.dismiss();
                        TimeZone timeZone = TimeZone.getDefault();
                        timezone = timeZone.getID();  // "It takes user device time zone"
                        Toast.makeText(getApplicationContext(), "You select your TimeZone", Toast.LENGTH_SHORT).show();
                        setTimezone(timezone);
                    }
                });
                AlertDialog alertDialog = alertDialogBuilder.create();
                // show alert
                alertDialog.show();
            }
            else if(save_task.equals("shared_task")){
                getSupportActionBar().setTitle(R.string.add_shared_task);
                share_user_id = intent.getStringExtra("friend_id");
                user_id=share_user_id;
                task_owner_id=sharedPreferences_for_user_info.getString("unique_id","null");
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ReminderEditActivity.this, R.style.AppCompatAlertDialogStyle);
                alertDialogBuilder.setMessage("Which Timezone you want to set for this task? Choose Timezone.");
                alertDialogBuilder.setCancelable(false);
                // set positive button YES message
                alertDialogBuilder.setPositiveButton("Friend Timezone", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        timezone = getIntent().getStringExtra("friend_timezone");
                        Toast.makeText(getApplicationContext(), "You select your friend TimeZone", Toast.LENGTH_SHORT).show();
                        setTimezone(timezone);
                    }
                });
                // set neutral button OK
                alertDialogBuilder.setNeutralButton("My Timezone", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Dismiss the dialog
                        dialog.dismiss();
                        TimeZone timeZone = TimeZone.getDefault();
                        timezone = timeZone.getID();  // "It takes user device time zone"
                        Toast.makeText(getApplicationContext(), "You select your TimeZone", Toast.LENGTH_SHORT).show();
                        setTimezone(timezone);
                    }
                });
                AlertDialog alertDialog = alertDialogBuilder.create();
                // show alert
                alertDialog.show();
            }
        }else{
            own_user_id=sharedPreferences_for_user_info.getString("unique_id","null");
            user_id=own_user_id;
            task_owner_id=own_user_id;
            TimeZone timeZone = TimeZone.getDefault();
            timezone = timeZone.getID();  // "It takes user device time zone"//pick current date
            setTimezone(timezone);
        }

        databaseReference = FirebaseDatabase.getInstance().getReference("Task").child(user_id);


        /*mCalendar = Calendar.getInstance();
        mTitleText = (EditText) findViewById(R.id.title);
        mDateButton = (Button) findViewById(R.id.reminder_date);
        mTimeButton = (Button) findViewById(R.id.reminder_time);
        mConfirmButton = (Button) findViewById(R.id.confirm);*/


        /*
        //pick current date
        SimpleDateFormat dateTimeFormat = new SimpleDateFormat(DATE_TIME_FORMAT);
        dateTimeFormat.setTimeZone(TimeZone.getTimeZone(timezone));
        currentDate= dateTimeFormat.format(mCalendar.getTime());
        try {
            current_date = new SimpleDateFormat(DATE_TIME_FORMAT).parse(currentDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }*/

        //buttonListenersAndSetDefaultText();


        btnChoose = (Button) findViewById(R.id.btnChoose);
        imageView_task_img = (ImageView) findViewById(R.id.task_img);
        if(task_img!=null && !task_img.equals("empty")) {
            Picasso.with(this).load(task_img).placeholder(R.drawable.task_icon).into(imageView_task_img);
        }
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        progressDialog = new ProgressDialog(ReminderEditActivity.this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCancelable(false);
        imageView_task_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage();
            }
        });
        btnChoose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage();
            }
        });
        takingStoragePermission();
    }


    public void setTimezone(String timezone){
        //pick current date
        SimpleDateFormat dateTimeFormat = new SimpleDateFormat(DATE_TIME_FORMAT);
        dateTimeFormat.setTimeZone(TimeZone.getTimeZone(timezone));
        currentDate= dateTimeFormat.format(mCalendar.getTime());
        try {
            current_date = new SimpleDateFormat(DATE_TIME_FORMAT).parse(currentDate);
            mCalendar = Calendar.getInstance();
            mCalendar.setTime(current_date);
            buttonListenersAndSetDefaultText();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        populateFields();
    }



    @Override
    protected Dialog onCreateDialog(int id) {
        switch(id) {
            case DATE_PICKER_DIALOG:
                return showDatePicker();
            case TIME_PICKER_DIALOG:
                return showTimePicker();
        }
        return super.onCreateDialog(id);
    }



    private DatePickerDialog showDatePicker() {
        DatePickerDialog datePicker = new DatePickerDialog(ReminderEditActivity.this, new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                mCalendar.set(Calendar.YEAR, year);
                mCalendar.set(Calendar.MONTH, monthOfYear);
                mCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateDateButtonText();
            }
        }, mCalendar.get(Calendar.YEAR), mCalendar.get(Calendar.MONTH), mCalendar.get(Calendar.DAY_OF_MONTH));
        return datePicker;
    }



    private TimePickerDialog showTimePicker() {
        TimePickerDialog timePicker = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {

            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                mCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                mCalendar.set(Calendar.MINUTE, minute);
                updateTimeButtonText();
            }
        }, mCalendar.get(Calendar.HOUR_OF_DAY), mCalendar.get(Calendar.MINUTE), false);

        return timePicker;
    }



    private void buttonListenersAndSetDefaultText() {

        mDateButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                showDialog(DATE_PICKER_DIALOG);
            }
        });

        mTimeButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                showDialog(TIME_PICKER_DIALOG);
            }
        });

        mConfirmButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                final Animation myAnim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.bounce);
                view.startAnimation(myAnim);
                saveReminder();
                uploadImage();
                //setResult(RESULT_OK);
                //backActivity();
            }
        });
        updateDateButtonText();
        updateTimeButtonText();
    }



    private void populateFields()  {
        // Only populate the text boxes and change the calendar date
        // if the row is not null from the database.
        if (mRowId != null) {
            mTitleText.setText(alarm_title);
            // Get the date from the database and format it for our use.
            SimpleDateFormat dateTimeFormat = new SimpleDateFormat(DATE_TIME_FORMAT);
            Date date = null;
            try {
                String dateString = alarm_time;
                date = dateTimeFormat.parse(dateString);
                mCalendar.setTime(date);
            } catch (ParseException e) {
                Log.e("ReminderEditActivity", e.getMessage(), e);
            }
        }

        updateDateButtonText();
        updateTimeButtonText();
    }


    private void updateTimeButtonText() {
        // Set the time button text based upon the value from the database
        SimpleDateFormat timeFormat = new SimpleDateFormat(TIME_FORMAT);
        String timeForButton = timeFormat.format(mCalendar.getTime());
        mTimeButton.setText(timeForButton);


    }


    private void updateDateButtonText() {
        // Set the date button text based upon the value from the database
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
        String dateForButton = dateFormat.format(mCalendar.getTime());
        mDateButton.setText(dateForButton);
    }


    //save task reminder
    private void saveReminder() {
        String title = mTitleText.getText().toString();
        String state = "On";
        String passing_alarm_manager_id = null;

        //check if user given date is valid or not
        SimpleDateFormat dateTimeFormat;
        String reminderDateTime = null;
        dateTimeFormat = new SimpleDateFormat(DATE_TIME_FORMAT);
        userDate = dateTimeFormat.format(mCalendar.getTime());
        try {
            user_date = new SimpleDateFormat(DATE_TIME_FORMAT).parse(userDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (current_date.after(user_date)) {
            mCalendar.setTime(user_date);
            int hour = mCalendar.get(Calendar.HOUR_OF_DAY);
            int minute = mCalendar.get(Calendar.MINUTE);
            int second = mCalendar.get(Calendar.SECOND);
            mCalendar.setTime(current_date);
            int year = mCalendar.get(Calendar.YEAR);
            int month = mCalendar.get(Calendar.MONTH);
            int day = mCalendar.get(Calendar.DAY_OF_MONTH);
            mCalendar.set(Calendar.YEAR, year);
            mCalendar.set(Calendar.MONTH, month);
            mCalendar.set(Calendar.DAY_OF_MONTH, day);
            mCalendar.set(Calendar.HOUR_OF_DAY, hour);
            mCalendar.set(Calendar.MINUTE, minute);
            mCalendar.add(mCalendar.DATE, 1);
            dateTimeFormat = new SimpleDateFormat(DATE_TIME_FORMAT);
            reminderDateTime = dateTimeFormat.format(mCalendar.getTime());
        } else if (current_date.before(user_date)) {
            dateTimeFormat = new SimpleDateFormat(DATE_TIME_FORMAT);
            reminderDateTime = dateTimeFormat.format(mCalendar.getTime());
        } else if (current_date.equals(user_date)) {
            mCalendar.add(mCalendar.MINUTE, 1);
            dateTimeFormat = new SimpleDateFormat(DATE_TIME_FORMAT);
            reminderDateTime = dateTimeFormat.format(mCalendar.getTime());
        }


        if (save_task != null && save_task.equals("shared_task")) {
            final String _id = String.valueOf(System.currentTimeMillis());
            task_type = "Shared task";
            //getting a unique id using push().getKey() method.It will create a unique id and we will use it as the Primary Key for our task
            String id = databaseReference.push().getKey();
            Map<String, Object> value = new HashMap<>();
            value.put("id", id);
            value.put("title", title);
            value.put("time", reminderDateTime);
            value.put("state", state);
            value.put("alarmManagerId", _id);
            value.put("userId", user_id);
            value.put("taskType", task_type);
            value.put("taskOwnerId", task_owner_id);
            value.put("taskSeen", "No");
            value.put("taskImage", task_img);
            databaseReference.child(id).setValue(value);
            sendNotificationForSavingAlarm(title, reminderDateTime, user_id);
            Toast.makeText(ReminderEditActivity.this, getString(R.string.task_saved_message), Toast.LENGTH_SHORT).show();
            alarm_id = id;
        }
        else if (save_task != null && (save_task.equals("edit_friends_shared_task"))) {
            //updating friends shared task
            FriendsSharedTaskModel friendsSharedTaskModel = new FriendsSharedTaskModel(alarm_id, title, reminderDateTime, state, alarm_manager_id, user_id, task_type, task_owner_id, "No", task_img);
            databaseReference.child(alarm_id).setValue(friendsSharedTaskModel);
            sendNotificationForSavingAlarm(title, reminderDateTime, user_id);
            Toast.makeText(ReminderEditActivity.this, getString(R.string.task_saved_message), Toast.LENGTH_SHORT).show();
        }
        else if (save_task != null && (save_task.equals("edit_my_task"))) {
            //updating my task
            MyTaskModel alarmModel = new MyTaskModel(alarm_id, title, reminderDateTime, state, alarm_manager_id, user_id, task_type, task_owner_id, task_img);
            databaseReference.child(alarm_id).setValue(alarmModel);
            mRowId = Long.valueOf(alarm_manager_id.substring(7, 12));
            passing_alarm_manager_id = alarm_manager_id;
            new ReminderManager(this).setReminder(alarm_id, mRowId, title, mCalendar, "On", passing_alarm_manager_id, user_id, task_type, task_owner_id, task_img);
            Toast.makeText(ReminderEditActivity.this, getString(R.string.task_updated_message), Toast.LENGTH_SHORT).show();
        }
        else if (save_task != null && save_task.equals("edit_shared_task")) {
            //updating shared task
            SharedTaskModel sharedTaskModel = new SharedTaskModel(alarm_id, title, reminderDateTime, state, alarm_manager_id, user_id, task_type, task_owner_id, "Yes", task_img);
            databaseReference.child(alarm_id).setValue(sharedTaskModel);
            mRowId = Long.valueOf(alarm_manager_id.substring(7, 12));
            passing_alarm_manager_id = alarm_manager_id;
            new ReminderManager(this).setReminder(alarm_id, mRowId, title, mCalendar, "On", passing_alarm_manager_id, user_id, task_type, task_owner_id, task_img);
            Toast.makeText(ReminderEditActivity.this, getString(R.string.task_updated_message), Toast.LENGTH_SHORT).show();
        }
        else if (save_task == null && mRowId == null) {
            final String _id = String.valueOf(System.currentTimeMillis());
            passing_alarm_manager_id = _id;
            task_type = "Own task";
            //getting a unique id using push().getKey() method
            String id = databaseReference.push().getKey();
            MyTaskModel alarmModel = new MyTaskModel(id, title, reminderDateTime, state, _id, user_id, task_type, task_owner_id, task_img);
            //Saving the task
            databaseReference.child(id).setValue(alarmModel);
            mRowId = Long.valueOf(passing_alarm_manager_id.substring(7, 12));
            new ReminderManager(this).setReminder(alarm_id, mRowId, title, mCalendar, "On", passing_alarm_manager_id, user_id, task_type, task_owner_id, task_img);
            Toast.makeText(ReminderEditActivity.this, getString(R.string.task_saved_message), Toast.LENGTH_SHORT).show();
            alarm_id = id;

        }

    }



    //send notification for shared task
    void sendNotificationForSavingAlarm(final String reminder_title, final String reminder_time, final String reminder_user_id){
        StringRequest stringRequest = new StringRequest(Request.Method.POST, "https://ityeard.com/Restaurant/shared_task/sendNotification.php",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }){
            @Override
            protected Map<String, String> getParams(){
                Map<String,String> params = new HashMap<String, String>();
                params.put("reminder_task",reminder_title);
                params.put("reminder_date",reminder_time);
                params.put("reminder_user_id",reminder_user_id);
                return params;
            }

        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);

    }





    public void backActivity() {
        if(save_task!=null) {
            if (save_task.equals("edit_my_task")) {
                Intent i = new Intent(ReminderEditActivity.this, MyTaskActivity.class);
                startActivity(i);
                finish();
            }
            else if (save_task.equals("edit_shared_task")) {
                Intent i = new Intent(ReminderEditActivity.this, SharedTaskActivity.class);
                startActivity(i);
                finish();
            }
            else if (save_task.equals("shared_task")) {
                Intent i = new Intent(ReminderEditActivity.this, FriendsSharedTaskActivity.class);
                i.putExtra("friend_id", user_id);
                i.putExtra("friend_timezone", (getIntent().getStringExtra("friend_timezone")));
                startActivity(i);
                finish();
            }else if (save_task.equals("edit_friends_shared_task")) {
                Intent i = new Intent(ReminderEditActivity.this, FriendsSharedTaskActivity.class);
                i.putExtra("friend_id", user_id);
                i.putExtra("friend_timezone", (getIntent().getStringExtra("friend_timezone")));
                startActivity(i);
                finish();
            }
        }
        else {
            Intent i = new Intent(ReminderEditActivity.this, MyTaskActivity.class);
            startActivity(i);
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        backActivity();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
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

















    //start
    //pick user profile picture And resize a large image And finally upload image in firebase storage
    // Start pick image activity with chooser.
    private void chooseImage() {
        CropImage.activity(null).setGuidelines(CropImageView.Guidelines.ON).start(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // handle result of CropImageActivity
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            //now resize image
            //start
            String imagePath = getRealPathFromURI(String.valueOf(result.getUri()));
            Bitmap scaledBitmap = null;
            BitmapFactory.Options options = new BitmapFactory.Options();
            //by setting this field as true, the actual bitmap pixels are not loaded in the memory. Just the bounds are loaded. If
            //you try the use the bitmap here, you will get null.
            options.inJustDecodeBounds = true;
            Bitmap bmp = BitmapFactory.decodeFile(imagePath, options);
            int actualHeight = options.outHeight;
            int actualWidth = options.outWidth;
            //max Height and width values of the compressed image is taken as 816x612
            float maxHeight = 816.0f;
            float maxWidth = 612.0f;
            float imgRatio = actualWidth / actualHeight;
            float maxRatio = maxWidth / maxHeight;
            //width and height values are set maintaining the aspect ratio of the image
            if (actualHeight > maxHeight || actualWidth > maxWidth) {
                if (imgRatio < maxRatio) {
                    imgRatio = maxHeight / actualHeight;
                    actualWidth = (int) (imgRatio * actualWidth);
                    actualHeight = (int) maxHeight;
                } else if (imgRatio > maxRatio) {
                    imgRatio = maxWidth / actualWidth;
                    actualHeight = (int) (imgRatio * actualHeight);
                    actualWidth = (int) maxWidth;
                } else {
                    actualHeight = (int) maxHeight;
                    actualWidth = (int) maxWidth;
                }
            }

            //setting inSampleSize value allows to load a scaled down version of the original image
            options.inSampleSize = calculateInSampleSize(options, actualWidth, actualHeight);
            //inJustDecodeBounds set to false to load the actual bitmap
            options.inJustDecodeBounds = false;
            //this options allow android to claim the bitmap memory if it runs low on memory
            options.inPurgeable = true;
            options.inInputShareable = true;
            options.inTempStorage = new byte[16 * 1024];
            try {
                //load the bitmap from its path
                bmp = BitmapFactory.decodeFile(imagePath, options);
            } catch (OutOfMemoryError exception) {
                exception.printStackTrace();
            }
            try {
                scaledBitmap = Bitmap.createBitmap(actualWidth, actualHeight, Bitmap.Config.ARGB_8888);
            } catch (OutOfMemoryError exception) {
                exception.printStackTrace();
            }
            float ratioX = actualWidth / (float) options.outWidth;
            float ratioY = actualHeight / (float) options.outHeight;
            float middleX = actualWidth / 2.0f;
            float middleY = actualHeight / 2.0f;
            Matrix scaleMatrix = new Matrix();
            scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);
            Canvas canvas = new Canvas(scaledBitmap);
            canvas.setMatrix(scaleMatrix);
            canvas.drawBitmap(bmp, middleX - bmp.getWidth() / 2, middleY - bmp.getHeight() / 2, new Paint(Paint.FILTER_BITMAP_FLAG));
            //check the rotation of the image and display it properly
            ExifInterface exif;
            try {
                exif = new ExifInterface(imagePath);
                int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 0);
                Log.d("EXIF", "Exif: " + orientation);
                Matrix matrix = new Matrix();
                if (orientation == 6) {
                    matrix.postRotate(90);
                    Log.d("EXIF", "Exif: " + orientation);
                } else if (orientation == 3) {
                    matrix.postRotate(180);
                    Log.d("EXIF", "Exif: " + orientation);
                } else if (orientation == 8) {
                    matrix.postRotate(270);
                    Log.d("EXIF", "Exif: " + orientation);
                }
                scaledBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);
            } catch (IOException e) {
                e.printStackTrace();
            }
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
            String path = MediaStore.Images.Media.insertImage(getApplicationContext().getContentResolver(), scaledBitmap, "Title", null);
            //end
            filePath = Uri.parse(path);
            imageView_task_img.setImageURI(Uri.parse(path));
            PICK_IMAGE_REQUEST = 1;
        } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
            Toast.makeText(this, "Please try again!", Toast.LENGTH_LONG).show();
        }
    }
    private String getRealPathFromURI(String contentURI) {
        Uri contentUri = Uri.parse(contentURI);
        Cursor cursor = getContentResolver().query(contentUri, null, null, null, null);
        if (cursor == null) {
            return contentUri.getPath();
        } else {
            cursor.moveToFirst();
            int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            return cursor.getString(index);
        }
    }
    public int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        final float totalPixels = width * height;
        final float totalReqPixelsCap = reqWidth * reqHeight * 2;
        while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
            inSampleSize++;
        }
        return inSampleSize;
    }

    //now uploading image into firebase storage
    public void uploadImage() {
        if(PICK_IMAGE_REQUEST==1) {
            if (filePath != null) {
                progressDialog.show();
                StorageReference ref = storageReference.child("Task Image/" + alarm_id);
                ref.putFile(filePath)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                //getting image url and then inserting task image url into firebase database
                                task_img = taskSnapshot.getDownloadUrl().toString();
                                databaseReference.child(alarm_id).child("taskImage").setValue(task_img);
                                progressDialog.dismiss();
                                setResult(RESULT_OK);
                                backActivity();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                progressDialog.dismiss();
                                Toast.makeText(ReminderEditActivity.this, "Error in uploading Task image!", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                                // Setting progressDialog Title.
                                double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot
                                        .getTotalByteCount());
                                progressDialog.setMessage("Task image is uploading..... "+(int)progress+"%");
                            }
                        });
            }
        }else{
            databaseReference.child(alarm_id).child("taskImage").setValue(task_img);
            setResult(RESULT_OK);
            backActivity();
        }
    }





    // Called when the 'storage permission' function is triggered.
    //start
    public void takingStoragePermission() {
        // Check if the READ_PHONE_STATE permission is already available.
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // READ_PHONE_STATE permission has not been granted.
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
                //Re-request if the permission was not granted.If the user has previously denied the permission.
                ActivityCompat.requestPermissions(ReminderEditActivity.this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_PHONE_STATE);
            } else {
                // READ_PHONE_STATE permission has not been granted yet. Request it directly.
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_PHONE_STATE);
            }
        } else {
            // READ_PHONE_STATE permission is already been granted.
        }
    }
    // Callback received when a permissions request has been completed.
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == MY_PERMISSIONS_REQUEST_READ_PHONE_STATE) {
            // Check if the only required permission has been granted
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // READ_PHONE_STATE permission has been granted
            }else {
                new android.support.v7.app.AlertDialog.Builder(ReminderEditActivity.this)
                        .setTitle("Permission Request")
                        .setMessage("If you don't give permission,then you can't upload task picture! So please give permission")
                        .setCancelable(false)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                takingStoragePermission();
                            }
                        }).show();
            }
        }
    }
    //end


}
