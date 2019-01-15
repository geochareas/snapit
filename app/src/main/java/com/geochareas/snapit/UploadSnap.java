package com.geochareas.snapit;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.geochareas.snapit.Utility.Reply;
import com.geochareas.snapit.Utility.Seen;
import com.geochareas.snapit.Utility.Snap;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import java.util.List;
import java.util.Locale;


import static android.support.constraint.Constraints.TAG;

public class UploadSnap extends Activity implements SensorEventListener, View.OnClickListener {

    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;
    private boolean isRecording = false;

    private Camera mCamera;
    private CameraPreview mPreview;
    private Camera.PictureCallback mPicture;
    private String mSnapFile = null;
    private FirebaseUser fbUser;
    private DatabaseReference database;
    private String mSnapId;
    private SensorManager mSensorManager;
    private Sensor mSensor;
    private MediaRecorder mMediaRecorder;
    private TextView mRecordingIndicator;
    private String mReason;
    private Location mLocation = null;
    private FusedLocationProviderClient mFusedLocationClient;

    Button captureButton;
    private Chronometer mChronometer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.upload_snap);

        Intent intent = getIntent();

        mSnapId = intent.getStringExtra("snapId");
        mReason = intent.getStringExtra("reason");

        fbUser = FirebaseAuth.getInstance().getCurrentUser();
        if (fbUser == null) {
            finish();
        }


        database = FirebaseDatabase.getInstance().getReference();
        captureButton = (Button) findViewById(R.id.button_capture);

        // Create an instance of Camera
        mCamera = getCameraInstance();
        mCamera.setDisplayOrientation(90);

        mCamera = Camera.open();


        final Vibrator vib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        if (mReason.equals("upload_snap")) {
            // Create an instance of Camera

            mCamera = Camera.open();

            mPreview = new CameraPreview(this, mCamera);
            FrameLayout preview = findViewById(R.id.camera_preview);
            preview.addView(mPreview);


            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    // initialize video camera
                    if (prepareVideoRecorder() == true) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            vib.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
                        } else {
                            //deprecated in API 26
                            vib.vibrate(500);
                        }

                        // Camera is available and unlocked, MediaRecorder is prepared,
                        // now you can start recording
                        mMediaRecorder.start();


                        isRecording = true;
                        mRecordingIndicator = findViewById(R.id.recording);
                        mRecordingIndicator.setVisibility(View.VISIBLE);
                        mChronometer = findViewById(R.id.chronometer);
                        mChronometer.setVisibility(View.VISIBLE);
                        mChronometer.start();

                    } else {
                        // prepare didn't work, release the camera
                        releaseMediaRecorder();
                        // inform user
                        Toast.makeText(getApplicationContext(), "Error occured", Toast.LENGTH_SHORT).show();
                    }
                }
            }, 500);
        } else if (mReason.equals("upload_reply")) {
            mCamera = Camera.open(1);
            Camera.Parameters p = mCamera.getParameters();

            p.setPictureSize(1920, 1080);
            p.setJpegQuality(100);
            mCamera.setParameters(p);
            mPreview = new CameraPreview(this, mCamera);
            FrameLayout preview = findViewById(R.id.camera_preview);
            preview.addView(mPreview);
            preview.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));


            Button captureButton = findViewById(R.id.button_capture);
            captureButton.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // get an image from the camera

                        }
                    }
            );
            captureButton.setVisibility(View.VISIBLE);


        }
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);

        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);

        captureButton.setOnClickListener(this);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            // Logic to handle location object
                            mLocation = location;
                        }

                    }
                });



        mPicture = new Camera.PictureCallback() {

            @Override
            public void onPictureTaken(byte[] data, Camera camera) {

                File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
                if (pictureFile == null) {
                    Log.d(TAG, "Error creating media file, check storage permissions");
                    return;
                }

                try {

                    FileOutputStream fos = new FileOutputStream(pictureFile);
                    //fos.write(data);

                    Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
                    bmp = rotateSnapReply(bmp, -90);
                    bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos); //100-best quality

                    fos.close();
                } catch (FileNotFoundException e) {
                    Log.d(TAG, "File not found: " + e.getMessage());
                } catch (IOException e) {
                    Log.d(TAG, "Error accessing file: " + e.getMessage());

                }

                mSnapFile = pictureFile.getAbsolutePath();
                uploadSnap();
                finish();


            }
        };
    }


    private boolean prepareVideoRecorder() {

        mMediaRecorder = new MediaRecorder();

        // Step 1: Unlock and set camera to MediaRecorder
        mCamera.unlock();

        mMediaRecorder.setOrientationHint(90);
        mMediaRecorder.setCamera(mCamera);


        // Step 2: Set sources
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);


        // Step 3: Set a CamcorderProfile (requires API Level 8 or higher)
        //mMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));
        CamcorderProfile highCameraProfile = CamcorderProfile.get(CamcorderProfile.QUALITY_720P);
        mMediaRecorder.setProfile(highCameraProfile);
        mMediaRecorder.setMaxDuration(8000); // 8 seconds
        mMediaRecorder.setMaxFileSize(500000000); // Approximately 30 megabytes
        mMediaRecorder.setVideoFrameRate(30);

