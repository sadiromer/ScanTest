package com.example.android.scantest;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.widget.TextView;

import java.util.ArrayList;

public class Main2Activity extends AppCompatActivity {

    public ArrayList<String> StringResults = new ArrayList<String>();
    public int sizeScan = 0;
    public ArrayList<String> FinalString = new ArrayList<String>();
    public int Phase = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        StringResults =  (ArrayList<String>)getIntent().getSerializableExtra("FILES_TO_SEND");

        TextView displayView = (TextView) findViewById(R.id.base64text);
        displayView.setMovementMethod(new ScrollingMovementMethod());
        displayView.setText(String.valueOf(StringResults));

        sizeScan = StringResults.size();
        TextView displayView2 = (TextView) findViewById(R.id.base64details);
        displayView2.setText(String.valueOf(sizeScan));

    switch (Phase) {
        case 1:
            for (int i = 0; i < sizeScan; i++) {
                if (!(StringResults.get(i).equals("START"))) {
                    if (!(StringResults.get(i).equals(StringResults.get(i - 1)))) {
                        FinalString.add(StringResults.get(i));



                        if (StringResults.get(i).equals("START")) {
                            FinalString.remove(i);
                            Phase = 2;


                        }
                    }

                }

            }
        case 2:
            TextView displayView3 = (TextView) findViewById(R.id.base64details2);
            displayView3.setMovementMethod(new ScrollingMovementMethod());
            displayView3.setText(String.valueOf(FinalString));
            
        default:
    }



    }//onCreate
}//Main2Activity
