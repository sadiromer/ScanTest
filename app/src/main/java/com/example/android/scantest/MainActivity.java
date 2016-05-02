package com.example.android.scantest;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.widget.ZoomControls;

import com.example.android.scantest.engine.PlanarYUVLuminanceSource;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;

public class MainActivity extends Activity implements SurfaceHolder.Callback {


    //Define Variables------------------------------------------------------------------------------
    public final static String EXTRA_DECODED_FILE= "DecodedFile";
    public final static int CAMERA_CODE = 2;





    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private Handler handler;

    private Camera camera;
    private Button CaptureButton;
    private ZoomControls zoomControls;
    private int PreviewWidth=320;
    private int PreviewHeight=240;
    private int currentZoomLevel = 0, maxZoomLevel = 0;
    private Camera.Parameters cameraParameters;
    private final static int AUTOFOCUS_DELAY= 800;

    private int NumberOfBitmaps=0;
    private int ExtractingPeriod= 40;

    private boolean AutoFocusLoops=true;
    private boolean previewRunning = false;
    private boolean exiting= false;
    private boolean extracting= false;


    private PowerManager.WakeLock wakeLock;

    Timer timer= new Timer();
    ArrayList<BinaryBitmap> binBitmapArray = new ArrayList<BinaryBitmap>();
    public ArrayList<String>StringResults = new ArrayList<String>();
    //----------------------------------------------------------------------------------------------





    private void closeActivity(boolean abort, ArrayList<BinaryBitmap> binBitmapArray){
        exiting= true;
        handler.removeCallbacks(previewRunnable);
        stopAutofocus();
        camera.stopPreview();
        previewRunning = false;

/*
        if (!abort) {
            try {
                qrDecoder.decodeFromQR(binBitmapArray);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (NotFileTypeException e) {
                e.printStackTrace();
            }
        }
        */
        finish();
    }

    private void stopAutofocus(){
        handler.removeCallbacks(autoFocusRunnable);
        if (camera != null) {
            camera.cancelAutoFocus();
            AutoFocusLoops=false;
        }

    }

    private View.OnClickListener tapScreenListener= new View.OnClickListener() {
        public void onClick(View v) {
            if (AutoFocusLoops){
                stopAutofocus();
            }else{
                camera.autoFocus(null);
            }
        }
    };

