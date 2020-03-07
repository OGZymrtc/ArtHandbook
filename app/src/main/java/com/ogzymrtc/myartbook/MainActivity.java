package com.ogzymrtc.myartbook;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    Button button1,button2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button1 = findViewById(R.id.startButton);
        button2 = findViewById(R.id.aboutButton);
    }
    public void startButtonClick(View view ){
        Intent intent = new Intent(MainActivity.this, ListActivity.class);
        startActivity(intent);
    }
    public void aboutButtonClick(View view ){
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("About");
        alert.setMessage("This application was made Necati Oguzhan YUMURTACI to save the historical artifacts you are interested in,get information about it remind you.\n\n\nReleased on:27 December 2019\n\nUpdate on :5 March 2020\n\nVersion:1.3.0");
        alert.show();


    }
}
