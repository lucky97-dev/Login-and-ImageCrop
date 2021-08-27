package com.example.demoapp;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.demoapp.crop.CropImage;
import com.example.demoapp.crop.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;

public class HomeActivity extends AppCompatActivity {
    TextView userName,userEmailId,userMobile;

     TextView state, districts,vill,post,police,pin,textFatherName,textMotherName,gender, marital_status,hobbies;
    sharedPreference sharedPreference;
    CircleImageView userImage;
    DBHandler handler;
    Button addDetails;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar mToolbar = findViewById(R.id.login_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Dashboard");
        userName = findViewById(R.id.userName);
        userEmailId = findViewById(R.id.userEmailId);
        userMobile = findViewById(R.id.userMobile);
        addDetails = findViewById(R.id.addDetails);
        state = findViewById(R.id.state);
        districts = findViewById(R.id.districts);
        vill = findViewById(R.id.vill);
        post = findViewById(R.id.post);
        police = findViewById(R.id.police);
        pin = findViewById(R.id.pin);
        textFatherName = findViewById(R.id.textFatherName);
        textMotherName = findViewById(R.id.textMotherName);
        gender = findViewById(R.id.gender);
        marital_status = findViewById(R.id.marital_status);
        hobbies = findViewById(R.id.hobbies);
        handler = new DBHandler(this);
        userDetails();
        sharedPreference = new sharedPreference(this);
        userName.setText(sharedPreference.getName());
        userEmailId.setText(sharedPreference.getEmail());
        userMobile.setText(sharedPreference.getMobile());
        userImage = findViewById(R.id.userImage);
        try {
             Bitmap image = handler.getImage(sharedPreference.getId());
             if (image != null) {
                 userImage.setImageBitmap(image);
             }
        } catch (Exception e) {
            e.printStackTrace();
        }
        addDetails.setOnClickListener(v -> {
            startActivity(new Intent(HomeActivity.this,AddDetails.class));
        });

        userImage.setOnClickListener(v -> CropImage.activity(null).setGuidelines(CropImageView.Guidelines.ON).start( HomeActivity.this));
    }
    public void userDetails() {
        Cursor cursor = handler.getUserDetails(sharedPreference.getId());
        while (cursor.moveToNext()) {
            state.setText(cursor.getString(cursor.getColumnIndex("state")));
            districts.setText(cursor.getString(cursor.getColumnIndex("dist")));
            vill.setText(cursor.getString(cursor.getColumnIndex("village_present")));
            post.setText(cursor.getString(cursor.getColumnIndex("post_present")));
            police.setText(cursor.getString(cursor.getColumnIndex("police_present")));
            pin.setText(cursor.getString(cursor.getColumnIndex("pin_present")));
            textFatherName.setText(cursor.getString(cursor.getColumnIndex("father_name")));
            textMotherName.setText(cursor.getString(cursor.getColumnIndex("mother_name")));
            gender.setText(cursor.getString(cursor.getColumnIndex("gender")));
            marital_status.setText(cursor.getString(cursor.getColumnIndex("marital_status")));
            hobbies.setText(cursor.getString(cursor.getColumnIndex("hobbies")));
        }
    }

    @Override
    protected void onRestart() {
        userDetails();
        super.onRestart();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        String imageString;
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Bitmap bitmap = null;
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), result.getUri());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ByteArrayOutputStream byteArrayOS = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOS);
                byte[] toByteArray = byteArrayOS.toByteArray();
                imageString = Base64.encodeToString(toByteArray, Base64.DEFAULT);
                byte[] imageBytes1 = Base64.decode(imageString, Base64.DEFAULT);
                Bitmap decodedImage = BitmapFactory.decodeByteArray(imageBytes1, 0, imageBytes1.length);

                userImage.setImageBitmap(decodedImage);
                handler.setImage(sharedPreference.getId(),imageBytes1);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Toast.makeText(this, "failed: " + result.getError(), Toast.LENGTH_LONG).show();
            }
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.more_tab_menu, menu);
        return super.onCreateOptionsMenu(menu);

    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId())
        {
            case R.id.logout:
                sharedPreference.removeUsers();
                startActivity(new Intent(HomeActivity.this,loginActivity.class));
                finishAffinity();
                break;
        }
        return true;
    }

}