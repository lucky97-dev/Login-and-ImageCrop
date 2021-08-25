package com.example.demoapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.regex.Pattern;

public class loginActivity extends AppCompatActivity {
    EditText emailId,password;
    Button login,register_button;
    DBHandler dbHandler;
    sharedPreference sharedPreference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sharedPreference = new sharedPreference(this);
        Toolbar mToolbar = findViewById(R.id.login_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("LOG IN");
        dbHandler = new DBHandler(this);
        emailId = findViewById(R.id.emailId);
        password = findViewById(R.id.password);
        login = findViewById(R.id.loginButton);
        if (!sharedPreference.getId().equals("")){
            startActivity(new Intent(getApplicationContext(),HomeActivity.class));
            finish();
        }
        register_button = findViewById(R.id.login_register_button);
        login.setOnClickListener(v -> {
            if (emailId.getText().toString().length() != 0 && Patterns.EMAIL_ADDRESS.matcher(emailId.getText().toString()).matches()) {
                if (password.getText().toString().length() >= 8){
                    int checkUser = dbHandler.checkUserData(emailId.getText().toString(),password.getText().toString());
                    if(checkUser > 0) {
                        startActivity(new Intent(loginActivity.this,HomeActivity.class));
                        Toast.makeText(getApplicationContext(),"login Successful",Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(getApplicationContext(),"User not exist",Toast.LENGTH_SHORT).show();
                    }
                } else {
                    password.setError("Enter valid password");
                }

            } else {
               emailId.setError("Enter valid email Id");
            }

        });
        register_button.setOnClickListener(v -> startActivity(new Intent(loginActivity.this,RegistrationActivity.class)));
    }
}