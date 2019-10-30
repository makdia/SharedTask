package com.example.computergallery.alarm;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.LinearLayout;
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
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class ChangePasswordActivity extends AppCompatActivity {

    EditText editText_current_password,editText_new_password,editText_confirm_password;
    String current_password,new_password,confirm_password;
    String value,user_id;
    SharedPreferences sharedPreferences_for_user_info;
    DatabaseReference databaseReference;
    LinearLayout lay_old_password,lay_new_password;
    boolean isLoggedIn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorAccent)));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getSupportActionBar().setElevation(0);
        }

        sharedPreferences_for_user_info=getSharedPreferences("login",MODE_PRIVATE);
        isLoggedIn=sharedPreferences_for_user_info.getBoolean("login",false);
        user_id=sharedPreferences_for_user_info.getString("unique_id","null");
        databaseReference = FirebaseDatabase.getInstance().getReference("User").child(user_id);

        if (isLoggedIn != true){
            Intent intent = new Intent(getApplicationContext(), OldUserLoginOptionActivity.class);
            startActivity(intent);
            finish();
        } else if (isLoggedIn == true) {

            lay_old_password = (LinearLayout) findViewById(R.id.lay_old_password);
            lay_new_password = (LinearLayout) findViewById(R.id.lay_new_password);
            editText_current_password = (EditText) findViewById(R.id.current_password);
            editText_new_password = (EditText) findViewById(R.id.new_password);
            editText_confirm_password = (EditText) findViewById(R.id.confirm_password);


            value = getIntent().getStringExtra("update");
            if (value != null) {
                if (value.equals("update_password")) {
                    current_password = sharedPreferences_for_user_info.getString("password", "null");
                    if(current_password.equals("blank")){
                        lay_old_password.setVisibility(View.GONE);
                        lay_new_password.setVisibility(View.VISIBLE);
                    }
                }
            }
        }
    }




    //verifying current password
    public void currentPassword(View view) {
        final Animation myAnim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.bounce);
        view.startAnimation(myAnim);
        String current_password_again = editText_current_password.getText().toString();
        if (value != null) {
            if (value.equals("update_password")) {
                if (!current_password_again.isEmpty()) {
                    if (getMD5(current_password_again).equals(current_password)) {
                        lay_old_password.setVisibility(View.GONE);
                        lay_new_password.setVisibility(View.VISIBLE);
                    }
                    else {
                        Toast.makeText(getApplicationContext(), "Incorrect password!Please enter correct password", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Please enter your current password", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }




    //change password
    public void changePassword(View view) {
        final Animation myAnim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.bounce);
        view.startAnimation(myAnim);
        new_password = editText_new_password.getText().toString();
        confirm_password = editText_confirm_password.getText().toString();
        if (!new_password.isEmpty() || !confirm_password.isEmpty()) {
            if(new_password.equals(confirm_password)) {
                databaseReference.child("password").setValue(getMD5(new_password));
                SharedPreferences.Editor editor = sharedPreferences_for_user_info.edit();
                editor.putString("password", getMD5(new_password));
                editor.commit();
                Toast.makeText(getApplicationContext(), "Your password changed successfully!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                startActivity(intent);
                finish();
            }else{
                Toast.makeText(getApplicationContext(), "Password didn't match!\nPlease enter the same password twice", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getApplicationContext(), "Please enter the same password twice", Toast.LENGTH_SHORT).show();
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



    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(this, HomeActivity.class));
        finish();
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
}
