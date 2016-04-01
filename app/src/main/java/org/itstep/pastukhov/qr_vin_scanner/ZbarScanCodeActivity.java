package org.itstep.pastukhov.qr_vin_scanner;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Toast;

import net.sourceforge.zbar.Image;
import net.sourceforge.zbar.ImageScanner;
import net.sourceforge.zbar.Symbol;
import net.sourceforge.zbar.SymbolSet;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by DWork on 1/20/2016.
 */
public class ZbarScanCodeActivity extends Activity {


    Activity activity;

    private static final int CAMERA = 1;

    private ImageButton btnFlashSwitch;

    private Camera mCamera;
    private Activity_ZbarScanCode_Preview mPreview;
    private Handler autoFocusHandler;

    private boolean isFlashOn;
    private boolean hasFlash;
    private static Camera.Parameters cameraParams;

    private int screenWidth;
    private int screenHeight;

    ImageScanner scanner;
    ZbarScanCodeRect zbarScanCodeRect;

    private boolean previewing = true;
    private static int cameraId = 0;

    static {
        System.loadLibrary("iconv");
    }


    private int typeScanner = 0;
    private View layout;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activity = this;

        Log.d("tag", "ZbarScanCodeActivity.onCreate");

        Intent intent = getIntent();

        typeScanner = intent.getIntExtra(Globals.TYPE_OF_SCANNER, 0);
        Log.d("tag", "ZbarScanCodeActivity.typeScanner = " + typeScanner);

        if (typeScanner == Globals.REQUEST_CODE_VIN_SCANNER) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else if (typeScanner == Globals.REQUEST_CODE_QR_SCANNER) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        setContentView(R.layout.activity_vin_scancode);

        layout = findViewById(R.id.rootlayout);

        zbarScanCodeRect = (ZbarScanCodeRect) this.findViewById(R.id.scan_code_rect);

        DisplayMetrics metrics = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        screenWidth = metrics.widthPixels;
        screenHeight = metrics.heightPixels;

        int frameHeight = (screenHeight - screenWidth / 6) / 2;
        Log.d("tag", "ZbarScanCodeActivity.frameHeight = " + frameHeight);

        zbarScanCodeRect.setCodeRect(screenWidth, screenHeight, frameHeight, typeScanner);

