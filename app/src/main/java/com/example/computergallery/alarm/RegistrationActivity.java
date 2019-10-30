package com.example.computergallery.alarm;

import android.*;
import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;

import android.app.ProgressDialog;
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
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Environment;
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

//import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.TimeZone;

/**
 * Created by Makdia Hussain on 2/5/2018.
 */
public class RegistrationActivity extends AppCompatActivity {
    DatabaseReference databaseReference;
    EditText editText_username, editText_userpassword;
    TextView textView_userid;
    TextView textView_key;
    SharedPreferences sharedPreferences_for_user_info;
    boolean isLoggedIn;
    String user_id, user_name, key, user_password;
    String value;
    int u_id;
    String user_timeZone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        sharedPreferences_for_user_info = getSharedPreferences("login", MODE_PRIVATE);
        isLoggedIn = sharedPreferences_for_user_info.getBoolean("login", false);

        editText_username = (EditText) findViewById(R.id.user_name);
        editText_userpassword = (EditText) findViewById(R.id.user_password);
        textView_userid = (TextView) findViewById(R.id.user_id);
        textView_key = (TextView) findViewById(R.id.key);


        //for user registration
        if (isLoggedIn == true) {
            Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
            startActivity(intent);
            finish();
        } else {
            //database reference object
            databaseReference = FirebaseDatabase.getInstance().getReference("User");
            Query query = databaseReference.orderByKey().limitToLast(1);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getChildrenCount() == 0) {
                        u_id = 00001;
                        textView_userid.setText(String.valueOf(String.format("%05d", u_id)));
                    } else {
                        for (DataSnapshot child : dataSnapshot.getChildren()) {
                            int last_id = Integer.parseInt(child.getKey());
                            u_id = last_id + 1;
                            textView_userid.setText(String.valueOf(String.format("%05d", u_id)));
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            Random rand = new Random();
            int n = 100000 + rand.nextInt(900000);
            key = String.valueOf(n);
            textView_key.setText(key);

            TimeZone timeZone = TimeZone.getDefault();
            user_timeZone = timeZone.getID();  // "It takes user device time zone"

        }
    }


    //create Account
    public void createAccount(View view) {
        final Animation myAnim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.bounce);
        view.startAnimation(myAnim);

        user_name = editText_username.getText().toString();
        user_password = editText_userpassword.getText().toString();
        if (user_password.isEmpty()) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this, R.style.AppCompatAlertDialogStyle);
            alertDialogBuilder.setMessage("If you don't give password, then you can't login into this app from another device! Do you want to give password?");
            alertDialogBuilder.setCancelable(false);
            // set positive button YES message
            alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            // set neutral button OK
            alertDialogBuilder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // Dismiss the dialog
                    dialog.dismiss();
                    if (!user_name.isEmpty()) {
                        if (storingUserInfo(String.valueOf(String.format("%05d", u_id)), user_name, key, "blank", OldUserLoginOptionActivity.IMEINumber) == true) {
                            Toast.makeText(getApplicationContext(), "Congrats!Your account has been created successfully!", Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    } else {
                        Toast.makeText(getApplicationContext(), "Please enter your user name", Toast.LENGTH_LONG).show();
                    }

                }
            });
            AlertDialog alertDialog = alertDialogBuilder.create();
            // show alert
            alertDialog.show();
        } else {
            if (!user_name.isEmpty()) {
                if (storingUserInfo(String.valueOf(String.format("%05d", u_id)), user_name, key, getMD5(user_password), OldUserLoginOptionActivity.IMEINumber) == true) {
                    Toast.makeText(getApplicationContext(), "Congrats!Your account has been created successfully!", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                    startActivity(intent);
                    finish();
                }
            } else {
                Toast.makeText(getApplicationContext(), "Please enter your user name", Toast.LENGTH_LONG).show();
            }
        }
    }


    //storing User Info Into Firebase Database
    public boolean storingUserInfo(String id, String name, String key, String password, String IMEI) {
        //getting the specified user reference
        DatabaseReference dR = FirebaseDatabase.getInstance().getReference("User").child(id);
        //updating user info
        Map<String, Object> value = new HashMap<>();
        value.put("user_id", id);
        value.put("user_name", name);
        value.put("key", key);
        value.put("password", password);
        value.put("device_imei", IMEI);
        value.put("user_timezone", user_timeZone);
        dR.setValue(value);
        SharedPreferences.Editor editor = sharedPreferences_for_user_info.edit();
        editor.putBoolean("login", true);
        editor.putString("unique_id", id);
        editor.putString("user_id", id);
        editor.putString("user_name", name);
        editor.putString("key", key);
        editor.putString("password", password);
        editor.putString("device_imei", IMEI);
        editor.putString("profile_pic", null);
        editor.commit();
        return true;
    }


    public static String getMD5(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(password.getBytes());
            BigInteger number = new BigInteger(1, messageDigest);
            String hashtext = number.toString(16);
            // Now we need to zero pad it if you actually want the full 32 chars.
            while (hashtext.length() < 32) {
                hashtext = "0" + hashtext;
            }
            return hashtext;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(this, LoginActivity.class));
        finish();

    }

}
