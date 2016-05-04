package com.example.android.scantest;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Base64;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {


    //Variables
    public ArrayList<String> StringResults = new ArrayList<String>();
    public int sizeScan = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        StringResults =  (ArrayList<String>)getIntent().getSerializableExtra("FILES_TO_SEND");

        TextView displayView = (TextView) findViewById(R.id.base64text);
        displayView.setMovementMethod(new ScrollingMovementMethod());
        displayView.setText(String.valueOf(StringResults));

       // sizeScan = StringResults.size();
        TextView displayView2 = (TextView) findViewById(R.id.base64details);
        displayView2.setText(String.valueOf(sizeScan));

    }//onCreate


    //Functions

    //The following is to decode. CAn use it in the decoding part
    public static Bitmap decodeBase64(String input) {
        byte[] decodedByte = Base64.decode(input, 0);
        return BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.length);
    }

   //Start the scan activity
    public void scanButton(View view) {
        Intent intent = new Intent(MainActivity.this, CameraActivity.class);
        startActivity(intent);
    }

}//MainActivity