        hasFlash = getApplicationContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH); //?

        //disable auto sleep screen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        btnFlashSwitch = (ImageButton) this.findViewById(R.id.btnFlashSwitch);
        btnFlashSwitch.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (isFlashOn) {
                    turnOffFlash();
                } else {
                    turnOnFlash();
                }
            }
        });

        if (hasFlash) {
            btnFlashSwitch.setVisibility(View.VISIBLE);
        } else {
            btnFlashSwitch.setVisibility(View.GONE);
        }

        autoFocusHandler = new Handler();


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            Log.d("tag", "Build.VERSION.SDK_INT >= Build.VERSION_CODES.M");

            if (permissionRequest(this, Manifest.permission.CAMERA, CAMERA)) {

                Log.d("tag", "permissionRequest - true");
                mCamera = getCameraInstance();

                Log.d("tag", "mCamera = " + mCamera);
                cameraPreView();
            }

        } else {

            Log.d("tag", " ! Build.VERSION.SDK_INT >= Build.VERSION_CODES.M");
            mCamera = getCameraInstance();
            cameraPreView();
        }

    }


    private void cameraPreView() {

        Log.d("tag", "cameraPreView()");

        if (mCamera == null) {
//            Camera.Parameters cameraParam = mCamera.getParameters();
            Toast.makeText(ZbarScanCodeActivity.this, "No camera detected", Toast.LENGTH_LONG).show();
        } else {

            Log.d("tag", "camera != null");

            /* Instance barcode scanner */
            scanner = new ImageScanner();

            mPreview = new Activity_ZbarScanCode_Preview(this, mCamera, cameraId, previewCb, autoFocusCB);
            FrameLayout preview = (FrameLayout) findViewById(R.id.cameraPreview);
            preview.addView(mPreview);
        }

        //Turn On Flash
        turnOnFlash();

    }


    private boolean permissionRequest(final Activity activity, final String permission, final int requestCode) {

        Log.d("tag", "permissionRequest()");

        if (ContextCompat.checkSelfPermission(activity,
                permission) != PackageManager.PERMISSION_GRANTED) {

            Log.d("tag", "! PackageManager.PERMISSION_GRANTED");

            if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {

                Log.d("tag", "Show an explanation");
//                Snackbar.make(layout, "Permission is needed.",
//                        Snackbar.LENGTH_INDEFINITE)
//                        .setAction("OK", new View.OnClickListener() {
//                            @Override
//                            public void onClick(View view) {
//                                ActivityCompat.requestPermissions(
//                                        activity, new String[]{permission}, requestCode);
//                            }
//                        })
//                        .show();

            } else {
                Log.d("tag", "No explanation needed, we can request the permission.");
                Log.d("tag", "requestPermissions - " + permission);
                Log.d("tag", "requestCode - " + requestCode);

                ActivityCompat.requestPermissions(activity, new String[]{permission}, requestCode);
            }

        } else {
            Log.d("tag", "PERMISSION_GRANTED");
            return true;
        }

        Log.d("tag", "permissionRequest().return - false");
        return false;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {

        Log.d("tag", "onRequestPermissionsResult()");
        Log.d("tag", "requestCode = " + requestCode);
        Log.d("tag", "grantResults.length > 0 = " + (grantResults.length > 0));
        Log.d("tag", "grantResults[0] == PackageManager.PERMISSION_GRANTED = "
                + (grantResults[0] == PackageManager.PERMISSION_GRANTED));

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            switch (requestCode) {

                case CAMERA: {

                    mCamera = getCameraInstance();
                    cameraPreView();

                    break;
                }
            }
        }
    }


    @Override
    public void onPause() {
        super.onPause();
        releaseCamera();
    }


    @Override
    protected void onResume() {
        super.onResume();

        if (mCamera == null) {

            Log.d("tag", "onResume().mCamera == null");

            Intent intent = new Intent();
            setResult(RESULT_OK, intent);
            finish();
        }
    }

    /**
     * A safe way to get an instance of the Camera object.
     */
    public static Camera getCameraInstance() {
        Camera camera = null;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
                int cameraCount = Camera.getNumberOfCameras();
                Camera.CameraInfo cameraInfo = new Camera.CameraInfo();

                if (cameraCount == 1) {
                    for (int camIdx = 0; camIdx < cameraCount; camIdx++) {
                        Camera.getCameraInfo(camIdx, cameraInfo);
                        if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT
                                || cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                            try {
                                cameraId = camIdx;
                                camera = Camera.open(camIdx);
                                break;
                            } catch (RuntimeException e) {
                                Log.d("tag","getCameraInstance().RuntimeException - "+e.getMessage() );
                            }
                        }
                    }
                } else {
                    camera = Camera.open();
                }
            } else {
                camera = Camera.open();
            }

            if (camera == null) {
                Log.d("tag","getCameraInstance().camera == null - throw new Exception(\"No camera available\");");
                throw new Exception("No camera available");
            }
            cameraParams = camera.getParameters();
            List<Camera.Size> cameraPreviewSizes = camera.getParameters().getSupportedPreviewSizes();

            List<Camera.Size> cameraSizes = cameraParams.getSupportedPictureSizes();
            if (cameraSizes.size() > 2) {
                if (cameraSizes.get(0).width < cameraSizes.get(1).width) {
                    cameraSizes = resort(cameraSizes);
                }
            }

            for (Camera.Size size : cameraSizes) {
                int maxSize = size.height;
                if (size.width > size.height) {
                    maxSize = size.width;
                }
                if (maxSize <= 1600) {
                    cameraParams.setPictureSize(size.width, size.height);
                    camera.setParameters(cameraParams);
                    break;
                }
            }

            if (cameraPreviewSizes.size() > 2) {
                if (cameraPreviewSizes.get(0).width < cameraPreviewSizes.get(1).width) {
                    cameraPreviewSizes = resort(cameraPreviewSizes);
                }
            }
            int maxWidthHeight = 0;
            int indexMaxWidthHeight = 0;
            for (int i = 0; i < cameraPreviewSizes.size(); i++) {
                //Log.d(Debug.CAMERA_DEBUG,cameraPreviewSizes.get(i).width+ " " + cameraPreviewSizes.get(i).height);

                if (maxWidthHeight < cameraPreviewSizes.get(i).width * cameraPreviewSizes.get(i).height) {
                    maxWidthHeight = cameraPreviewSizes.get(i).width * cameraPreviewSizes.get(i).height;
                    indexMaxWidthHeight = i;
                }
            }

            cameraParams.setPreviewSize(cameraPreviewSizes.get(indexMaxWidthHeight).width, cameraPreviewSizes.get(indexMaxWidthHeight).height);
            camera.setParameters(cameraParams);
        } catch (Exception e) {
            Log.d("tag", e.getMessage());
        }

        Log.d("tag", "getCameraInstance().return.camera - "+camera);
        return camera;
    }


    private void turnOnFlash() {
        if (hasFlash && !isFlashOn) {
            if (mCamera == null || cameraParams == null) {
                return;
            }

            cameraParams = mCamera.getParameters();
            cameraParams.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            mCamera.setParameters(cameraParams);
            mCamera.startPreview();
            isFlashOn = true;

            // changing button/switch image
            toggleButtonImage();
        }
    }


    private void turnOffFlash() {
        if (hasFlash && isFlashOn) {
            if (mCamera == null || cameraParams == null) {
                return;
            }

            cameraParams = mCamera.getParameters();
            cameraParams.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            mCamera.setParameters(cameraParams);
            mCamera.stopPreview();
            isFlashOn = false;

            // changing button/switch image
            toggleButtonImage();
        }
    }


    private void toggleButtonImage() {

        if (isFlashOn) {
            btnFlashSwitch.setImageResource(R.drawable.ic_action_flash_off);
        } else {
            btnFlashSwitch.setImageResource(R.drawable.ic_action_flash_on);
        }
    }


    private void releaseCamera() {

        Log.d("tag", "ZbarScanCodeActivity.releaseCamera().mCamera = " + mCamera);

        if (mCamera != null) {
            previewing = false;
            mCamera.setPreviewCallback(null);
            mPreview.getHolder().removeCallback(mPreview);
            mCamera.release();
            mCamera = null;
        }
    }


    private Runnable doAutoFocus = new Runnable() {
        public void run() {

            Log.d("tag", "ZbarScanCodeActivity.doAutoFocus.run()");

            if (previewing) {
                Log.d("tag", "ZbarScanCodeActivity.doAutoFocus.run().previewing.mCamera.autoFocus(autoFocusCB)");
                mCamera.autoFocus(autoFocusCB);
            }
        }
    };


    Camera.PreviewCallback previewCb = new Camera.PreviewCallback() {

        public void onPreviewFrame(byte[] data, Camera camera) {

            Log.d("tag", "ZbarScanCodeActivity.previewCb.onPreviewFrame()");

            Camera.Parameters parameters = camera.getParameters();
            parameters.setPreviewFormat(PixelFormat.YCbCr_422_SP);

            Camera.Size size = parameters.getPreviewSize();

            Image barcode = new Image(size.width, size.height, "NV21");

            barcode.setData(data);
            barcode = barcode.convert("Y800");

            int result = scanner.scanImage(barcode);

            Log.d("tag", "scanner.scanImage - result = " + result);

            if (result != 0) {

                SymbolSet syms = scanner.getResults();

                for (Symbol sym : syms) {

                    switch (typeScanner) {

                        case Globals.REQUEST_CODE_VIN_SCANNER: {

                            Log.d("tag", "VIN");

                            if (sym.getType() == Symbol.CODE39) {
                                answer(sym, camera);
                            }
                            break;
                        }


                        case Globals.REQUEST_CODE_QR_SCANNER: {

                            if (sym.getType() == Symbol.QRCODE) {
                                answer(sym, camera);
                            }
                            break;
                        }
                    }
                }
            }
        }
    };


    private void answer(Symbol sym, Camera camera) {

        Log.d("tag", "sym.getData() - " + sym.getData());

        Intent intent = new Intent();
        intent.putExtra(Globals.KEY_SCAN_RESULT, sym.getData());
        setResult(RESULT_OK, intent);

        previewing = false;
        camera.setPreviewCallback(null);
        camera.stopPreview();

        finish();

    }


    // Mimic continuous auto-focusing
    Camera.AutoFocusCallback autoFocusCB = new Camera.AutoFocusCallback() {

        public void onAutoFocus(boolean success, Camera camera) {

            Log.d("tag", "ZbarScanCodeActivity.onAutoFocus()");

            autoFocusHandler.postDelayed(doAutoFocus, 1000);
        }
    };


    private static List<Camera.Size> resort(List<Camera.Size> cameraSizes) {

        Log.d("tag", "ZbarScanCodeActivity.resort()");

        ArrayList<Camera.Size> resortedCameraSizes = new ArrayList<Camera.Size>();

        int size = cameraSizes.size();

        for (int i = 0; i < size; i++) {
            resortedCameraSizes.add(cameraSizes.get(size - 1 - i));
        }
        return resortedCameraSizes;
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}