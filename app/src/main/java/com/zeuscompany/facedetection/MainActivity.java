package com.zeuscompany.facedetection;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Camera;
import android.graphics.Rect;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.google.firebase.ml.common.FirebaseMLException;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata;
import com.google.firebase.ml.vision.common.FirebaseVisionPoint;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceLandmark;
import com.zeuscompany.facedetection.faceDetection.FaceDetectionProcessor;
import com.zeuscompany.facedetection.utils.CameraSource;
import com.zeuscompany.facedetection.utils.CameraSourcePreview;
import com.zeuscompany.facedetection.utils.GraphicOverlay;



import java.io.File;
import java.io.IOException;
import java.util.List;

/////////////////////////////////////////
//FIREBASE ADDED FROM VINUPOLLY@GMAIL.COM
/////////////////////////////////////////
public class MainActivity extends AppCompatActivity {

    Button openCamera;
    private CameraSourcePreview firePreview;
    FaceDetector faceDetector;
    private com.zeuscompany.facedetection.utils.CameraSource cameraSource = null;
    private GraphicOverlay graphicOverlay;

    private static final String FACE_DETECTION = "Face Detection";
    private static final String TEXT_DETECTION = "Text Detection";
    private static final String BARCODE_DETECTION = "Barcode Detection";
    private static final String IMAGE_LABEL_DETECTION = "Label Detection";
    private static final String CLASSIFICATION = "Classification";
    private static final String TAG = "LivePreviewActivity";
    private static final int PERMISSION_REQUESTS = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        openCamera = (Button) findViewById(R.id.openCamera);
        firePreview = (CameraSourcePreview) findViewById(R.id.firePreview);

        graphicOverlay = (GraphicOverlay) findViewById(R.id.fireFaceOverlay);
        if (graphicOverlay == null)
        {
            Log.d(TAG, "graphicOverlay is null");
        }

        if (allPermissionsGranted())
        {
            createCameraSource(FACE_DETECTION);
        }
        else
        {
            //getRuntimePermissions();
            Toast.makeText(MainActivity.this, "No Permissions granted", Toast.LENGTH_SHORT).show();
        }

        openCamera.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if (allPermissionsGranted())
                {
                    //createCameraSource(FACE_DETECTION);
                    startCameraSource();
                }
                else
                {
                    //getRuntimePermissions();
                    Toast.makeText(MainActivity.this, "No Permission Granted", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void createCameraSource(String model)
    {
        // If there's no existing cameraSource, create one.
        if (cameraSource == null)
        {
            cameraSource = new CameraSource(this, graphicOverlay);
        }


            switch (model)
            {
                case FACE_DETECTION:
                    Log.i(TAG, "Using Face Detector Processor");
                    cameraSource.setMachineLearningFrameProcessor(new FaceDetectionProcessor());
                    break;
                default:
                    Log.e(TAG, "Unknown model: " + model);
            }
    }

    /**
     * Starts or restarts the camera source, if it exists. If the camera source doesn't exist yet
     * (e.g., because onResume was called before the camera source was created), this will be called
     * again when the camera source is created.
     */
    private void startCameraSource()
    {
        if (cameraSource != null) {
            try {
                if (firePreview == null)
                {
                    Log.d(TAG, "resume: Preview is null");
                }
                if (graphicOverlay == null)
                {
                    Log.d(TAG, "resume: graphOverlay is null");
                }
                firePreview.start(cameraSource, graphicOverlay);
            }
            catch (IOException e)
            {
                Log.e(TAG, "Unable to start camera source.", e);
                cameraSource.release();
                cameraSource = null;
            }
        }
    }

    private boolean allPermissionsGranted() {
        for (String permission : getRequiredPermissions()) {
            if (!isPermissionGranted(this, permission)) {
                return false;
            }
        }
        return true;
    }

    private String[] getRequiredPermissions() {
        try {
            PackageInfo info =
                    this.getPackageManager()
                            .getPackageInfo(this.getPackageName(), PackageManager.GET_PERMISSIONS);
            String[] ps = info.requestedPermissions;
            if (ps != null && ps.length > 0) {
                return ps;
            } else {
                return new String[0];
            }
        } catch (Exception e) {
            return new String[0];
        }
    }

    private static boolean isPermissionGranted(Context context, String permission) {
        if (ContextCompat.checkSelfPermission(context, permission)
                == PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "Permission granted: " + permission);
            return true;
        }
        Log.i(TAG, "Permission NOT granted: " + permission);
        return false;
    }
}

