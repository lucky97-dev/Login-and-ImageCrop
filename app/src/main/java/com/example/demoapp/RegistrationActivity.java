package com.example.demoapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class RegistrationActivity extends AppCompatActivity {
    Button registration;
    EditText name,mobileNumber,emailId,password;
    DBHandler dbHandler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        Toolbar mToolbar = findViewById(R.id.login_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Registration");
        dbHandler = new DBHandler(this);
        name = findViewById(R.id.userName);
        mobileNumber = findViewById(R.id.mobileNumber);
        emailId = findViewById(R.id.emailId);
        password = findViewById(R.id.password);
        registration = findViewById(R.id.registration);
        registration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(name.getText().toString().length() != 0){
                    if(mobileNumber.getText().toString().length() == 10 && isValidPhoneNumber(mobileNumber.getText().toString())){
                        if(Patterns.EMAIL_ADDRESS.matcher(emailId.getText().toString()).matches()) {
                            if(password.getText().toString().length() >= 8){
                                dbHandler.saveUserData(name.getText().toString(),mobileNumber.getText().toString(),
                                        emailId.getText().toString(),password.getText().toString());
                                startActivity(new Intent(getApplicationContext(),loginActivity.class));
                                finishAffinity();
                            } else {
                                password.setError("Use 8 or more characters with a mix of letters, numbers & symbols");
                            }
                        } else {

                        }
                    } else {
                       mobileNumber.setError("Enter valid mobile number");
                    }
                } else {
                    name.setError("Enter User Name");
                }

            }
        });
    }
    public Boolean isValidPhoneNumber(String s) {
        String  patterns="^[2-9][0-9]{9}$";
        return  s.matches(patterns);
    }
}