//        mMediaRecorder.setOnInfoListener(new MediaRecorder.OnInfoListener() {
//            @Override
//            public void onInfo(MediaRecorder mr, int what, int extra) {
//                if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
//                    mMediaRecorder.stop();
//                }
//                else if(what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED)
//                {
//                    mMediaRecorder.stop();
//                }
//            }
//        });


        // Step 4: Set output file
        mSnapFile = getOutputMediaFile(MEDIA_TYPE_VIDEO).toString();
        mMediaRecorder.setOutputFile(mSnapFile);


        // Step 5: Set the preview output
        mMediaRecorder.setPreviewDisplay(mPreview.getHolder().getSurface());

        // Step 6: Prepare configured MediaRecorder
        try {
            mMediaRecorder.prepare();
        } catch (IllegalStateException e) {
            Log.d("TAG", "IllegalStateException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        } catch (IOException e) {
            Log.d("TAG", "IOException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        }
        return true;
    }


    @Override
    protected void onPause() {
        super.onPause();
        releaseMediaRecorder();       // if you are using MediaRecorder, release it first
        releaseCamera();              // release the camera immediately on pause event

        mSensorManager.unregisterListener(this);

    }

    private void releaseMediaRecorder() {
        if (mMediaRecorder != null) {
            mMediaRecorder.reset();   // clear recorder configuration
            mMediaRecorder.release(); // release the recorder object
            mMediaRecorder = null;
            mCamera.lock();           // lock camera for later use
        }
    }

    private void releaseCamera() {
        if (mCamera != null) {
            mCamera.release();        // release the camera for other applications
            mCamera = null;
            mPreview.getHolder().removeCallback(mPreview);
        }
    }

    public static Camera getCameraInstance() {
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        } catch (Exception e) {
            // Camera is not available (in use or does not exist)
            Log.d("TAG", "Camera is not available (in use or does not exist)" + e.getMessage());
        }
        return c; // returns null if camera is unavailable
    }


    public static Bitmap rotateSnapReply(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    public void uploadSnap() {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        StorageReference imagesRef = storageRef.child("snaps");
        StorageReference userRef = imagesRef.child(fbUser.getUid());
        final String timeStamp = new SimpleDateFormat("ddMMyyyy_HHmmss").format(new Date());
        String filename = fbUser.getUid() + "_" + timeStamp;
        final StorageReference fileRef = userRef.child(filename);


        //RotateBitmap(out, 90);
        File file = new File(mSnapFile);
        Uri uri = Uri.fromFile(file);

        if (mReason.equals("upload_reply")) {
            Toast.makeText(UploadSnap.this, "Uploading...", Toast.LENGTH_SHORT).show();
        }

        UploadTask uploadTask = fileRef.putFile(uri);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
                Toast.makeText(UploadSnap.this, "Upload failed!\n" + exception.getMessage(), Toast.LENGTH_LONG).show();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {

            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {


                        Uri downloadUrl = uri;

                        // save snap to database

                        if (mReason.equals("upload_snap")) {


                            if (mLocation == null) {
                                Toast.makeText(UploadSnap.this, "Upload failed (could not detect location),\n please enable GPS Location.", Toast.LENGTH_SHORT).show();

                            } else {
                                Geocoder geocoder;
                                List<Address> addresses;


                                double longitude = mLocation.getLongitude();
                                double latitude = mLocation.getLatitude();


                                geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());

                                try {
                                    addresses = geocoder.getFromLocation(latitude, longitude, 1);
                                    String city = addresses.get(0).getLocality();
                                    String country = addresses.get(0).getCountryName();
                                    String location = city + ", " + country;
                                    String date = timeStamp;

                                    String key = database.child("snaps").push().getKey();
                                    Snap snap = new Snap(key, fbUser.getUid(), location, date, downloadUrl.toString());
                                    database.child("snaps").child(key).setValue(snap);

                                    database.child("seen").push().setValue(new Seen(fbUser.getUid(), key));
                                    Toast.makeText(UploadSnap.this, "Uploaded", Toast.LENGTH_SHORT).show();

                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                            }


                        } else {


                            String key = database.child("replies").push().getKey();
                            Reply reply = new Reply(fbUser.getDisplayName(), mSnapId, fbUser.getUid(), downloadUrl.toString());
                            database.child("replies").child(mSnapId).child(key).setValue(reply);
                            Toast.makeText(UploadSnap.this, "Replied", Toast.LENGTH_SHORT).show();
                        }

                    }
                });
            }
        });
    }

    private void confirmReply() {
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(UploadSnap.this, android.R.style.Theme_Material_Light_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(UploadSnap.this);
        }
        builder.setTitle("Confirm Submission")
                .setMessage("Do you want to send this snap?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        uploadSnap();

                        Intent returnIntent = new Intent();
                        returnIntent.putExtra("result", 1);
                        setResult(Activity.RESULT_OK, returnIntent);
                        finish();
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                        Intent returnIntent = new Intent();
                        returnIntent.putExtra("result", 0);
                        setResult(Activity.RESULT_CANCELED, returnIntent);
                        finish();
                    }
                })
                .show();
    }


    // Create a File for saving an image or video
    private static File getOutputMediaFile(int type) {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "Snapit");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("Snapit", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("ddMMyyyy_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "SNAPi_" + timeStamp + ".jpg");
        } else if (type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "SNAPv_" + timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        Vibrator vib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (event.values[0] != 0) {
            if (isRecording) {
                // stop recording and release camera
                if (mMediaRecorder != null) {
                    mMediaRecorder.stop();  // stop the recording
                    releaseMediaRecorder(); // release the MediaRecorder object
                    mCamera.lock();         // take camera access back from MediaRecorder

                }

                mChronometer.stop();
                mRecordingIndicator.setVisibility(View.GONE);
                mChronometer.setVisibility(View.GONE);

                // inform the user that recording has stopped
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vib.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
                } else {
                    //deprecated in API 26
                    vib.vibrate(500);
                }
                isRecording = false;
                confirmReply();
            }


        }
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.button_capture) {
            mCamera.takePicture(null, null, mPicture);
        }
    }

    private final LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(final Location location) {
            //your code here
        }
    };
}