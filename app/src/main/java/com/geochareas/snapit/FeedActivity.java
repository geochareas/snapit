package com.geochareas.snapit;


import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;


import com.facebook.shimmer.ShimmerFrameLayout;
import com.geochareas.snapit.Adapters.SnapAdapter;
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
import com.google.firebase.storage.FirebaseStorage;


import java.util.ArrayList;


import me.zhanghai.android.materialprogressbar.MaterialProgressBar;


public class FeedActivity extends Activity implements SensorEventListener {

    private FirebaseUser fbUser;
    private DatabaseReference database;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private SwipeRefreshLayout mSwipeRefresh;
    private SnapAdapter mAdapter;
    private ShimmerFrameLayout container;
    private ArrayList<Snap> snaps = new ArrayList<>();
    private SensorManager mSensorManager;
    private Sensor mSensor;
    private MaterialProgressBar bar;
    private TextView mConnectionIndicator;



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflator = getMenuInflater();
        inflator.inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);

        mConnectionIndicator = findViewById(R.id.no_internet);
        container = findViewById(R.id.shimmer_post_placeholder);

        if (isNetworkAvailable() != true) {

            mConnectionIndicator.setVisibility(View.VISIBLE);
        }
        else
        {

            container.startShimmer();
            container.setVisibility(View.VISIBLE);
        }


        fbUser = FirebaseAuth.getInstance().getCurrentUser();
        if (fbUser == null) {
            finish();
        }

        database = FirebaseDatabase.getInstance().getReference();


        Toolbar toolbar = findViewById(R.id.main_navigation);
        setActionBar(toolbar);
        toolbar.setTitle(null);


        // Setup the RecyclerView
        recyclerView = findViewById(R.id.recyclerView);
        mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new SnapAdapter(snaps, this);
        recyclerView.setAdapter(mAdapter);

        // insert a divider between recyclerview items
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                DividerItemDecoration.VERTICAL);
        dividerItemDecoration.setDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.divider));
        recyclerView.addItemDecoration(dividerItemDecoration);

        // initialize sensors
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        bar = findViewById(R.id.uploading);

        loadSnaps();

        // start listening to swipe down to refresh calls
        mSwipeRefresh = findViewById(R.id.swiperefresh);
        mSwipeRefresh.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        if (isNetworkAvailable() != true) {

                            mConnectionIndicator.setVisibility(View.VISIBLE);
                            mSwipeRefresh.setRefreshing(false);
                        }
                        else {
                            mConnectionIndicator.setVisibility(View.GONE);
                            // first clear the recycler view so items are not populated twice
                            snaps.clear();
                            Toast.makeText(FeedActivity.this, "Refreshing...", Toast.LENGTH_SHORT).show();
                            loadSnaps();
                            mSwipeRefresh.setRefreshing(false);
                        }

                    }
                }
        );




    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }


    private boolean checkPermissions() {
        int PERMISSION_ALL = 1;
        String[] permissions = {
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.CAMERA
        };


        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }

        return true;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == RESULT_OK) {
            bar.setIndeterminate(true);
            bar.setVisibility(View.VISIBLE);
        } else if(resultCode == RESULT_CANCELED){
            bar.setVisibility(View.GONE);
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.log_out:
                FirebaseAuth.getInstance().signOut();
                Toast.makeText(this, "Logging out...", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, MainActivity.class));
                break;
            case R.id.search_user:
                startActivity(new Intent(this, SearchUser.class));
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        if (event.values[0] == 0) {
            if (checkPermissions()) {
                Intent i = new Intent(getApplicationContext(), UploadSnap.class);
                i.putExtra("reason", "upload_snap");
                startActivityForResult(i, 1);
            } else {
                Toast.makeText(this, "Invalid permissions. Grant all required permissions and try again", Toast.LENGTH_SHORT).show();
            }

        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    protected void onResume() {
        // Register a listener for the sensor.
        super.onResume();
        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        // Unregister the sensor when the activity pauses.
        super.onPause();
        mSensorManager.unregisterListener(this);
    }


    public void loadSnaps() {
        // Get the latest 100 snaps
        Query imagesQuery = database.child("snaps").orderByKey().limitToFirst(100);
        imagesQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                bar.setVisibility(View.GONE);

                // A new snap has been added, add it to the displayed list
                final Snap snap = dataSnapshot.getValue(Snap.class);

                // get the snap user
                database.child("users/" + snap.userId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        User user = dataSnapshot.getValue(User.class);
                        snap.user = user;
                        mAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                mAdapter.addSnap(snap);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }


}

