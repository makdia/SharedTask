package com.example.computergallery.alarm;

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
import android.graphics.drawable.ColorDrawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
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
import java.util.HashMap;
import java.util.Map;

public class UpdateInfoActivity extends AppCompatActivity {

    DatabaseReference databaseReference;
    EditText editText_username;
    TextView textView_userid;
    TextView textView_key;
    SharedPreferences sharedPreferences_for_user_info;
    boolean isLoggedIn;
    String user_id,user_name,key,IMEINumber,user_profile_pic;
    String old_id,old_user_id,old_user_name,old_key,old_password;
    String value;
    private Button btnChoose;
    private ImageView imageView;
    private Uri filePath;
    private int PICK_IMAGE_REQUEST = 0;
    //Firebase
    FirebaseStorage storage;
    StorageReference storageReference;
    ProgressDialog progressDialog;
    private static final int MY_PERMISSIONS_REQUEST_READ_PHONE_STATE = 0;
    ImageView img_show_password;
    boolean show_password=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_info);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorAccent)));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getSupportActionBar().setElevation(0);
        }

        sharedPreferences_for_user_info = getSharedPreferences("login", MODE_PRIVATE);
        isLoggedIn = sharedPreferences_for_user_info.getBoolean("login", false);


        editText_username = (EditText) findViewById(R.id.user_name);
        textView_userid = (TextView) findViewById(R.id.user_id);
        textView_key = (TextView) findViewById(R.id.key);
        btnChoose = (Button) findViewById(R.id.btnChoose);
        imageView = (ImageView) findViewById(R.id.profile_pic);
        img_show_password = (ImageView) findViewById(R.id.imageView_show_password);


        //for updating user profile info
        if (isLoggedIn != true) {
            Intent intent = new Intent(getApplicationContext(), OldUserLoginOptionActivity.class);
            startActivity(intent);
            finish();
        } else if (isLoggedIn == true) {
            old_id = sharedPreferences_for_user_info.getString("unique_id", "null");
            old_user_id = sharedPreferences_for_user_info.getString("user_id", "null");
            old_user_name = sharedPreferences_for_user_info.getString("user_name", "null");
            old_key = sharedPreferences_for_user_info.getString("key", "null");
            old_password = sharedPreferences_for_user_info.getString("password", "null");
            IMEINumber = sharedPreferences_for_user_info.getString("device_imei", "null");
            user_profile_pic = sharedPreferences_for_user_info.getString("profile_pic", "null");
            editText_username.setText(old_user_name);
            textView_userid.setText(old_user_id);
            textView_key.setText(old_key);
            //Picasso.with(this).load(user_profile_pic).error(R.drawable.profilepic).into(imageView);
            Picasso.with(this).load(user_profile_pic).placeholder(R.drawable.proficon).into(imageView);
            storage = FirebaseStorage.getInstance();
            storageReference = storage.getReference();
            progressDialog = new ProgressDialog(UpdateInfoActivity.this);
            progressDialog.setTitle("Please wait");
            progressDialog.setCancelable(false);
            imageView.setOnClickListener(new View.OnClickListener() {
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
    }



    //show password
    public void showPassword(View view) {
        if (show_password == false) {
            // show password
            textView_key.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            img_show_password.setBackgroundResource(R.drawable.show_password);
            show_password=true;
        } else {
            // hide password
            textView_key.setTransformationMethod(PasswordTransformationMethod.getInstance());
            img_show_password.setBackgroundResource(R.drawable.hide_password);
            show_password=false;
        }
    }



    //save user info
    public void saveUserInfo(View view) {
        final Animation myAnim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.bounce);
        view.startAnimation(myAnim);
        user_name = editText_username.getText().toString();
        if (!user_name.isEmpty()) {
            if (storingUserInfo(old_user_id, user_name, old_key, old_password, IMEINumber) == true) {
                uploadImage();
            }
        } else {
            Toast.makeText(getApplicationContext(), "Please enter your user name", Toast.LENGTH_LONG).show();
        }

    }



    //storing User Info Into Firebase Database
    public boolean storingUserInfo(String id,String name,String key,String password,String IMEI){
        //getting the specified user reference
        DatabaseReference dR = FirebaseDatabase.getInstance().getReference("User").child(id);
        //updating user info
        Map<String, Object> value = new HashMap<>();
        value.put("user_id", id);
        value.put("user_name", name);
        value.put("key", key);
        value.put("password", password);
        value.put("device_imei", IMEI);
        dR.setValue(value);
        SharedPreferences.Editor editor = sharedPreferences_for_user_info.edit();
        editor.putBoolean("login", true);
        editor.putString("unique_id", id);
        editor.putString("user_id", id);
        editor.putString("user_name", name);
        editor.putString("key", key);
        editor.putString("password", password);
        editor.putString("device_imei", IMEI);
        editor.commit();
        return true;
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
            imageView.setImageURI(Uri.parse(path));
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
    public boolean uploadImage() {
        if(PICK_IMAGE_REQUEST==1) {
            if (filePath != null) {
                progressDialog.show();
                //At first delete user current profile pic and then uploading user new profile pic
                //Deleting user current profile pic
                if(user_profile_pic.isEmpty()) {
                    if (!storage.getReferenceFromUrl(user_profile_pic).equals(null)) {
                        StorageReference photoRef = storage.getReferenceFromUrl(user_profile_pic);
                        photoRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                // File deleted successfully
                                Log.d("Deleted............", "onSuccess: deleted file");
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                // Uh-oh, an error occurred!
                                Log.d("Error............", "onFailure: did not delete file");
                            }
                        });
                    }
                }
                // After deleting pic successfully now storing new profile picture
                StorageReference ref = storageReference.child("User Profile Pic/" + old_user_id);
                ref.putFile(filePath)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                //getting image url and then inserting user profile pic url into firebase database
                                user_profile_pic = taskSnapshot.getDownloadUrl().toString();
                                progressDialog.dismiss();
                                DatabaseReference dR = FirebaseDatabase.getInstance().getReference("User").child(old_user_id);
                                dR.child("profile_pic").setValue(user_profile_pic);
                                SharedPreferences.Editor editor = sharedPreferences_for_user_info.edit();
                                editor.putString("profile_pic", user_profile_pic);
                                editor.commit();
                                Toast.makeText(getApplicationContext(), "Your info successfully updated", Toast.LENGTH_LONG).show();
                                Intent intent = new Intent(getApplicationContext(), RegistrationActivity.class);
                                intent.putExtra("update", "update_user_info");
                                startActivity(intent);
                                finish();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                progressDialog.dismiss();
                                Toast.makeText(UpdateInfoActivity.this, "Error in uploading profile picture!", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                                // Setting progressDialog Title.
                                double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot
                                        .getTotalByteCount());
                                progressDialog.setMessage("Your Profile uploading..... "+(int)progress+"%");
                            }
                        });
            }
        }else{
            DatabaseReference dR = FirebaseDatabase.getInstance().getReference("User").child(old_user_id);
            dR.child("profile_pic").setValue(user_profile_pic);
            SharedPreferences.Editor editor = sharedPreferences_for_user_info.edit();
            editor.putString("profile_pic", user_profile_pic);
            editor.commit();
            Toast.makeText(getApplicationContext(), "Your info successfully updated", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(getApplicationContext(), RegistrationActivity.class);
            intent.putExtra("update", "update_user_info");
            startActivity(intent);
            finish();
        }
        return true;
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
                ActivityCompat.requestPermissions(UpdateInfoActivity.this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_PHONE_STATE);
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
                new android.support.v7.app.AlertDialog.Builder(UpdateInfoActivity.this)
                        .setTitle("Permission Request")
                        .setMessage("If you don't give permission,then you can't upload your profile picture! So please give permission")
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