    private DialogInterface.OnClickListener errorQuitListener = new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
            closeActivity(true, null);
        }
    };

    private Runnable previewRunnable= new Runnable() {
        public void run() {
            if (!exiting) camera.setOneShotPreviewCallback(previewCallback);
        }
    };

    private Runnable autoFocusRunnable= new Runnable() {
        public void run() {
            if (!exiting) camera.autoFocus(autoFocusCallback);
        }
    };

    private Camera.AutoFocusCallback autoFocusCallback = new Camera.AutoFocusCallback() {
        public void onAutoFocus(boolean success, final Camera camera) {
            handler.postDelayed(autoFocusRunnable, AUTOFOCUS_DELAY);
        }
    };









    //-----------------------onCreate---------------------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_main);
        surfaceView = (SurfaceView) findViewById(R.id.surface_camera);
        zoomControls=(ZoomControls)findViewById(R.id.zoomControls);
        zoomControls.setOnZoomInClickListener(new View.OnClickListener(){
            //@Override
            public void onClick(View v){
                if(currentZoomLevel*6 < maxZoomLevel-6){
                    currentZoomLevel++;
                    cameraParameters.setZoom(currentZoomLevel*6);
                    camera.setParameters(cameraParameters);
                }
                else{
                    cameraParameters.setZoom(maxZoomLevel);
                    camera.setParameters(cameraParameters);
                }
            }
        });

        zoomControls.setOnZoomOutClickListener(new View.OnClickListener(){
            //@Override
            public void onClick(View v){
                if(currentZoomLevel*6 > 0){
                    currentZoomLevel--;
                    cameraParameters.setZoom(currentZoomLevel*6);
                    camera.setParameters(cameraParameters);
                }else{
                    cameraParameters.setZoom(0);
                    camera.setParameters(cameraParameters);
                }
            }
        });

        surfaceView.setOnClickListener(tapScreenListener);
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        CaptureButton = (Button) findViewById(R.id.button_capture);
        CaptureButton.setOnClickListener(new View.OnClickListener() {
            //@Override
            public void onClick(View v) {
                if (extracting) {
                    extracting=false;
                    CaptureButton.setText("Start Capturing");
                    timer.cancel();
                    System.out.println("binBitmapArray length=" +binBitmapArray.size());
//					System.out.println("NumberOfBitmaps=" +NumberOfBitmaps);//to check whether this two values are the same

                    //TODO pass the array list
                    closeActivity(false, binBitmapArray);
//					closeActivity(true, null);

                }

                else {//TODO manual to capture the pictures
                    extracting=true;
                    stopAutofocus();
                    CaptureButton.setText("STOP!");

//					setTimerTask(BinaryBitmap binBitmap);
                }
            }
        });


        handler= new Handler();





    }//onCreate

    //----------------------------------------------------------------------------------------------

    @Override
    protected void onResume() {

        super.onResume();

        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(
                PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP,
                MainActivity.class.getName());
        wakeLock.acquire();
    }

    @Override
    protected void onPause() {

        if (!exiting) closeActivity(true, null);
        wakeLock.release();

        super.onPause();
    }




    //_________surfaceHolder callback_______________________________________________________________

    /**
     * This is called immediately after the surface is first created.
     * Implementations of this should start up whatever rendering code
     * they desire.  Note that only one thread can ever draw into
     * a {@link Surface}, so you should not draw into the Surface here
     * if your normal rendering will be in another thread.
     *
     * @param holder The SurfaceHolder whose surface is being created.
     */
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        camera = Camera.open();
        camera.setDisplayOrientation(90);
        if (camera==null){
//			notificationManager.showErrorMessage(R.string.error_camera_preview, errorQuitListener);
        }
    }

    /**
     * This is called immediately after any structural changes (format or
     * size) have been made to the surface.  You should at this point update
     * the imagery in the surface.  This method is always called at least
     * once, after {@link #surfaceCreated}.
     *
     * @param holder The SurfaceHolder whose surface has changed.
     * @param format The new PixelFormat of the surface.
     * @param width  The new width of the surface.
     * @param height The new height of the surface.
     */
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (previewRunning) camera.stopPreview();

        cameraParameters = camera.getParameters();
        cameraParameters.setPreviewSize(PreviewWidth, PreviewHeight);


        currentZoomLevel=cameraParameters.getZoom();


        if(cameraParameters.isZoomSupported()){
            maxZoomLevel = cameraParameters.getMaxZoom();
            zoomControls.setIsZoomInEnabled(true);
            zoomControls.setIsZoomOutEnabled(true);
        }

        camera.setParameters(cameraParameters);

        try {
            camera.setPreviewDisplay(holder);
        } catch (IOException e) {

        }

        camera.startPreview();
        previewRunning = true;

        handler.post(previewRunnable);

        String focusMode = camera.getParameters().getFocusMode();
        if (focusMode != Camera.Parameters.FOCUS_MODE_FIXED &&
                focusMode != Camera.Parameters.FOCUS_MODE_INFINITY)
            handler.post(autoFocusRunnable);
    }

    /**
     * This is called immediately before a surface is being destroyed. After
     * returning from this call, you should no longer try to access this
     * surface.  If you have a rendering thread that directly accesses
     * the surface, you must ensure that thread is no longer touching the
     * Surface before returning from this function.
     *
     * @param holder The SurfaceHolder whose surface is being destroyed.
     */
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        camera.stopPreview();
        previewRunning = false;
        timer.cancel();
        camera.release();
    }

    //______________________________________________________________________________________________


    //---------------------------PreviewCallback----------------------------------------------------
    private Camera.PreviewCallback previewCallback = new Camera.PreviewCallback() {

        public void onPreviewFrame(byte[] data, Camera camera) {
            LuminanceSource source= buildLuminanceSourceFromCameraPreview(data, camera.getParameters());

            BinaryBitmap binBitmap = new BinaryBitmap(new HybridBinarizer(source));
            if(!extracting){
                try {
                    Result result = new MultiFormatReader().decode(binBitmap);

                    Toast.makeText(getApplicationContext(), result.toString() , Toast.LENGTH_LONG).show();

                    if(result.toString().equals("START")){
                        stopAutofocus();
                        CaptureButton.setText("STOP!");
                        Log.i("Extrating", "Begin!");

                        extracting=true;
                        System.out.println(result.toString());
                    }

                } catch (NotFoundException e) {

                    // Auto-generated catch block
                    e.printStackTrace();
                }//try...catch
            }else{

                try {
                    Result result = new MultiFormatReader().decode(binBitmap);

                    StringResults.add(result.toString());
                    //String resultString= result.toString();
                    System.out.println("\n result.toString(): >>"+StringResults+"<<");

                    Toast.makeText(getApplicationContext(), result.toString() , Toast.LENGTH_LONG).show();

                } catch (NotFoundException e) {

                    // Auto-generated catch block
                    e.printStackTrace();
                }




                binBitmapArray.add(binBitmap);
                NumberOfBitmaps++;

                Log.i("Extrating", NumberOfBitmaps +" frames added.");

            }
            handler.postDelayed(previewRunnable, ExtractingPeriod);

        }
    };

    //----------------------------------------------------------------------------------------------


    //______________________FUNCTIONS from other files______________________________________________

    //------------qrDecoder-------------------------------------------------------------------------

    public LuminanceSource buildLuminanceSourceFromCameraPreview(
            byte[] previewData, Camera.Parameters cameraParameters) {

        int previewFormat = cameraParameters.getPreviewFormat();
        String previewFormatString = cameraParameters.get("preview-format");
        Camera.Size previewSize = cameraParameters.getPreviewSize();

        switch (previewFormat) {
            // This is the standard Android format which all devices are REQUIRED to
            // support.
            // In theory, it's the only one we should ever care about.
            case PixelFormat.YCbCr_420_SP:
                // This format has never been seen in the wild, but is compatible as
                // we only care
                // about the Y channel, so allow it.
            case PixelFormat.YCbCr_422_SP:
                return new PlanarYUVLuminanceSource(previewData, previewSize.width,
                        previewSize.height);
            default:
                // The Samsung Moment incorrectly uses this variant instead of the
                // 'sp' version.
                // Fortunately, it too has all the Y data up front, so we can read
                // it.
                if ("yuv420p".equals(previewFormatString)) {
                    return new PlanarYUVLuminanceSource(previewData,
                            previewSize.width, previewSize.height);
                }
        }
        throw new IllegalArgumentException("Unsupported picture format: "
                + previewFormat + '/' + previewFormatString);
    }
    //----------------------------------------------------------------------------------------------





    //______________________________________________________________________________________________



}//MainActivity
