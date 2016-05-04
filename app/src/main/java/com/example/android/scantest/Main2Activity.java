package com.example.android.scantest;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Base64;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class Main2Activity extends AppCompatActivity {

    public ArrayList<String> StringResults = new ArrayList<String>();
    public int sizeScan = 0;
    public ArrayList<String> FinalString = new ArrayList<String>();
    public int Phase = 1;
    public String input = "";
    public Bitmap Image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        StringResults = (ArrayList<String>) getIntent().getSerializableExtra("FILES_TO_SEND");

        TextView displayView = (TextView) findViewById(R.id.base64details2);
        displayView.setMovementMethod(new ScrollingMovementMethod());
        displayView.setMaxLines(3);
        displayView.setText(String.valueOf(StringResults));

        sizeScan = StringResults.size();
        TextView displayView2 = (TextView) findViewById(R.id.base64details);
        displayView2.setMovementMethod(new ScrollingMovementMethod());
        displayView2.setText(String.valueOf(sizeScan));

        switch (Phase) {
            case 1:
                for (int i = 0; i < sizeScan; i++) {
                    if (!(StringResults.get(i).equals("START"))) {
                        if (!(StringResults.get(i).equals(StringResults.get(i - 1)))) {
                            FinalString.add(StringResults.get(i));

                            if (StringResults.get(i).equals("START")) {
                                for (int j = 0; j < i; j++) {
                                    input += FinalString.get(j);
                                }

                                Phase = 2;


                            }
                        }

                    }

                }
            case 2:
                TextView displayView3 = (TextView) findViewById(R.id.base64text);
                displayView3.setMovementMethod(new ScrollingMovementMethod());
                displayView3.setMaxLines(3);
                displayView3.setText(String.valueOf(FinalString));


                //Bitmap Image = decodeBase64(input);

                byte[] decodedByte = Base64.decode(input, 0);
                Image = BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.length);

                //Set it in ImageView
                ImageView QRView = (ImageView) findViewById(R.id.image_holder);
                QRView.setImageBitmap(Image);

                int size = FinalString.size();


                TextView displayView4 = (TextView) findViewById(R.id.sizeString);
                displayView4.setMovementMethod(new ScrollingMovementMethod());
                displayView4.setMaxLines(3);
                displayView4.setText(String.valueOf(size));

            default:
                break;
        }

    }//onCreate


    //The following is to decode. CAn use it in the decoding part
    public static Bitmap decodeBase64(String input) {
        byte[] decodedByte = Base64.decode(input, 0);
        return BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.length);
    }


}//Main2Activity
