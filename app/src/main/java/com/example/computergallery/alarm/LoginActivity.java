package com.example.computergallery.alarm;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class LoginActivity extends AppCompatActivity {

    DatabaseReference databaseReference;
    EditText editText_userid, editText_password;
    SharedPreferences sharedPreferences_for_user_info;
    boolean isLoggedIn;
    String user_id, user_name, key, password, user_profile_pic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        editText_userid = (EditText) findViewById(R.id.user_id);
        editText_password = (EditText) findViewById(R.id.password);

        sharedPreferences_for_user_info = getSharedPreferences("login", MODE_PRIVATE);
        isLoggedIn = sharedPreferences_for_user_info.getBoolean("login", false);

        if (isLoggedIn == true) {
            Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
            startActivity(intent);
            finish();
        }

    }




    //login
    public void login(View view) {
        final Animation myAnim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.bounce);
        view.startAnimation(myAnim);
        user_id = editText_userid.getText().toString();
        password = editText_password.getText().toString();
        if (!user_id.isEmpty() && !password.isEmpty()) {
            DatabaseReference dR = FirebaseDatabase.getInstance().getReference("User");
            Query query = dR.orderByChild("user_id").equalTo(user_id);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.child(user_id).exists()) {
                        for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                            user_name = String.valueOf(postSnapshot.child("user_name").getValue());
                            key = String.valueOf(postSnapshot.child("key").getValue());
                            String user_password = String.valueOf(postSnapshot.child("password").getValue());
                            String device_imei = String.valueOf(postSnapshot.child("device_imei").getValue());
                            user_profile_pic = String.valueOf(postSnapshot.child("profile_pic").getValue());

                            if (getMD5(password).equals(user_password)) {
                                SharedPreferences.Editor editor = sharedPreferences_for_user_info.edit();
                                editor.putBoolean("login", true);
                                editor.putString("unique_id", user_id);
                                editor.putString("user_id", user_id);
                                editor.putString("user_name", user_name);
                                editor.putString("key", key);
                                editor.putString("password", user_password);
                                editor.putString("device_imei", OldUserLoginOptionActivity.IMEINumber);
                                editor.putString("profile_pic", user_profile_pic);
                                editor.commit();
                                if (device_imei.isEmpty() || !device_imei.equals(OldUserLoginOptionActivity.IMEINumber)) {
                                    DatabaseReference dR = FirebaseDatabase.getInstance().getReference("User").child(user_id);
                                    dR.child("device_imei").setValue(OldUserLoginOptionActivity.IMEINumber);
                                }
                                Toast.makeText(getApplicationContext(), "Login success!", Toast.LENGTH_LONG).show();
                                Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(getApplicationContext(), "Please enter valid password!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    } else {
                        Toast.makeText(getApplicationContext(), "Please enter valid user id!", Toast.LENGTH_SHORT).show();
                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        } else {
            Toast.makeText(getApplicationContext(), "Please fill up login requirements!", Toast.LENGTH_LONG).show();
        }
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



    //registration
    public void registration(View view) {
        Intent intent = new Intent(getApplicationContext(), RegistrationActivity.class);
        startActivity(intent);
        finish();
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(this, OldUserLoginOptionActivity.class));
        finish();
    }


}