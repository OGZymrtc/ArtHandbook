package com.ogzymrtc.myartbook;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class AddActivity extends AppCompatActivity {
    Bitmap selectedImage;
    ImageView imageView;
    EditText artNameText,painterNameText,yearText;
    Button button;
    Button deleteButton;
    Button detailButton;
    SQLiteDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);

        imageView = findViewById(R.id.imageView);
        artNameText = findViewById(R.id.artText);
        painterNameText = findViewById(R.id.painterNameText);
        yearText = findViewById(R.id.yearText);
        button = findViewById(R.id.button);
        deleteButton = findViewById(R.id.deleteButton);
        detailButton = findViewById(R.id.detailsButton);

        database = this.openOrCreateDatabase("Arts",MODE_PRIVATE,null);



        Intent intent = getIntent();
        String info = intent.getStringExtra("info");


        //To Understand The Clicked Place(Listview Or Add Art)
        if (info.matches("new")) {
            artNameText.setText("");
            painterNameText.setText("");
            yearText.setText("");
            button.setVisibility(View.VISIBLE);
            detailButton.setVisibility(View.INVISIBLE);
            deleteButton.setVisibility(View.INVISIBLE);
            artNameText.setClickable(true);
            painterNameText.setClickable(true);
            yearText.setClickable(true);


            Bitmap selectImage = BitmapFactory.decodeResource(getApplicationContext().getResources(),R.drawable.click);
            imageView.setImageBitmap(selectImage);


        } else {
            int artId = intent.getIntExtra("artId",1);
            button.setVisibility(View.INVISIBLE);


            try {

                Cursor cursor = database.rawQuery("SELECT * FROM arts WHERE id = ?",new String[] {String.valueOf(artId)});

                int artNameIx = cursor.getColumnIndex("artname");
                int painterNameIx = cursor.getColumnIndex("paintername");
                int yearIx = cursor.getColumnIndex("year");
                int imageIx = cursor.getColumnIndex("image");



                while (cursor.moveToNext()) {

                    artNameText.setText(cursor.getString(artNameIx));
                    painterNameText.setText(cursor.getString(painterNameIx));
                    yearText.setText(cursor.getString(yearIx));



                    byte[] bytes = cursor.getBlob(imageIx);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
                    imageView.setImageBitmap(bitmap);
                    imageView.setClickable(false);


                }

                cursor.close();

            } catch (Exception e) {

            }


        }
    }
    public void detailsClick(View view){
        String find=artNameText.getText().toString();
        Uri uri =Uri.parse("https://www.google.com/search?q="+find);
        Intent intent = new Intent(Intent.ACTION_VIEW,uri);
        startActivity(intent);

    }
    public void deleteClick(View view){

        int dYear=Integer.parseInt(yearText.getText().toString());

        try{
            database = this.openOrCreateDatabase("Arts",MODE_PRIVATE,null);
            database.execSQL("CREATE TABLE IF NOT EXISTS arts (id INTEGER PRIMARY KEY,artname VARCHAR, paintername VARCHAR, year INTEGER, image BLOB)");

            String sqlDeleteString = "DELETE FROM arts WHERE year ="+dYear;
            SQLiteStatement sqLiteStatement = database.compileStatement(sqlDeleteString);
            sqLiteStatement.execute();

        }
        catch (Exception e){

        }
        Intent intent = new Intent(AddActivity.this, ListActivity.class);
        //To Close All Other Open Intents While Inent Is On
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);

    }

    //Permission Check
    public void selectImage(View view) {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[] {Manifest.permission.READ_EXTERNAL_STORAGE},1);
        } else {
            Intent intentToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intentToGallery,2);
        }

    }
    //If Previously Allowed
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent intentToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intentToGallery,2);
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
    //Image Selection In Gallery
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if (requestCode == 2 && resultCode == RESULT_OK && data != null) {

            Uri imageData = data.getData();


            try {

                if (Build.VERSION.SDK_INT >= 28) {

                    ImageDecoder.Source source = ImageDecoder.createSource(this.getContentResolver(),imageData);
                    selectedImage = ImageDecoder.decodeBitmap(source);
                    imageView.setImageBitmap(selectedImage);

                } else {
                    selectedImage = MediaStore.Images.Media.getBitmap(this.getContentResolver(),imageData);
                    imageView.setImageBitmap(selectedImage);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }


        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    public void save(View view) {

        String artName = artNameText.getText().toString();
        String painterName = painterNameText.getText().toString();
        String year = yearText.getText().toString();

        Bitmap smallImage = makeSmallerImage(selectedImage,300);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        smallImage.compress(Bitmap.CompressFormat.PNG,50,outputStream);
        byte[] byteArray = outputStream.toByteArray();

        try {

            database = this.openOrCreateDatabase("Arts",MODE_PRIVATE,null);
            database.execSQL("CREATE TABLE IF NOT EXISTS arts (id INTEGER PRIMARY KEY,artname VARCHAR, paintername VARCHAR, year INTEGER, image BLOB)");


            String sqlString = "INSERT INTO arts (artname, paintername, year, image) VALUES (?, ?, ?, ?)";

            //To Run String In SQL

            SQLiteStatement sqLiteStatement = database.compileStatement(sqlString);
            sqLiteStatement.bindString(1,artName);
            sqLiteStatement.bindString(2,painterName);
            sqLiteStatement.bindString(3,year);
            sqLiteStatement.bindBlob(4,byteArray);
            sqLiteStatement.execute();



        }
        catch (Exception e){

        }
        Intent intent = new Intent(AddActivity.this, ListActivity.class);
        //To Close All Other Open Intents While Inent Is On
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);

        //finish();
    }
    //For Large Sized Images
    public Bitmap makeSmallerImage(Bitmap image, int maximumSize) {

        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float) width / (float) height;

        //If The Images Is Horizontal
        if (bitmapRatio > 1) {
            width = maximumSize;
            height = (int) (width / bitmapRatio);
        }
        //If The Image Is Vertical
        else {
            height = maximumSize;
            width = (int) (height * bitmapRatio);
        }

        return Bitmap.createScaledBitmap(image,width,height,true);
    }


}
