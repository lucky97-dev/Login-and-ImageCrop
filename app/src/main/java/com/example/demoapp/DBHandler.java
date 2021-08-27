package com.example.demoapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;


public class DBHandler {
    myDbHelper dbHelper;
    public DBHandler(Context context) {
        dbHelper = new myDbHelper(context);
    }

    public void saveUserData(String name,String mobileNumber,String email,String password) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("user_name", name);
        contentValues.put("user_mobile",mobileNumber);
        contentValues.put("email_id",email);
        contentValues.put("password",password);
        db.insert(dbHelper.TABLE_NAME, null , contentValues);
    }
    public void setImage(String id , byte[] image) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("user_id", id);
        contentValues.put("image",image);

        String[] whereArgs= {id};
        int count =db.update(dbHelper.TABLE_NAME2,contentValues, "user_id = ?",whereArgs );
        if(count <= 0) {
            long a = db.insert(dbHelper.TABLE_NAME2, null, contentValues);
        }

    }

    public void setUserDetails(String id,String state_name, String dist_name,
                               String villagePresent, String postPresent,
                               String policePresent, String pinPresent,
                               String textFatherName, String textMotherName,
                               String gender_name,String marital_status_string, String msg) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("id", id);
        contentValues.put("state",state_name);
        contentValues.put("dist",dist_name);
        contentValues.put("village_present",villagePresent);
        contentValues.put("post_present",postPresent);
        contentValues.put("police_present",policePresent);
        contentValues.put("pin_present",pinPresent);
        contentValues.put("father_name",textFatherName);
        contentValues.put("mother_name",textMotherName);
        contentValues.put("gender",gender_name);
        contentValues.put("marital_status",marital_status_string);
        contentValues.put("hobbies",msg);

        String[] whereArgs= {id};
        int count =db.update(dbHelper.TABLE_NAME_3,contentValues, "id = ?",whereArgs );
        if(count <= 0) {
            long a = db.insert(dbHelper.TABLE_NAME_3, null, contentValues);
        }

    }

    public Bitmap getImage(String id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor mCount= db.rawQuery("select * from " + dbHelper.TABLE_NAME2 + "  where user_id = " + id  , null);

        while (mCount.moveToNext()) {
            byte[] imageByte = mCount.getBlob(mCount.getColumnIndex("image"));
            Bitmap decodedImage = BitmapFactory.decodeByteArray(imageByte, 0, imageByte.length);
            return decodedImage;
        }
        return null;
    }

    public int checkUserData(String email,String pass) {
        sharedPreference sharedPreference = new sharedPreference(dbHelper.context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor mCount= db.rawQuery("select * from " + dbHelper.TABLE_NAME + "  where email_id = '" + email + "'" + " and password = '" + pass + "'", null);
        int count = 0;
        if (mCount.getCount() <= 0) {
            return 0;
        }
        while (mCount.moveToNext()) {
            sharedPreference.setLoginUser(mCount.getString(mCount.getColumnIndex("id")),
                    mCount.getString(mCount.getColumnIndex("user_name")),
                    mCount.getString(mCount.getColumnIndex("user_mobile")),
                    mCount.getString(mCount.getColumnIndex("email_id")));
            count += 1;
        }
        mCount.close();
        return count;

    }

    static class myDbHelper extends SQLiteOpenHelper {
        private static final String DATABASE_NAME = "myDatabase";
        private static final String TABLE_NAME = "user";
        private static final String TABLE_NAME2 = "user_image";
        private static final String TABLE_NAME_3 = "user_details";
        private static final int DATABASE_Version = 1;
        private static final String CREATE_TABLE = "CREATE TABLE "+TABLE_NAME+
                " ( id INTEGER PRIMARY KEY AUTOINCREMENT, user_name VARCHAR(50),user_mobile VARCHAR(15),email_id VARCHAR(25), password VARCHAR(20));";
        private static final String CREATE_TABLE2 = "CREATE TABLE "+TABLE_NAME2+ "( id INTEGER PRIMARY KEY AUTOINCREMENT,user_id VARCHAR(20) , image BLOB);";
        private static final String CREATE_TABLE_3 = "CREATE TABLE "+TABLE_NAME_3+ "( id INTEGER,state VARCHAR(20) ,dist VARCHAR(20) ,village_present VARCHAR(20)" +
                ",post_present VARCHAR(20) ,police_present VARCHAR(20) ,pin_present VARCHAR(20) ,father_name VARCHAR(20) ,mother_name VARCHAR(20)," +
                "gender VARCHAR(10),marital_status VARCHAR(10)" +
                ",hobbies VARCHAR(100));";
        private static final String DROP_TABLE ="DROP TABLE IF EXISTS "+TABLE_NAME;
        private static final String DROP_TABLE2 ="DROP TABLE IF EXISTS "+TABLE_NAME2;
        private static final String DROP_TABLE_3 ="DROP TABLE IF EXISTS "+TABLE_NAME_3;

        private Context context;

        public myDbHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_Version);
            this.context=context;
        }

        public void onCreate(SQLiteDatabase db) {
            try {
                db.execSQL(CREATE_TABLE);
                db.execSQL(CREATE_TABLE2);
                db.execSQL(CREATE_TABLE_3);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            try {
                db.execSQL(DROP_TABLE);
                db.execSQL(DROP_TABLE2);
                db.execSQL(DROP_TABLE_3);
                onCreate(db);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
