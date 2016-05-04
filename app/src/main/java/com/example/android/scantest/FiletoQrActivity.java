package com.example.android.scantest;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.example.android.scantest.engine.QrByteWriter;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.ByteMatrix;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

public class FiletoQrActivity extends AppCompatActivity {

    public final static String HEADER_FILE_PREFIX = "FILE:";
    public final static char HEADER_BODY_SEPARATOR = 0;
    public final static int FILENAME_MAX_CHARACTERS = 20;
    public final static char FILENAME_TRUNCATION_CHAR = '~';

    // TODO set on screen later
    public static int NumberOfBytes = 321;
    public static int NumberOfFrames;
    public static int filesize;

    public final static String CHAR_ENCODING = "ISO-8859-1";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fileto_qr);
    }//onCreate


    public void encodeToQR(File file, int imageDimension,
                           Handler encodeDoneHandler) throws IOException {

        /*byte[] header = (new String(HEADER_FILE_PREFIX
                + getTruncatedFileName(file) + HEADER_BODY_SEPARATOR))
                .getBytes(CHAR_ENCODING);
*/
        byte[] body = read(file);

        /*
        // TODO Java zip lib compressing begins:
        byte[] encoded = ByteUtilities.merge(header,
                body);

        filesize=encoded.length;
        System.out.println("encoded.length= "+ encoded.length);

        if(!MainActivity.TextBytesPerFrame.isEmpty()){
            NumberOfBytes= Integer.parseInt(MainActivity.TextBytesPerFrame);
        }
*/
        encodeToQR(body, imageDimension, encodeDoneHandler);
    }



    private void encodeToQR(byte[] contents, int imageDimension,
                            Handler encodeDoneHandler) {

        Thread encodeThread = new EncodeThread(contents, encodeDoneHandler,
                imageDimension);

        encodeThread.start();

    }



    private static final class EncodeThread extends Thread {
        private static final String TAG = "EncodeThread";

        private final byte[] contents;
        private final Handler handler;
        private final int pixelResolution;

        byte[] frameContents = new byte[NumberOfBytes];

        EncodeThread(byte[] contents, Handler handler, int pixelResolution) {
            this.contents = contents;
            this.handler = handler;
            this.pixelResolution = pixelResolution;

            if(contents.length%NumberOfBytes==0){
                NumberOfFrames = (int) contents.length / NumberOfBytes;
            }else{
                NumberOfFrames = (int) contents.length / NumberOfBytes+1;
            }

//			NumberOfFrames = (int) Math.ceil(contents.length / NumberOfBytes)+1;


        }

        @Override
        public void run() {
            ArrayList<Bitmap> bitmapArray = new ArrayList<Bitmap>();

            try {
                int NumberOfSameSizeFrames=NumberOfFrames-1;
                //Seldom used
                if(contents.length%NumberOfBytes==0){NumberOfSameSizeFrames=NumberOfFrames;}

                // TODO Loop starts
                for (int i = 0; i < NumberOfSameSizeFrames; i++) {// i<iMax
                    System.out.println("i= " + i);// TODO Delete

//					System.out.print("current frameCountents= ");

                    for(int j=0; j< NumberOfBytes; j++){
                        frameContents[j]=contents[i*NumberOfBytes +j];
//						System.out.print(frameContents[j]+ " ");
                    }//for


//					 try {
//						String frameContentsAsText = new String(frameContents, CHAR_ENCODING);
//						System.out.println("contentsAsText=\n "+frameContentsAsText);
//					} catch (UnsupportedEncodingException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}

//					System.arraycopy(contents, i*NumberOfBytes, frameContents, 0, NumberOfBytes);

                    ByteMatrix result = new QrByteWriter().encode(
                            frameContents, BarcodeFormat.QR_CODE,
                            pixelResolution, pixelResolution);
                    int width = result.getWidth();
                    int height = result.getHeight();
                    byte[][] array = result.getArray();
                    int[] pixels = new int[width * height];
                    for (int y = 0; y < height; y++) {
                        for (int x = 0; x < width; x++) {
                            int grey = array[y][x] & 0xff;
                            // pixels[y * width + x] = (0xff << 24) | (grey <<
                            // 16) |
                            // (grey << 8) | grey;
                            pixels[y * width + x] = 0xff000000 | (0x00010101 * grey);
                        }
                    }

                    Bitmap bitmap = Bitmap.createBitmap(width, height,
                            Bitmap.Config.ARGB_4444);
                    bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
                    bitmapArray.add(bitmap);

                }// End of frames for Loop

                System.out.println("(The last) i=" + NumberOfSameSizeFrames);
                System.out.print("current frameCountents= ");

                int restNumberOfBytes = contents.length % NumberOfBytes;

                byte[] lastFrameContents= new byte[restNumberOfBytes];
                for(int j=0; j< restNumberOfBytes; j++){
                    lastFrameContents[j]=contents[NumberOfSameSizeFrames*NumberOfBytes +j];
                    System.out.print(lastFrameContents[j]+ " ");
                }//for


                try {
                    String frameContentsAsText = new String(lastFrameContents, CHAR_ENCODING);
                    System.out.println("contentsAsText=\n "+frameContentsAsText);
                } catch (UnsupportedEncodingException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

//				System.arraycopy(contents, i*NumberOfBytes, frameContents, 0, NumberOfBytes);

                ByteMatrix result = new QrByteWriter().encode(
                        lastFrameContents, BarcodeFormat.QR_CODE,
                        pixelResolution, pixelResolution);
                int width = result.getWidth();
                int height = result.getHeight();
                byte[][] array = result.getArray();
                int[] pixels = new int[width * height];
                for (int y = 0; y < height; y++) {
                    for (int x = 0; x < width; x++) {
                        int grey = array[y][x] & 0xff;
                        // pixels[y * width + x] = (0xff << 24) | (grey <<
                        // 16) |
                        // (grey << 8) | grey;
                        pixels[y * width + x] = 0xff000000 | (0x00010101 * grey);
                    }
                }

                Bitmap bitmap = Bitmap.createBitmap(width, height,
                        Bitmap.Config.ARGB_4444);
                bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
                bitmapArray.add(bitmap);



                System.out.println("bitmapArray length=" +bitmapArray.size());

                //Message message = Message.obtain(handler,
                      //  R.id.encode_succeeded);
                //message.obj = bitmapArray;
                //message.sendToTarget();
                System.out.println("message.sendToTarget()");

            } catch (WriterException e) {
                Log.e(TAG, e.toString());
                //Message message = Message.obtain(handler, R.id.encode_failed);
               // message.sendToTarget();
            } catch (IllegalArgumentException e) {
                Log.e(TAG, e.toString());
                //Message message = Message.obtain(handler, R.id.encode_failed);
                //message.sendToTarget();
            }
        }
    }



    

    public byte[] read(File file) throws IOException{

        if (file.length() > Integer.MAX_VALUE)
            throw new IOException("File "+ file.getName() +" is too big.");
        byte[] bytes = new byte[(int) file.length()];

        FileInputStream in= new FileInputStream(file);

        // Read in the bytes
        int offset = 0;
        int numRead = 0;
        while (offset < bytes.length &&
                (numRead=in.read(bytes, offset, bytes.length-offset)) >= 0){
            offset += numRead;
        }

        // Ensure all the bytes have been read in
        if (offset < bytes.length)
            throw new IOException("Could not completely read file "+file.getName());

        in.close();

        return bytes;
    }


}
