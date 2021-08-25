package com.example.demoapp;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
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
        handler = new DBHandler(this);
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