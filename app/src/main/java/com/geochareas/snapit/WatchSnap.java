package com.geochareas.snapit;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.geochareas.snapit.Utility.Seen;
import com.geochareas.snapit.Utility.Snap;
import com.geochareas.snapit.Utility.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.util.Iterator;

import de.hdodenhof.circleimageview.CircleImageView;
import me.zhanghai.android.materialprogressbar.MaterialProgressBar;

public class WatchSnap extends Activity {
    private VideoView mVideo;
    private TextView mUsername;
    private CircleImageView mPhoto;
    private String mSnapId;
    private TextView mLocation;
    private Snap mSnap;
    private MaterialProgressBar mProgressBar;
    private Button mReplyButton;
    private TextView mViewReplies;
    private TextView mSeen;
    private DatabaseReference mDatabase;
    private FirebaseUser fbUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.watch_snap);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        Intent i = getIntent();

        final Snap snap = (Snap) i.getParcelableExtra("Snap");

        String videoURL = i.getStringExtra("downloadUrl");
        String photoUrl = i.getStringExtra("photoUrl");
        String username = i.getStringExtra("fullname");
        String location = i.getStringExtra("location");
        mSnapId = i.getStringExtra("snapId");


        Uri videoUri = Uri.parse(videoURL);

        fbUser = FirebaseAuth.getInstance().getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mUsername = findViewById(R.id.username);
        mLocation = findViewById(R.id.location);
        mPhoto = findViewById(R.id.profile_photo);
        mVideo = findViewById(R.id.snap_container);
        mProgressBar = findViewById(R.id.snap_loading);
        mReplyButton = findViewById(R.id.replyButton);
        mViewReplies = findViewById(R.id.view_replies);
        mSeen = findViewById(R.id.seen);

        seenStatus();
        getSeenNumber();

        try {
            mProgressBar.setVisibility(View.VISIBLE);
            mUsername.setText(username);
            mLocation.setText(location);
            //Picasso.get().load(photoUrl).into(mPhoto);

            mVideo.setVideoURI(videoUri);
            mVideo.requestFocus();
            mVideo.start();

            mVideo.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mProgressBar.setVisibility(View.GONE);

                }
            });

            mVideo.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    mVideo.start();
                }
            });

            mReplyButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(checkPermissions())
                    {
                        Intent i = new Intent(getApplicationContext(), UploadSnap.class);
                        i.putExtra("reason", "upload_reply");
                        i.putExtra("snapId", mSnapId);
                        startActivityForResult(i, 0);
                    } else {
                        Toast.makeText(getApplicationContext(), "Invalid permissions. Grant all required permissions and try again", Toast.LENGTH_SHORT).show();
                    }

                }
            });

            mViewReplies.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    Intent i = new Intent(getApplicationContext(), ViewReplies.class);
                    i.putExtra("snapId", mSnapId);
                    startActivity(i);

                    return false;
                }
            });


        } catch (Exception e) {

        }


    }

    private void seenStatus() {

        Query query = mDatabase.child("seen").orderByKey();

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                boolean hasSeen = false;
                for (DataSnapshot d : dataSnapshot.getChildren()) {
                    String user = d.child("seenUser").getValue(String.class);
                    String snapId = d.child("snapId").getValue(String.class);

                    if (user.equals(fbUser.getUid()) && snapId.equals(snapId)) {
                        hasSeen = true;
                        break;
                    }
                }

                if (hasSeen == false) {
                    mDatabase.child("seen").push().setValue(new Seen(fbUser.getUid(), mSnapId));
                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void getSeenNumber() {
        Query query = mDatabase.child("seen").orderByChild("snapId").equalTo(mSnapId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    mSeen.setText(" " + dataSnapshot.getChildrenCount());
                }
                else
                {
                    mSeen.setText("1");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private boolean checkPermissions() {
        int PERMISSION_ALL = 1;
        String[] permissions = {
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                android.Manifest.permission.CAMERA
        };


        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(getApplicationContext(), permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }

        return true;
    }

}
