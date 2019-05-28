package com.example.myapplication;


import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

public class CameraHandler extends Activity implements CameraBridgeViewBase.CvCameraViewListener2 {

    Mat mat;

    CameraBridgeViewBase cameraBridgeViewBase;
    private AcceptThread mAcceptThread;
    private static final String TAG = "CameraHandler";
    BaseLoaderCallback baseLoaderCallback;
    private static final String galleryPath = Environment.getExternalStorageState();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera);

        cameraBridgeViewBase = (JavaCameraView) findViewById(R.id.javaCameraView);
        cameraBridgeViewBase.setVisibility(SurfaceView.VISIBLE);
        cameraBridgeViewBase.setCvCameraViewListener(this);
        baseLoaderCallback = new BaseLoaderCallback(this) {
            @Override
            public void onManagerConnected(int status) {
                if (status == BaseLoaderCallback.SUCCESS) cameraBridgeViewBase.enableView();
                else super.onManagerConnected(status);
            }
        };

    }

    @Override
    public void onCameraViewStarted(int width, int height) {

        mat = new Mat(width, height, CvType.CV_8UC4);
    }

    @Override
    public void onCameraViewStopped() {

    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        mat = inputFrame.rgba();
        return mat;
    }

    @Override
    public void onResume(){
        super.onResume();
        if(!OpenCVLoader.initDebug()) Log.d(TAG, "unable to open camera");
        else {

            baseLoaderCallback.onManagerConnected(BaseLoaderCallback.SUCCESS);

        }
    }

    @Override
    public void onPause(){
        super.onPause();
    }

    public void capture(View view) {


    }
}
class AcceptThread extends Thread{


}