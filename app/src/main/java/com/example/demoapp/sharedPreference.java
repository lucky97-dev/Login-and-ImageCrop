package com.example.demoapp;

import android.content.Context;
import android.content.SharedPreferences;

public class sharedPreference {
    Context context;
    SharedPreferences sharedPreferences;

    public void setLoginUser(String Id,String name ,String mobile,String emailId) {
        sharedPreferences.edit().putString("id",Id).commit();
        sharedPreferences.edit().putString("name",name).commit();
        sharedPreferences.edit().putString("mobile",mobile).commit();
        sharedPreferences.edit().putString("emailId",emailId).commit();
    }
    public String getId() {
        return  sharedPreferences.getString("id","");
    }

    public String getName() {
        return  sharedPreferences.getString("name","");
    }

    public String getMobile() {
        return  sharedPreferences.getString("mobile","");
    }

    public String getEmail() {
        return  sharedPreferences.getString("emailId","");
    }




    public void removeUsers() {
       sharedPreferences.edit().clear().commit();
    }

    public sharedPreference(Context context) {
        this.context = context;
        sharedPreferences = context.getSharedPreferences("loginUserInfo", Context.MODE_PRIVATE);
    }
}